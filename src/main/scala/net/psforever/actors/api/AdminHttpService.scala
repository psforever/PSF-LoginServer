package net.psforever.actors.api

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization.write
import org.json4s.{DefaultFormats, Formats}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
  * The admin API, served over HTTP (Akka HTTP) instead of the old raw-TCP PSAdmin protocol.
  *
  * This actor owns the HTTP binding for its whole lifetime -- it is the "server hosted in an actor":
  * bound on preStart, unbound on stop, and any bind failure terminates the system exactly as the old
  * TCP listener did. Each route reuses the existing PSAdmin command actors (list/set/randomize) via a
  * per-request bridge, so all the domain + database logic is shared, not reimplemented. Responses are
  * the SAME json4s-serialised `data` maps the TCP protocol returned, so clients only change transport.
  *
  * The API is intentionally server-to-server (the web portal calls it); there is no browser CORS.
  */
/**
  * One audited administrative action.
  *
  * @param at      when it happened (epoch millis)
  * @param admin   the portal account that asked for it, or "unknown" for a direct API caller
  * @param action  short machine-readable action name, e.g. "zone.faction"
  * @param detail  the action's parameters
  * @param ok      whether the command reported success
  * @param message the command's own outcome text
  * @param source  the calling host
  */
case class AdminAction(
    at: Long,
    admin: String,
    action: String,
    detail: Map[String, String],
    ok: Boolean,
    message: String,
    source: String
)

/**
  * One interstellar event: a base changing hands, or failing to.
  *
  * @param at      when it happened (epoch millis)
  * @param kind    one of [[AdminHttpService.EventKinds]]
  * @param zone    zone id the base sits on
  * @param base    the facility's name
  * @param actor   who caused it -- a player name, or the admin account for a portal designation
  * @param from    empire that held the base beforehand (faction id, -1 unknown)
  * @param to      empire that holds it afterwards; equal to `from` when the hack failed
  * @param flipped whether ownership actually changed
  */
case class InterstellarEvent(
    at: Long,
    kind: String,
    zone: String,
    base: String,
    actor: String,
    from: Int,
    to: Int,
    flipped: Boolean
)

object AdminHttpService {

  /** Audited actions and interstellar events are both kept for seven days, then dropped. */
  val RetentionMillis: Long = 7L * 24L * 60L * 60L * 1000L

  /** Hard cap on retained entries, so a busy week cannot exhaust memory. */
  val MaxEntries: Int = 5000

  /**
    * The recognised interstellar event kinds. `HackHoldCompleted` / `HackHoldFailed` are a plain
    * hack-and-hold resolving; the `Llu*` kinds cover a facility that spawns a Lattice Logic Unit; the
    * `LluBypass*` kinds are a hack against a NEUTRAL base, where no LLU is required. `PortalDesignation`
    * is ownership set from this admin API rather than won in the field.
    */
  object EventKinds {
    /** A hack begun at a control console; carries the player, and flips nothing on its own. */
    val HackStarted          = "HackStarted"
    val HackHoldCompleted    = "HackHoldCompleted"
    val HackHoldFailed       = "HackHoldFailed"
    /** A player took the LLU off the socket; carries the player, and flips nothing on its own. */
    val LluPickedUp          = "LluPickedUp"
    val LluDelivered         = "LluDelivered"
    val LluLost              = "LluLost"
    val LluDestroyed         = "LluDestroyed"
    val LluBypassCompleted   = "LluBypassCompleted"
    val LluBypassFailed      = "LluBypassFailed"
    val PortalDesignation    = "PortalDesignation"
  }

  /**
    * Interstellar events are produced deep inside the world (the capture actor, the admin commands),
    * far from the HTTP service that serves them. This sink is set once when the service starts so any
    * of those places can report an event without needing a reference to the actor.
    */
  @volatile private var sink: Option[InterstellarEvent => Unit] = None

  private[api] def installSink(f: InterstellarEvent => Unit): Unit = sink = Some(f)

  /** Report an interstellar event. Does nothing if the admin API is not running. */
  def report(event: InterstellarEvent): Unit = sink.foreach(_(event))

  /** Audited actions that change base ownership, and so are also interstellar events. */
  val DesignationActions: Set[String] = Set("zone.faction", "building.faction", "zone.buildings")

  /** Faction token as sent by the portal -> faction id; -1 when it isn't one of the three empires. */
  def factionId(token: String): Int = token.toUpperCase match {
    case "TR" => 0
    case "NC" => 1
    case "VS" => 2
    case _    => 3
  }
}

class AdminHttpService(bindAddress: String, port: Int) extends Actor {
  private[this] val log = org.log4s.getLogger

  import context.dispatcher
  private implicit val system: akka.actor.ActorSystem = context.system
  private implicit val formats: Formats = DefaultFormats

  private var binding: Option[Http.ServerBinding] = None

  /**
    * In-memory audit trail of administrative actions, owned by this actor: it exists for the actor's
    * lifetime and is never persisted. Only state-changing calls are recorded -- the read endpoints are
    * polled every minute by every open admin page and would bury the interesting entries.
    *
    * Entries older than [[AdminHttpService.RetentionMillis]] (7 days) are dropped, as is the oldest
    * entry once the buffer is full, so the log cannot grow without bound on a long-lived server.
    * Guarded by its own lock because Akka HTTP routes run on the dispatcher rather than inside this
    * actor's message loop.
    */
  private val actionLog = scala.collection.mutable.ArrayDeque[AdminAction]()

  /** Record one completed action and evict anything expired or beyond the cap. */
  private def record(action: AdminAction): Unit = actionLog.synchronized {
    actionLog.append(action)
    val cutoff = action.at - AdminHttpService.RetentionMillis
    while (actionLog.nonEmpty && actionLog.head.at < cutoff) actionLog.removeHead()
    while (actionLog.size > AdminHttpService.MaxEntries) actionLog.removeHead()
  }

  /** The current log, newest first, with expired entries pruned. */
  private def snapshotLog(nowMillis: Long): Seq[AdminAction] = actionLog.synchronized {
    val cutoff = nowMillis - AdminHttpService.RetentionMillis
    while (actionLog.nonEmpty && actionLog.head.at < cutoff) actionLog.removeHead()
    actionLog.reverseIterator.toVector
  }

  /**
    * Base captures and failed captures, on the same seven-day in-memory terms as the action log. Fed
    * from the world (the capture actor) and from this service's own ownership commands.
    */
  private val eventLog = scala.collection.mutable.ArrayDeque[InterstellarEvent]()

  private def recordEvent(event: InterstellarEvent): Unit = eventLog.synchronized {
    eventLog.append(event)
    val cutoff = event.at - AdminHttpService.RetentionMillis
    while (eventLog.nonEmpty && eventLog.head.at < cutoff) eventLog.removeHead()
    while (eventLog.size > AdminHttpService.MaxEntries) eventLog.removeHead()
  }

  private def snapshotEvents(nowMillis: Long): Seq[InterstellarEvent] = eventLog.synchronized {
    val cutoff = nowMillis - AdminHttpService.RetentionMillis
    while (eventLog.nonEmpty && eventLog.head.at < cutoff) eventLog.removeHead()
    eventLog.reverseIterator.toVector
  }

  override def preStart(): Unit = {
    // Let the rest of the world report base captures without holding a reference to this actor.
    AdminHttpService.installSink(recordEvent)
    Http()
      .newServerAt(bindAddress, port)
      .bind(routes)
      .onComplete {
        case Success(b) => self ! b
        case Failure(e) =>
          log.error(s"Admin HTTP failed to bind to $bindAddress:$port: ${e.getMessage}")
          context.system.terminate()
      }
  }

  override def postStop(): Unit = binding.foreach(_.unbind())

  override def receive: Receive = {
    case b: Http.ServerBinding =>
      binding = Some(b)
      log.info(s"Admin HTTP API listening on ${b.localAddress}")
    case default =>
      log.error(s"Unexpected message $default")
  }

  // --- command bridging --------------------------------------------------------------------------

  /**
    * Run one of the existing PSAdmin command actors and capture its reply. The command actor replies
    * to its parent, so spawning it under a short-lived [[CommandBridge]] (whose parent-role receives
    * that reply) captures the response without changing any command.
    */
  private def run(handler: Class[_], args: Array[String]): Future[CommandResponse] = {
    val p = Promise[CommandResponse]()
    context.actorOf(Props(new CommandBridge(handler, args, p)))
    p.future
  }

  private def render(r: CommandResponse): HttpResponse = r match {
    case CommandGoodResponse(msg, data) =>
      data("message") = msg
      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, write(data.toMap)))
    case CommandErrorResponse(msg, data) =>
      data("message") = msg
      data("error") = true
      HttpResponse(StatusCodes.BadRequest, entity = HttpEntity(ContentTypes.`application/json`, write(data.toMap)))
  }

  /** Complete an HTTP request from a command actor's eventual response. */
  private def runRoute(handler: Class[_], args: Array[String]): Route =
    onComplete(run(handler, args)) {
      case Success(r) => complete(render(r))
      case Failure(e) =>
        complete(
          HttpResponse(
            StatusCodes.ServiceUnavailable,
            entity = HttpEntity(ContentTypes.`application/json`, write(Map("message" -> e.getMessage, "error" -> true)))
          )
        )
    }

  /**
    * Like [[runRoute]], but audits the call. Used for every state-changing route: the outcome is
    * recorded in the in-memory log along with which admin asked for it (the portal identifies itself
    * with an `X-Admin-User` header; unauthenticated direct callers show as "unknown").
    */
  private def auditedRoute(action: String, detail: Map[String, String])(
      handler: Class[_],
      args: Array[String]
  ): Route =
    optionalHeaderValueByName("X-Admin-User") { admin =>
      extractClientIP { ip =>
        onComplete(run(handler, args)) {
          case Success(r) =>
            val ok = r.isInstanceOf[CommandGoodResponse]
            // Ownership set from the portal is an interstellar event too -- the base changed hands,
            // it just wasn't won in the field. Recorded here so the ownership commands stay untouched.
            if (ok && AdminHttpService.DesignationActions.contains(action)) {
              recordEvent(
                InterstellarEvent(
                  at = System.currentTimeMillis(),
                  kind = AdminHttpService.EventKinds.PortalDesignation,
                  zone = detail.getOrElse("zone", ""),
                  base = detail.getOrElse("building", detail.getOrElse("assignments", "continent")),
                  actor = admin.getOrElse("unknown"),
                  from = -1,
                  to = AdminHttpService.factionId(detail.getOrElse("faction", "")),
                  flipped = true
                )
              )
            }
            record(
              AdminAction(
                at = System.currentTimeMillis(),
                admin = admin.getOrElse("unknown"),
                action = action,
                detail = detail,
                ok = ok,
                message = r.message.trim,
                source = ip.toOption.map(_.getHostAddress).getOrElse("")
              )
            )
            complete(render(r))
          case Failure(e) =>
            record(
              AdminAction(
                at = System.currentTimeMillis(),
                admin = admin.getOrElse("unknown"),
                action = action,
                detail = detail,
                ok = false,
                message = e.getMessage,
                source = ip.toOption.map(_.getHostAddress).getOrElse("")
              )
            )
            complete(
              HttpResponse(
                StatusCodes.ServiceUnavailable,
                entity =
                  HttpEntity(ContentTypes.`application/json`, write(Map("message" -> e.getMessage, "error" -> true)))
              )
            )
        }
      }
    }

  /** Pull a string field out of a JSON request body. */
  private def field(body: String, name: String): Option[String] =
    scala.util.Try((parse(body) \ name).extract[String]).toOption

  // --- routes ------------------------------------------------------------------------------------

  private def routes: Route = concat(
    // Reads.
    path("players")(get(runRoute(classOf[CmdListPlayers], Array.empty))),
    path("zones")(get(runRoute(classOf[CmdListZones], Array.empty))),
    path("lattice")(get(runRoute(classOf[CmdListLattice], Array.empty))),
    // Combat snapshot for one continent: soldiers, vehicles (with seat/cargo), and deployables.
    path("zones" / Segment / "combat") { zoneId =>
      get(runRoute(classOf[CmdCombatSnapshot], Array(zoneId)))
    },
    // Base captures and failed captures, newest first; same seven-day in-memory retention.
    path("interstellar-log") {
      get {
        val entries = snapshotEvents(System.currentTimeMillis())
        complete(
          HttpResponse(
            StatusCodes.OK,
            entity = HttpEntity(
              ContentTypes.`application/json`,
              write(Map("retention_days" -> 7, "count" -> entries.size, "events" -> entries))
            )
          )
        )
      }
    },
    // The in-memory audit trail of administrative actions, newest first.
    path("log") {
      get {
        val now = System.currentTimeMillis()
        val entries = snapshotLog(now)
        complete(
          HttpResponse(
            StatusCodes.OK,
            entity = HttpEntity(
              ContentTypes.`application/json`,
              write(
                Map(
                  "retention_days" -> 7,
                  "count"          -> entries.size,
                  "actions"        -> entries
                )
              )
            )
          )
        )
      }
    },

    // Ban or unban an account. The world server owns this so the ban also takes effect immediately:
    // every character on the account is kicked, rather than only being blocked at next login.
    path("accounts" / IntNumber / "ban") { accountId =>
      post(entity(as[String]) { body =>
        val banned = scala.util.Try((parse(body) \ "banned").extract[Boolean]).getOrElse(true)
        auditedRoute("account.ban", Map("account" -> accountId.toString, "banned" -> banned.toString))(
          classOf[CmdSetAccountBan],
          Array(accountId.toString, banned.toString)
        )
      })
    },

    // Act on one live entity from the Combat view: kill or kick a player, deconstruct or destroy a
    // vehicle or deployable, or hand a vehicle to another empire.
    // Body: { "action": "kill" | "kick" | "deconstruct" | "destroy" | "hack:<TR|NC|VS>" }
    path("zones" / Segment / "entities" / IntNumber / "action") { (zoneId, guid) =>
      post(entity(as[String]) { body =>
        field(body, "action") match {
          case Some(a) =>
            auditedRoute(
              "entity.action",
              Map("zone" -> zoneId, "entity" -> guid.toString, "action" -> a)
            )(classOf[CmdEntityAction], Array(zoneId, guid.toString, a))
          case None => complete(StatusCodes.BadRequest, """{"message":"missing action","error":true}""")
        }
      })
    },

    // Act on a whole continent at once. The portal password-confirms these before calling, since
    // none of them can be undone.
    // Body: { "action": "killall" | "kickall" | "strike", "x": n, "y": n, "radius": n }
    path("zones" / Segment / "action") { zoneId =>
      post(entity(as[String]) { body =>
        field(body, "action") match {
          case Some(a) =>
            val json = scala.util.Try(parse(body)).toOption
            def num(name: String): String =
              json.flatMap(j => scala.util.Try((j \ name).extract[Double]).toOption).fold("")(_.toString)
            val extra = Array(num("x"), num("y"), num("radius"))
            auditedRoute(
              "zone.action",
              Map("zone" -> zoneId, "action" -> a) ++
                (if (a == "strike") Map("at" -> s"${num("x")},${num("y")}", "radius" -> num("radius"))
                 else Map.empty[String, String])
            )(classOf[CmdZoneAction], Array(zoneId, a) ++ extra)
          case None => complete(StatusCodes.BadRequest, """{"message":"missing action","error":true}""")
        }
      })
    },

    // Set or clear the account-level game-master flag.
    path("accounts" / IntNumber / "gm") { accountId =>
      post(entity(as[String]) { body =>
        val gm = scala.util.Try((parse(body) \ "gm").extract[Boolean]).getOrElse(false)
        auditedRoute("account.gm", Map("account" -> accountId.toString, "gm" -> gm.toString))(
          classOf[CmdSetAccountGm],
          Array(accountId.toString, gm.toString)
        )
      })
    },

    // Grant or revoke a character's GM / spectator permissions. Owned here so the change reaches a
    // logged-in session rather than waiting for the player's next login.
    // Body: { "can_gm": bool?, "can_spectate": bool? } -- omitted fields keep their stored value.
    path("avatars" / IntNumber / "permissions") { avatarId =>
      post(entity(as[String]) { body =>
        val json = scala.util.Try(parse(body)).toOption
        val flags = Seq("can_gm" -> "gm", "can_spectate" -> "spectate").flatMap { case (name, token) =>
          json.flatMap(j => scala.util.Try((j \ name).extract[Boolean]).toOption).map(v => s"$token:$v")
        }
        if (flags.isEmpty) complete(StatusCodes.BadRequest, """{"message":"no permissions given","error":true}""")
        else
          auditedRoute(
            "avatar.permissions",
            Map("avatar" -> avatarId.toString, "set" -> flags.mkString(" "))
          )(classOf[CmdSetAvatarPermissions], avatarId.toString +: flags.toArray)
      })
    },

    // Place a vehicle into a continent. `kind` is "ams", "router", or "vehicle:<name>"; an AMS and a
    // Router arrive already deployed. Height and attitude are derived from the terrain, so the caller
    // supplies only a 2D point. A Router may also carry its telepad's point.
    // Body: { "kind": "...", "faction": "TR|NC|VS", "x": n, "y": n, "telepad_x"?: n, "telepad_y"?: n }
    path("zones" / Segment / "vehicles") { zoneId =>
      post(entity(as[String]) { body =>
        val json = scala.util.Try(parse(body)).toOption
        def str(k: String) = json.flatMap(j => scala.util.Try((j \ k).extract[String]).toOption).filter(_.nonEmpty)
        def num(k: String) = json.flatMap(j => scala.util.Try((j \ k).extract[Double]).toOption)

        (str("kind"), str("faction"), num("x"), num("y")) match {
          case (Some(kind), Some(faction), Some(px), Some(py)) =>
            val base = Array(zoneId, kind, faction, px.toString, py.toString)
            val pad  = (num("telepad_x"), num("telepad_y")) match {
              case (Some(tx), Some(ty)) => Array(tx.toString, ty.toString)
              case _                    => Array.empty[String]
            }
            auditedRoute(
              "zone.place_vehicle",
              Map("zone" -> zoneId, "kind" -> kind, "faction" -> faction, "at" -> s"$px,$py")
            )(classOf[CmdPlaceVehicle], base ++ pad)
          case _ =>
            complete(StatusCodes.BadRequest, """{"message":"kind, faction, x and y are required","error":true}""")
        }
      })
    },

    // Move a logged-in character: a forced sanctuary recall, or a transfer to another zone.
    // Body: { "to": "sanctuary" | "<zoneId>", "gate": "<gateId>"? }
    // Omitting "gate" on a zone transfer picks one of that zone's warp gates at random, matching
    // what the in-game `/zone` command does.
    path("players" / Segment / "transfer") { name =>
      post(entity(as[String]) { body =>
        val json = scala.util.Try(parse(body)).toOption
        val to   = json.flatMap(j => scala.util.Try((j \ "to").extract[String]).toOption).filter(_.nonEmpty)
        val gate = json.flatMap(j => scala.util.Try((j \ "gate").extract[String]).toOption).filter(_.nonEmpty)
        to match {
          case Some(dest) =>
            auditedRoute(
              "player.transfer",
              Map("player" -> name, "to" -> dest) ++ gate.map(g => "gate" -> g)
            )(classOf[CmdPlayerTransfer], Array(name, dest) ++ gate.toArray)
          case None =>
            complete(StatusCodes.BadRequest, """{"message":"missing destination","error":true}""")
        }
      })
    },

    // Writes: force a whole continent to an empire.
    path("zones" / Segment / "faction") { zoneId =>
      post(entity(as[String]) { body =>
        field(body, "faction") match {
          case Some(f) =>
            auditedRoute("zone.faction", Map("zone" -> zoneId, "faction" -> f))(
              classOf[CmdSetZoneFaction],
              Array(zoneId, f)
            )
          case None => complete(StatusCodes.BadRequest, """{"message":"missing faction","error":true}""")
        }
      })
    },
    // Force a single facility.
    path("zones" / Segment / "buildings" / IntNumber / "faction") { (zoneId, localId) =>
      post(entity(as[String]) { body =>
        field(body, "faction") match {
          case Some(f) =>
            auditedRoute(
              "building.faction",
              Map("zone" -> zoneId, "building" -> localId.toString, "faction" -> f)
            )(classOf[CmdSetBuildingFaction], Array(zoneId, localId.toString, f))
          case None => complete(StatusCodes.BadRequest, """{"message":"missing faction","error":true}""")
        }
      })
    },
    // Bulk-set many facilities: body { "assignments": { "<localId>": <factionId 0-3>, ... } }.
    path("zones" / Segment / "buildings") { zoneId =>
      post(entity(as[String]) { body =>
        val pairs = scala.util
          .Try {
            (parse(body) \ "assignments").extract[Map[String, BigInt]].map { case (id, f) => s"$id:$f" }.toArray
          }
          .getOrElse(Array.empty[String])
        if (pairs.isEmpty) complete(StatusCodes.BadRequest, """{"message":"no assignments","error":true}""")
        else
          auditedRoute("zone.buildings", Map("zone" -> zoneId, "assignments" -> pairs.length.toString))(
            classOf[CmdSetZoneBuildings],
            zoneId +: pairs
          )
      })
    }
  )
}

/**
  * Runs a single PSAdmin command actor and fulfils `promise` with its response. The command actor
  * replies to its parent (this bridge); a timeout guards against a command that never answers (e.g.
  * the interstellar cluster service is unavailable).
  */
class CommandBridge(handler: Class[_], args: Array[String], promise: Promise[CommandResponse]) extends Actor {
  import scala.concurrent.duration._
  import context.dispatcher

  context.actorOf(Props(handler, args, mutable.Map[String, ActorRef]()))
  private val timeout = context.system.scheduler.scheduleOnce(8.seconds, self, "timeout")

  override def receive: Receive = {
    case r: CommandResponse =>
      timeout.cancel()
      promise.trySuccess(r)
      context.stop(self)
    case "timeout" =>
      promise.tryFailure(new RuntimeException("admin command timed out"))
      context.stop(self)
  }
}

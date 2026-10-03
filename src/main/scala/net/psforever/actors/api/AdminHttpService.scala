package net.psforever.actors.api

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, Directive0, Directive1, Route}
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization.write
import org.json4s.{DefaultFormats, Formats}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
  * The PSF-HTTP API, served over Akka HTTP in place of the old raw-TCP PSAdmin protocol.
  *
  * This actor owns the HTTP binding for its whole lifetime -- it is the "server hosted in an actor":
  * bound on preStart, unbound on stop, and any bind failure terminates the system exactly as the old
  * TCP listener did. Each route reuses the existing command actors (list/set/randomize) via a
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
    * is ownership set through this API rather than won in the field.
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

  /** Report an interstellar event. Does nothing if the PSF-HTTP API is not running. */
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
      log.info(s"PSF-HTTP API listening on ${b.localAddress}")
    case default =>
      log.error(s"Unexpected message $default")
  }

  // --- command bridging --------------------------------------------------------------------------

  /**
    * Run one of the existing command actors and capture its reply. The command actor replies
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
    * Like [[runRoute]], but audits the call. Used for every state-changing route, which is also why
    * the game-master check lives here rather than being repeated on each one: a route added later
    * cannot forget it.
    *
    * The entry is attributed to the account behind the caller's session -- a name this process
    * resolved from the database, not one the caller supplied. `X-Admin-User` used to fill this in and
    * anyone could write anything in it, so the log recorded whoever the caller claimed to be.
    */
  private def auditedRoute(action: String, detail: Map[String, String])(
      handler: Class[_],
      args: Array[String]
  ): Route =
    gameMasterCaller { actor =>
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
                  // Prefer the facility name the command resolved for us. The request only carries a
                  // local id, which is meaningless in a log a person reads; a whole-continent or bulk
                  // designation has no single facility, so it says so rather than reporting a count.
                  base = r match {
                    case CommandGoodResponse(_, d) if d.contains("building_name") => d("building_name").toString
                    case _ if action == "zone.faction"    => "(whole continent)"
                    case _ if action == "zone.buildings"  => s"(${detail.getOrElse("assignments", "?")} facilities)"
                    case _                                => s"building ${detail.getOrElse("building", "?")}"
                  },
                  actor = actor.username,
                  from = -1,
                  to = AdminHttpService.factionId(detail.getOrElse("faction", "")),
                  flipped = true
                )
              )
            }
            record(
              AdminAction(
                at = System.currentTimeMillis(),
                admin = actor.username,
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
                admin = actor.username,
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

  /**
    * Complete a request from a database read.
    *
    * Portal reads are plain queries with no world-state effect, so they take this path rather than the
    * command-actor bridge: there is nothing to serialise through an actor, and nothing to audit. A
    * failed query answers 503 rather than 500 -- the database being unreachable is the login server
    * being degraded, not the caller having asked for something wrong.
    */
  private def queryRoute(query: => Future[Any]): Route =
    onComplete(query) {
      case Success(rows) =>
        // json4s serialises a bare `None` as nothing at all, which is an empty body rather than JSON.
        // A route that looks up one row and finds none has to answer `null` for the portal to be able
        // to tell "absent" from "the server broke".
        val body = rows match {
          case None  => "null"
          case other => write(other)
        }
        complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, body)))
      case Failure(e) =>
        log.error(e)("portal query failed")
        complete(
          HttpResponse(
            StatusCodes.ServiceUnavailable,
            entity = HttpEntity(ContentTypes.`application/json`, write(Map("message" -> "database unavailable", "error" -> true)))
          )
        )
    }

  /** A 200 carrying a JSON body. */
  private def jsonOk(value: Any): HttpResponse =
    HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, write(value)))

  /**
    * A page of rows plus the total the pager needs.
    *
    * The portal computes page numbers from this; it is deliberately not told a page number back,
    * since it is the side that decided which page to ask for.
    */
  private def paged(items: Seq[Any], total: Long): Map[String, Any] =
    Map("items" -> items, "item_count" -> total)

  /**
    * An upper bound on page size.
    *
    * A caller supplies `limit`, and a caller that asks for a million rows should not get them: this is
    * a listing API, and no page the portal renders is larger than this. Also guards against a negative
    * limit, which Postgres rejects outright.
    */
  private def capped(limit: Int): Int = math.max(1, math.min(limit, 500))

  // --- caller authorisation ----------------------------------------------------------------------

  /**
    * Refuse a request, in the shape every other error here takes.
    *
    * 401 means "I do not know who you are"; 403 means "I do, and you may not". Keeping them distinct
    * matters to the portal, which retries nothing on a 403 but can surface a re-login on a 401.
    */
  private def refuse(status: StatusCode, message: String): Route =
    complete(
      HttpResponse(
        status,
        entity = HttpEntity(ContentTypes.`application/json`, write(Map("message" -> message, "error" -> true)))
      )
    )

  /**
    * The caller behind `X-PSF-Session`, or a 401.
    *
    * A banned account is rejected here rather than at each call site. It has a valid session and a
    * real identity, so 403 is the honest answer -- but it may do nothing at all, and letting it read
    * even public-ish endpoints through an authenticated path would be a way to check whether a ban
    * had been lifted.
    */
  private def caller: Directive1[CallerAuth.Caller] =
    optionalHeaderValueByName(CallerAuth.SessionHeader).flatMap {
      case None =>
        Directive(_ => refuse(StatusCodes.Unauthorized, s"${CallerAuth.SessionHeader} required"))
      case Some(sid) =>
        onComplete(CallerAuth.resolve(sid)).flatMap {
          case Success(Some(c)) if c.inactive =>
            Directive(_ => refuse(StatusCodes.Forbidden, "account is banned"))
          case Success(Some(c)) => provide(c)
          case Success(None) =>
            Directive(_ => refuse(StatusCodes.Unauthorized, "session is unknown or expired"))
          case Failure(e) =>
            log.error(e)("session lookup failed")
            Directive(_ => refuse(StatusCodes.ServiceUnavailable, "cannot verify session"))
        }
    }

  /** Any signed-in account. */
  private def authenticated: Directive1[CallerAuth.Caller] = caller

  /** A game master, keeping the identity for attribution. */
  private def gameMasterCaller: Directive1[CallerAuth.Caller] =
    caller.flatMap { c =>
      if (c.gm) provide(c)
      else Directive(_ => refuse(StatusCodes.Forbidden, "game-master rights required"))
    }

  /** A game master, and nobody else. */
  private def gameMaster: Directive0 =
    caller.flatMap { c =>
      if (c.gm) pass
      else Directive(_ => refuse(StatusCodes.Forbidden, "game-master rights required"))
    }

  /**
    * The account named in the path, or a game master.
    *
    * This is what keeps one player out of another's login history and character list. A game master
    * passes because the admin panel legitimately reads any account.
    */
  private def selfOrGameMaster(accountId: Int): Directive0 =
    caller.flatMap { c =>
      if (c.gm || c.accountId == accountId) pass
      else Directive(_ => refuse(StatusCodes.Forbidden, "not your account"))
    }

  /**
    * The owner of the character named in the path, or a game master.
    *
    * Ownership is looked up, not inferred. The refusal is a 404 rather than a 403 on purpose: a 403
    * would confirm the character exists, which turns this into a way to enumerate valid character
    * ids. Someone else's character and a character that was never created answer identically.
    */
  private def avatarOwnerOrGameMaster(avatarId: Int): Directive0 =
    caller.flatMap { c =>
      if (c.gm) pass
      else
        onComplete(CallerAuth.ownsAvatar(c.accountId, avatarId)).flatMap {
          case Success(true) => pass
          case Success(false) =>
            Directive(_ => refuse(StatusCodes.NotFound, s"no character $avatarId"))
          case Failure(e) =>
            log.error(e)("ownership check failed")
            Directive(_ => refuse(StatusCodes.ServiceUnavailable, "cannot verify ownership"))
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
    // Exact positions of every online player. Admin-only, and always was.
    path("zones" / Segment / "combat") { zoneId =>
      get(gameMaster(runRoute(classOf[CmdCombatSnapshot], Array(zoneId))))
    },
    // Who holds each capturable facility, from the live zones rather than the database.
    path("zones" / "control") {
      get(runRoute(classOf[CmdListBuildingControl], Array.empty))
    },
    path("zones" / Segment / "control") { zoneId =>
      get(runRoute(classOf[CmdListBuildingControl], Array(zoneId)))
    },

    // --- portal reads --------------------------------------------------------------------------
    // Projections of the database for the web portal, which no longer has a PostgreSQL pool of its
    // own. Grouped under /portal so they read as a distinct surface from the world-admin routes, and
    // served without auditing: they change nothing, and every open admin page polls them.
    path("portal" / "accounts") {
      get {
        parameters("offset".as[Int].withDefault(0), "limit".as[Int].withDefault(25),
                   "sort".withDefault("created"), "order".withDefault("desc"),
                   "filter".withDefault("all")) { (offset, limit, sort, order, filter) =>
          gameMaster(queryRoute(
            for {
              items <- PortalQueries.accountsWithLastLogin(offset, capped(limit), sort, order == "asc", filter)
              total <- PortalQueries.accountCount(filter)
            } yield paged(items, total)
          ))
        }
      }
    },
    path("portal" / "accounts" / IntNumber) { id =>
      get(selfOrGameMaster(id)(queryRoute(PortalQueries.account(id).map(_.headOption))))
    },
    path("portal" / "accounts" / IntNumber / "logins") { id =>
      get {
        parameters("offset".as[Int].withDefault(0), "limit".as[Int].withDefault(25)) { (offset, limit) =>
          selfOrGameMaster(id) {
            queryRoute(
              for {
                items <- PortalQueries.accountLogins(id, offset, capped(limit))
                total <- PortalQueries.loginCount(id)
              } yield paged(items, total)
            )
          }
        }
      }
    },
    path("portal" / "accounts" / IntNumber / "characters") { id =>
      get(selfOrGameMaster(id)(queryRoute(PortalQueries.charactersByAccount(id))))
    },
    path("portal" / "characters") {
      get {
        parameters("offset".as[Int].withDefault(0), "limit".as[Int].withDefault(25)) { (offset, limit) =>
          gameMaster {
            queryRoute(
              for {
                items <- PortalQueries.characters(offset, capped(limit))
                total <- PortalQueries.characterCount()
              } yield paged(items, total)
            )
          }
        }
      }
    },
    path("portal" / "characters" / "by-name" / Segment) { name =>
      get(queryRoute(PortalQueries.characterByName(name).map(_.headOption)))
    },
    path("portal" / "characters" / "batch" / IntNumber) { batch =>
      get {
        parameters("sort".withDefault("id"), "order".withDefault("asc")) { (sort, order) =>
          queryRoute(PortalQueries.characterBatch(batch, sort, order == "asc"))
        }
      }
    },
    path("portal" / "roles") {
      get {
        parameters("offset".as[Int].withDefault(0), "limit".as[Int].withDefault(25)) { (offset, limit) =>
          gameMaster {
            queryRoute(
              for {
                items <- PortalQueries.roles(offset, capped(limit))
                total <- PortalQueries.roleCount()
              } yield paged(items, total)
            )
          }
        }
      }
    },
    path("portal" / "avatars" / IntNumber) { id =>
      get(queryRoute(PortalQueries.avatar(id).map(_.headOption)))
    },
    path("portal" / "avatars" / IntNumber / "owner") { id =>
      get(avatarOwnerOrGameMaster(id)(queryRoute(PortalQueries.avatarOwner(id).map(_.headOption))))
    },
    path("portal" / "avatars" / IntNumber / "weapon-stats") { id =>
      get(queryRoute(PortalQueries.weaponStats(id)))
    },
    path("portal" / "avatars" / IntNumber / "kd-by-date") { id =>
      get(queryRoute(PortalQueries.avatarKdByDate(id)))
    },
    path("portal" / "avatars" / IntNumber / "locker") { id =>
      get(avatarOwnerOrGameMaster(id)(queryRoute(PortalQueries.lockerItems(id).map(_.headOption))))
    },
    path("portal" / "avatars" / IntNumber / "loadouts") { id =>
      get(avatarOwnerOrGameMaster(id)(queryRoute(PortalQueries.loadouts(id))))
    },
    path("portal" / "avatars" / IntNumber / "vehicle-loadouts") { id =>
      get(avatarOwnerOrGameMaster(id)(queryRoute(PortalQueries.vehicleLoadouts(id))))
    },
    path("portal" / "leaderboard" / "top-kills") {
      get(queryRoute(PortalQueries.topKills()))
    },
    path("portal" / "leaderboard" / "top-kills-by-date") {
      get(queryRoute(PortalQueries.topKillsByDate()))
    },
    path("portal" / "leaderboard" / "top-outfits") {
      get(queryRoute(PortalQueries.topOutfits()))
    },
    path("portal" / "outfits" / IntNumber) { id =>
      get(queryRoute(PortalQueries.outfit(id).map(_.headOption)))
    },
    path("portal" / "outfits" / IntNumber / "members") { id =>
      get(queryRoute(PortalQueries.outfitMembers(id)))
    },
    // Accounts and characters matching one term. Both halves are returned separately rather than
    // merged: they carry different fields, and the portal labels and sorts them itself.
    path("portal" / "search") {
      get {
        parameters("q", "offset".as[Int].withDefault(0), "limit".as[Int].withDefault(25)) { (term, offset, limit) =>
          gameMaster {
          // `%` is stripped rather than escaped -- a caller has no business steering the LIKE pattern,
          // and a term shorter than three characters matches too much to be worth running.
          val cleaned = term.replace("%", "")
          if (cleaned.length < 3) {
            complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`,
              write(Map("accounts" -> List.empty[String], "characters" -> List.empty[String])))))
          } else {
            val pattern = s"%${cleaned.toUpperCase}%"
            queryRoute(
              for {
                accounts   <- PortalQueries.searchAccounts(pattern, offset, capped(limit))
                characters <- PortalQueries.searchCharacters(pattern, offset, capped(limit))
              } yield Map("accounts" -> accounts, "characters" -> characters)
            )
          }
          }
        }
      }
    },
    path("portal" / "stats") {
      get(
        queryRoute(
          for {
            accounts   <- PortalQueries.accountTotal()
            characters <- PortalQueries.characterCount()
            newest     <- PortalQueries.newestCharacter()
          } yield Map(
            "accounts"       -> accounts,
            "characters"     -> characters,
            "last_character" -> newest.headOption
          )
        )
      )
    },
    // Registration. 409 rather than 400 for a taken username: the request was well-formed, the
    // world just already contains that name.
    path("portal" / "accounts") {
      post(entity(as[String]) { body =>
        (field(body, "username"), field(body, "password")) match {
          case (Some(username), Some(password)) =>
            onComplete(PortalQueries.createAccount(username, password)) {
              case Success(Some(id)) => complete(jsonOk(Map("id" -> id)))
              case Success(None) =>
                complete(HttpResponse(StatusCodes.Conflict, entity = HttpEntity(ContentTypes.`application/json`,
                  write(Map("message" -> "username already taken", "error" -> true)))))
              case Failure(e) =>
                log.error(e)("account creation failed")
                complete(HttpResponse(StatusCodes.ServiceUnavailable, entity = HttpEntity(
                  ContentTypes.`application/json`, write(Map("message" -> "database unavailable", "error" -> true)))))
            }
          case _ => complete(StatusCodes.BadRequest, """{"message":"username and password required","error":true}""")
        }
      })
    },
    // Password check, and the ONLY place a session is bound to an account. No hash crosses the wire,
    // and the refusal is identical whether the account is missing, wrong-password, or banned.
    //
    // The caller passes the session id it wants bound. That is what makes every other check on this
    // API mean something: `account_id` is written here, after a password has been verified, and
    // `sessionSet` refuses to let a caller write it any other way. Without this split, anyone able to
    // store a session could name themselves a game master.
    //
    // Public by necessity -- it is how a caller stops being anonymous.
    path("portal" / "login") {
      post(entity(as[String]) { body =>
        (field(body, "username"), field(body, "password")) match {
          case (Some(username), Some(password)) =>
            onComplete(PortalQueries.validateAccount(username, password)) {
              case Success(Some(id)) =>
                (field(body, "session_id"), scala.util.Try((parse(body) \ "expires").extract[Long]).toOption) match {
                  case (Some(sid), Some(expires)) =>
                    onComplete(PortalQueries.sessionBindAccount(sid, id, expires)) {
                      case Success(_) => complete(jsonOk(Map("account_id" -> id)))
                      case Failure(e) =>
                        log.error(e)("could not bind the session")
                        complete(HttpResponse(StatusCodes.ServiceUnavailable, entity = HttpEntity(
                          ContentTypes.`application/json`,
                          write(Map("message" -> "could not establish a session", "error" -> true)))))
                    }
                  // A caller that verified a password but asked for no session gets the answer and no
                  // session. Useful for a health check; useless for acting as anyone.
                  case _ => complete(jsonOk(Map("account_id" -> id)))
                }
              case Success(None) =>
                complete(HttpResponse(StatusCodes.Unauthorized, entity = HttpEntity(
                  ContentTypes.`application/json`, write(Map("message" -> "invalid credentials", "error" -> true)))))
              case Failure(e) =>
                log.error(e)("login check failed")
                complete(HttpResponse(StatusCodes.ServiceUnavailable, entity = HttpEntity(
                  ContentTypes.`application/json`, write(Map("message" -> "database unavailable", "error" -> true)))))
            }
          case _ => complete(StatusCodes.BadRequest, """{"message":"username and password required","error":true}""")
        }
      })
    },
    // The portal's Express session store. Opaque here on purpose: the session body is whatever
    // express-session serialised, and this end only keeps it and expires it.
    //
    // These are the one group NOT behind a caller check, and deliberately so -- a session cannot be
    // required in order to store a session. They are self-authenticating instead: the session id in
    // the path IS the credential, exactly as the browser cookie carrying it is. Reading, touching or
    // deleting a session requires already knowing its id, which is the same thing as holding it.
    //
    // Writing is the case that would otherwise be dangerous, and is defused in `sessionSet` rather
    // than here: it strips `account_id` from whatever is offered and carries the stored value across,
    // so a caller can write junk into a session it knows the id of but cannot name itself an account
    // -- let alone a game master. `sessionBindAccount`, reached only from the login route once a
    // password has been checked, is the sole writer of that field.
    //
    // `reap` deletes rows that have ALREADY expired, and nothing else.
    path("portal" / "sessions" / "reap") {
      post(queryRoute(PortalQueries.sessionReap().map(n => Map("removed" -> n))))
    },
    path("portal" / "sessions" / Segment) { sid =>
      get(queryRoute(PortalQueries.sessionGet(sid).map(_.headOption.map(_.sess)))) ~
        put(entity(as[String]) { body =>
          (field(body, "sess"), scala.util.Try((parse(body) \ "expires").extract[Long]).toOption) match {
            case (Some(sess), Some(expires)) =>
              queryRoute(PortalQueries.sessionSet(sid, sess, expires).map(n => Map("stored" -> n)))
            case _ => complete(StatusCodes.BadRequest, """{"message":"sess and expires required","error":true}""")
          }
        }) ~
        patch(entity(as[String]) { body =>
          scala.util.Try((parse(body) \ "expires").extract[Long]).toOption match {
            case Some(expires) => queryRoute(PortalQueries.sessionTouch(sid, expires).map(n => Map("touched" -> n)))
            case None => complete(StatusCodes.BadRequest, """{"message":"expires required","error":true}""")
          }
        }) ~
        delete(queryRoute(PortalQueries.sessionDestroy(sid).map(n => Map("removed" -> n))))
    },
    // ---- Packet Review -------------------------------------------------------------------
    //
    // Capture exists only while somebody is looking at it. The page joins, keeps saying so while it
    // collects, and says when it leaves; the world server arms the union of what the current
    // watchers asked for and holds a small shared window they all read from. Nothing here persists,
    // and nothing is captured at all when the page is closed -- see `PacketCapture`.
    //
    // Every route is game-master only. Captured packets are other players' traffic.

    // The opcode tables, both spaces, with what is charted, what is armed and what may never be.
    path("packet-review" / "opcodes") {
      get {
        gameMaster {
          complete(
            HttpResponse(
              StatusCodes.OK,
              entity = HttpEntity(
                ContentTypes.`application/json`,
                write(
                  Map(
                    "opcodes"  -> PacketCapture.catalogue,
                    "redacted" -> PacketCapture.Redacted.map { case (k, v) => k.toString -> v },
                    "state"    -> PacketCapture.state
                  )
                )
              )
            )
          )
        }
      }
    },
    // Join the capture, or renew a place in it, with this page's selection. Called on start and on
    // every poll, so a selection changed in the UI applies on the next collection without a second
    // round trip and without the two ever disagreeing.
    path("packet-review" / "subscribe") {
      post {
        gameMasterCaller { c =>
          entity(as[String]) { body =>
            val json   = parse(body)
            val viewer = scala.util.Try((json \ "viewer").extract[String]).toOption.getOrElse("")
            val game   = scala.util.Try((json \ "game").extract[List[Int]]).toOption.getOrElse(Nil).toSet
            val ctl    = scala.util.Try((json \ "control").extract[List[Int]]).toOption.getOrElse(Nil).toSet
            if (viewer.isEmpty) {
              complete(StatusCodes.BadRequest, """{"message":"viewer required","error":true}""")
            } else {
              val (state, refused) = PacketCapture.subscribe(viewer, c.username, game, ctl)
              complete(
                HttpResponse(
                  StatusCodes.OK,
                  entity = HttpEntity(
                    ContentTypes.`application/json`,
                    write(
                      Map(
                        "state" -> state,
                        // Named rather than merely absent, so the page can say why a selection it
                        // offered did not take rather than appearing to lose it.
                        "refused" -> refused.toList.sorted.map { o =>
                          Map("opcode" -> o.toString, "reason" -> PacketCapture.Redacted(o))
                        }
                      )
                    )
                  )
                )
              )
            }
          }
        }
      }
    },
    // Give up a place immediately -- the page navigating away or the tab closing. The lease would
    // lapse on its own shortly after; this makes leaving take effect at once.
    path("packet-review" / "leave") {
      post {
        gameMaster {
          entity(as[String]) { body =>
            val viewer = scala.util.Try((parse(body) \ "viewer").extract[String]).toOption.getOrElse("")
            PacketCapture.leave(viewer)
            complete(
              HttpResponse(
                StatusCodes.OK,
                entity = HttpEntity(ContentTypes.`application/json`, write(Map("state" -> PacketCapture.state)))
              )
            )
          }
        }
      }
    },
    // Stop capture for everyone at once, whoever started it.
    path("packet-review" / "stop") {
      post {
        gameMaster {
          PacketCapture.stopAll()
          complete(
            HttpResponse(
              StatusCodes.OK,
              entity = HttpEntity(ContentTypes.`application/json`, write(Map("state" -> PacketCapture.state)))
            )
          )
        }
      }
    },
    // Read forward from a cursor. Reading does not consume: every watcher walks the same window at
    // its own pace, so two admins reviewing the same opcode share one capture. `oldest` is how a
    // page that fell behind knows it missed something rather than quietly skipping it.
    path("packet-review") {
      get {
        gameMaster {
          parameters("viewer".withDefault(""), "since".as[Long].withDefault(0L), "limit".as[Int].withDefault(200)) {
            (viewer, cursor, limit) =>
              val packets = PacketCapture.since(viewer, cursor, math.min(math.max(limit, 1), 1000))
              val state   = PacketCapture.state
              complete(
                HttpResponse(
                  StatusCodes.OK,
                  entity = HttpEntity(
                    ContentTypes.`application/json`,
                    write(
                      Map(
                        "packets" -> packets,
                        "cursor"  -> packets.lastOption.map(_.id).getOrElse(cursor),
                        "oldest"  -> state.oldestId,
                        "state"   -> state
                      )
                    )
                  )
                )
              )
          }
        }
      }
    },
    // Base captures and failed captures, newest first; same seven-day in-memory retention.
    path("interstellar-log") {
      get {
        gameMaster {
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
      }
    },
    // The in-memory audit trail of administrative actions, newest first.
    path("log") {
      get {
        gameMaster {
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
  * Runs a single command actor and fulfils `promise` with its response. The command actor
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

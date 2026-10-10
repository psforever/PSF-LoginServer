package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, Cancellable}
import net.psforever.objects.LivePlayerList
import net.psforever.objects.zones.Zone
import net.psforever.persistence
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import org.joda.time.LocalDateTime

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * One input an action needs, described well enough that a form can be built from it elsewhere.
  *
  * The portal renders the Scheduling form from this rather than from anything it knows about a
  * particular action, which is what makes adding an action cheap: an entry in [[ScheduledActions]]
  * and nothing else. Without it, every new action would still need a hand-written form and the
  * registry would only be moving the work rather than removing it.
  */
case class ActionField(
    kind: String,
    name: String,
    label: String,
    help: Option[String] = None,
    placeholder: Option[String] = None,
    rows: Option[Int] = None,
    min: Option[Double] = None,
    step: Option[Double] = None,
    options: Option[List[Map[String, String]]] = None,
    value: Option[String] = None
)

case class ActionSpec(key: String, label: String, description: String, fields: List[ActionField])

/**
  * The actions a schedule can perform, and the catalogue the portal builds its form from.
  *
  * ==Why this is here and not in the portal==
  * It used to be in the portal, for one reason: applying a layout means turning zone NUMBERS into
  * zone ids, and the portal ships a continent index that knows the mapping. That reason does not
  * survive contact with `Zone.Number` -- the world server has known both all along, so the asset was
  * never actually needed.
  *
  * What decided it is availability. A schedule that fires at four in the morning has to be evaluated
  * by a process that is running at four in the morning, and the process that is up whenever the game
  * is up is this one. With the evaluator in the portal, a portal restart or a portal outage meant
  * schedules silently did not run while the world carried on regardless -- and nothing in the world
  * server could even tell you a schedule existed.
  */
object ScheduledActions {

  val ContinentLayout = "continent_layout"
  val ZoneFaction     = "zone_faction"

  /** Faction tokens the zone action accepts, in the order the UI should offer them. */
  private val Empires = List("TR" -> "Terran Republic", "NC" -> "New Conglomerate", "VS" -> "Vanu Sovereignty")

  /**
    * The catalogue, given the zones the world currently holds.
    *
    * The continent picker's options come from the live zone list rather than a static table, so a
    * zone added to the server appears here without anybody remembering to update a list.
    */
  def catalogue(zones: Seq[Zone]): List[ActionSpec] = {
    val continents = zones
      .filterNot(z => z.id.startsWith("home") || z.id.startsWith("tz"))
      .map(z => Map("value" -> z.id, "label" -> z.map.name))
      .sortBy(_.getOrElse("label", ""))
      .toList

    List(
      ActionSpec(
        key = ContinentLayout,
        label = "Apply a continent layout",
        description = "Hands each named base to a named empire, exactly as the Continents view would.",
        fields = List(
          ActionField(
            kind = "json",
            name = "control",
            label = "Layout (JSON)",
            rows = Some(10),
            help = Some(
              "Zone number to building local id to faction id (0 TR, 1 NC, 2 VS). The same shape the Continents view applies, so a layout can be built there and pasted here."
            ),
            placeholder = Some("{\n  \"1\": { \"2\": 0, \"5\": 1 },\n  \"2\": { \"3\": 2 }\n}")
          )
        )
      ),
      ActionSpec(
        key = ZoneFaction,
        label = "Give a continent to an empire",
        description = "Hands every base on one continent to a single empire.",
        fields = List(
          ActionField(
            kind = "select",
            name = "zoneId",
            label = "Continent",
            help = Some("Which continent to hand over."),
            options = Some(continents)
          ),
          ActionField(
            kind = "select",
            name = "faction",
            label = "Empire",
            value = Some("TR"),
            options = Some(Empires.map { case (v, l) => Map("value" -> v, "label" -> l) })
          )
        )
      )
    )
  }

  def known(key: String): Boolean = key == ContinentLayout || key == ZoneFaction
}

/**
  * Evaluates the portal's scheduled actions.
  *
  * ==Triggers==
  * A schedule carries a time trigger, a condition trigger, or both, and either firing runs it -- they
  * are alternatives, not a conjunction.
  *
  * A condition fires on the CROSSING, not while it stays true. "Fewer than ten players" holds all
  * night; firing on the state would re-apply the action every tick. `armed` makes it an edge, and it
  * is a stored column rather than a field here because a restart during a quiet night must not
  * re-arm and fire a second time.
  *
  * ==Claims==
  * The claim survives from when this lived in the portal, where two processes could evaluate the same
  * schedule. Only one world server runs, so it is no longer load-bearing -- but it is kept, because
  * it also protects against this actor being restarted mid-run and against anything else that ever
  * gains the ability to fire a schedule. It costs one statement.
  *
  * A time trigger claims against its due instant, which any evaluator computes identically. A
  * condition has no such instant, so its claim is the atomic disarm: only the update that actually
  * flips `armed` wins.
  */
class ScheduledActionsActor extends Actor {
  private[this] val log = org.log4s.getLogger

  import context.dispatcher

  /** How often schedules are evaluated. */
  private val TickInterval: FiniteDuration = 30.seconds

  /**
    * How late a time-triggered run may still fire.
    *
    * A schedule due at 04:00 that could not run -- the server was restarting -- should still run at
    * 04:05. It should not still run at 16:00, quietly rearranging a continent half a day late.
    */
  private val LateGraceMillis: Long = 60 * 60 * 1000L

  /**
    * How long to wait between starting one continent's facility changes and the next.
    *
    * Comfortably longer than a single continent's paced run, so a layout covering the whole map is a
    * sequence of small bursts rather than one large one. A full map takes well under a minute this
    * way, which matters to nothing that runs on a schedule.
    */
  private val ZoneStagger: FiniteDuration = 1500.milliseconds

  private var ticker: Cancellable = Default.cancellable
  private var zones: Seq[Zone]    = Nil

  override def preStart(): Unit = {
    ServiceManager.receptionist ! Receptionist.Find(
      InterstellarClusterService.InterstellarClusterServiceKey,
      context.self
    )
    ticker = context.system.scheduler.scheduleWithFixedDelay(TickInterval, TickInterval)(() => context.self ! Tick)
  }

  override def postStop(): Unit = ticker.cancel()

  private case object Tick
  private case class ApplyZoneLayout(args: Array[String])
  private case class Loaded(rows: Seq[persistence.Portalschedule])
  private case class Claimed(row: persistence.Portalschedule, due: Option[Long])
  private case class Ran(row: persistence.Portalschedule, result: String, wasTimeTrigger: Boolean)

  override def receive: Receive = {
    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      listings.head ! InterstellarClusterService.FilterZones(_ => true, context.self)

    case InterstellarClusterService.ZonesResponse(z) =>
      zones = z.toSeq

    case Tick =>
      // Zones are re-read each tick rather than cached once: a zone list captured at startup would go
      // stale, and the layout action needs the number-to-id mapping to be current.
      ServiceManager.receptionist ! Receptionist.Find(
        InterstellarClusterService.InterstellarClusterServiceKey,
        context.self
      )
      PortalQueries.enabledSchedules().onComplete {
        case Success(rows) => self ! Loaded(rows)
        case Failure(e)    => log.debug(s"could not read schedules: ${e.getMessage}")
      }

    case Loaded(rows) =>
      val now     = LocalDateTime.now()
      val players = LivePlayerList.WorldPopulation(_ => true).size
      rows.foreach(row => evaluate(row, now, players))

    case Claimed(row, due) =>
      perform(row).onComplete {
        case Success(result) => self ! Ran(row, result, due.isDefined)
        case Failure(e)      => self ! Ran(row, s"failed: ${e.getMessage}", due.isDefined)
      }

    case ApplyZoneLayout(args) =>
      context.actorOf(akka.actor.Props(new CmdSetZoneBuildings(args, scala.collection.mutable.Map())))

    case Ran(row, result, wasTimeTrigger) =>
      // A condition run has already disarmed itself as its claim; a time run leaves arming alone.
      PortalQueries.recordScheduleResult(row.id, result, if (wasTimeTrigger) row.armed else false)
      log.info(s"scheduled action #${row.id} '${row.name}': $result")

    case default =>
      log.debug(s"unexpected message $default")
  }

  /** Decide whether this schedule should run, and if so claim it. */
  private def evaluate(row: persistence.Portalschedule, now: LocalDateTime, players: Int): Unit = {
    val holds = conditionHolds(row, players)

    // A condition that has gone back the other way re-arms, and that is all it does this tick.
    if (holds.contains(false) && !row.armed) {
      PortalQueries.setScheduleArmed(row.id, armed = true)
      return
    }

    val due            = timeDue(row, now)
    val conditionFires = holds.contains(true) && row.armed
    if (due.isEmpty && !conditionFires) return

    val claim = due match {
      case Some(instant) => PortalQueries.claimSchedule(row.id, new LocalDateTime(instant), now)
      case None          => PortalQueries.claimArmedSchedule(row.id, now)
    }
    claim.onComplete {
      case Success(n) if n > 0 => self ! Claimed(row, due)
      case Success(_)          => ()
      case Failure(e)          => log.debug(s"could not claim schedule ${row.id}: ${e.getMessage}")
    }
  }

  /** Whether a condition trigger's comparison currently holds. */
  private def conditionHolds(row: persistence.Portalschedule, players: Int): Option[Boolean] = {
    (row.condition, row.threshold) match {
      case (Some("players_below"), Some(t)) => Some(players < t)
      case (Some("players_above"), Some(t)) => Some(players > t)
      case _                                => None
    }
  }

  /**
    * When this time-triggered schedule was most recently due, or None if it is not due.
    *
    * The DUE INSTANT is returned rather than a boolean because that instant is what the claim is
    * made against.
    */
  private def timeDue(row: persistence.Portalschedule, now: LocalDateTime): Option[Long] = {
    for {
      atTime  <- row.atTime
      minutes <- parseHhMm(atTime)
    } yield {
      val everyDays = math.max(1, row.everyDays.getOrElse(1))
      val due = now.withTime(minutes / 60, minutes % 60, 0, 0)
      val dueMillis = due.toDate.getTime
      val nowMillis = now.toDate.getTime

      if (nowMillis < dueMillis) None
      else if (nowMillis - dueMillis > LateGraceMillis) None
      else
        row.lastRun match {
          case Some(last) =>
            val lastMillis = last.toDate.getTime
            if (lastMillis >= dueMillis) None
            else if ((dueMillis - lastMillis) / (24 * 60 * 60 * 1000L) < everyDays) None
            else Some(dueMillis)
          case None => Some(dueMillis)
        }
    }
  }.flatten

  private def parseHhMm(value: String): Option[Int] = {
    value.trim.split(":") match {
      case Array(h, m) =>
        (h.toIntOption, m.toIntOption) match {
          case (Some(hh), Some(mm)) if hh >= 0 && hh <= 23 && mm >= 0 && mm <= 59 => Some(hh * 60 + mm)
          case _                                                                  => None
        }
      case _ => None
    }
  }

  /** Perform a schedule's action, answering with the sentence recorded as its result. */
  private def perform(row: persistence.Portalschedule): scala.concurrent.Future[String] = {
    val json = scala.util.Try(org.json4s.native.JsonMethods.parse(row.payload)).toOption
    row.action match {
      case ScheduledActions.ContinentLayout =>
        applyLayout(json)
      case ScheduledActions.ZoneFaction =>
        applyZoneFaction(json)
      case other =>
        scala.concurrent.Future.successful(s"failed: unknown action '$other'")
    }
  }

  /**
    * Apply a layout: zone number -> { building local id -> faction id }.
    *
    * The number-to-id mapping comes from the live zone list. This is the mapping the portal used to
    * need a shipped asset for; the world server has had it all along.
    */
  private def applyLayout(json: Option[org.json4s.JValue]): scala.concurrent.Future[String] = {
    implicit val formats: org.json4s.Formats = org.json4s.DefaultFormats
    val control = json
      .flatMap(j => scala.util.Try((j \ "control").extract[Map[String, Map[String, Int]]]).toOption)
      .getOrElse(Map.empty)
    if (control.isEmpty) return scala.concurrent.Future.successful("failed: the layout names no continents")

    val byNumber = zones.map(z => z.Number.toString -> z.id).toMap
    var applied  = 0
    control.foreach {
      case (zoneNumber, buildings) =>
        byNumber.get(zoneNumber).foreach { zoneId =>
          val args = zoneId +: buildings.map { case (localId, faction) => s"$localId:$faction" }.toArray
          // Continents are started one after another rather than all at once. Each one already paces
          // its own facilities, but a layout naming the whole map would otherwise run seventeen of
          // those paced runs concurrently and put the total broadcast rate right back where it was.
          // Spawning is deferred through a message to self so the actor is created on this actor's
          // own thread rather than on a scheduler thread.
          context.system.scheduler.scheduleOnce(ZoneStagger * applied.toLong, self, ApplyZoneLayout(args))
          applied += 1
        }
    }
    scala.concurrent.Future.successful(s"applying to $applied continent(s)")
  }

  private def applyZoneFaction(json: Option[org.json4s.JValue]): scala.concurrent.Future[String] = {
    implicit val formats: org.json4s.Formats = org.json4s.DefaultFormats
    val zoneId  = json.flatMap(j => scala.util.Try((j \ "zoneId").extract[String]).toOption).getOrElse("")
    val faction = json.flatMap(j => scala.util.Try((j \ "faction").extract[String]).toOption).getOrElse("")
    if (zoneId.isEmpty || faction.isEmpty) {
      scala.concurrent.Future.successful("failed: a continent and an empire are required")
    } else {
      context.actorOf(akka.actor.Props(new CmdSetZoneFaction(Array(zoneId, faction), scala.collection.mutable.Map())))
      scala.concurrent.Future.successful(s"$zoneId given to $faction")
    }
  }
}

private object Default {
  val cancellable: Cancellable = new Cancellable {
    def cancel(): Boolean      = true
    def isCancelled: Boolean   = true
  }
}

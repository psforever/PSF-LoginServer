package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, ActorRef}
import net.psforever.objects.Default
import net.psforever.objects.zones.Zone
import net.psforever.services.ServiceManager.{Lookup, LookupResult}
import net.psforever.services.{InterstellarClusterService, ServiceManager}

import scala.collection.mutable.Map

/**
  * Act on everything in a continent at once, from the administration portal's Combat view.
  *
  * These are the blunt instruments -- clearing a continent, emptying it of players, or flattening a
  * chosen patch of it. They exist for the cases where acting entity by entity is impractical: a stuck
  * fight, a griefing incident, a continent that has to be emptied before a reset. The portal makes an
  * administrator re-enter their password before any of them fire, because unlike the single-entity
  * actions there is no way to undo a mistake here.
  *
  * `strike` calls in the game's own orbital strike -- the client plays the strike it already knows
  * how to play, and the game's own targeting decides what dies. It is the CR4 (10m) or CR5 (20m)
  * strike, the only two the game defines.
  *
  * A strike's target height is never taken from a caller -- see [[AdminWorldOps.orbitalStrike]] for
  * why -- so there is no height argument here to go with it.
  *
  * args: `<zoneId> <action> [x] [y] [radius]`, where action is one of
  * `killall`, `kickall`, `strike`
  */
class CmdZoneAction(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val zoneId: String = args.headOption.getOrElse("")
  private val action: String = args.lift(1).getOrElse("")
  private val x: Float = args.lift(2).flatMap(_.toFloatOption).getOrElse(Float.NaN)
  private val y: Float = args.lift(3).flatMap(_.toFloatOption).getOrElse(Float.NaN)
  private val radius: Float = args.lift(4).flatMap(_.toFloatOption).getOrElse(0f)

  private var accountPersistence: ActorRef = Default.Actor
  private var persistenceReady: Boolean = false

  /** The zone, if it arrived before the service lookup did; the two replies race. */
  private var pendingZone: Option[Zone] = None

  override def preStart(): Unit = {
    if (zoneId.isEmpty) {
      fail("invalid zone")
    } else if (!Set("killall", "kickall", "strike").contains(action)) {
      fail(s"unknown action '$action'")
    } else if (action == "strike" && (x.isNaN || y.isNaN)) {
      fail("a strike needs a position")
    } else if (action == "strike" && radius != 10f && radius != 20f) {
      // The game defines exactly two strikes, CR4 (10m) and CR5 (20m). Anything else would be
      // silently snapped to one of them, so it is rejected instead.
      fail("a strike radius must be 10 (CR4) or 20 (CR5)")
    } else {
      ServiceManager.serviceManager ! Lookup("accountPersistence")
    }
  }

  private def fail(msg: String): Unit = {
    context.parent ! CommandErrorResponse(s"$msg\n", Map[String, Any]())
    context.stop(self)
  }

  override def receive: Receive = {
    case LookupResult("accountPersistence", endpoint) =>
      accountPersistence = endpoint
      persistenceReady = true
      pendingZone match {
        case Some(zone) => act(zone)
        case None =>
          ServiceManager.receptionist ! Receptionist.Find(
            InterstellarClusterService.InterstellarClusterServiceKey,
            context.self
          )
      }

    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      listings.head ! InterstellarClusterService.FilterZones(_.id.equals(zoneId), context.self)

    case InterstellarClusterService.ZonesResponse(zones) =>
      zones.headOption match {
        case None                           => fail(s"no zone '$zoneId'")
        case Some(zone) if persistenceReady => act(zone)
        case Some(zone)                     => pendingZone = Some(zone)
      }

    case default => log.error(s"Unexpected message $default")
  }

  private def act(zone: Zone): Unit = action match {
    case "killall" =>
      val victims = zone.LivePlayers.filter(_.isAlive)
      victims.foreach(AdminWorldOps.destroy)
      finish(
        s"killed ${victims.size} in $zoneId",
        Map("players" -> victims.size, "names" -> victims.map(_.Name).toArray)
      )

    case "kickall" =>
      val victims = zone.LivePlayers
      victims.foreach(p => AdminWorldOps.kick(zone, p, accountPersistence))
      finish(
        s"kicked ${victims.size} from $zoneId",
        Map("players" -> victims.size, "names" -> victims.map(_.Name).toArray)
      )

    case "strike" =>
      // The real thing: the client is told to play the strike it already knows, and the game's own
      // targeting decides what dies five seconds later. The count reported here is what was standing
      // in the blast when it was called in, which is not necessarily what it catches when it lands.
      import scala.concurrent.ExecutionContext.Implicits.global
      val caught = AdminWorldOps.orbitalStrike(zone, x, y, radius, context.system.scheduler)
      finish(
        s"orbital strike called in at ${x.toInt},${y.toInt} r=${radius.toInt}; $caught in the blast",
        Map("radius" -> radius.toInt, "in_blast" -> caught)
      )

    case other => fail(s"unknown action '$other'")
  }

  private def finish(message: String, extra: scala.collection.Map[String, Any]): Unit = {
    val data = Map[String, Any]()
    data("zone_id") = zoneId
    data("action") = action
    extra.foreach { case (k, v) => data(k) = v }
    log.warn(s"admin API: $message")
    context.parent ! CommandGoodResponse(s"$message\n", data)
    context.stop(self)
  }
}

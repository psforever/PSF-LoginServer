package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, ActorRef}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.zones.Zone
import net.psforever.objects.{Default, Player, Vehicle, Vehicles}
import net.psforever.services.ServiceManager.{Lookup, LookupResult}
import net.psforever.services.base.envelope.MessageEnvelope
import net.psforever.services.base.message.SetEmpire
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable.Map

/**
  * Act on one live entity in a continent from the administration portal's Combat view.
  *
  * Everything here is something an administrator could already do from inside the game with customer
  * service commands; the point of routing it through the PSF-Server HTTP API is that it does not require an
  * administrator to be logged in and standing next to the thing.
  *
  * "Deconstruct" and "destroy" are deliberately different. Deconstruction is the clean nanite recall
  * the game uses when a vehicle times out -- the entity simply goes away. Destruction runs the normal
  * death path, so the wreck, the explosion and the kill feed all behave as if it had been shot; that
  * is what [[SuicideReason]] is for, a damage profile that guarantees death with no adversary
  * attached, so nothing is credited with the kill.
  *
  * args: `<zoneId> <guid> <action>`, where action is one of
  * `kill`, `kick`, `deconstruct`, `destroy`, or `hack:<TR|NC|VS>`
  */
class CmdEntityAction(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val zoneId: String = args.headOption.getOrElse("")
  private val guid: Int = args.lift(1).flatMap(_.toIntOption).getOrElse(-1)
  private val action: String = args.lift(2).getOrElse("")

  /** Only set for `hack:`; the empire the entity is being handed to. */
  private val hackTo: Option[PlanetSideEmpire.Value] =
    if (action.startsWith("hack:")) {
      scala.util.Try(PlanetSideEmpire.withName(action.drop(5).toUpperCase)).toOption
        .filter(_ != PlanetSideEmpire.NEUTRAL)
    } else {
      None
    }

  private val ValidActions = Set("kill", "kick", "deconstruct", "destroy")

  private var accountPersistence: ActorRef = Default.Actor
  private var persistenceReady: Boolean = false

  /** The zone, if it arrived before the service lookup did; the two replies race. */
  private var pendingZone: Option[Zone] = None

  override def preStart(): Unit = {
    if (zoneId.isEmpty || guid < 0) {
      fail("invalid entity reference")
    } else if (!ValidActions.contains(action) && hackTo.isEmpty) {
      fail(s"unknown action '$action'")
    } else {
      // Only the player kick needs it, but it is cheap and the lookup is asynchronous either way.
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
        case None => fail(s"no zone '$zoneId'")
        // A kick needs the persistence service; hold the zone until the lookup lands.
        case Some(zone) if persistenceReady => act(zone)
        case Some(zone)                     => pendingZone = Some(zone)
      }

    case default => log.error(s"Unexpected message $default")
  }

  private def act(zone: Zone): Unit = {
    zone.GUID(guid) match {
      case Some(p: Player)      => actOnPlayer(zone, p)
      case Some(v: Vehicle)     => actOnVehicle(zone, v)
      case Some(d: Deployable)  => actOnDeployable(zone, d)
      case Some(other)          => fail(s"entity $guid is a ${other.Definition.Name}, which takes no actions")
      case None                 => fail(s"no entity $guid in $zoneId")
    }
  }

  // --- players -------------------------------------------------------------------------------

  private def actOnPlayer(zone: Zone, p: Player): Unit = action match {
    case "kill" =>
      AdminWorldOps.destroy(p)
      finish(s"${p.Name} was killed", Map("name" -> p.Name))

    case "kick" =>
      // Mirrors an in-game administrative kick: the flag makes the player's own session send the
      // disconnect, and the persistence service records a logout rather than a dropped connection.
      AdminWorldOps.kick(zone, p, accountPersistence)
      finish(s"${p.Name} was kicked", Map("name" -> p.Name))

    case other => fail(s"cannot '$other' a player")
  }

  // --- vehicles ------------------------------------------------------------------------------

  private def actOnVehicle(zone: Zone, v: Vehicle): Unit = {
    val occupants = v.Seats.values.flatMap(_.occupant).toSeq

    (action, hackTo) match {
      case ("deconstruct", _) =>
        // Occupants are put out first so they are left standing rather than vanishing with the hull.
        AdminWorldOps.evictOccupants(zone, v)
        v.Actor ! Vehicle.Deconstruct(None)
        finish(
          s"${v.Definition.Name} deconstructed",
          Map("evicted" -> occupants.map(_.Name).toArray)
        )

      case ("destroy", _) =>
        // Everyone aboard dies with it, which is what destroying an occupied vehicle means.
        occupants.foreach(AdminWorldOps.destroy)
        AdminWorldOps.destroy(v)
        finish(
          s"${v.Definition.Name} destroyed",
          Map("killed" -> occupants.map(_.Name).toArray)
        )

      case (_, Some(faction)) =>
        if (v.Faction == faction) {
          fail(s"${v.Definition.Name} already belongs to $faction")
        } else {
          // The same shape as an in-game jack: everyone is thrown out, the old owner loses the
          // vehicle, and the hull changes colours for every client that can see it.
          AdminWorldOps.evictOccupants(zone, v)
          v.OwnerGuid.flatMap(zone.GUID).collect { case owner: Player => Vehicles.Disown(owner, v) }
          v.Faction = faction
          zone.AvatarEvents ! MessageEnvelope(zone.id, SetEmpire(v.GUID, faction))
          finish(
            s"${v.Definition.Name} hacked to $faction",
            Map("faction" -> faction.id, "evicted" -> occupants.map(_.Name).toArray)
          )
        }

      case (other, _) => fail(s"cannot '$other' a vehicle")
    }
  }

  // --- deployables ---------------------------------------------------------------------------

  private def actOnDeployable(zone: Zone, d: Deployable): Unit = action match {
    case "deconstruct" =>
      d.Actor ! Deployable.Deconstruct()
      finish(s"${d.Definition.Name} deconstructed", Map[String, Any]())

    case "destroy" =>
      AdminWorldOps.destroy(d)
      finish(s"${d.Definition.Name} destroyed", Map[String, Any]())

    case other => fail(s"cannot '$other' a deployable")
  }

  // --- shared --------------------------------------------------------------------------------

  private def finish(message: String, extra: scala.collection.Map[String, Any]): Unit = {
    val data = Map[String, Any]()
    data("zone_id") = zoneId
    data("guid") = guid
    data("action") = action
    extra.foreach { case (k, v) => data(k) = v }
    log.warn(s"PSF-Server HTTP API: $message (${zoneId}/$guid)")
    context.parent ! CommandGoodResponse(s"$message\n", data)
    context.stop(self)
  }
}

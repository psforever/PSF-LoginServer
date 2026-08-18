package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.{Actor, ActorRef}
import akka.actor.typed.scaladsl.adapter._
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
import net.psforever.objects.serverobject.structures.StructureType
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.types.PlanetSideEmpire

import scala.concurrent.duration._
import scala.collection.mutable.Map

/**
  * Force a SINGLE facility's ownership to an empire, live -- the per-building counterpart to
  * `set_zone_faction`. Lets an admin correct one base's state without sweeping the whole continent.
  *
  * Usage: `set_building_faction <zone_id> <local_id> <TR|NC|VS|neutral>` (faction also accepts 0-3).
  *
  * `local_id` is the building's map id (matching `building.local_id` in the database and the `id`
  * the portal's facility data uses). The change goes through `BuildingActor.SetFaction`, which writes
  * the database AND the live entity and broadcasts it, then the zone's lock is recomputed a moment
  * later (the update is asynchronous, so the lock must read the settled faction, not the old one).
  */
class CmdSetBuildingFaction(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val parsed: Either[String, (String, Int, PlanetSideEmpire.Value)] =
    if (args.length < 3) {
      Left("usage: set_building_faction <zone_id> <local_id> <TR|NC|VS|neutral>")
    } else {
      val faction = CmdSetZoneFaction.parseFaction(args(2))
      args(1).toIntOption match {
        case None                          => Left(s"'${args(1)}' is not a valid local_id")
        case Some(_) if faction.isEmpty    => Left(s"'${args(2)}' is not a valid faction (TR, NC, VS, neutral)")
        case Some(localId)                 => Right((args(0), localId, faction.get))
      }
    }

  override def preStart(): Unit = {
    parsed match {
      case Right(_) =>
        ServiceManager.receptionist ! Receptionist.Find(
          InterstellarClusterService.InterstellarClusterServiceKey,
          context.self
        )
      case Left(msg) =>
        context.parent ! CommandErrorResponse(msg + "\n", Map[String, Any]())
        context.stop(self)
    }
  }

  override def receive: Receive = {
    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      val zoneId = parsed.toOption.get._1
      listings.head ! InterstellarClusterService.FilterZones(_.id == zoneId, context.self)

    case InterstellarClusterService.ZonesResponse(zones) =>
      val (zoneId, localId, faction) = parsed.toOption.get
      zones.headOption match {
        case None =>
          context.parent ! CommandErrorResponse(s"no loaded zone with id '$zoneId'\n", Map[String, Any]())

        case Some(zone) =>
          zone.Buildings.values.find(b => b.MapId == localId && b.CaptureTerminal.isDefined) match {
            case None =>
              context.parent ! CommandErrorResponse(
                s"no capturable building with local_id $localId in $zoneId\n",
                Map[String, Any]()
              )

            case Some(building) =>
              val terminal = building.CaptureTerminal.get
              building.Actor ! BuildingActor.SetFaction(faction)
              building.Actor ! BuildingActor.AmenityStateChange(terminal, Some(false))
              if (building.BuildingType == StructureType.Tower) {
                building.Actor ! BuildingActor.MapUpdate()
              }

              // Let the SetFaction settle, then recompute and broadcast the continental lock.
              import context.dispatcher
              context.system.scheduler.scheduleOnce(750.milliseconds) {
                zone.actor ! ZoneActor.ZoneMapUpdate()
                zone.actor ! ZoneActor.AssignLockedBy(zone, notifyPlayers = true)
              }

              val data = Map[String, Any]()
              data("zone_id")  = zone.id
              data("local_id") = localId
              data("faction")  = faction.toString
              context.parent ! CommandGoodResponse(
                s"set $zoneId building $localId to ${faction.toString}\n",
                data
              )
          }
      }

    case default => log.error(s"Unexpected message $default")
  }
}

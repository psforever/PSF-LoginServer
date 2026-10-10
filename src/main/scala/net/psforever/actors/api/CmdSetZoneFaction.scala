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
  * Force a whole continent's ownership to one empire, live, the same way the CSR `/capturebase`
  * command does -- so an admin can set the current state of a zone without restarting the server.
  *
  * Usage: `set_zone_faction <zone_id> <TR|NC|VS|neutral>` (faction also accepts 0/1/2/3).
  *
  * Only capturable buildings (those with a capture terminal -- facilities and towers) are set; warp
  * gates and geowarps are game-controlled and left alone. Each building's faction is written through
  * `BuildingActor.SetFaction`, which updates the database AND the live entity and broadcasts the
  * change, then the zone's lock is recomputed by `ZoneActor.AssignLockedBy`. The lock reassessment is
  * scheduled a moment later because the per-building updates are asynchronous: the zone must read the
  * new factions, not the old ones.
  */
class CmdSetZoneFaction(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val parsed: Either[String, (String, PlanetSideEmpire.Value)] =
    if (args.length < 2) {
      Left("usage: set_zone_faction <zone_id> <TR|NC|VS|neutral>")
    } else {
      CmdSetZoneFaction.parseFaction(args(1)) match {
        case Some(faction) => Right((args(0), faction))
        case None          => Left(s"'${args(1)}' is not a valid faction (TR, NC, VS, neutral)")
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
      val (zoneId, faction) = parsed.toOption.get
      zones.headOption match {
        case None =>
          context.parent ! CommandErrorResponse(s"no loaded zone with id '$zoneId'\n", Map[String, Any]())

        case Some(zone) =>
          // Same set the CSR capture command touches: buildings with a capture terminal (facilities
          // and towers), skipping any already owned by the target faction.
          val toChange = zone.Buildings.values
            .filter(b => b.CaptureTerminal.isDefined && b.Faction != faction)
            .toSeq

          toChange.foreach { building =>
            val terminal = building.CaptureTerminal.get
            building.Actor ! BuildingActor.SetFaction(faction)
            building.Actor ! BuildingActor.AmenityStateChange(terminal, Some(false))
            if (building.BuildingType == StructureType.Tower) {
              building.Actor ! BuildingActor.MapUpdate()
            }
          }

          // Let the per-building SetFaction messages settle, then recompute and broadcast the lock.
          import context.dispatcher
          context.system.scheduler.scheduleOnce(750.milliseconds) {
            zone.actor ! ZoneActor.ZoneMapUpdate()
            zone.actor ! ZoneActor.AssignLockedBy(zone, notifyPlayers = true)
          }

          val data = Map[String, Any]()
          data("zone_id")           = zone.id
          data("zone_number")       = zone.Number
          data("faction")           = faction.toString
          data("buildings_changed") = toChange.size
          context.parent ! CommandGoodResponse(
            s"set ${toChange.size} buildings in ${zone.id} to ${faction.toString}\n",
            data
          )
      }

    case default => log.error(s"Unexpected message $default")
  }
}

object CmdSetZoneFaction {
  /** Accept an empire by short name (TR/NC/VS/neutral/none) or numeric id (0..3). */
  def parseFaction(token: String): Option[PlanetSideEmpire.Value] =
    token.trim.toLowerCase match {
      case "tr" | "0"                 => Some(PlanetSideEmpire.TR)
      case "nc" | "1"                 => Some(PlanetSideEmpire.NC)
      case "vs" | "2"                 => Some(PlanetSideEmpire.VS)
      case "neutral" | "none" | "3"   => Some(PlanetSideEmpire.NEUTRAL)
      case _                          => None
    }
}

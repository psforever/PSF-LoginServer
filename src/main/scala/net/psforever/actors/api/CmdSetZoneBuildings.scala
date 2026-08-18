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
  * Set MANY of a zone's facilities to (possibly different) empires in one command -- the bulk form of
  * `set_building_faction`. This exists so a computed continent state (e.g. the admin "randomize"
  * feature, which assigns each facility independently) can be applied in one round trip per zone
  * instead of hundreds of per-building commands.
  *
  * Usage: `set_zone_buildings <zone_id> <local_id>:<faction_id> <local_id>:<faction_id> ...`, where
  * faction_id is 0 TR / 1 NC / 2 VS / 3 neutral. Each building is set through `BuildingActor.SetFaction`
  * (database + live entity + broadcast); the zone lock is recomputed once after they settle.
  */
class CmdSetZoneBuildings(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val parsed: Either[String, (String, scala.collection.immutable.Map[Int, PlanetSideEmpire.Value])] = {
    if (args.length < 2) {
      Left("usage: set_zone_buildings <zone_id> <local_id>:<faction_id> ...")
    } else {
      val pairs = args.drop(1).map { tok =>
        tok.split(":") match {
          case Array(a, b) =>
            (a.toIntOption, b.toIntOption) match {
              case (Some(id), Some(f)) if f >= 0 && f <= 3 => Some(id -> PlanetSideEmpire(f))
              case _                                       => None
            }
          case _ => None
        }
      }
      if (pairs.contains(None)) Left("each argument must be <local_id>:<faction_id 0-3>")
      else Right((args(0), pairs.flatten.toMap))
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
      val (zoneId, wanted) = parsed.toOption.get
      zones.headOption match {
        case None =>
          context.parent ! CommandErrorResponse(s"no loaded zone with id '$zoneId'\n", Map[String, Any]())

        case Some(zone) =>
          var changed = 0
          zone.Buildings.values
            .filter(b => b.CaptureTerminal.isDefined && wanted.contains(b.MapId))
            .foreach { building =>
              val faction = wanted(building.MapId)
              if (building.Faction != faction) {
                val terminal = building.CaptureTerminal.get
                building.Actor ! BuildingActor.SetFaction(faction)
                building.Actor ! BuildingActor.AmenityStateChange(terminal, Some(false))
                if (building.BuildingType == StructureType.Tower) {
                  building.Actor ! BuildingActor.MapUpdate()
                }
                changed += 1
              }
            }

          import context.dispatcher
          context.system.scheduler.scheduleOnce(750.milliseconds) {
            zone.actor ! ZoneActor.ZoneMapUpdate()
            zone.actor ! ZoneActor.AssignLockedBy(zone, notifyPlayers = true)
          }

          val data = Map[String, Any]()
          data("zone_id")           = zone.id
          data("buildings_changed") = changed
          context.parent ! CommandGoodResponse(s"set $changed buildings in $zoneId\n", data)
      }

    case default => log.error(s"Unexpected message $default")
  }
}

package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.{Actor, ActorRef}
import akka.actor.typed.scaladsl.adapter._
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.zones.Zone
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
  *
  * WHY THE WORK IS PACED
  * Every facility set here broadcasts to every client, and a continent carries around thirty of them.
  * Applied in one pass that is a burst of hundreds of packets per client arriving in a single moment,
  * which is enough to push a packet clean out of the outbound resend history before a client that
  * missed it can ask for it again. The client then waits forever for a packet the server can no
  * longer produce, and its reliable stream stalls -- the session stays up and locally predicted
  * actions still look fine, but anything needing a server round trip stops working. Draining the
  * facilities a few at a time keeps the burst inside that window. It costs a second or so per
  * continent, which nothing here is in a hurry enough to miss.
  */
class CmdSetZoneBuildings(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  import context.dispatcher

  /** Facilities applied per batch, and the gap between batches. */
  private val BatchSize: Int              = 5
  private val BatchInterval: FiniteDuration = 100.milliseconds

  /**
    * How long the facility changes are given to settle before the zone lock is recomputed.
    * Measured from the last batch rather than from the start, or a paced run would recompute the
    * lock while it was still changing facilities.
    */
  private val SettleDelay: FiniteDuration = 750.milliseconds

  /** Drain one batch of pending facilities. */
  private case object Drain

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
          context.stop(self)

        case Some(zone) =>
          // Settled up front so the count reported back is the count actually applied, and so a
          // facility that is already the wanted empire never reaches a batch.
          val pending = zone.Buildings.values
            .filter(b => b.CaptureTerminal.isDefined && wanted.contains(b.MapId))
            .filter(b => b.Faction != wanted(b.MapId))
            .toList
          context.become(draining(zone, wanted, pending, pending.size))
          self ! Drain
      }

    case default => log.error(s"Unexpected message $default")
  }

  private def draining(
                        zone: Zone,
                        wanted: scala.collection.immutable.Map[Int, PlanetSideEmpire.Value],
                        remaining: List[Building],
                        total: Int
                      ): Receive = {
    case Drain =>
      val (batch, rest) = remaining.splitAt(BatchSize)
      batch.foreach { building =>
        val faction  = wanted(building.MapId)
        val terminal = building.CaptureTerminal.get
        building.Actor ! BuildingActor.SetFaction(faction)
        building.Actor ! BuildingActor.AmenityStateChange(terminal, Some(false))
        if (building.BuildingType == StructureType.Tower) {
          building.Actor ! BuildingActor.MapUpdate()
        }
      }
      if (rest.isEmpty) {
        // Scheduled on the system scheduler rather than as a message to self, because this actor is
        // finished and its parent may stop the moment it is answered.
        context.system.scheduler.scheduleOnce(SettleDelay) {
          zone.actor ! ZoneActor.ZoneMapUpdate()
          zone.actor ! ZoneActor.AssignLockedBy(zone, notifyPlayers = true)
        }
        val data = Map[String, Any]()
        data("zone_id")           = zone.id
        data("buildings_changed") = total
        context.parent ! CommandGoodResponse(s"set $total buildings in ${zone.id}\n", data)
        context.stop(self)
      } else {
        context.become(draining(zone, wanted, rest, total))
        context.system.scheduler.scheduleOnce(BatchInterval, self, Drain)
      }

    case default => log.error(s"Unexpected message $default")
  }
}

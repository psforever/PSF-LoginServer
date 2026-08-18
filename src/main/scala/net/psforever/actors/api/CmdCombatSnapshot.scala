package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.{Actor, ActorRef}
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.types.ExoSuitType
import scala.collection.mutable.Map
import akka.actor.typed.scaladsl.adapter._

private case class SnapOccupant(guid: Int, name: String)

/** One vehicle seat: its index, the resolved role, and who (if anyone) is sitting in it. */
private case class SnapSeat(index: Int, role: String, occupant: Option[SnapOccupant])

/** A vehicle riding in a carrier's cargo hold (Lodestar / Galaxy vehicle slot). */
private case class SnapCargo(index: Int, guid: Int, name: String)

private case class SnapSoldier(
    guid: Int,
    name: String,
    faction: Int,
    x: Float,
    y: Float,
    facing: Float,
    max: Boolean,
    /** GUID of the vehicle this soldier is mounted in, or None when on foot. */
    seated: Option[Int]
)

private case class SnapVehicle(
    guid: Int,
    /** Internal codename, e.g. "reaver" / "galaxy" / "lodestar" / "ams"; the client formats it. */
    name: String,
    faction: Int,
    x: Float,
    y: Float,
    facing: Float,
    seats: Array[SnapSeat],
    cargo: Array[SnapCargo]
)

private case class SnapDeployable(guid: Int, name: String, category: String, faction: Int, x: Float, y: Float)

private case class CombatSnapshot(
    zone_id: String,
    zone_number: Int,
    soldiers: Array[SnapSoldier],
    vehicles: Array[SnapVehicle],
    deployables: Array[SnapDeployable]
)

/**
  * A one-shot combat snapshot of a single continent: every live soldier, vehicle (with seat
  * occupancy and cargo), and deployable (CE) currently on the zone, with world coordinates for map
  * placement. Read on demand when an admin opens a continent in the Combat view -- never polled for
  * every zone at once, since it is a lot of data.
  *
  * Seat roles follow the game's own grouping (Vehicle.scala): seat 0 is the driver (a "pilot" for
  * flying vehicles), any seat with a mounted weapon is a "gunner", the rest are "passenger" -- except
  * a MAX occupant, which always reads as a "max" seat.
  */
class CmdCombatSnapshot(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)
  private val zoneId: String = args.headOption.getOrElse("")

  override def preStart(): Unit = {
    ServiceManager.receptionist ! Receptionist.Find(
      InterstellarClusterService.InterstellarClusterServiceKey,
      context.self
    )
  }

  override def receive: Receive = {
    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      listings.head ! InterstellarClusterService.FilterZones(_.id == zoneId, context.self)

    case InterstellarClusterService.ZonesResponse(zones) =>
      zones.headOption match {
        case None =>
          context.parent ! CommandErrorResponse(s"zone '$zoneId' not found\n", Map[String, Any]())

        case Some(zone) =>
          val soldiers = zone.LivePlayers.map { p =>
            SnapSoldier(
              p.GUID.guid,
              p.Name,
              p.Faction.id,
              p.Position.x,
              p.Position.y,
              p.Orientation.z,
              p.ExoSuit == ExoSuitType.MAX,
              p.VehicleSeated.map(_.guid)
            )
          }.toArray

          val vehicles = zone.Vehicles.map { v =>
            val gunnerSeats = v.VisibleSlots
            val fly         = v.Definition.CanFly
            val seats = v.Seats.toSeq.sortBy(_._1).map { case (idx, seat) =>
              val occ = seat.occupant
              val role = occ match {
                case Some(pl) if pl.ExoSuit == ExoSuitType.MAX => "max"
                case _ if idx == 0                             => if (fly) "pilot" else "driver"
                case _ if gunnerSeats.contains(idx)            => "gunner"
                case _                                         => "passenger"
              }
              SnapSeat(idx, role, occ.map(pl => SnapOccupant(pl.GUID.guid, pl.Name)))
            }.toArray
            val cargo = v.CargoHolds.toSeq.sortBy(_._1).flatMap { case (idx, hold) =>
              hold.occupant.map(cv => SnapCargo(idx, cv.GUID.guid, cv.Definition.Name))
            }.toArray
            SnapVehicle(
              v.GUID.guid,
              v.Definition.Name,
              v.Faction.id,
              v.Position.x,
              v.Position.y,
              v.Orientation.z,
              seats,
              cargo
            )
          }.toArray

          val deployables = zone.DeployableList.filterNot(_.Destroyed).map { d =>
            SnapDeployable(
              d.GUID.guid,
              d.Definition.Name,
              d.Definition.DeployCategory.toString,
              d.Faction.id,
              d.Position.x,
              d.Position.y
            )
          }.toArray

          val data = Map[String, Any]()
          data("snapshot") = CombatSnapshot(zone.id, zone.Number, soldiers, vehicles, deployables)
          context.parent ! CommandGoodResponse(
            s"${soldiers.length} soldiers, ${vehicles.length} vehicles, ${deployables.length} deployables on ${zone.id}\n",
            data
          )
      }

    case default => log.error(s"Unexpected message $default")
  }
}

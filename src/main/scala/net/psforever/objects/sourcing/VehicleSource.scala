// Copyright (c) 2017 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.Vehicle
import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.types.{DriveState, PlanetSideEmpire, Vector3}

final case class UniqueVehicle(spawnTime: Long, originalOwnerName: String) extends SourceUniqueness

object UniqueVehicle {
  def apply(obj: Vehicle): UniqueVehicle = {
    UniqueVehicle(
      obj.History.headOption match {
        case Some(entry) => entry.time
        case None => 0L
      },
      obj.OriginalOwnerName.getOrElse("none")
    )
  }
}

final case class VehicleSource(
                                Definition: VehicleDefinition,
                                Faction: PlanetSideEmpire.Value,
                                health: Int,
                                shields: Int,
                                Position: Vector3,
                                Orientation: Vector3,
                                Velocity: Option[Vector3],
                                deployed: DriveState.Value,
                                owner: Option[UniquePlayer],
                                occupants: List[SourceEntry],
                                Modifiers: ResistanceProfile,
                                unique: UniqueVehicle
                              ) extends SourceWithHealthEntry with SourceWithShieldsEntry with MountableEntry {
  def Name: String = SourceEntry.NameFormat(Definition.Name)
  def Health: Int  = health
  def Shields: Int = shields
  def total: Int   = health + shields
}

object VehicleSource {
  def apply(obj: Vehicle): VehicleSource = {
    val faction = obj.HackedBy match {
      case Some(o) => o.player.Faction
      case _ => obj.Faction
    }
    val vehicle = VehicleSource(
      obj.Definition,
      faction,
      obj.Health,
      obj.Shields,
      obj.Position,
      obj.Orientation,
      obj.Velocity,
      obj.DeploymentState,
      None,
      Nil,
      obj.Definition.asInstanceOf[ResistanceProfile],
      UniqueVehicle(obj)
    )
    //shallow information that references the existing source entry
    vehicle.copy(
      owner = obj.Owners,
      occupants = obj.Seats.map { case (seatNumber, seat) =>
        seat.occupant match {
          case Some(p) => PlayerSource.inSeat(p, vehicle, seatNumber)
          case _ => PlayerSource.Nobody
        }
      }.toList
    )
  }
}

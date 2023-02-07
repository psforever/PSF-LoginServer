// Copyright (c) 2017 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.Vehicle
import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.types.{DriveState, PlanetSideEmpire, Vector3}

final case class UniqueVehicle(spawnTime: Long, originalOwnerName: String) extends SourceUniqueness

final case class VehicleSource(
                                Definition: VehicleDefinition,
                                Faction: PlanetSideEmpire.Value,
                                health: Int,
                                shields: Int,
                                Position: Vector3,
                                Orientation: Vector3,
                                Velocity: Option[Vector3],
                                deployed: DriveState.Value,
                                occupants: List[SourceEntry],
                                Modifiers: ResistanceProfile,
                                unique: UniqueVehicle
                              ) extends SourceWithHealthEntry with SourceWithShieldsEntry {
  def Name: String                  = SourceEntry.NameFormat(Definition.Name)
  def Health: Int                   = health
  def Shields: Int                  = shields
  def total: Int                    = health + shields
}

object VehicleSource {
  def apply(obj: Vehicle): VehicleSource = {
    val faction = obj.HackedBy match {
      case Some(o) => o.player.Faction
      case _ => obj.Faction
    }
    VehicleSource(
      obj.Definition,
      faction,
      obj.Health,
      obj.Shields,
      obj.Position,
      obj.Orientation,
      obj.Velocity,
      obj.DeploymentState,
      obj.Seats.values.map { seat =>
        seat.occupant match {
          case Some(p) => PlayerSource(p)
          case _ => SourceEntry.None
        }
      }.toList,
      obj.Definition.asInstanceOf[ResistanceProfile],
      UniqueVehicle(
        obj.History.headOption match {
          case Some(entry) => entry.time
          case None => 0L
        },
        obj.OriginalOwnerName.getOrElse("none")
      )
    )
  }
}

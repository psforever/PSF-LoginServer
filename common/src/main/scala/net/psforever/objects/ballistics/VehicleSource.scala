// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.Vehicle
import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.types.{PlanetSideEmpire, Vector3}

final case class VehicleSource(obj_def : VehicleDefinition,
                               faction : PlanetSideEmpire.Value,
                               health : Int,
                               shields : Int,
                               position : Vector3,
                               orientation : Vector3,
                               velocity : Option[Vector3],
                               modifiers : ResistanceProfile) extends SourceEntry {
  override def Name = SourceEntry.NameFormat(obj_def.Name)
  override def Faction = faction
  def Definition : VehicleDefinition = obj_def
  def Health = health
  def Shields = shields
  def Position = position
  def Orientation = orientation
  def Velocity = velocity
  def Modifiers = modifiers
}

object VehicleSource {
  def apply(obj : Vehicle) : VehicleSource = {
    VehicleSource(
      obj.Definition,
      obj.Faction,
      obj.Health,
      obj.Shields,
      obj.Position,
      obj.Orientation,
      obj.Velocity,
      obj.Definition.asInstanceOf[ResistanceProfile]
    )
  }
}

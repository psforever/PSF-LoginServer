// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.TurretDeployable
import net.psforever.objects.ce.ComplexDeployable
import net.psforever.objects.definition.{DeployableDefinition, ObjectDefinition}
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.types.{PlanetSideEmpire, Vector3}

final case class ComplexDeployableSource(
    obj_def: ObjectDefinition with DeployableDefinition,
    faction: PlanetSideEmpire.Value,
    health: Int,
    shields: Int,
    ownerName: String,
    position: Vector3,
    orientation: Vector3
) extends SourceEntry {
  override def Name                                              = SourceEntry.NameFormat(obj_def.Name)
  override def Faction                                           = faction
  def Definition: ObjectDefinition with DeployableDefinition = obj_def
  def Health                                                     = health
  def Shields                                                    = shields
  def OwnerName                                                  = ownerName
  def Position                                                   = position
  def Orientation                                                = orientation
  def Velocity                                                   = None
  def Modifiers                                                  = obj_def.asInstanceOf[ResistanceProfile]
}

object ComplexDeployableSource {
  def apply(obj: ComplexDeployable): ComplexDeployableSource = {
    ComplexDeployableSource(
      obj.Definition,
      obj.Faction,
      obj.Health,
      obj.Shields,
      obj.OwnerName.getOrElse(""),
      obj.Position,
      obj.Orientation
    )
  }

  def apply(obj: TurretDeployable): ComplexDeployableSource = {
    ComplexDeployableSource(
      obj.Definition,
      obj.Faction,
      obj.Health,
      obj.Shields,
      obj.OwnerName.getOrElse(""),
      obj.Position,
      obj.Orientation
    )
  }
}

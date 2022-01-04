// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.ce.Deployable
import net.psforever.objects.definition.{DeployableDefinition, ObjectDefinition}
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.types.{PlanetSideEmpire, Vector3}

final case class DeployableSource(
    obj_def: ObjectDefinition with DeployableDefinition,
    faction: PlanetSideEmpire.Value,
    health: Int,
    shields: Int,
    owner: SourceEntry,
    position: Vector3,
    orientation: Vector3
) extends SourceEntry {
  override def Name                                   = obj_def.Descriptor
  override def Faction                   = faction
  def Definition: ObjectDefinition with DeployableDefinition = obj_def
  def Health                                            = health
  def Shields                                           = shields
  def OwnerName                                       = owner.Name
  def Position                                       = position
  def Orientation                                    = orientation
  def Velocity                                               = None
  def Modifiers                               = obj_def.asInstanceOf[ResistanceProfile]
}

object DeployableSource {
  def apply(obj: Deployable): DeployableSource = {
    val ownerName = obj.OwnerName
    val ownerSource = (obj.Zone.LivePlayers ++ obj.Zone.Corpses)
      .find { p => ownerName.contains(p.Name) }
    match {
      case Some(p) => SourceEntry(p)
      case _ => SourceEntry.None
    }
    DeployableSource(
      obj.Definition,
      obj.Faction,
      obj.Health,
      obj.Shields,
      ownerSource,
      obj.Position,
      obj.Orientation
    )
  }
}

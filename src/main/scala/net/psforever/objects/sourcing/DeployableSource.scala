// Copyright (c) 2017 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.ce.Deployable
import net.psforever.objects.definition.{DeployableDefinition, ObjectDefinition}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.types.{PlanetSideEmpire, Vector3}

final case class DeployableSource(
                                   Definition: ObjectDefinition with DeployableDefinition,
                                   Faction: PlanetSideEmpire.Value,
                                   health: Int,
                                   shields: Int,
                                   owner: SourceEntry,
                                   Position: Vector3,
                                   Orientation: Vector3,
                                   occupants: List[SourceEntry],
                                   unique: UniqueDeployable
                                 ) extends SourceWithHealthEntry {
  def Name: String = Definition.Descriptor
  def Health: Int = health
  def Shields: Int = shields
  def OwnerName: String = owner.Name
  def Velocity: Option[Vector3] = None
  def Modifiers: ResistanceProfile = Definition.asInstanceOf[ResistanceProfile]

  def total: Int                             = health + shields
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
    val occupants = obj match {
      case o: Mountable =>
        o.Seats.values.flatMap { _.occupants }.map { PlayerSource(_) }.toList
      case _ =>
        Nil
    }
    DeployableSource(
      obj.Definition,
      obj.Faction,
      obj.Health,
      obj.Shields,
      ownerSource,
      obj.Position,
      obj.Orientation,
      occupants,
      UniqueDeployable(
        obj.History.headOption match {
          case Some(entry) => entry.time
          case None => 0L
        },
        obj.OriginalOwnerName.getOrElse("none")
      )
    )
  }
}

// Copyright (c) 2023 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.hackable.Hackable.HackInfo
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.sourcing
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.objects.vital.{Vitality, VitalityDefinition}
import net.psforever.types.{PlanetSideEmpire, Vector3}

final case class AmenitySource(
                                private val objdef: ObjectDefinition,
                                Faction: PlanetSideEmpire.Value,
                                health: Int,
                                Orientation: Vector3,
                                occupants: List[SourceEntry],
                                hacked: Option[HackInfo],
                                unique: UniqueAmenity
                              ) extends SourceWithHealthEntry {
  private val definition = objdef match {
    case vital: VitalityDefinition => vital
    case genericDefinition => NonvitalDefinition(genericDefinition)
  }
  private val modifiers = definition match {
    case nonvital: NonvitalDefinition => nonvital
    case _ => ObjectSource.FixedResistances
  }

  def Name: String = SourceEntry.NameFormat(definition.Descriptor)
  def Definition: ObjectDefinition with VitalityDefinition = definition
  def Health: Int = health
  def total: Int = health
  def Modifiers: ResistanceProfile = modifiers
  def Position: Vector3 = unique.position
  def Velocity: Option[Vector3] = None
}

object AmenitySource {
  def apply(obj: Amenity): AmenitySource = {
    val occupants = obj match {
      case o: Mountable =>
        o.Seats.values.flatMap { _.occupants }.map { PlayerSource(_) }.toList
      case _ =>
        Nil
    }
    val health: Int = obj match {
      case o: Vitality => o.Health
      case _ => 1
    }
    val hackData = obj match {
      case o: Hackable => o.HackedBy
      case _ => None
    }
    AmenitySource(
      obj.Definition,
      obj.Faction,
      health,
      obj.Orientation,
      occupants,
      hackData,
      sourcing.UniqueAmenity(obj.Zone.Number, obj.GUID, obj.Position)
    )
  }
}

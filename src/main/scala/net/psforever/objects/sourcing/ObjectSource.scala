// Copyright (c) 2017 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.VitalityDefinition
import net.psforever.objects.vital.resistance.{ResistanceProfile, ResistanceProfileMutators}
import net.psforever.types.{PlanetSideEmpire, Vector3}

final case class UniqueObject(objectId: Int) extends SourceUniqueness

final case class ObjectSource(
                               private val obj_def: ObjectDefinition,
                               Faction: PlanetSideEmpire.Value,
                               Position: Vector3,
                               Orientation: Vector3,
                               Velocity: Option[Vector3],
                               unique: UniqueObject
                             ) extends SourceEntry {
  private val definition = obj_def match {
    case vital : VitalityDefinition => vital
    case genericDefinition => NonvitalDefinition(genericDefinition)
  }
  private val modifiers = definition match {
    case nonvital : NonvitalDefinition => nonvital
    case _ => ObjectSource.FixedResistances
  }
  def Name: String = SourceEntry.NameFormat(Definition.Name)
  def Definition: ObjectDefinition with VitalityDefinition = definition
  def Modifiers: ResistanceProfile = modifiers
}

object ObjectSource {
  final val FixedResistances = new ResistanceProfileMutators() { }

  def apply(obj: PlanetSideGameObject): ObjectSource = {
    ObjectSource(
      obj.Definition,
      obj match {
        case aligned: FactionAffinity => aligned.Faction
        case _ => PlanetSideEmpire.NEUTRAL
      },
      obj.Position,
      obj.Orientation,
      obj.Velocity,
      UniqueObject(obj.Definition.ObjectId)
    )
  }
}

// Copyright (c) 2023 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.structures.{Building, BuildingDefinition}
import net.psforever.objects.vital.VitalityDefinition
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.types.{LatticeBenefit, PlanetSideEmpire, PlanetSideGUID, Vector3}

final case class UniqueBuilding(
                                 zone_number: Int,
                                 building_guid: PlanetSideGUID
                               ) extends SourceUniqueness

final case class BuildingSource(
                                 private val obj_def: BuildingDefinition,
                                 Faction: PlanetSideEmpire.Value,
                                 Position: Vector3,
                                 Orientation: Vector3,
                                 benefits: Set[LatticeBenefit],
                                 unique: UniqueBuilding
                               ) extends SourceEntry {
  private val definition = NonvitalDefinition(obj_def)
  def Name: String = SourceEntry.NameFormat(Definition.Name)
  def Definition: ObjectDefinition with VitalityDefinition = definition
  def Modifiers: ResistanceProfile = ObjectSource.FixedResistances
  def Velocity: Option[Vector3] = None
}

object BuildingSource {
  def apply(b: Building): BuildingSource = {
    BuildingSource(
      b.Definition,
      b.Faction,
      b.Position,
      b.Orientation,
      b.latticeConnectedFacilityBenefits(),
      UniqueBuilding(b.Zone.Number, b.GUID)
    )
  }
}

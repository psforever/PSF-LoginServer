// Copyright (c) 2017 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.ce.Deployable
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.vital.VitalityDefinition
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.objects.{PlanetSideGameObject, Player, TurretDeployable, Vehicle}
import net.psforever.types.{PlanetSideEmpire, Vector3}

trait SourceEntry {
  def Name: String
  def Definition: ObjectDefinition with VitalityDefinition
  def CharId: Long = 0L
  def Faction: PlanetSideEmpire.Value
  def Position: Vector3
  def Orientation: Vector3
  def Velocity: Option[Vector3]
  def Modifiers: ResistanceProfile
  def unique: SourceUniqueness
}

trait SourceWithHealthEntry extends SourceEntry {
  def health: Int
  def total: Int
}

trait SourceWithShieldsEntry extends SourceWithHealthEntry {
  def shields: Int
}

object SourceEntry {
  final protected val nonUnique: SourceUniqueness = new SourceUniqueness() { }

  final val None = new SourceEntry() {
    def Name: String                                         = "none"
    def Definition: ObjectDefinition with VitalityDefinition = null
    def Faction: PlanetSideEmpire.Value                      = PlanetSideEmpire.NEUTRAL
    def Position: Vector3                                    = Vector3.Zero
    def Orientation: Vector3                                 = Vector3.Zero
    def Velocity: Option[Vector3]                            = Some(Vector3.Zero)
    def Modifiers: ResistanceProfile                         = null
    def unique: SourceUniqueness                             = nonUnique
  }

  def apply(target: PlanetSideGameObject with FactionAffinity): SourceEntry = {
    target match {
      case obj: Player           => PlayerSource(obj)
      case obj: Vehicle          => VehicleSource(obj)
      case obj: FacilityTurret   => TurretSource(obj)
      case obj: Amenity          => AmenitySource(obj)
      case obj: TurretDeployable => TurretSource(obj)
      case obj: Deployable       => DeployableSource(obj)
      case obj: Building         => BuildingSource(obj)
      case _                     => ObjectSource(target)
    }
  }

  def NameFormat(name: String): String = {
    name
      .replace("_", " ")
      .split(" ")
      .map(_.capitalize)
      .mkString(" ")
  }
}

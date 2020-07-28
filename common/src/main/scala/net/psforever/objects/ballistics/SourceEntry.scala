// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.ce.{ComplexDeployable, SimpleDeployable}
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.{PlanetSideGameObject, Player, Vehicle}
import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.VitalityDefinition
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.types.{PlanetSideEmpire, Vector3}

trait SourceEntry extends WorldEntity {
  def Name: String = ""
  def Definition: ObjectDefinition with VitalityDefinition
  def CharId: Long                     = 0L
  def Faction: PlanetSideEmpire.Value  = PlanetSideEmpire.NEUTRAL
  def Position_=(pos: Vector3)         = Position
  def Orientation_=(pos: Vector3)      = Position
  def Velocity_=(pos: Option[Vector3]) = Velocity
  def Modifiers: ResistanceProfile
}

object SourceEntry {
  final val None = new SourceEntry() {
    def Definition  = null
    def Position    = Vector3.Zero
    def Orientation = Vector3.Zero
    def Velocity    = Some(Vector3.Zero)
    def Modifiers   = null
  }

  def apply(target: PlanetSideGameObject with FactionAffinity): SourceEntry = {
    target match {
      case obj: Player            => PlayerSource(obj)
      case obj: Vehicle           => VehicleSource(obj)
      case obj: ComplexDeployable => ComplexDeployableSource(obj)
      case obj: SimpleDeployable  => DeployableSource(obj)
      case _                      => ObjectSource(target)
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

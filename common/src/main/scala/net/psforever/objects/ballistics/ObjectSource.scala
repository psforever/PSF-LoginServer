// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.resistance.ResistanceProfileMutators
import net.psforever.types.{PlanetSideEmpire, Vector3}

final case class ObjectSource(obj : PlanetSideGameObject with FactionAffinity,
                              faction : PlanetSideEmpire.Value,
                              position : Vector3,
                              orientation : Vector3,
                              velocity : Option[Vector3]) extends SourceEntry {
  override def Name = SourceEntry.NameFormat(obj.Definition.Name)
  override def Faction = faction
  def Definition = obj.Definition
  def Position = position
  def Orientation = orientation
  def Velocity = velocity
  def Modifiers = new ResistanceProfileMutators { }
}

object ObjectSource {
  def apply(obj : PlanetSideGameObject with FactionAffinity) : ObjectSource = {
    ObjectSource(
      obj,
      obj.Faction,
      obj.Position,
      obj.Orientation,
      obj.Velocity
    )
  }
}

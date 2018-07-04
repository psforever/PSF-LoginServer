// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.types.{PlanetSideEmpire, Vector3}

final case class ObjectSource(name : String,
                              obj_def : ObjectDefinition,
                              faction : PlanetSideEmpire.Value,
                              position : Vector3,
                              orientation : Vector3,
                              velocity : Option[Vector3] = None) extends SourceEntry {
  override def Name = name
  override def Faction = faction
  def Definition = obj_def
  def Position = position
  def Orientation = orientation
  def Velocity = velocity
}

object ObjectSource {
  def apply(obj : PlanetSideGameObject with FactionAffinity) : ObjectSource = {
    ObjectSource(
      obj.Definition.Name
        .replace("_", " ")
        .split(" ")
        .map(_.capitalize)
        .mkString(" "),
      obj.Definition,
      obj.Faction,
      obj.Position,
      obj.Orientation,
      obj.Velocity
    )
  }
}

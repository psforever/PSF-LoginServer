// Copyright (c) 2026 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.ActorContext
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.types.{PlanetSideEmpire, Vector3}

class VirtualTrainingTeleporter extends PlanetSideServerObject {
  def Definition: VirtualTrainingTeleporterDefinition = GlobalDefinitions.spawn_zone

  def Faction: PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
}

object VirtualTrainingTeleporter {

  /**
    * Overloaded constructor.
    * @return the `VirtualTrainingTeleporter` object
    */
  def apply(): VirtualTrainingTeleporter = {
    new VirtualTrainingTeleporter()
  }

  /**
    * Instantiate an configure a `VirtualTrainingTeleporter` object
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality;
    *                not necessary for this object, but required by signature
    * @return the `VirtualTrainingTeleporter` object
    */
  def Constructor(id: Int, context: ActorContext): VirtualTrainingTeleporter = {
    val obj = VirtualTrainingTeleporter()
    obj
  }

  def Constructor(pos: Vector3)(id: Int, context: ActorContext): VirtualTrainingTeleporter = {
    val obj = VirtualTrainingTeleporter()
    obj.Position = pos
    obj
  }
}

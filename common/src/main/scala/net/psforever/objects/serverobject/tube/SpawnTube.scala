// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.tube

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.structures.Amenity

class SpawnTube(tubeDef : ObjectDefinition) extends Amenity {
  def Definition : ObjectDefinition = tubeDef
}

object SpawnTube {
  def apply(tubeDef : ObjectDefinition) : SpawnTube = {
    new SpawnTube(tubeDef)
  }

//  import akka.actor.ActorContext
//  import net.psforever.types.Vector3
//  /**
//    * Instantiate an configure a `SpawnTube` object
//    * @param pos the position (used to determine spawn point)
//    * @param orient the orientation (used to indicate spawn direction)
//    * @param id the unique id that will be assigned to this entity
//    * @param context a context to allow the object to properly set up `ActorSystem` functionality
//    * @return the `SpawnTube` object
//    */
//  def Constructor(pos : Vector3, orient : Vector3)(id : Int, context : ActorContext) : SpawnTube = {
//    import net.psforever.objects.GlobalDefinitions
//
//    val obj = SpawnTube(GlobalDefinitions.ams_respawn_tube)
//    obj.Position = pos
//    obj.Orientation = orient
//    obj
//  }
}

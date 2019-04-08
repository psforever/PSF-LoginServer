// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.tube

import net.psforever.objects.{GlobalDefinitions, SpawnPoint}
import net.psforever.objects.serverobject.structures.Amenity

/**
  * An owned server object that is used as a placeholder for the position and direction
  * that infantry will be arranged upon spawning into the game world.
  * @param tDef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class SpawnTube(tDef : SpawnTubeDefinition) extends Amenity
  with SpawnPoint {
  def Definition : SpawnTubeDefinition = tDef
}

object SpawnTube {
  /**
    * Overloaded constructor.
    * @param tDef the spawn tube's definition entry
    * @return a `SpawnTube` object
    */
  def apply(tDef : SpawnTubeDefinition) : SpawnTube = {
    new SpawnTube(tDef)
  }

  import akka.actor.ActorContext
  import net.psforever.types.Vector3
  /**
    * Instantiate an configure a `SpawnTube` object
    * @param pos the position (used to determine spawn point)
    * @param orient the orientation (used to indicate spawn direction)
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `SpawnTube` object
    */
  def Constructor(pos : Vector3, orient : Vector3)(id : Int, context : ActorContext) : SpawnTube = {
    Constructor(GlobalDefinitions.respawn_tube, pos, orient)(id, context)
  }

  /**
    * Instantiate an configure a `SpawnTube` object with the given definition
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    * @param pos the position (used to determine spawn point)
    * @param orient the orientation (used to indicate spawn direction)
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `SpawnTube` object
    */
  def Constructor(tdef : SpawnTubeDefinition, pos : Vector3, orient : Vector3)(id : Int, context : ActorContext) : SpawnTube = {
    import akka.actor.Props

    val obj = SpawnTube(tdef)
    obj.Position = pos
    obj.Orientation = orient
    obj.Actor = context.actorOf(Props(classOf[SpawnTubeControl], obj), s"${tdef.Name}_$id")
    obj
  }
}

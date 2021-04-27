// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.doors

import akka.actor.Props
import akka.actor.ActorContext
import net.psforever.types.Vector3

object SpawnTubeDoor {
  /**
    * Instantiate and configure a `Door` object that is to later be paired with a `SpawnTube` entity.
    * @param pos the position of the door
    * @param ddef the definition for this specific type of door
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `Door` object
    */
  def Constructor(pos: Vector3, ddef: DoorDefinition)(id: Int, context: ActorContext): Door = {
    val obj = Door(ddef)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[SpawnTubeDoorControl], obj), s"${ddef.Name}_$id")
    obj
  }
}

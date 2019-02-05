// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.ActorContext
import net.psforever.objects.Player
import net.psforever.objects.serverobject.structures.Amenity

/**
  * The definition for any `Terminal` that is of a type "matrix_terminal".
  * Matrix terminal objects are used to create anchor points in the game environment
  * in reference to a working set of spawn points attached to a `Building` object or `Vehicle` object
  * depending on the spawn group.
  * @see `SpawnTube`
  * @see `Zone.CreateSpawnGroups`
  * @see `Zone.SpawnGroups`
  * @param objectId the object's identifier number
  */
class MatrixTerminalDefinition(objectId : Int) extends TerminalDefinition(objectId) {
  def Request(player : Player, msg : Any) : Terminal.Exchange = Terminal.NoDeal()
}

object MatrixTerminalDefinition {
  /**
    * Assemble some logic for a provided object.
    * @param obj an `Amenity` object;
    *            anticipating a `Terminal` object using this same definition
    * @param context hook to the local `Actor` system
    */
  def Setup(obj : Amenity, context : ActorContext) : Unit = {
    import akka.actor.{ActorRef, Props}
    if(obj.Actor == ActorRef.noSender) {
      obj.Actor = context.actorOf(Props(classOf[TerminalControl], obj), s"${obj.Definition.Name}_${obj.GUID.guid}")
    }
  }
}

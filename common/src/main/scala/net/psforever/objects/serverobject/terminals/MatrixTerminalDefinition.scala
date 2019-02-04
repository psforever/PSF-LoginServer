// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.ActorContext
import net.psforever.objects.Player
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.ItemTransactionMessage

/**
  * The definition for any `Terminal` that is of a type "matrix_terminal".
  */
class MatrixTerminalDefinition(object_id : Int) extends TerminalDefinition(object_id) {
  Name = if(object_id == 517) {
    "matrix_terminala"
  }
  else if(object_id == 518) {
    "matrix_terminalb"
  }
  else if(object_id == 519) {
    "matrix_terminalc"
  }
  else if(object_id == 812) {
    "spawn_terminal"
  }
  else {
    throw new IllegalArgumentException("terminal must be object id 517-519 or 812")
  }

  def Request(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
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

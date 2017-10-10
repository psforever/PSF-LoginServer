// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.doors

import akka.actor.{Actor, Cancellable}

/**
  * An `Actor` that handles messages being dispatched to a specific `Door`.
  * @param door the `Door` object being governed
  */
class DoorControl(door : Door) extends Actor {
  def receive : Receive = {
    case Door.Use(player, msg) =>
      sender ! Door.DoorMessage(player, msg, door.Use(player, msg))
    case _ =>
      sender ! Door.NoEvent()
  }
}

object DoorControl {
  final val DefaultCloser : Cancellable = new Cancellable() {
    override def cancel : Boolean = true
    override def isCancelled : Boolean = true
  }
}

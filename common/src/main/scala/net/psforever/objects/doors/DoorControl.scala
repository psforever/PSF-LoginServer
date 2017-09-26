// Copyright (c) 2017 PSForever
package net.psforever.objects.doors

import akka.actor.{Actor, Cancellable}

/**
  * An `Actor` that handles messages being dispatched to a specific `Door`.
  * @param door the `Door` object being governed
  */
class DoorControl(door : Door) extends Actor {
  private var doorCloser : Cancellable = DoorControl.DefaultCloser

  def receive : Receive = {
    case Door.Request(player, msg) =>
      sender ! Door.DoorMessage(player, msg, door.Request(player, msg))
      //doorCloser = context.system.scheduler.scheduleOnce(5000L, sender, Door.DoorMessage())
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

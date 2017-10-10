// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.locks

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.doors.Door

class IFFLockControl(lock : IFFLock) extends Actor {
  def receive : Receive = {
    case CommonMessages.Hack(player) =>
      lock.HackedBy = player
    case CommonMessages.ClearHack() =>
      lock.HackedBy = None
    case _ =>
      sender ! Door.NoEvent()
  }
}

object IFFLockControl {
  final val DefaultCloser : Cancellable = new Cancellable() {
    override def cancel : Boolean = true
    override def isCancelled : Boolean = true
  }
}
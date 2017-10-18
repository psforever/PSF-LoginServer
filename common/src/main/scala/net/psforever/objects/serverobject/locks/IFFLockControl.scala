// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.locks

import akka.actor.Actor
import net.psforever.objects.serverobject.CommonMessages

/**
  * An `Actor` that handles messages being dispatched to a specific `IFFLock`.
  * @param lock the `IFFLock` object being governed
  * @see `CommonMessages`
  */
class IFFLockControl(lock : IFFLock) extends Actor {
  def receive : Receive = {
    case CommonMessages.Hack(player) =>
      lock.HackedBy = player

    case CommonMessages.ClearHack() =>
      lock.HackedBy = None

    case _ => ; //no default message
  }
}

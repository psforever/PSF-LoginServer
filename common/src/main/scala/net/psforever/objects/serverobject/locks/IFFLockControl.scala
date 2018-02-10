// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.locks

import akka.actor.Actor
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

/**
  * An `Actor` that handles messages being dispatched to a specific `IFFLock`.
  * @param lock the `IFFLock` object being governed
  * @see `CommonMessages`
  */
class IFFLockControl(lock : IFFLock) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = lock

  def receive : Receive = checkBehavior.orElse {
    case CommonMessages.Hack(player) =>
      lock.HackedBy = player

    case CommonMessages.ClearHack() =>
      lock.HackedBy = None

    case _ => ; //no default message
  }
}

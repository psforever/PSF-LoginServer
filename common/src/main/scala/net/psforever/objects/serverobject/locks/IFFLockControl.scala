// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.locks

import akka.actor.Actor
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.HackableBehavior

/**
  * An `Actor` that handles messages being dispatched to a specific `IFFLock`.
  * @param lock the `IFFLock` object being governed
  * @see `CommonMessages`
  */
class IFFLockControl(lock : IFFLock) extends Actor with FactionAffinityBehavior.Check with HackableBehavior.GenericHackable {
  def FactionObject : FactionAffinity = lock
  def HackableObject = lock

  def receive : Receive = checkBehavior
    .orElse(hackableBehavior)
    .orElse {
      case _ => ; //no default message
  }
}

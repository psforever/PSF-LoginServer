// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.mblocker

import akka.actor.Actor
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

/**
  * An `Actor` that handles messages being dispatched to a specific `Locker`.
  * @param locker the `Locker` object being governed
  */
class LockerControl(locker : Locker) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = locker

  def receive : Receive = checkBehavior.orElse {
    case CommonMessages.Hack(player) =>
      locker.HackedBy = player
      sender ! true
    case CommonMessages.ClearHack() =>
      locker.HackedBy = None
    case _ => ;
  }
}

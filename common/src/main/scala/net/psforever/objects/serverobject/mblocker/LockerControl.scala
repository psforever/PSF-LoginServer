// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.mblocker

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, SimpleItem}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.HackableBehavior

/**
  * An `Actor` that handles messages being dispatched to a specific `Locker`.
  * @param locker the `Locker` object being governed
  */
class LockerControl(locker : Locker) extends Actor with FactionAffinityBehavior.Check with HackableBehavior.GenericHackable {
  def FactionObject : FactionAffinity = locker
  def HackableObject = locker

  def receive : Receive = checkBehavior
    .orElse(hackableBehavior)
    .orElse {
      case CommonMessages.Use(player, Some(item : SimpleItem)) if item.Definition == GlobalDefinitions.remote_electronics_kit =>
        //TODO setup certifications check
        if(locker.Faction != player.Faction && locker.HackedBy.isEmpty) {
          sender ! CommonMessages.Hack(player, locker, Some(item))
        }
      case _ => ;
  }
}

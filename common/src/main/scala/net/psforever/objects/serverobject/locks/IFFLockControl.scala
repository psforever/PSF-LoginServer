// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.locks

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, SimpleItem}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.{GenericHackables, HackableBehavior}

/**
  * An `Actor` that handles messages being dispatched to a specific `IFFLock`.
  * @param lock the `IFFLock` object being governed
  * @see `CommonMessages`
  */
class IFFLockControl(lock: IFFLock)
    extends Actor
    with FactionAffinityBehavior.Check
    with HackableBehavior.GenericHackable {
  def FactionObject: FactionAffinity = lock
  def HackableObject                 = lock

  def receive: Receive =
    checkBehavior
      .orElse(hackableBehavior)
      .orElse {
        case CommonMessages.Use(player, Some(item: SimpleItem))
            if item.Definition == GlobalDefinitions.remote_electronics_kit =>
          if (lock.Faction != player.Faction && lock.HackedBy.isEmpty) {
            sender() ! CommonMessages.Progress(
              GenericHackables.GetHackSpeed(player, lock),
              GenericHackables.FinishHacking(lock, player, 1114636288L),
              GenericHackables.HackingTickAction(progressType = 1, player, lock, item.GUID)
            )
          } else if (lock.Faction == player.Faction && lock.HackedBy.nonEmpty) {
            sender() ! CommonMessages.Progress(
              GenericHackables.GetHackSpeed(player, lock),
              IFFLocks.FinishResecuringIFFLock(lock),
              GenericHackables.HackingTickAction(progressType = 1, player, lock, item.GUID)
            )
          } else {
            val log = org.log4s.getLogger
            log.warn("IFF lock is being hacked, but don't know how to handle this state:")
            log.warn(s"Lock - Faction=${lock.Faction}, HackedBy=${lock.HackedBy}")
            log.warn(s"Player - Faction=${player.Faction}")
          }

        case _ => ; //no default message
      }
}

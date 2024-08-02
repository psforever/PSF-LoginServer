// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.locks

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, SimpleItem}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.hackable.{GenericHackables, HackableBehavior}
import net.psforever.objects.serverobject.structures.Building
import net.psforever.packet.game.HackState1
import net.psforever.types.{PlanetSideEmpire, Vector3}

/**
  * An `Actor` that handles messages being dispatched to a specific `IFFLock`.
  * @param lock the `IFFLock` object being governed
  * @see `CommonMessages`
  */
class IFFLockControl(lock: IFFLock)
    extends Actor
    with FactionAffinityBehavior.Check
    with HackableBehavior.GenericHackable {
  def FactionObject: IFFLock  = lock
  def HackableObject: IFFLock = lock

  def receive: Receive =
    checkBehavior
      .orElse(hackableBehavior)
      .orElse {
        case CommonMessages.Use(player, Some(item: SimpleItem))
            if item.Definition == GlobalDefinitions.remote_electronics_kit =>
          if (lock.Faction != player.Faction) {
            // 300 / 1
            sender() ! CommonMessages.Progress(
              GenericHackables.GetHackSpeed(player, lock),
              GenericHackables.FinishHacking(lock, player, hackValue = 60, hackClearValue = 60),
              GenericHackables.HackingTickAction(HackState1.Unk1, player, lock, item.GUID)
            )
          } else if (lock.Faction == player.Faction && lock.HackedBy.nonEmpty) {
            sender() ! CommonMessages.Progress(
              GenericHackables.GetHackSpeed(player, lock),
              IFFLocks.FinishResecuringIFFLock(lock),
              GenericHackables.HackingTickAction(HackState1.Unk1, player, lock, item.GUID)
            )
          } else {
            val log = org.log4s.getLogger
            log.warn(s"IFF lock is being hacked by ${player.Faction}, but don't know how to handle this state:")
            log.warn(s"Lock - Faction=${lock.Faction}, HackedBy=${lock.HackedBy.map{_.player}}")
          }

        case IFFLock.DoorOpenRequest(target, door, replyTo) =>
          val owner = lock.Owner.asInstanceOf[Building]
          /*
          If one of the following conditions are met:
          1. target and door have same faction affinity
          2. lock or lock owner is neutral
          3. lock is hacked
          4. facility capture terminal (owner is a building) has been hacked
          5. requestee is on the inside of the door (determined by the lock orientation)
          ... open the door.
           */
          if (
            lock.Faction == target.Faction ||
            lock.Faction == PlanetSideEmpire.NEUTRAL || owner.Faction == PlanetSideEmpire.NEUTRAL ||
            lock.HackedBy.isDefined ||
            owner.CaptureTerminalIsHacked ||
            Vector3.ScalarProjection(lock.Outwards, target.Position - door.Position) < 0f
          ) {
            replyTo ! IFFLock.DoorOpenResponse(target)
          }

        case _ => ; //no default message
      }
}

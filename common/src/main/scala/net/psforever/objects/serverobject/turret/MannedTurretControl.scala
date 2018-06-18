// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.Actor
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

/**
  * An `Actor` that handles messages being dispatched to a specific `MannedTurret`.
  * @param turret the `MannedTurret` object being governed
  */
class MannedTurretControl(turret : MannedTurret) extends Actor
  with FactionAffinityBehavior.Check
  with MountableBehavior.Dismount {
  def MountableObject = turret //do not add type!

  def FactionObject : FactionAffinity = turret

  def receive : Receive = checkBehavior
    .orElse(dismountBehavior)
    .orElse {
      case Mountable.TryMount(user, seat_num) =>
        val obj = MountableObject
        obj.Seat(seat_num) match {
          case Some(seat) =>
            if((!turret.Definition.FactionLocked || user.Faction == turret.Faction) &&
              (seat.Occupant = user).contains(user)) {
              user.VehicleSeated = obj.GUID
              sender ! Mountable.MountMessages(user, Mountable.CanMount(obj, seat_num))
            }
            else {
              sender ! Mountable.MountMessages(user, Mountable.CanNotMount(obj, seat_num))
            }
          case None =>
            sender ! Mountable.MountMessages(user, Mountable.CanNotMount(obj, seat_num))
        }

      case _ => ;
    }
}

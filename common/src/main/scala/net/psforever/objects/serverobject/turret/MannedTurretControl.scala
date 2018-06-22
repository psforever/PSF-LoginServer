// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.Actor
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

/**
  * An `Actor` that handles messages being dispatched to a specific `MannedTurret`.<br>
  * <br>
  * Mounted turrets have only slightly different entry requirements than a normal vehicle
  * because they encompass both faction-specific facility turrets
  * and faction-blind cavern sentry turrets.
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
        turret.Seat(seat_num) match {
          case Some(seat) =>
            if((!turret.Definition.FactionLocked || user.Faction == turret.Faction) &&
              (seat.Occupant = user).contains(user)) {
              user.VehicleSeated = turret.GUID
              sender ! Mountable.MountMessages(user, Mountable.CanMount(turret, seat_num))
            }
            else {
              sender ! Mountable.MountMessages(user, Mountable.CanNotMount(turret, seat_num))
            }
          case None =>
            sender ! Mountable.MountMessages(user, Mountable.CanNotMount(turret, seat_num))
        }

      case _ => ;
    }
}

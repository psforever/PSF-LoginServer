// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.mount

import akka.actor.Actor
import net.psforever.objects.{PlanetSideGameObject, Vehicle}
import net.psforever.objects.entity.{Identifiable, WorldEntity}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.turret.TurretDefinition
import net.psforever.types.DriveState

object MountableBehavior {
  /**
    * The logic governing `Mountable` objects that use the `TryMount` message.
    * This is a mix-in trait for combining with existing `Receive` logic.
    * @see `Seat`
    * @see `Mountable`
    */
  trait Mount {
    this : Actor =>

    def MountableObject : PlanetSideGameObject with Mountable with FactionAffinity

    val mountBehavior : Receive = {
      case Mountable.TryMount(user, seat_num) =>
        val obj = MountableObject
        obj.Seat(seat_num) match {
          case Some(seat) =>

            var isHacked = false
            if(MountableObject.isInstanceOf[Hackable]) {
              // This is a special case for implant terminals, since they're both mountable and hackable, but not jackable.
              isHacked = MountableObject.asInstanceOf[Hackable].HackedBy.isDefined
            }

            if((user.Faction == obj.Faction || isHacked) && (seat.Occupant = user).contains(user)) {
              user.VehicleSeated = obj.GUID
              sender ! Mountable.MountMessages(user, Mountable.CanMount(obj, seat_num))
            }
            else {
              sender ! Mountable.MountMessages(user, Mountable.CanNotMount(obj, seat_num))
            }
          case None =>
            sender ! Mountable.MountMessages(user, Mountable.CanNotMount(obj, seat_num))
        }
    }

    val turretMountBehavior : Receive = {
      case Mountable.TryMount(user, seat_num) =>
        val obj = MountableObject
        val definition = obj.Definition.asInstanceOf[TurretDefinition]
        obj.Seat(seat_num) match {
          case Some(seat) =>
            if((!definition.FactionLocked || user.Faction == obj.Faction) &&
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
    }
  }

  /**
    * The logic governing `Mountable` objects that use the `TryDismount` message.
    * This is a mix-in trait for combining with existing `Receive` logic.
    * @see `Seat`
    * @see `Mountable`
    */
  trait Dismount {
    this : Actor =>

    def MountableObject : Mountable with Identifiable with WorldEntity with FactionAffinity

    val dismountBehavior : Receive = {
      case Mountable.TryDismount(user, seat_num) =>
        val obj = MountableObject
        obj.Seat(seat_num) match {
          case Some(seat) =>
            if(seat.Bailable || !obj.isMoving(1) || (obj.isInstanceOf[Vehicle] && obj.asInstanceOf[Vehicle].DeploymentState == DriveState.Deployed)) {
              seat.Occupant = None
              user.VehicleSeated = None
              sender ! Mountable.MountMessages(user, Mountable.CanDismount(obj, seat_num))
            }
            else {
              sender ! Mountable.MountMessages(user, Mountable.CanNotDismount(obj, seat_num))
            }
          case None =>
            sender ! Mountable.MountMessages(user, Mountable.CanNotDismount(obj, seat_num))
        }
    }
  }
}

// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.mount

import akka.actor.Actor
import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.entity.{Identifiable, WorldEntity}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.turret.WeaponTurret
import net.psforever.types.DriveState

object MountableBehavior {
  /**
    * The logic governing `Mountable` objects that use the `TryMount` message.
    * This is a mix-in trait for combining with existing `Receive` logic.
    * @see `Seat`
    * @see `Mountable`
    */
  trait Mount {
    _ : Actor =>
    def MountableObject : PlanetSideServerObject with Mountable with FactionAffinity

    val mountBehavior : Receive = {
      case Mountable.TryMount(user, seat_num) =>
        val obj = MountableObject
        if(MountTest(MountableObject, seat_num, user)) {
          user.VehicleSeated = obj.GUID
          sender ! Mountable.MountMessages(user, Mountable.CanMount(obj, seat_num))
        }
        else {
          sender ! Mountable.MountMessages(user, Mountable.CanNotMount(obj, seat_num))
        }
    }

    protected def MountTest(obj : PlanetSideServerObject with Mountable, seatNumber : Int, player : Player) : Boolean = {
      (player.Faction == obj.Faction ||
        (obj match {
          case o : Hackable => o.HackedBy.isDefined
          case _ => false
        })) &&
        !obj.Destroyed &&
        (obj.Seats.get(seatNumber) match {
          case Some(seat) => (seat.Occupant = player).contains(player)
          case _ => false
        })
    }
  }

  trait TurretMount extends Mount {
    _ : Actor =>

    override protected def MountTest(obj : PlanetSideServerObject with Mountable, seatNumber : Int, player : Player) : Boolean = {
      obj match {
        case wep : WeaponTurret =>
          (!wep.Definition.FactionLocked || player.Faction == obj.Faction) &&
            !obj.Destroyed &&
            (obj.Seats.get(seatNumber) match {
              case Some(seat) => (seat.Occupant = player).contains(player)
              case _ => false
            })
        case _ =>
          super.MountTest(obj, seatNumber, player)
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

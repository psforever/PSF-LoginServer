// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.mount

import akka.actor.Actor
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.Player
import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.types.BailType

import scala.annotation.unused
import scala.collection.mutable

trait MountableBehavior {
  _ : Actor =>
  def MountableObject: PlanetSideServerObject with Mountable

  /** retain the mount point that was used by this occupant to mount */
  val usedMountPoint: mutable.HashMap[String, Int] = mutable.HashMap()

  def getUsedMountPoint(playerName: String, seatNumber: Int): Int = {
    usedMountPoint
      .remove(playerName)
      .getOrElse {
        MountableObject
          .Definition
          .MountPoints
          .find { case (_, mp) => mp.seatIndex == seatNumber } match {
          case Some((mount, _)) => mount
          case None             => -1
        }
      }
  }

  /**
    * The logic governing `Mountable` objects that use the `TryMount` message.
    * This is a mix-in trait for combining with existing `Receive` logic.
    * @see `Seat`
    * @see `Mountable`
    */
  val mountBehavior: Receive = {
    case Mountable.TryMount(user, mount_point) =>
      val obj = MountableObject
      obj.GetSeatFromMountPoint(mount_point) match {
        case Some(seatNum) if mountTest(obj, seatNum, user) && tryMount(obj, seatNum, user) =>
          user.VehicleSeated = obj.GUID
          usedMountPoint.put(user.Name, mount_point)
          mountActionResponse(user, mount_point, seatNum)
          sender() ! Mountable.MountMessages(user, Mountable.CanMount(obj, seatNum, mount_point))
        case _ =>
          sender() ! Mountable.MountMessages(user, Mountable.CanNotMount(obj, mount_point))
      }
  }

  def mountActionResponse(user: Player, @unused mountPoint: Int, @unused seatIndex: Int): Unit = {
    MountableObject.Zone.actor ! ZoneActor.RemoveFromBlockMap(user)
  }

  protected def mountTest(
                           obj: PlanetSideServerObject with Mountable,
                           seatNumber: Int,
                           player: Player
                         ): Boolean = {
    (player.Faction == obj.Faction ||
     (obj match {
       case o : Hackable => o.HackedBy.isDefined
       case _ => false
     })) &&
    !obj.Destroyed
  }

  protected def tryMount(
                        obj: PlanetSideServerObject with Mountable,
                        seatNumber: Int,
                        player: Player
                      ): Boolean = {
    obj.Seat(seatNumber) match {
      case Some(seat) => seat.mount(player).contains(player)
      case _ => false
    }
  }

  /**
    * The logic governing `Mountable` objects that use the `TryDismount` message.
    * This is a mix-in trait for combining with existing `Receive` logic.
    * @see `Seat`
    * @see `Mountable`
    */
  val dismountBehavior: Receive = {
    case Mountable.TryDismount(user, seat_number, bail_type) =>
      val obj = MountableObject
      if (dismountTest(obj, seat_number, user) && tryDismount(obj, seat_number, user, bail_type)) {
        user.VehicleSeated = None
        dismountActionResponse(user, seat_number)
        sender() ! Mountable.MountMessages(
          user,
          Mountable.CanDismount(obj, seat_number, getUsedMountPoint(user.Name, seat_number))
        )
      }
      else {
        sender() ! Mountable.MountMessages(user, Mountable.CanNotDismount(obj, seat_number, bail_type))
      }
  }

  def dismountActionResponse(user: Player, @unused seatIndex: Int): Unit = {
    MountableObject.Zone.actor ! ZoneActor.AddToBlockMap(user, MountableObject.Position)
  }

  protected def dismountTest(
                              obj: Mountable with WorldEntity,
                              seatNumber: Int,
                              user: Player
                            ): Boolean = {
    obj.PassengerInSeat(user).contains(seatNumber) &&
    (obj.Seats.get(seatNumber) match {
      case Some(seat) => seat.bailable || !obj.isMoving(test = 1f)
      case _          => false
    })
  }

  protected def tryDismount(
                           obj: Mountable,
                           seatNumber: Int,
                           user: Player,
                           bailType: BailType.Value
                         ): Boolean = {
    obj.Seats.get(seatNumber) match {
      case Some(seat) => seat.unmount(user, bailType).isEmpty
      case _          => false
    }
  }
}

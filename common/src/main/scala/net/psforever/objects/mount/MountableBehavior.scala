// Copyright (c) 2017 PSForever
package net.psforever.objects.mount

import akka.actor.Actor

/**
  * The logic governing `Mountable` objects that use the `TryMount` message.
  * This is a mix-in trait for combining the `Receive` logic.
  * @see `Seat`
  * @see `Mountable`
  */
trait MountableBehavior {
  this : Actor =>

  def MountableObject : Mountable

  val mountableBehavior : Receive = {
    case Mountable.TryMount(user, seat_num) =>
      MountableObject.Seat(seat_num) match {
        case Some(seat) =>
          if((seat.Occupant = user).contains(user)) {
            sender ! Mountable.MountMessages(user, Mountable.CanMount(MountableObject, seat_num))
          }
          else {
            sender ! Mountable.MountMessages(user, Mountable.CanNotMount(MountableObject, seat_num))
          }
        case None =>
          sender ! Mountable.MountMessages(user, Mountable.CanNotMount(MountableObject, seat_num))
      }
  }
}

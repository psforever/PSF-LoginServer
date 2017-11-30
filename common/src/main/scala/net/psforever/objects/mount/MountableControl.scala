// Copyright (c) 2017 PSForever
package net.psforever.objects.mount

import akka.actor.Actor

/**
  * The logic governing `Mountable` objects that use the `TryMount` message.
  * @see `Seat`
  * @see `Mountable`
  * @param obj the `Mountable` object governed beholden to this logic
  */
abstract class MountableControl(obj : Mountable) extends Actor {
  def receive : Receive = {
    case Mountable.TryMount(user, seat_num) =>
      obj.Seat(seat_num) match {
        case Some(seat) =>
          if((seat.Occupant = user).contains(user)) {
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

// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import akka.actor.Actor
import net.psforever.objects.Vehicle

/**
  * An `Actor` that handles messages being dispatched to a specific `Vehicle`.<br>
  * <br>
  * Vehicle-controlling actors have two behavioral states - responsive and "`Disabled`."
  * The latter is applicable only when the specific vehicle is being deconstructed.
  * @param vehicle the `Vehicle` object being governed
  */
class VehicleControl(private val vehicle : Vehicle) extends Actor {
  def receive : Receive = {
    case Vehicle.PrepareForDeletion =>
      context.become(Disabled)

    case Vehicle.TrySeatPlayer(seat_num, player) =>
      vehicle.Seat(seat_num) match {
        case Some(seat) =>
          if((seat.Occupant = player).contains(player)) {
            sender ! Vehicle.VehicleMessages(player, Vehicle.CanSeatPlayer(vehicle, seat_num))
          }
          else {
            sender ! Vehicle.VehicleMessages(player, Vehicle.CannotSeatPlayer(vehicle, seat_num))
          }
        case None =>
          sender ! Vehicle.VehicleMessages(player, Vehicle.CannotSeatPlayer(vehicle, seat_num))
      }
    case _ => ;
  }

  def Disabled : Receive = {
    case _ => ;
  }
}

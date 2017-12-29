// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import akka.actor.Actor
import net.psforever.objects.Vehicle
import net.psforever.objects.mount.MountableBehavior

/**
  * An `Actor` that handles messages being dispatched to a specific `Vehicle`.<br>
  * <br>
  * Vehicle-controlling actors have two behavioral states - responsive and "`Disabled`."
  * The latter is applicable only when the specific vehicle is being deconstructed.
  * @param vehicle the `Vehicle` object being governed
  */
class VehicleControl(private val vehicle : Vehicle) extends Actor with MountableBehavior {
  override def MountableObject = vehicle

  def receive : Receive =  mountableBehavior.orElse {
    case Vehicle.PrepareForDeletion =>
      context.become(Disabled)

    case _ => ;
  }

  def Disabled : Receive = {
    case _ => ;
  }
}

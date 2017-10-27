// Copyright (c) 2017 PSForever
package services.vehicle.support

import akka.actor.{Actor, Props}
import net.psforever.objects.vehicles.VehicleControl
import services.vehicle.VehicleServiceMessage

/**
  * Provide a context for a `Vehicle` `Actor` - the `VehicleControl`.<br>
  * <br>
  * A vehicle can be passed between different zones and, therefore, does not belong to the zone.
  * A vehicle cna be given to different players and can persist and change though players have gone.
  * Therefore, also does not belong to `WorldSessionActor`.
  * A vehicle must anchored to something that exists outside of the `InterstellarCluster` and its agents.<br>
  * <br>
  * The only purpose of this `Actor` is to allow vehicles to borrow a context for the purpose of `Actor` creation.
  * It is also be allowed to be responsible for cleaning up that context.
  * (In reality, it can be cleaned up anywhere a `PoisonPill` can be sent.)
  */
class VehicleContextActor() extends Actor {
  def receive : Receive = {
    case VehicleServiceMessage.GiveActorControl(vehicle, actorName) =>
      vehicle.Actor = context.actorOf(Props(classOf[VehicleControl], vehicle), s"${vehicle.Definition.Name}_$actorName")

    case VehicleServiceMessage.RevokeActorControl(vehicle) =>
      vehicle.Actor ! akka.actor.PoisonPill

    case _ => ;
  }
}

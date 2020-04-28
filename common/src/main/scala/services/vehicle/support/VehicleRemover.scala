// Copyright (c) 2017-2020 PSForever
package services.vehicle.support

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.Vehicle
import net.psforever.objects.guid.GUIDTask.UnregisterVehicle
import services.{RemoverActor, ServiceManager}

class VehicleRemover extends Actor {
  var taskResolver : ActorRef = Actor.noSender

  override def preStart() : Unit = {
    super.preStart()
    ServiceManager.serviceManager ! ServiceManager.Lookup("taskResolver") //ask for a resolver to deal with the GUID system
  }

  def receive : Receive = {
    case ServiceManager.LookupResult("taskResolver", endpoint) =>
      taskResolver = endpoint
      context.become(Processing)

    case _ => ;
  }

  def Processing : Receive = {
    case RemoverActor.AddTask(obj : Vehicle, zone, _) =>
      taskResolver ! UnregisterVehicle(obj)(zone.GUID)

    case _ => ;
  }
}

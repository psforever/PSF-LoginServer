// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.objects.Vehicle
import net.psforever.objects.vehicles.VehicleControl

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

/**
  * Synchronize management of the list of `Vehicles` maintained by some `Zone`.
  */
//COMMENTS IMPORTED FROM FORMER VehicleContextActor:
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
  * (In reality, it can be cleaned up anywhere a `PoisonPill` can be sent.)<br>
  * <br>
  * This `Actor` is intended to sit on top of the event system that handles broadcast messaging.
  */
class ZoneVehicleActor(zone : Zone, vehicleList : ListBuffer[Vehicle]) extends Actor {
  //private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case Zone.Vehicle.Spawn(vehicle) =>
      if(!vehicle.HasGUID) {
        sender ! Zone.Vehicle.CanNotSpawn(zone, vehicle, "not registered yet")
      }
      else if(vehicleList.contains(vehicle)) {
        sender ! Zone.Vehicle.CanNotSpawn(zone, vehicle, "already in zone")
      }
      else if(vehicle.Actor != ActorRef.noSender) {
        sender ! Zone.Vehicle.CanNotSpawn(zone, vehicle, "already in another zone")
      }
      else {
        vehicleList += vehicle
        vehicle.Zone = zone
        vehicle.Actor = context.actorOf(Props(classOf[VehicleControl], vehicle), s"${vehicle.Definition.Name}_${vehicle.GUID.guid}")
      }

    case Zone.Vehicle.Despawn(vehicle) =>
      ZoneVehicleActor.recursiveFindVehicle(vehicleList.iterator, vehicle) match {
        case Some(index) =>
          vehicleList.remove(index)
          vehicle.Actor ! akka.actor.PoisonPill
          vehicle.Actor = ActorRef.noSender
        case None => ;
          sender ! Zone.Vehicle.CanNotDespawn(zone, vehicle, "can not find")
      }

    case _ => ;
  }
}

object ZoneVehicleActor {
  @tailrec final def recursiveFindVehicle(iter : Iterator[Vehicle], target : Vehicle, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      if(iter.next.equals(target)) {
        Some(index)
      }
      else {
        recursiveFindVehicle(iter, target, index + 1)
      }
    }
  }
}

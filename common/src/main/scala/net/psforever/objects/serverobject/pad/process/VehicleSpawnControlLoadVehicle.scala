// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.Props
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles vehicle spawning orders for a `VehicleSpawnPad`.
  * The basic `VehicleSpawnControl` is the root of a simple tree of "spawn control" objects that chain to each other.
  * Each object performs on (or more than one related) actions upon the vehicle order that was submitted.<br>
  * <br>
  * This object introduces the vehicle into the game environment.
  * The vehicle must be added to the `Continent`, loaded onto other players' clients, and given an initial timed deconstruction event.
  * For actual details on this process, please refer to the external source represented by `Continent.VehicleEvents`.
  * It has failure cases should the driver be in an incorrect state.
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControlLoadVehicle(pad : VehicleSpawnPad) extends VehicleSpawnControlBase(pad) {
  def LogId = "-loader"

  val seatDriver = context.actorOf(Props(classOf[VehicleSpawnControlSeatDriver], pad), s"${context.parent.path.name}-seat")

  def receive : Receive = {
    case VehicleSpawnControl.Process.LoadVehicle(entry) =>
      val vehicle = entry.vehicle
      if(entry.driver.Continent == Continent.Id) {
        trace(s"loading the ${vehicle.Definition.Name}")
        Continent.VehicleEvents ! VehicleSpawnPad.LoadVehicle(vehicle, Continent)
        context.system.scheduler.scheduleOnce(100 milliseconds, seatDriver, VehicleSpawnControl.Process.SeatDriver(entry))
      }
      else {
        trace("owner lost; abort order fulfillment")
        VehicleSpawnControl.DisposeVehicle(vehicle, entry.driver, Continent)
        context.parent ! VehicleSpawnControl.ProcessControl.GetOrder
      }

    case VehicleSpawnControl.ProcessControl.Reminder =>
      context.parent ! VehicleSpawnControl.ProcessControl.Reminder

    case VehicleSpawnControl.ProcessControl.GetNewOrder =>
      context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder

    case _ => ;
  }
}

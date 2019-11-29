// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.Props
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.types.Vector3

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles vehicle spawning orders for a `VehicleSpawnPad`.
  * The basic `VehicleSpawnControl` is the root of a simple tree of "spawn control" objects that chain to each other.
  * Each object performs on (or more than one related) actions upon the vehicle order that was submitted.<br>
  * <br>
  * This object asserts automated control over the vehicle's motion after it has been released from its lifting platform.
  * Normally, the vehicle drives forward for a bit under its own power.
  * After a certain amount of time, control of the vehicle is given over to the driver.
  * It has failure cases should the driver be in an incorrect state.
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControlServerVehicleOverride(pad : VehicleSpawnPad) extends VehicleSpawnControlBase(pad) {
  def LogId = "-overrider"

  val driverControl = context.actorOf(Props(classOf[VehicleSpawnControlDriverControl], pad), s"${context.parent.path.name}-driver")

  def receive : Receive = {
    case order @ VehicleSpawnControl.Order(driver, vehicle) =>
      val vehicleFailState = vehicle.Health == 0 || vehicle.Position == Vector3.Zero
      val driverFailState = !driver.isAlive || driver.Continent != pad.Continent || !vehicle.PassengerInSeat(driver).contains(0)
      pad.Owner.Zone.VehicleEvents ! VehicleSpawnPad.DetachFromRails(vehicle, pad)
      if(vehicleFailState || driverFailState) {
        if(vehicleFailState) {
          trace(s"vehicle was already destroyed")
        }
        else {
          trace(s"driver is not ready")
        }
        pad.Owner.Zone.VehicleEvents ! VehicleSpawnPad.RevealPlayer(order.DriverGUID)
        driverControl ! order
      }
      else {
        trace(s"telling ${driver.Name} that the server is assuming control of the ${vehicle.Definition.Name}")
        pad.Owner.Zone.VehicleEvents ! VehicleSpawnPad.ServerVehicleOverrideStart(driver.Name, vehicle, pad)
        context.system.scheduler.scheduleOnce(4000 milliseconds, driverControl, order)
      }

    case msg @ (VehicleSpawnControl.ProcessControl.Reminder | VehicleSpawnControl.ProcessControl.GetNewOrder) =>
      context.parent ! msg

    case _ => ;
  }
}

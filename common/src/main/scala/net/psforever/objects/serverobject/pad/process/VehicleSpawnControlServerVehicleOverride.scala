// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.{ActorRef, Props}
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}

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

  val finalClear = context.actorOf(Props(classOf[VehicleSpawnControlFinalClearance], pad), s"${context.parent.path.name}-final")

  def receive : Receive = {
    case VehicleSpawnControl.Process.ServerVehicleOverride(entry) =>
      val vehicle = entry.vehicle
      //TODO detach vehicle from pad rails
      if(vehicle.Health == 0) {
        trace(s"vehicle was already destroyed; but, everything is fine")
        finalClear ! VehicleSpawnControl.Process.FinalClearance(entry)
      }
      else if(entry.sendTo != ActorRef.noSender && entry.driver.VehicleSeated.contains(vehicle.GUID)) {
        trace(s"telling ${entry.driver.Name} that the server is assuming control of the ${vehicle.Definition.Name}")
        entry.sendTo ! VehicleSpawnPad.ServerVehicleOverrideStart(vehicle, pad)
        context.system.scheduler.scheduleOnce(3000 milliseconds, self, VehicleSpawnControl.Process.DriverVehicleControl(entry))
      }
      else {
        if(pad.Railed) {
          Continent.VehicleEvents ! VehicleSpawnPad.DetachFromRails(vehicle, pad, Continent.Id)
        }
        finalClear ! VehicleSpawnControl.Process.FinalClearance(entry)
      }

    case VehicleSpawnControl.Process.DriverVehicleControl(entry) =>
      val vehicle = entry.vehicle
      if(vehicle.Health == 0) {
        trace(s"vehicle was already destroyed; but, everything is fine")
      }
      if(entry.sendTo != ActorRef.noSender) {
        val driver = entry.driver
        entry.sendTo ! VehicleSpawnPad.ServerVehicleOverrideEnd(vehicle, pad)
        if(driver.VehicleSeated.contains(vehicle.GUID)) {
          trace(s"returning control of ${vehicle.Definition.Name} to ${driver.Name}")
        }
        else {
          trace(s"${driver.Name} is not seated in ${vehicle.Definition.Name}; can not properly return control to driver")
        }
      }
      else {
        if(pad.Railed) {
          Continent.VehicleEvents ! VehicleSpawnPad.ResetSpawnPad(pad, Continent.Id)
        }
        trace("can not properly return control to driver")
      }
      finalClear ! VehicleSpawnControl.Process.FinalClearance(entry)

    case msg @ VehicleSpawnControl.Process.FinalClearance(_) =>
      finalClear ! msg

    case VehicleSpawnControl.ProcessControl.GetNewOrder =>
      context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder

    case _ => ;
  }
}

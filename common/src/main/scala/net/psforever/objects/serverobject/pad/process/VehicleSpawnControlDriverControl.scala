// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.{ActorRef, Props}
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}

/**
  * An `Actor` that handles vehicle spawning orders for a `VehicleSpawnPad`.
  * The basic `VehicleSpawnControl` is the root of a simple tree of "spawn control" objects that chain to each other.
  * Each object performs on (or more than one related) actions upon the vehicle order that was submitted.<br>
  * <br>
  * A certain amount of time after the server has asserted control over a newly-spawned vehicle,
  * control of that vehicle is given over to the driver.
  * It has failure cases should the driver be in an incorrect state.
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControlDriverControl(pad : VehicleSpawnPad) extends VehicleSpawnControlBase(pad) {
  def LogId = "-overrider"

  val finalClear = context.actorOf(Props(classOf[VehicleSpawnControlFinalClearance], pad), s"${context.parent.path.name}-final")

  def receive : Receive = {
    case VehicleSpawnControl.Process.DriverVehicleControl(entry) =>
      val vehicle = entry.vehicle
      if(pad.Railed) {
        Continent.VehicleEvents ! VehicleSpawnPad.ResetSpawnPad(pad, Continent.Id)
      }
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
          trace(s"${driver.Name} is not seated in ${vehicle.Definition.Name}; vehicle controls have been locked")
        }
      }
      else {
        trace("can not properly return control to driver")
      }
      finalClear ! VehicleSpawnControl.Process.FinalClearance(entry)

    case msg @ (VehicleSpawnControl.ProcessControl.Reminder | VehicleSpawnControl.ProcessControl.GetNewOrder) =>
      context.parent ! msg

    case msg @ VehicleSpawnControl.Process.FinalClearance(_) =>
      finalClear ! msg

    case _ => ;
  }
}

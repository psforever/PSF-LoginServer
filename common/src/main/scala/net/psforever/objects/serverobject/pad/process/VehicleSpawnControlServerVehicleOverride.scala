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

  val vehicleGuide = context.actorOf(Props(classOf[VehicleSpawnControlGuided], pad), s"${context.parent.path.name}-guide")

  def receive : Receive = {
    case VehicleSpawnControl.Process.ServerVehicleOverride(entry) =>
      val vehicle = entry.vehicle
      val pad_railed = pad.Railed
      if(pad_railed) {
        Continent.VehicleEvents ! VehicleSpawnPad.DetachFromRails(vehicle, pad, Continent.Id)
      }
      if(vehicle.Health == 0) {
        trace(s"vehicle was already destroyed; but, everything is fine")
        if(pad_railed) {
          Continent.VehicleEvents ! VehicleSpawnPad.ResetSpawnPad(pad, Continent.Id)
        }
        vehicleGuide ! VehicleSpawnControl.Process.FinalClearance(entry)
      }
      else if(entry.sendTo != ActorRef.noSender && entry.driver.isAlive && entry.driver.Continent == Continent.Id && entry.driver.VehicleSeated.contains(vehicle.GUID)) {
        trace(s"telling ${entry.driver.Name} that the server is assuming control of the ${vehicle.Definition.Name}")
        entry.sendTo ! VehicleSpawnPad.ServerVehicleOverrideStart(vehicle, pad)
        context.system.scheduler.scheduleOnce(3000 milliseconds, vehicleGuide, VehicleSpawnControl.Process.StartGuided(entry))
      }
      else {
        if(pad_railed) {
          Continent.VehicleEvents ! VehicleSpawnPad.ResetSpawnPad(pad, Continent.Id)
        }
        vehicleGuide ! VehicleSpawnControl.Process.FinalClearance(entry)
      }

    case msg @ (VehicleSpawnControl.ProcessControl.Reminder | VehicleSpawnControl.ProcessControl.GetNewOrder) =>
      context.parent ! msg

    case msg @ VehicleSpawnControl.Process.FinalClearance(_) =>
      vehicleGuide ! msg

    case _ => ;
  }
}

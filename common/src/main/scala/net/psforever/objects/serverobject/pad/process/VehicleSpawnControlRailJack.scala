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
  * When the vehicle is added into the environment, it is attached to the spawn pad platform.
  * On cue, the trapdoor of the platform will open, and the vehicle will be raised up into plain sight on a group of rails.
  * It has failure cases should the driver be in an incorrect state.<br>
  * __It currently does not work__.
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControlRailJack(pad : VehicleSpawnPad) extends VehicleSpawnControlBase(pad) {
  def LogId = "-jacker"

  val vehicleOverride = context.actorOf(Props(classOf[VehicleSpawnControlServerVehicleOverride], pad), s"${context.parent.path.name}-override")

  def receive : Receive = {
    case VehicleSpawnControl.Process.RailJackAction(entry) =>
      if(entry.vehicle.Health == 0) {
        //TODO detach vehicle from pad rails if necessary
        trace(s"vehicle was already destroyed; clean it up")
        VehicleSpawnControl.DisposeSpawnedVehicle(entry.vehicle, entry.driver, Continent)
        context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder
      }
      else {
        trace(s"extending rails with vehicle attached")
        context.parent ! VehicleSpawnControl.ProcessControl.Reminder
        context.system.scheduler.scheduleOnce(10 milliseconds, vehicleOverride, VehicleSpawnControl.Process.ServerVehicleOverride(entry))
      }

    case VehicleSpawnControl.ProcessControl.GetNewOrder =>
      context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder

    case _ => ;
  }
}

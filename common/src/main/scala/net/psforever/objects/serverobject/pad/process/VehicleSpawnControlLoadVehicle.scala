// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.Props
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.types.Vector3

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

  val railJack = context.actorOf(Props(classOf[VehicleSpawnControlRailJack], pad), s"${context.parent.path.name}-rails")

  def receive : Receive = {
    case VehicleSpawnControl.Process.LoadVehicle(entry) =>
      val vehicle = entry.vehicle
      if(entry.driver.Continent == Continent.Id) {
        trace(s"loading the ${vehicle.Definition.Name}")
        if(pad.Railed) {
          //load the vehicle in the spawn pad trench, underground, initially
          vehicle.Position = vehicle.Position - Vector3(0, 0, if(GlobalDefinitions.isFlightVehicle(vehicle.Definition)) 9 else 5)
        }
        vehicle.Cloaked = vehicle.Definition.CanCloak && entry.driver.Cloaked
        Continent.VehicleEvents ! VehicleSpawnPad.LoadVehicle(vehicle, Continent)
        context.system.scheduler.scheduleOnce(100 milliseconds, railJack, VehicleSpawnControl.Process.RailJackAction(entry))
      }
      else {
        trace("owner lost; abort order fulfillment")
        VehicleSpawnControl.DisposeVehicle(entry, Continent)
        context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder
      }

    case msg @ (VehicleSpawnControl.ProcessControl.Reminder | VehicleSpawnControl.ProcessControl.GetNewOrder) =>
      context.parent ! msg

    case _ => ;
  }
}

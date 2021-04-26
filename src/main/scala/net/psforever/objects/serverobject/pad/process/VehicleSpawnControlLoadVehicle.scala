// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.{Cancellable, Props}
import net.psforever.objects.{Default, GlobalDefinitions}
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.zones.Zone
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.Vector3

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles vehicle spawning orders for a `VehicleSpawnPad`.
  * The basic `VehicleSpawnControl` is the root of a simple tree of "spawn control" objects that chain to each other.
  * Each object performs on (or more than one related) actions upon the vehicle order that was submitted.<br>
  * <br>
  * This object introduces the vehicle into the game environment.
  * The vehicle must be added to the `Zone` object, loaded onto other players' clients, and given an initial timed deconstruction event.
  * For actual details on this process, please refer to the external source represented by `pad.Zone.VehicleEvents`.
  * It has failure cases should the driver be in an incorrect state.
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControlLoadVehicle(pad: VehicleSpawnPad) extends VehicleSpawnControlBase(pad) {
  def LogId = "-loader"

  val railJack = context.actorOf(Props(classOf[VehicleSpawnControlRailJack], pad), s"${context.parent.path.name}-rails")

  var temp: Cancellable = Default.Cancellable

  override def postStop() : Unit = {
    temp.cancel()
    super.postStop()
  }

  def receive: Receive = {
    case order @ VehicleSpawnControl.Order(driver, vehicle) =>
      if (driver.Continent == pad.Continent && vehicle.Health > 0 && driver.isAlive) {
        trace(s"loading the ${vehicle.Definition.Name}")
        vehicle.Position = vehicle.Position - Vector3.z(
          if (GlobalDefinitions.isFlightVehicle(vehicle.Definition)) 9 else 5
        ) //appear below the trench and doors
        vehicle.Cloaked = vehicle.Definition.CanCloak && driver.Cloaked
        pad.Zone.Transport.tell(Zone.Vehicle.Spawn(vehicle), self)
        temp = context.system.scheduler.scheduleOnce(
          delay = 100 milliseconds,
          self,
          VehicleSpawnControlLoadVehicle.WaitOnSpawn(order)
        )
      } else {
        trace("owner lost or vehicle in poor condition; abort order fulfillment")
        VehicleSpawnControl.DisposeVehicle(order.vehicle, pad.Zone)
        context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder
      }

    case Zone.Vehicle.HasSpawned(zone, vehicle) =>
      val definition = vehicle.Definition
      val vtype      = definition.ObjectId
      val vguid      = vehicle.GUID
      val vdata      = definition.Packet.ConstructorData(vehicle).get
      zone.VehicleEvents ! VehicleServiceMessage(
        zone.id,
        VehicleAction.LoadVehicle(Service.defaultPlayerGUID, vehicle, vtype, vguid, vdata)
      )

    case VehicleSpawnControlLoadVehicle.WaitOnSpawn(order) =>
      if (pad.Zone.Vehicles.contains(order.vehicle)) {
        railJack ! order
      } else {
        VehicleSpawnControl.DisposeVehicle(order.vehicle, pad.Zone)
        context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder
      }

    case Zone.Vehicle.CanNotSpawn(zone, vehicle, reason) =>
      trace(s"vehicle $reason; abort order fulfillment")
      temp.cancel()
      context.parent ! VehicleSpawnControl.ProcessControl.GetNewOrder

    case msg @ (VehicleSpawnControl.ProcessControl.Reminder | VehicleSpawnControl.ProcessControl.GetNewOrder) =>
      context.parent ! msg

    case _ => ;
  }
}

object VehicleSpawnControlLoadVehicle {
  private case class WaitOnSpawn(order: VehicleSpawnControl.Order)
}

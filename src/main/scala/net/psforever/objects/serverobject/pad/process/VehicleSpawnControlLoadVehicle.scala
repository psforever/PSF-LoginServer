// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.zones.Zone
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.Vector3

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Success

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

  var temp: Option[VehicleSpawnControl.Order] = None

  implicit val timeout = Timeout(3.seconds)

  def receive: Receive = {
    case order @ VehicleSpawnControl.Order(driver, vehicle) =>
      if (VehicleSpawnControl.validateOrderCredentials(pad, driver, vehicle).isEmpty) {
        trace(s"loading the ${vehicle.Definition.Name}")
        vehicle.Position = vehicle.Position - Vector3.z(
          if (GlobalDefinitions.isFlightVehicle(vehicle.Definition)) 9 else 5
        ) //appear below the trench and doors
        vehicle.Cloaked = vehicle.Definition.CanCloak && driver.Cloaked

        temp = Some(order)
        val result = ask(pad.Zone.Transport, Zone.Vehicle.Spawn(vehicle))
        //if too long, or something goes wrong
        result.recover {
          case _ =>
            temp = None
            context.parent ! VehicleSpawnControl.ProcessControl.OrderCancelled
        }
        //resolution
        result.onComplete {
          case Success(Zone.Vehicle.HasSpawned(zone, v))
          if (temp match { case Some(_order) => _order.vehicle eq v; case _ => false }) =>
            val definition = v.Definition
            val vtype      = definition.ObjectId
            val vguid      = v.GUID
            val vdata      = definition.Packet.ConstructorData(v).get
            zone.VehicleEvents ! VehicleServiceMessage(
              zone.id,
              VehicleAction.LoadVehicle(Service.defaultPlayerGUID, v, vtype, vguid, vdata)
            )
            railJack ! temp.get
            temp = None

          case Success(Zone.Vehicle.CanNotSpawn(_, _, reason)) =>
            trace(s"vehicle can not spawn - $reason; abort order fulfillment")
            temp = None
            context.parent ! VehicleSpawnControl.ProcessControl.OrderCancelled

          case _ =>
            temp match {
              case Some(_) =>
                trace(s"abort order fulfillment")
                context.parent ! VehicleSpawnControl.ProcessControl.OrderCancelled
              case None => ; //should we have gotten this message?
            }
            temp = None
        }
      } else {
        trace("owner lost or vehicle in poor condition; abort order fulfillment")
        context.parent ! VehicleSpawnControl.ProcessControl.OrderCancelled
      }

    case msg @ (VehicleSpawnControl.ProcessControl.Reminder | VehicleSpawnControl.ProcessControl.GetNewOrder) =>
      context.parent ! msg

    case _ => ;
  }
}

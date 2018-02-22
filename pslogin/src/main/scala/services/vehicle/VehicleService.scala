// Copyright (c) 2017 PSForever
package services.vehicle

import akka.actor.{Actor, ActorRef, Props}
import services.vehicle.support.{DeconstructionActor, DelayedDeconstructionActor, VehicleContextActor}
import services.{GenericEventBus, Service}

class VehicleService extends Actor {
  private val vehicleContext : ActorRef = context.actorOf(Props[VehicleContextActor], "vehicle-context-root")
  private val vehicleDecon : ActorRef = context.actorOf(Props[DeconstructionActor], "vehicle-decon-agent")
  private val vehicleDelayedDecon : ActorRef = context.actorOf(Props[DelayedDeconstructionActor], "vehicle-delayed-decon-agent")
  vehicleDecon ! DeconstructionActor.RequestTaskResolver
  private [this] val log = org.log4s.getLogger

  override def preStart = {
    log.info("Starting...")
  }

  val VehicleEvents = new GenericEventBus[VehicleServiceResponse]

  def receive = {
    case Service.Join(channel) =>
      val path = s"/$channel/Vehicle"
      val who = sender()
      log.info(s"$who has joined $path")
      VehicleEvents.subscribe(who, path)

    case Service.Leave(None) =>
      VehicleEvents.unsubscribe(sender())

    case Service.Leave(Some(channel)) =>
      val path = s"/$channel/Vehicle"
      val who = sender()
      log.info(s"$who has left $path")
      VehicleEvents.unsubscribe(who, path)

    case Service.LeaveAll() =>
      VehicleEvents.unsubscribe(sender())

    case VehicleServiceMessage(forChannel, action) =>
      action match {
        case VehicleAction.Awareness(player_guid, vehicle_guid) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.Awareness(vehicle_guid))
          )
        case VehicleAction.ChildObjectState(player_guid, object_guid, pitch, yaw) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.ChildObjectState(object_guid, pitch, yaw))
          )
        case VehicleAction.DeployRequest(player_guid, object_guid, state, unk1, unk2, pos) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.DeployRequest(object_guid, state, unk1, unk2, pos))
          )
        case VehicleAction.DismountVehicle(player_guid, unk1, unk2) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.DismountVehicle(unk1, unk2))
          )
        case VehicleAction.InventoryState(player_guid, obj, parent_guid, start, con_data) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.InventoryState(obj, parent_guid, start, con_data))
          )
        case VehicleAction.KickPassenger(player_guid, unk1, unk2, vehicle_guid) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.KickPassenger(unk1, unk2, vehicle_guid))
          )
        case VehicleAction.LoadVehicle(player_guid, vehicle, vtype, vguid, vdata) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.LoadVehicle(vehicle, vtype, vguid, vdata))
          )
        case VehicleAction.MountVehicle(player_guid, vehicle_guid, seat) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.MountVehicle(vehicle_guid, seat))
          )
        case VehicleAction.SeatPermissions(player_guid, vehicle_guid, seat_group, permission) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.SeatPermissions(vehicle_guid, seat_group, permission))
          )
        case VehicleAction.StowEquipment(player_guid, vehicle_guid, slot, item) =>
          val definition = item.Definition
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.StowEquipment(vehicle_guid, slot, definition.ObjectId, item.GUID, definition.Packet.DetailedConstructorData(item).get))
          )
        case VehicleAction.UnstowEquipment(player_guid, item_guid) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.UnstowEquipment(item_guid))
          )
        case VehicleAction.VehicleState(player_guid, vehicle_guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel_direction, unk5, unk6) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.VehicleState(vehicle_guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel_direction, unk5, unk6))
          )
        case _ => ;
    }

    //message to VehicleContext
    case VehicleServiceMessage.GiveActorControl(vehicle, actorName) =>
      vehicleContext ! VehicleServiceMessage.GiveActorControl(vehicle, actorName)

    //message to VehicleContext
    case VehicleServiceMessage.RevokeActorControl(vehicle) =>
      vehicleContext ! VehicleServiceMessage.RevokeActorControl(vehicle)

    //message to DeconstructionActor
    case VehicleServiceMessage.RequestDeleteVehicle(vehicle, continent) =>
      vehicleDecon ! DeconstructionActor.RequestDeleteVehicle(vehicle, continent)

    //message to DelayedDeconstructionActor
    case VehicleServiceMessage.DelayedVehicleDeconstruction(vehicle, zone, timeAlive) =>
      vehicleDelayedDecon ! DelayedDeconstructionActor.ScheduleDeconstruction(vehicle, zone, timeAlive)

    //message to DelayedDeconstructionActor
    case VehicleServiceMessage.UnscheduleDeconstruction(vehicle_guid) =>
      vehicleDelayedDecon ! DelayedDeconstructionActor.UnscheduleDeconstruction(vehicle_guid)

    //response from DeconstructionActor
    case DeconstructionActor.DeleteVehicle(vehicle_guid, zone_id) =>
      VehicleEvents.publish(
        VehicleServiceResponse(s"/$zone_id/Vehicle", Service.defaultPlayerGUID, VehicleResponse.UnloadVehicle(vehicle_guid))
      )

    case msg =>
      log.info(s"Unhandled message $msg from $sender")
  }
}

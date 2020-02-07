// Copyright (c) 2017 PSForever
package services.vehicle

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.objects.{GlobalDefinitions, TelepadDeployable, Vehicle}
import net.psforever.objects.ballistics.VehicleSource
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.terminals.{MedicalTerminalDefinition, ProximityUnit}
import net.psforever.objects.vehicles.{Utility, UtilityType}
import net.psforever.objects.vital.RepairFromTerm
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import services.vehicle.support.{TurretUpgrader, VehicleRemover}
import net.psforever.types.{DriveState, PlanetSideGUID}
import services.local.LocalServiceMessage
import services.{GenericEventBus, RemoverActor, Service}

import scala.concurrent.duration._

class VehicleService(zone : Zone) extends Actor {
  private val vehicleDecon : ActorRef = context.actorOf(Props[VehicleRemover], s"${zone.Id}-vehicle-decon-agent")
  private val turretUpgrade : ActorRef = context.actorOf(Props[TurretUpgrader], s"${zone.Id}-turret-upgrade-agent")
  private [this] val log = org.log4s.getLogger

  override def preStart = {
    log.trace(s"Awaiting ${zone.Id} vehicle events ...")
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
        case VehicleAction.ChildObjectState(player_guid, object_guid, pitch, yaw) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.ChildObjectState(object_guid, pitch, yaw))
          )
        case VehicleAction.DeployRequest(player_guid, object_guid, state, unk1, unk2, pos) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.DeployRequest(object_guid, state, unk1, unk2, pos))
          )
        case VehicleAction.DismountVehicle(player_guid, bailType, unk2) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.DismountVehicle(bailType, unk2))
          )
        case VehicleAction.EquipmentInSlot(player_guid, target_guid, slot, equipment) =>
          val definition = equipment.Definition
          val pkt = ObjectCreateMessage(
            definition.ObjectId,
            equipment.GUID,
            ObjectCreateMessageParent(target_guid, slot),
            definition.Packet.ConstructorData(equipment).get
          )
          ObjectCreateMessageParent(target_guid, slot)
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.EquipmentInSlot(pkt))
          )
        case VehicleAction.InventoryState(player_guid, obj, parent_guid, start, con_data) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.InventoryState(obj, parent_guid, start, con_data))
          )
        case VehicleAction.InventoryState2(player_guid, obj_guid, parent_guid, value) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.InventoryState2(obj_guid, parent_guid, value))
          )
        case VehicleAction.KickPassenger(player_guid, seat_num, kickedByDriver, vehicle_guid) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.KickPassenger(seat_num, kickedByDriver, vehicle_guid))
          )
        case VehicleAction.LoadVehicle(player_guid, vehicle, vtype, vguid, vdata) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.LoadVehicle(vehicle, vtype, vguid, vdata))
          )
        case VehicleAction.MountVehicle(player_guid, vehicle_guid, seat) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.MountVehicle(vehicle_guid, seat))
          )
        case VehicleAction.Ownership(player_guid, vehicle_guid) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.Ownership(vehicle_guid))
          )
        case VehicleAction.PlanetsideAttribute(exclude_guid, target_guid, attribute_type, attribute_value) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", exclude_guid, VehicleResponse.PlanetsideAttribute(target_guid, attribute_type, attribute_value))
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
        case VehicleAction.UnloadVehicle(player_guid, continent, vehicle, vehicle_guid) =>
          vehicleDecon ! RemoverActor.ClearSpecific(List(vehicle), continent) //precaution
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.UnloadVehicle(vehicle, vehicle_guid))
          )
        case VehicleAction.UnstowEquipment(player_guid, item_guid) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.UnstowEquipment(item_guid))
          )
        case VehicleAction.VehicleState(player_guid, vehicle_guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel_direction, unk5, unk6) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.VehicleState(vehicle_guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel_direction, unk5, unk6))
          )
        case VehicleAction.SendResponse(player_guid, msg) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.SendResponse(msg))
          )

        //unlike other messages, just return to sender, don't publish
        case VehicleAction.UpdateAmsSpawnPoint(zone : Zone) =>
          sender ! VehicleServiceResponse(s"/$forChannel/Vehicle", Service.defaultPlayerGUID, VehicleResponse.UpdateAmsSpawnPoint(AmsSpawnPoints(zone)))

        case VehicleAction.TransferPassengerChannel(player_guid, old_channel, temp_channel, vehicle, vehicle_to_delete) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.TransferPassengerChannel(old_channel, temp_channel, vehicle, vehicle_to_delete))
          )

        case VehicleAction.ForceDismountVehicleCargo(player_guid, vehicle_guid, bailed, requestedByPassenger, kicked) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.ForceDismountVehicleCargo(vehicle_guid, bailed, requestedByPassenger, kicked))
          )
        case VehicleAction.KickCargo(player_guid, cargo, speed, delay) =>
          VehicleEvents.publish(
            VehicleServiceResponse(s"/$forChannel/Vehicle", player_guid, VehicleResponse.KickCargo(cargo, speed, delay))
          )
        case _ => ;
    }

    //message to VehicleRemover
    case VehicleServiceMessage.Decon(msg) =>
      vehicleDecon forward msg

    //message to TurretUpgrader
    case VehicleServiceMessage.TurretUpgrade(msg) =>
      turretUpgrade forward msg

    //from VehicleSpawnControl, etc.
    case VehicleSpawnPad.ConcealPlayer(player_guid) =>
      VehicleEvents.publish(
        VehicleServiceResponse(s"/${zone.Id}/Vehicle", Service.defaultPlayerGUID, VehicleResponse.ConcealPlayer(player_guid))
      )

    case VehicleSpawnPad.AttachToRails(vehicle, pad) =>
      VehicleEvents.publish(
        VehicleServiceResponse(s"/${zone.Id}/Vehicle", Service.defaultPlayerGUID, VehicleResponse.AttachToRails(vehicle.GUID, pad.GUID))
      )

    case VehicleSpawnPad.StartPlayerSeatedInVehicle(driver_name, vehicle, pad) =>
      VehicleEvents.publish(
        VehicleServiceResponse(s"/$driver_name/Vehicle", Service.defaultPlayerGUID, VehicleResponse.StartPlayerSeatedInVehicle(vehicle, pad))
      )

    case VehicleSpawnPad.PlayerSeatedInVehicle(driver_name, vehicle, pad) =>
      VehicleEvents.publish(
        VehicleServiceResponse(s"/$driver_name/Vehicle", Service.defaultPlayerGUID, VehicleResponse.PlayerSeatedInVehicle(vehicle, pad))
      )

    case VehicleSpawnPad.ServerVehicleOverrideStart(driver_name, vehicle, pad) =>
      VehicleEvents.publish(
        VehicleServiceResponse(s"/$driver_name/Vehicle", Service.defaultPlayerGUID, VehicleResponse.ServerVehicleOverrideStart(vehicle, pad))
      )

    case VehicleSpawnPad.ServerVehicleOverrideEnd(driver_name, vehicle, pad) =>
      VehicleEvents.publish(
        VehicleServiceResponse(s"/$driver_name/Vehicle", Service.defaultPlayerGUID, VehicleResponse.ServerVehicleOverrideEnd(vehicle, pad))
      )

    case VehicleSpawnPad.DetachFromRails(vehicle, pad) =>
      VehicleEvents.publish(
        VehicleServiceResponse(s"/${zone.Id}/Vehicle", Service.defaultPlayerGUID, VehicleResponse.DetachFromRails(vehicle.GUID, pad.GUID, pad.Position, pad.Orientation.z))
      )
    case VehicleSpawnPad.ResetSpawnPad(pad) =>
      VehicleEvents.publish(
        VehicleServiceResponse(s"/${zone.Id}/Vehicle", Service.defaultPlayerGUID, VehicleResponse.ResetSpawnPad(pad.GUID))
      )

    case VehicleSpawnPad.RevealPlayer(player_guid) =>
      VehicleEvents.publish(
        VehicleServiceResponse(s"/${zone.Id}/Vehicle", Service.defaultPlayerGUID, VehicleResponse.RevealPlayer(player_guid))
      )

    case VehicleSpawnPad.PeriodicReminder(to, reason, data) =>
      VehicleEvents.publish(
        VehicleServiceResponse(s"/$to/Vehicle", Service.defaultPlayerGUID, VehicleResponse.PeriodicReminder(reason, data))
      )

    case VehicleSpawnPad.LoadVehicle(vehicle) =>
      val definition = vehicle.Definition
      val vtype = definition.ObjectId
      val vguid = vehicle.GUID
      val vdata = definition.Packet.ConstructorData(vehicle).get
      zone.Transport ! Zone.Vehicle.Spawn(vehicle)
      VehicleEvents.publish(
        VehicleServiceResponse(s"/${zone.Id}/Vehicle", Service.defaultPlayerGUID, VehicleResponse.LoadVehicle(vehicle, vtype, vguid, vdata))
      )
      //avoid unattended vehicle spawning blocking the pad; user should mount (and does so normally) to reset decon timer
      vehicleDecon forward RemoverActor.AddTask(vehicle, zone, Some(30 seconds))

    case VehicleSpawnPad.DisposeVehicle(vehicle) =>
      vehicleDecon forward RemoverActor.AddTask(vehicle, zone, Some(0 seconds))
      vehicleDecon forward RemoverActor.HurrySpecific(List(vehicle), zone)

    //correspondence from WorldSessionActor
    case VehicleServiceMessage.AMSDeploymentChange(_) =>
      VehicleEvents.publish(
        VehicleServiceResponse(s"/${zone.Id}/Vehicle", Service.defaultPlayerGUID, VehicleResponse.UpdateAmsSpawnPoint(AmsSpawnPoints(zone)))
      )

    //from ProximityTerminalControl (?)
    case ProximityUnit.Action(term, target : Vehicle) =>
      val medDef = term.Definition.asInstanceOf[MedicalTerminalDefinition]
      val healAmount = medDef.HealAmount
      if(healAmount != 0 && term.Validate(target) && target.Health < target.MaxHealth) {
        target.Health = target.Health + healAmount
        target.History(RepairFromTerm(VehicleSource(target), healAmount, medDef))
        VehicleEvents.publish(
          VehicleServiceResponse(s"/${term.Continent}/Vehicle", PlanetSideGUID(0), VehicleResponse.PlanetsideAttribute(target.GUID, 0, target.Health))
        )
      }

    case msg =>
      log.info(s"Unhandled message $msg from $sender")
  }

  import net.psforever.objects.serverobject.tube.SpawnTube
  def AmsSpawnPoints(zone : Zone) : List[SpawnTube] = {
    import net.psforever.objects.vehicles.UtilityType
    import net.psforever.objects.GlobalDefinitions
    zone.Vehicles
      .filter(veh => veh.Health > 0 && veh.Definition == GlobalDefinitions.ams && veh.DeploymentState == DriveState.Deployed)
      .flatMap(veh => veh.Utilities.values.filter(util => util.UtilType == UtilityType.ams_respawn_tube) )
      .map(util => util().asInstanceOf[SpawnTube])
  }
}

object VehicleService {
  /**
    * Before a vehicle is removed from the game world, the following actions must be performed.
    * @param vehicle the vehicle
    */
  def BeforeUnloadVehicle(vehicle : Vehicle, zone : Zone) : Unit = {
    vehicle.Definition match {
      case GlobalDefinitions.ams =>
        zone.VehicleEvents ! VehicleServiceMessage.AMSDeploymentChange(zone)
      case GlobalDefinitions.router =>
        RemoveTelepads(vehicle, zone)
      case _ => ;
    }
  }

  def RemoveTelepads(vehicle: Vehicle, zone : Zone) : Unit = {
    (vehicle.Utility(UtilityType.internal_router_telepad_deployable) match {
      case Some(util : Utility.InternalTelepad) =>
        val telepad = util.Telepad
        util.Telepad = None
        zone.GUID(telepad)
      case _ =>
        None
    }) match {
      case Some(telepad : TelepadDeployable) =>
        telepad.Active = false
        zone.LocalEvents ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(telepad), zone))
        zone.LocalEvents ! LocalServiceMessage.Deployables(RemoverActor.AddTask(telepad, zone, Some(0 seconds)))
      case _ => ;
    }
  }
}

// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.Actor.Receive
import akka.actor.ActorContext
import net.psforever.actors.session.support.{SessionData, SessionVehicleHandlers, VehicleHandlerFunctions}
import net.psforever.objects.{Tool, Vehicle, Vehicles}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.packet.game.{ChangeAmmoMessage, ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, ChildObjectStateMessage, DeadState, DeployRequestMessage, DismountVehicleMsg, FrameVehicleStateMessage, GenericObjectActionMessage, InventoryStateMessage, ObjectAttachMessage, ObjectCreateDetailedMessage, ObjectCreateMessage, ObjectDeleteMessage, ObjectDetachMessage, PlanetsideAttributeMessage, ReloadMessage, ServerVehicleOverrideMsg, VehicleStateMessage, WeaponDryFireMessage}
import net.psforever.services.base.envelope.GenericResponseEnvelope
import net.psforever.services.base.message.{ChangeAmmo, ChangeFireState_Start, ChangeFireState_Stop, GenericObjectAction, ObjectDelete, PlanetsideAttribute, ReloadTool, SendResponse, WeaponDryFire}
import net.psforever.services.vehicle.{VehicleAction, VehicleStamp}
import net.psforever.types.{BailType, PlanetSideGUID, Vector3}

object VehicleHandlerLogic {
  def apply(ops: SessionVehicleHandlers): VehicleHandlerLogic = {
    new VehicleHandlerLogic(ops, ops.context)
  }
}

class VehicleHandlerLogic(val ops: SessionVehicleHandlers, implicit val context: ActorContext) extends VehicleHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  //private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  //private val galaxyService: ActorRef = ops.galaxyService

  def receive: Receive = {
    case VehicleAction.VehicleState(
    vehicleGuid,
    unk1,
    pos,
    orient,
    vel,
    unk2,
    unk3,
    unk4,
    wheelDirection,
    unk5,
    unk6
    ) if isNotSameTarget && player.VehicleSeated.contains(vehicleGuid) =>
      //player who is also in the vehicle (not driver)
      sendResponse(VehicleStateMessage(vehicleGuid, unk1, pos, orient, vel, unk2, unk3, unk4, wheelDirection, unk5, unk6))
      player.Position = pos
      player.Orientation = orient
      player.Velocity = vel
      sessionLogic.updateLocalBlockMap(pos)

    case VehicleAction.VehicleState(
    vehicleGuid,
    unk1,
    pos,
    ang,
    vel,
    unk2,
    unk3,
    unk4,
    wheelDirection,
    unk5,
    unk6
    ) if isNotSameTarget =>
      //player who is watching the vehicle from the outside
      sendResponse(VehicleStateMessage(vehicleGuid, unk1, pos, ang, vel, unk2, unk3, unk4, wheelDirection, unk5, unk6))

    case VehicleAction.ChildObjectState(objectGuid, pitch, yaw) if isNotSameTarget =>
      sendResponse(ChildObjectStateMessage(objectGuid, pitch, yaw))

    case VehicleAction.FrameVehicleState(vguid, u1, pos, oient, vel, u2, u3, u4, is_crouched, u6, u7, u8, u9, uA)
      if isNotSameTarget =>
      sendResponse(FrameVehicleStateMessage(vguid, u1, pos, oient, vel, u2, u3, u4, is_crouched, u6, u7, u8, u9, uA))

    case VehicleAction.DismountVehicle(bailType, wasKickedByDriver) if isNotSameTarget =>
      sendResponse(DismountVehicleMsg(resolvedGuid, bailType, wasKickedByDriver))

    case VehicleAction.MountVehicle(vehicleGuid, seat) if isNotSameTarget =>
      sendResponse(ObjectAttachMessage(vehicleGuid, resolvedGuid, seat))

    case VehicleAction.DeployRequest(objectGuid, state, unk1, unk2, pos) if isNotSameTarget =>
      sendResponse(DeployRequestMessage(resolvedGuid, objectGuid, state, unk1, unk2, pos))

    case VehicleAction.EquipmentCreatedInSlot(pkt) if isNotSameTarget =>
      sendResponse(pkt)

    case VehicleAction.InventoryState(obj, parentGuid, start, conData) if isNotSameTarget =>
      //TODO prefer ObjectDetachMessage, but how to force ammo pools to update properly?
      val objGuid = obj.GUID
      sendResponse(ObjectDeleteMessage(objGuid, unk1=0))
      sendResponse(ObjectCreateDetailedMessage(
        obj.Definition.ObjectId,
        objGuid,
        ObjectCreateMessageParent(parentGuid, start),
        conData
      ))

    case VehicleAction.KickPassenger(_, wasKickedByDriver, vehicleGuid) if isSameTarget =>
      //seat number (first field) seems to be correct if passenger is kicked manually by driver
      //but always seems to return 4 if user is kicked by mount permissions changing
      sendResponse(DismountVehicleMsg(resolvedGuid, BailType.Kicked, wasKickedByDriver))
      continent.GUID(vehicleGuid) match {
        case Some(obj: Vehicle) =>
          sessionLogic.general.unaccessContainer(obj)
        case _ => ()
      }

    case VehicleAction.KickPassenger(_, wasKickedByDriver, _) =>
      //seat number (first field) seems to be correct if passenger is kicked manually by driver
      //but always seems to return 4 if user is kicked by mount permissions changing
      sendResponse(DismountVehicleMsg(resolvedGuid, BailType.Kicked, wasKickedByDriver))

    case VehicleAction.InventoryState2(objGuid, parentGuid, value) if isNotSameTarget =>
      sendResponse(InventoryStateMessage(objGuid, unk=0, parentGuid, value))

    case VehicleAction.LoadVehicle(vehicle, vtype, vguid, vdata) if isNotSameTarget =>
      //this is not be suitable for vehicles with people who are seated in it before it spawns (if that is possible)
      sendResponse(ObjectCreateMessage(vtype, vguid, vdata))
      Vehicles.ReloadAccessPermissions(vehicle, player.Name)

    case VehicleAction.SeatPermissions(vehicleGuid, seatGroup, permission) if isNotSameTarget =>
      sendResponse(PlanetsideAttributeMessage(vehicleGuid, seatGroup, permission))

    case VehicleAction.UnloadVehicle(_, vehicleGuid) =>
      sendResponse(ObjectDeleteMessage(vehicleGuid, unk1=1))

    case VehicleAction.UnstowEquipment(itemGuid) if isNotSameTarget =>
      //TODO prefer ObjectDetachMessage, but how to force ammo pools to update properly?
      sendResponse(ObjectDeleteMessage(itemGuid, unk1=0))

    case VehicleAction.UpdateAmsSpawnList(list) =>
      sessionLogic.zoning.spawn.amsSpawnPoints = list.filter(tube => tube.Faction == player.Faction)
      sessionLogic.zoning.spawn.DrawCurrentAmsSpawnPoint()

    case VehicleAction.KickCargo(vehicle, speed, delay)
      if player.VehicleSeated.nonEmpty && sessionLogic.zoning.spawn.deadState == DeadState.Alive && speed > 0 =>
      val strafe = 1 + Vehicles.CargoOrientation(vehicle)
      val reverseSpeed = if (strafe > 1) { 0 } else { speed }
      //strafe or reverse, not both
      sessionLogic.vehicles.ServerVehicleOverrideWithPacket(
        vehicle,
        ServerVehicleOverrideMsg(
          lock_accelerator=true,
          lock_wheel=true,
          reverse=true,
          unk4=false,
          lock_vthrust=0,
          strafe,
          reverseSpeed,
          unk8=Some(0)
        )
      )
      import scala.concurrent.ExecutionContext.Implicits.global
      import scala.concurrent.duration._
      val resp = GenericResponseEnvelope(
        VehicleStamp,
        "",
        PlanetSideGUID(0),
        VehicleAction.KickCargo(vehicle, speed=0, delay)
      )
      context.system.scheduler.scheduleOnce(delay milliseconds, context.self, resp)

    case VehicleAction.KickCargo(cargo, _, _)
      if player.VehicleSeated.nonEmpty && sessionLogic.zoning.spawn.deadState == DeadState.Alive =>
      sessionLogic.vehicles.TotalDriverVehicleControl(cargo)

    case VehicleSpawnPad.AttachToRails(vehicle, pad) =>
      sendResponse(ObjectAttachMessage(pad.GUID, vehicle.GUID, slot=3))

    case VehicleSpawnPad.ConcealPlayer(playerGuid) =>
      sendResponse(GenericObjectActionMessage(playerGuid, code=9))

    case VehicleSpawnPad.DetachFromRails(vehicle, pad) =>
      val padDefinition = pad.Definition
      sendResponse(
        ObjectDetachMessage(
          pad.GUID,
          vehicle.GUID,
          pad.Position + Vector3.z(padDefinition.VehicleCreationZOffset),
          pad.Orientation.z + padDefinition.VehicleCreationZOrientOffset
        )
      )

    case VehicleSpawnPad.ResetSpawnPad(pad) =>
      sendResponse(GenericObjectActionMessage(pad.GUID, code=23))

    case VehicleSpawnPad.RevealPlayer(playerGuid) =>
      sendResponse(GenericObjectActionMessage(playerGuid, code=10))

    case VehicleSpawnPad.ServerVehicleOverrideEnd(vehicle, _) =>
      sessionLogic.vehicles.ServerVehicleOverrideStop(vehicle)

    case VehicleAction.ChangeLoadout(target, oldWeapons, _, oldInventory, _) =>
      //TODO when vehicle weapons can be changed without visual glitches, rewrite this
      continent.GUID(target).collect { case vehicle: Vehicle =>
        changeLoadoutDeleteOldEquipment(vehicle, oldWeapons, oldInventory)
      }
  }

  private def changeLoadoutDeleteOldEquipment(
                                               vehicle: Vehicle,
                                               oldWeapons: Iterable[(Equipment, PlanetSideGUID)],
                                               oldInventory: Iterable[(Equipment, PlanetSideGUID)]
                                             ): Unit = {
    vehicle.PassengerInSeat(player) match {
      case Some(seatNum) =>
        //participant: observe changes to equipment
        (oldWeapons ++ oldInventory).foreach {
          case (_, eguid) => sendResponse(ObjectDeleteMessage(eguid, unk1=0))
        }
        sessionLogic.mountResponse.updateWeaponAtSeatPosition(vehicle, seatNum)
      case None =>
        //observer: observe changes to external equipment
        oldWeapons.foreach { case (_, eguid) => sendResponse(ObjectDeleteMessage(eguid, unk1=0)) }
    }
  }
}

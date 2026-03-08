// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{SessionData, SessionVehicleHandlers, VehicleHandlerFunctions}
import net.psforever.objects.avatar.SpecialCarry
import net.psforever.objects.{GlobalDefinitions, Player, Tool, Vehicle, Vehicles}
import net.psforever.objects.equipment.{Equipment, JammableMountedWeapons, JammableUnit}
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.interior.Sidedness.OutsideOf
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.packet.game.{ChangeAmmoMessage, ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, ChatMsg, ChildObjectStateMessage, DeadState, DeployRequestMessage, DismountVehicleMsg, FrameVehicleStateMessage, GenericObjectActionMessage, HitHint, InventoryStateMessage, ObjectAttachMessage, ObjectCreateDetailedMessage, ObjectCreateMessage, ObjectDeleteMessage, ObjectDetachMessage, PlanetsideAttributeMessage, ReloadMessage, ServerVehicleOverrideMsg, VehicleStateMessage, WeaponDryFireMessage}
import net.psforever.services.Service
import net.psforever.services.base.envelope.GenericResponseEnvelope
import net.psforever.services.base.message.{ChangeAmmo, ChangeFireState_Start, ChangeFireState_Stop, EventResponse, GenericObjectAction, HintsAtAttacker, ObjectDelete, PlanetsideAttribute, ReloadTool, SendResponse, WeaponDryFire}
import net.psforever.services.local.support.CaptureFlagManager
import net.psforever.services.vehicle.{VehicleAction, VehicleStamp}
import net.psforever.types.{BailType, ChatMessageType, PlanetSideGUID, Vector3}

object VehicleHandlerLogic {
  def apply(ops: SessionVehicleHandlers): VehicleHandlerLogic = {
    new VehicleHandlerLogic(ops, ops.context)
  }
}

class VehicleHandlerLogic(val ops: SessionVehicleHandlers, implicit val context: ActorContext) extends VehicleHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  private val galaxyService: ActorRef = ops.galaxyService

  /**
   * na
   *
   * @param toChannel na
   * @param guid      na
   * @param reply     na
   */
  def handle(toChannel: String, guid: PlanetSideGUID, reply: EventResponse): Unit = {
    val resolvedPlayerGuid = if (player.HasGUID) {
      player.GUID
    } else {
      PlanetSideGUID(-1)
    }
    val isNotSameTarget = resolvedPlayerGuid != guid
    reply match {
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
        //llu destruction check
        if (player.Carrying.contains(SpecialCarry.CaptureFlag)) {
          continent
            .GUID(player.VehicleSeated)
            .collect { case vehicle: Vehicle =>
              CaptureFlagManager.ReasonToLoseFlagViolently(continent, sessionLogic.general.specialItemSlotGuid, vehicle)
            }
        }

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

      case ChangeFireState_Start(weaponGuid) if isNotSameTarget =>
        sendResponse(ChangeFireStateMessage_Start(weaponGuid))

      case ChangeFireState_Stop(weaponGuid) if isNotSameTarget =>
        sendResponse(ChangeFireStateMessage_Stop(weaponGuid))

      case ReloadTool(itemGuid) if isNotSameTarget =>
        sendResponse(ReloadMessage(itemGuid, ammo_clip=1, unk1=0))

      case ChangeAmmo(weapon_guid, weapon_slot, previous_guid, ammo_id, ammo_guid, ammo_data) if isNotSameTarget =>
        sendResponse(ObjectDetachMessage(weapon_guid, previous_guid, Vector3.Zero, 0))
        //TODO? sendResponse(ObjectDeleteMessage(previousAmmoGuid, 0))
        sendResponse(
          ObjectCreateMessage(
            ammo_id,
            ammo_guid,
            ObjectCreateMessageParent(weapon_guid, weapon_slot),
            ammo_data
          )
        )
        sendResponse(ChangeAmmoMessage(weapon_guid, 1))

      case WeaponDryFire(weaponGuid) if isNotSameTarget =>
        continent.GUID(weaponGuid).collect {
          case tool: Tool if tool.Magazine == 0 =>
            // check that the magazine is still empty before sending WeaponDryFireMessage
            // if it has been reloaded since then, other clients will not see it firing
            sendResponse(WeaponDryFireMessage(weaponGuid))
        }

      case VehicleAction.DismountVehicle(bailType, wasKickedByDriver) if isNotSameTarget =>
        sendResponse(DismountVehicleMsg(guid, bailType, wasKickedByDriver))

      case VehicleAction.MountVehicle(vehicleGuid, seat) if isNotSameTarget =>
        sendResponse(ObjectAttachMessage(vehicleGuid, guid, seat))

      case VehicleAction.DeployRequest(objectGuid, state, unk1, unk2, pos) if isNotSameTarget =>
        sendResponse(DeployRequestMessage(guid, objectGuid, state, unk1, unk2, pos))

      case SendResponse(msgs) =>
        msgs.foreach(sendResponse)

      case VehicleAction.EquipmentCreatedInSlot(pkt) if isNotSameTarget =>
        sendResponse(pkt)

      case GenericObjectAction(objectGuid, action) if isNotSameTarget =>
        sendResponse(GenericObjectActionMessage(objectGuid, action))

      case HintsAtAttacker(sourceGuid) if player.isAlive =>
        sendResponse(HitHint(sourceGuid, player.GUID))

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

      case VehicleAction.KickPassenger(_, wasKickedByDriver, vehicleGuid) if resolvedPlayerGuid == guid =>
        //seat number (first field) seems to be correct if passenger is kicked manually by driver
        //but always seems to return 4 if user is kicked by mount permissions changing
        sendResponse(DismountVehicleMsg(guid, BailType.Kicked, wasKickedByDriver))
        val typeOfRide = continent.GUID(vehicleGuid) match {
          case Some(obj: Vehicle) =>
            sessionLogic.general.unaccessContainer(obj)
            s"the ${obj.Definition.Name}'s seat by ${obj.OwnerName.getOrElse("the pilot")}"
          case _ =>
            s"${player.Sex.possessive} ride"
        }
        log.info(s"${player.Name} has been kicked from $typeOfRide!")
        player.WhichSide = OutsideOf

      case VehicleAction.KickPassenger(_, wasKickedByDriver, _) =>
        //seat number (first field) seems to be correct if passenger is kicked manually by driver
        //but always seems to return 4 if user is kicked by mount permissions changing
        sendResponse(DismountVehicleMsg(guid, BailType.Kicked, wasKickedByDriver))

      case VehicleAction.InventoryState2(objGuid, parentGuid, value) if isNotSameTarget =>
        sendResponse(InventoryStateMessage(objGuid, unk=0, parentGuid, value))

      case VehicleAction.LoadVehicle(vehicle, vtype, vguid, vdata) if isNotSameTarget =>
        //this is not be suitable for vehicles with people who are seated in it before it spawns (if that is possible)
        sendResponse(ObjectCreateMessage(vtype, vguid, vdata))
        Vehicles.ReloadAccessPermissions(vehicle, player.Name)

      case ObjectDelete(itemGuid, _) if isNotSameTarget =>
        sendResponse(ObjectDeleteMessage(itemGuid, unk1=0))

      case VehicleAction.Ownership(vehicleGuid) if resolvedPlayerGuid == guid =>
        //Only the player that owns this vehicle needs the ownership packet
        avatarActor ! AvatarActor.SetVehicle(Some(vehicleGuid))
        sendResponse(PlanetsideAttributeMessage(resolvedPlayerGuid, attribute_type=21, vehicleGuid))

      case VehicleAction.LoseOwnership(_, vehicleGuid) =>
        ops.announceAmsDecay(vehicleGuid,msg = "@ams_decaystarted")

      case PlanetsideAttribute(vehicleGuid, attributeType, attributeValue) if isNotSameTarget =>
        sendResponse(PlanetsideAttributeMessage(vehicleGuid, attributeType, attributeValue))

      case VehicleAction.SeatPermissions(vehicleGuid, seatGroup, permission) if isNotSameTarget =>
        sendResponse(PlanetsideAttributeMessage(vehicleGuid, seatGroup, permission))

      case VehicleAction.StowCreatedEquipment(vehicleGuid, slot, itemType, itemGuid, itemData) if isNotSameTarget =>
        //TODO prefer ObjectAttachMessage, but how to force ammo pools to update properly?
        sendResponse(ObjectCreateDetailedMessage(itemType, itemGuid, ObjectCreateMessageParent(vehicleGuid, slot), itemData))

      case VehicleAction.UnloadVehicle(_, vehicleGuid) =>
        sendResponse(ObjectDeleteMessage(vehicleGuid, unk1=1))
        if (sessionLogic.zoning.spawn.prevSpawnPoint.map(_.Owner).exists {
          case ams: Vehicle =>
            ams.GUID == vehicleGuid &&
              ams.OwnerGuid.isEmpty
          case _ =>
            false
        }) {
          sessionLogic.zoning.spawn.prevSpawnPoint = None
          sendResponse(ChatMsg(ChatMessageType.UNK_229, "@ams_decayed"))
        }

      case VehicleAction.UnstowEquipment(itemGuid) if isNotSameTarget =>
        //TODO prefer ObjectDetachMessage, but how to force ammo pools to update properly?
        sendResponse(ObjectDeleteMessage(itemGuid, unk1=0))

      case VehicleAction.UpdateAmsSpawnList(list) =>
        sessionLogic.zoning.spawn.amsSpawnPoints = list.filter(tube => tube.Faction == player.Faction)
        sessionLogic.zoning.spawn.DrawCurrentAmsSpawnPoint()

      case VehicleAction.TransferPassengerChannel(oldChannel, tempChannel, vehicle, vehicleToDelete) if isNotSameTarget =>
        sessionLogic.zoning.interstellarFerry = Some(vehicle)
        sessionLogic.zoning.interstellarFerryTopLevelGUID = Some(vehicleToDelete)
        continent.VehicleEvents ! Service.Leave(oldChannel) //old vehicle-specific channel (was s"${vehicle.Actor}")
        galaxyService ! Service.Join(tempChannel) //temporary vehicle-specific channel
        log.debug(s"TransferPassengerChannel: ${player.Name} now subscribed to $tempChannel for vehicle gating")

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
          toChannel,
          PlanetSideGUID(0),
          VehicleAction.KickCargo(vehicle, speed=0, delay)
        )
        context.system.scheduler.scheduleOnce(delay milliseconds, context.self, resp)

      case VehicleAction.KickCargo(cargo, _, _)
        if player.VehicleSeated.nonEmpty && sessionLogic.zoning.spawn.deadState == DeadState.Alive =>
        sessionLogic.vehicles.TotalDriverVehicleControl(cargo)

      case VehicleAction.ChangeLoadout(target, oldWeapons, addedWeapons, oldInventory, newInventory)
        if player.avatar.vehicle.contains(target) =>
        //TODO when vehicle weapons can be changed without visual glitches, rewrite this
        continent.GUID(target).collect { case vehicle: Vehicle =>
          import net.psforever.login.WorldSession.boolToInt
          //owner: must unregister old equipment, and register and install new equipment
          (oldWeapons ++ oldInventory).foreach {
            case (obj, eguid) =>
              sendResponse(ObjectDeleteMessage(eguid, unk1=0))
              TaskWorkflow.execute(GUIDTask.unregisterEquipment(continent.GUID, obj))
          }
          sessionLogic.general.applyPurchaseTimersBeforePackingLoadout(player, vehicle, addedWeapons ++ newInventory)
          //jammer or unjamm new weapons based on vehicle status
          val vehicleJammered = vehicle.Jammed
          addedWeapons
            .map { _.obj }
            .collect {
              case jamItem: JammableUnit if jamItem.Jammed != vehicleJammered =>
                jamItem.Jammed = vehicleJammered
                JammableMountedWeapons.JammedWeaponStatus(vehicle.Zone, jamItem, vehicleJammered)
            }
          changeLoadoutDeleteOldEquipment(vehicle, oldWeapons, oldInventory)
        }

      case VehicleAction.ChangeLoadout(target, oldWeapons, _, oldInventory, _)
        if sessionLogic.general.accessedContainer.map(_.GUID).contains(target) =>
        //TODO when vehicle weapons can be changed without visual glitches, rewrite this
        continent.GUID(target).collect { case vehicle: Vehicle =>
          //external participant: observe changes to equipment
          (oldWeapons ++ oldInventory).foreach { case (_, eguid) => sendResponse(ObjectDeleteMessage(eguid, unk1=0)) }
          changeLoadoutDeleteOldEquipment(vehicle, oldWeapons, oldInventory)
        }

      case VehicleAction.ChangeLoadout(target, oldWeapons, _, oldInventory, _) =>
        //TODO when vehicle weapons can be changed without visual glitches, rewrite this
        continent.GUID(target).collect { case vehicle: Vehicle =>
          changeLoadoutDeleteOldEquipment(vehicle, oldWeapons, oldInventory)
        }

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

      case VehicleSpawnPad.StartPlayerSeatedInVehicle(vehicle, _)
        if player.VisibleSlots.contains(player.DrawnSlot) =>
        player.DrawnSlot = Player.HandsDownSlot
        startPlayerSeatedInVehicle(vehicle)

      case VehicleSpawnPad.StartPlayerSeatedInVehicle(vehicle, _) =>
        startPlayerSeatedInVehicle(vehicle)

      case VehicleSpawnPad.PlayerSeatedInVehicle(vehicle, _) =>
        Vehicles.ReloadAccessPermissions(vehicle, player.Name)
        sessionLogic.vehicles.ServerVehicleOverrideWithPacket(
          vehicle,
          ServerVehicleOverrideMsg(
            lock_accelerator=true,
            lock_wheel=true,
            reverse=true,
            unk4=false,
            lock_vthrust=1,
            lock_strafe=0,
            movement_speed=0,
            unk8=Some(0)
          )
        )
        sessionLogic.vehicles.serverVehicleControlVelocity = Some(0)

      case VehicleSpawnPad.ServerVehicleOverrideStart(vehicle, _) =>
        val vdef = vehicle.Definition
        sessionLogic.vehicles.ServerVehicleOverrideWithPacket(
          vehicle,
          ServerVehicleOverrideMsg(
            lock_accelerator=true,
            lock_wheel=true,
            reverse=false,
            unk4=false,
            lock_vthrust=if (GlobalDefinitions.isFlightVehicle(vdef)) { 1 } else { 0 },
            lock_strafe=0,
            movement_speed=vdef.AutoPilotSpeed1,
            unk8=Some(0)
          )
        )

      case VehicleSpawnPad.ServerVehicleOverrideEnd(vehicle, _) =>
        sessionLogic.vehicles.ServerVehicleOverrideStop(vehicle)

      case VehicleSpawnPad.PeriodicReminder(VehicleSpawnPad.Reminders.Blocked, data) =>
        val str = s"${data.getOrElse("The vehicle spawn pad where you placed your order is blocked.")}"
        val msg = if (str.contains("@")) {
          ChatMsg(ChatMessageType.UNK_229, str)
        } else {
          ChatMsg(ChatMessageType.CMT_OPEN, wideContents = true, recipient = "", str, note = None)
        }
        sendResponse(msg)

      case VehicleSpawnPad.PeriodicReminder(_, data) =>
        val (isType, flag, msg): (ChatMessageType, Boolean, String) = data match {
          case Some(msg: String) if msg.startsWith("@") => (ChatMessageType.UNK_227, false, msg)
          case Some(msg: String) => (ChatMessageType.CMT_OPEN, true, msg)
          case _ => (ChatMessageType.CMT_OPEN, true, "Your vehicle order has been cancelled.")
        }
        sendResponse(ChatMsg(isType, flag, recipient="", msg, None))

      case _ => ()
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

  private def startPlayerSeatedInVehicle(vehicle: Vehicle): Unit = {
    val vehicle_guid = vehicle.GUID
    sessionLogic.actionsToCancel()
    sessionLogic.terminals.CancelAllProximityUnits()
    sessionLogic.vehicles.serverVehicleControlVelocity = Some(0)
    sendResponse(PlanetsideAttributeMessage(vehicle_guid, attribute_type=22, attribute_value=1L)) //mount points off
    sendResponse(PlanetsideAttributeMessage(player.GUID, attribute_type=21, vehicle_guid)) //ownership
    vehicle.MountPoints.find { case (_, mp) => mp.seatIndex == 0 }.collect {
      case (mountPoint, _) => vehicle.Actor ! Mountable.TryMount(player, mountPoint)
    }
  }
}

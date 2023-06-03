// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.objects.equipment.{Equipment, JammableMountedWeapons, JammableUnit}
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.{GlobalDefinitions, Player, Tool, Vehicle, Vehicles}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.packet.game._
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleResponse, VehicleServiceResponse}
import net.psforever.types.{BailType, ChatMessageType, PlanetSideGUID, Vector3}

import scala.concurrent.duration._

class SessionVehicleHandlers(
                              val sessionData: SessionData,
                              avatarActor: typed.ActorRef[AvatarActor.Command],
                              galaxyService: ActorRef,
                              implicit val context: ActorContext
                            ) extends CommonSessionInterfacingFunctionality {
  /**
   * na
   *
   * @param toChannel na
   * @param guid      na
   * @param reply     na
   */
  def handle(toChannel: String, guid: PlanetSideGUID, reply: VehicleResponse.Response): Unit = {
    val resolvedPlayerGuid = if (player.HasGUID) {
      player.GUID
    } else {
      PlanetSideGUID(-1)
    }
    val isNotSameTarget = resolvedPlayerGuid != guid
    reply match {
      case VehicleResponse.VehicleState(
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

      case VehicleResponse.VehicleState(
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

      case VehicleResponse.ChildObjectState(objectGuid, pitch, yaw) if isNotSameTarget =>
        sendResponse(ChildObjectStateMessage(objectGuid, pitch, yaw))

      case VehicleResponse.FrameVehicleState(vguid, u1, pos, oient, vel, u2, u3, u4, is_crouched, u6, u7, u8, u9, uA)
        if isNotSameTarget =>
        sendResponse(FrameVehicleStateMessage(vguid, u1, pos, oient, vel, u2, u3, u4, is_crouched, u6, u7, u8, u9, uA))

      case VehicleResponse.ChangeFireState_Start(weaponGuid) if isNotSameTarget =>
        sendResponse(ChangeFireStateMessage_Start(weaponGuid))

      case VehicleResponse.ChangeFireState_Stop(weaponGuid) if isNotSameTarget =>
        sendResponse(ChangeFireStateMessage_Stop(weaponGuid))

      case VehicleResponse.Reload(itemGuid) if isNotSameTarget =>
        sendResponse(ReloadMessage(itemGuid, ammo_clip=1, unk1=0))

      case VehicleResponse.ChangeAmmo(weapon_guid, weapon_slot, previous_guid, ammo_id, ammo_guid, ammo_data) if isNotSameTarget =>
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

      case VehicleResponse.WeaponDryFire(weaponGuid) if isNotSameTarget =>
        continent.GUID(weaponGuid).collect {
          case tool: Tool if tool.Magazine == 0 =>
            // check that the magazine is still empty before sending WeaponDryFireMessage
            // if it has been reloaded since then, other clients will not see it firing
            sendResponse(WeaponDryFireMessage(weaponGuid))
        }

      case VehicleResponse.DismountVehicle(bailType, wasKickedByDriver) if isNotSameTarget =>
        sendResponse(DismountVehicleMsg(guid, bailType, wasKickedByDriver))

      case VehicleResponse.MountVehicle(vehicleGuid, seat) if isNotSameTarget =>
        sendResponse(ObjectAttachMessage(vehicleGuid, guid, seat))

      case VehicleResponse.DeployRequest(objectGuid, state, unk1, unk2, pos) if isNotSameTarget =>
        sendResponse(DeployRequestMessage(guid, objectGuid, state, unk1, unk2, pos))

      case VehicleResponse.SendResponse(msg) =>
        sendResponse(msg)

      case VehicleResponse.AttachToRails(vehicleGuid, padGuid) =>
        sendResponse(ObjectAttachMessage(padGuid, vehicleGuid, slot=3))

      case VehicleResponse.ConcealPlayer(playerGuid) =>
        sendResponse(GenericObjectActionMessage(playerGuid, code=9))

      case VehicleResponse.DetachFromRails(vehicleGuid, padGuid, padPosition, padOrientationZ) =>
        val pad = continent.GUID(padGuid).get.asInstanceOf[VehicleSpawnPad].Definition
        sendResponse(
          ObjectDetachMessage(
            padGuid,
            vehicleGuid,
            padPosition + Vector3.z(pad.VehicleCreationZOffset),
            padOrientationZ + pad.VehicleCreationZOrientOffset
          )
        )

      case VehicleResponse.EquipmentInSlot(pkt) if isNotSameTarget =>
        sendResponse(pkt)

      case VehicleResponse.GenericObjectAction(objectGuid, action) if isNotSameTarget =>
        sendResponse(GenericObjectActionMessage(objectGuid, action))

      case VehicleResponse.HitHint(sourceGuid) if player.isAlive =>
        sendResponse(HitHint(sourceGuid, player.GUID))

      case VehicleResponse.InventoryState(obj, parentGuid, start, conData) if isNotSameTarget =>
        //TODO prefer ObjectDetachMessage, but how to force ammo pools to update properly?
        val objGuid = obj.GUID
        sendResponse(ObjectDeleteMessage(objGuid, unk1=0))
        sendResponse(ObjectCreateDetailedMessage(
          obj.Definition.ObjectId,
          objGuid,
          ObjectCreateMessageParent(parentGuid, start),
          conData
      ))

      case VehicleResponse.KickPassenger(_, wasKickedByDriver, vehicleGuid) if resolvedPlayerGuid == guid =>
        //seat number (first field) seems to be correct if passenger is kicked manually by driver
        //but always seems to return 4 if user is kicked by mount permissions changing
        sendResponse(DismountVehicleMsg(guid, BailType.Kicked, wasKickedByDriver))
        val typeOfRide = continent.GUID(vehicleGuid) match {
          case Some(obj: Vehicle) =>
            sessionData.unaccessContainer(obj)
            s"the ${obj.Definition.Name}'s seat by ${obj.OwnerName.getOrElse("the pilot")}"
          case _ =>
            s"${player.Sex.possessive} ride"
        }
        log.info(s"${player.Name} has been kicked from $typeOfRide!")

      case VehicleResponse.KickPassenger(_, wasKickedByDriver, _) =>
        //seat number (first field) seems to be correct if passenger is kicked manually by driver
        //but always seems to return 4 if user is kicked by mount permissions changing
        sendResponse(DismountVehicleMsg(guid, BailType.Kicked, wasKickedByDriver))

      case VehicleResponse.InventoryState2(objGuid, parentGuid, value) if isNotSameTarget =>
        sendResponse(InventoryStateMessage(objGuid, unk=0, parentGuid, value))

      case VehicleResponse.LoadVehicle(vehicle, vtype, vguid, vdata) if isNotSameTarget =>
        //this is not be suitable for vehicles with people who are seated in it before it spawns (if that is possible)
        sendResponse(ObjectCreateMessage(vtype, vguid, vdata))
        Vehicles.ReloadAccessPermissions(vehicle, player.Name)

      case VehicleResponse.ObjectDelete(itemGuid) if isNotSameTarget =>
        sendResponse(ObjectDeleteMessage(itemGuid, unk1=0))

      case VehicleResponse.Ownership(vehicleGuid) if resolvedPlayerGuid == guid =>
        //Only the player that owns this vehicle needs the ownership packet
        avatarActor ! AvatarActor.SetVehicle(Some(vehicleGuid))
        sendResponse(PlanetsideAttributeMessage(resolvedPlayerGuid, attribute_type=21, vehicleGuid))

      case VehicleResponse.PlanetsideAttribute(vehicleGuid, attributeType, attributeValue) if isNotSameTarget =>
        sendResponse(PlanetsideAttributeMessage(vehicleGuid, attributeType, attributeValue))

      case VehicleResponse.ResetSpawnPad(padGuid) =>
        sendResponse(GenericObjectActionMessage(padGuid, code=23))

      case VehicleResponse.RevealPlayer(playerGuid) =>
        sendResponse(GenericObjectActionMessage(playerGuid, code=10))

      case VehicleResponse.SeatPermissions(vehicleGuid, seatGroup, permission) if isNotSameTarget =>
        sendResponse(PlanetsideAttributeMessage(vehicleGuid, seatGroup, permission))

      case VehicleResponse.StowEquipment(vehicleGuid, slot, itemType, itemGuid, itemData) if isNotSameTarget =>
        //TODO prefer ObjectAttachMessage, but how to force ammo pools to update properly?
        sendResponse(ObjectCreateDetailedMessage(itemType, itemGuid, ObjectCreateMessageParent(vehicleGuid, slot), itemData))

      case VehicleResponse.UnloadVehicle(_, vehicleGuid) =>
        sendResponse(ObjectDeleteMessage(vehicleGuid, unk1=0))

      case VehicleResponse.UnstowEquipment(itemGuid) if isNotSameTarget =>
        //TODO prefer ObjectDetachMessage, but how to force ammo pools to update properly?
        sendResponse(ObjectDeleteMessage(itemGuid, unk1=0))

      case VehicleResponse.UpdateAmsSpawnPoint(list) =>
        sessionData.zoning.spawn.amsSpawnPoints = list.filter(tube => tube.Faction == player.Faction)
        sessionData.zoning.spawn.DrawCurrentAmsSpawnPoint()

      case VehicleResponse.TransferPassengerChannel(oldChannel, tempChannel, vehicle, vehicleToDelete) if isNotSameTarget =>
        sessionData.zoning.interstellarFerry = Some(vehicle)
        sessionData.zoning.interstellarFerryTopLevelGUID = Some(vehicleToDelete)
        continent.VehicleEvents ! Service.Leave(Some(oldChannel)) //old vehicle-specific channel (was s"${vehicle.Actor}")
        galaxyService ! Service.Join(tempChannel) //temporary vehicle-specific channel
        log.debug(s"TransferPassengerChannel: ${player.Name} now subscribed to $tempChannel for vehicle gating")

      case VehicleResponse.KickCargo(vehicle, speed, delay)
        if player.VehicleSeated.nonEmpty && sessionData.zoning.spawn.deadState == DeadState.Alive && speed > 0 =>
        val strafe = 1 + Vehicles.CargoOrientation(vehicle)
        val reverseSpeed = if (strafe > 1) { 0 } else { speed }
        //strafe or reverse, not both
        sessionData.vehicles.ServerVehicleOverrideWithPacket(
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
        context.system.scheduler.scheduleOnce(
          delay milliseconds,
          context.self,
          VehicleServiceResponse(toChannel, PlanetSideGUID(0), VehicleResponse.KickCargo(vehicle, speed=0, delay))
        )

      case VehicleResponse.KickCargo(cargo, _, _)
        if player.VehicleSeated.nonEmpty && sessionData.zoning.spawn.deadState == DeadState.Alive =>
        sessionData.vehicles.TotalDriverVehicleControl(cargo)

      case VehicleResponse.StartPlayerSeatedInVehicle(vehicle, _)
        if player.VisibleSlots.contains(player.DrawnSlot) =>
        player.DrawnSlot = Player.HandsDownSlot
        startPlayerSeatedInVehicle(vehicle)

      case VehicleResponse.StartPlayerSeatedInVehicle(vehicle, _) =>
        startPlayerSeatedInVehicle(vehicle)

      case VehicleResponse.PlayerSeatedInVehicle(vehicle, _) =>
        Vehicles.ReloadAccessPermissions(vehicle, player.Name)
        sessionData.vehicles.ServerVehicleOverrideWithPacket(
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
        sessionData.vehicles.serverVehicleControlVelocity = Some(0)

      case VehicleResponse.ServerVehicleOverrideStart(vehicle, _) =>
        val vdef = vehicle.Definition
        sessionData.vehicles.ServerVehicleOverrideWithPacket(
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

      case VehicleResponse.ServerVehicleOverrideEnd(vehicle, _) =>
        sessionData.vehicles.ServerVehicleOverrideStop(vehicle)

      case VehicleResponse.PeriodicReminder(VehicleSpawnPad.Reminders.Blocked, data) =>
        sendResponse(ChatMsg(
          ChatMessageType.CMT_OPEN,
          wideContents=true,
          recipient="",
          s"The vehicle spawn where you placed your order is blocked. ${data.getOrElse("")}",
          note=None
        ))

      case VehicleResponse.PeriodicReminder(_, data) =>
        val (isType, flag, msg): (ChatMessageType, Boolean, String) = data match {
          case Some(msg: String) if msg.startsWith("@") => (ChatMessageType.UNK_227, false, msg)
          case Some(msg: String) => (ChatMessageType.CMT_OPEN, true, msg)
          case _ => (ChatMessageType.CMT_OPEN, true, "Your vehicle order has been cancelled.")
        }
        sendResponse(ChatMsg(isType, flag, recipient="", msg, None))

      case VehicleResponse.ChangeLoadout(target, oldWeapons, addedWeapons, oldInventory, newInventory)
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
          sessionData.applyPurchaseTimersBeforePackingLoadout(player, vehicle, addedWeapons ++ newInventory)
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

      case VehicleResponse.ChangeLoadout(target, oldWeapons, _, oldInventory, _)
        if sessionData.accessedContainer.map { _.GUID }.contains(target) =>
        //TODO when vehicle weapons can be changed without visual glitches, rewrite this
        continent.GUID(target).collect { case vehicle: Vehicle =>
          //external participant: observe changes to equipment
          (oldWeapons ++ oldInventory).foreach { case (_, eguid) => sendResponse(ObjectDeleteMessage(eguid, unk1=0)) }
          changeLoadoutDeleteOldEquipment(vehicle, oldWeapons, oldInventory)
        }

      case VehicleResponse.ChangeLoadout(target, oldWeapons, _, oldInventory, _) =>
        //TODO when vehicle weapons can be changed without visual glitches, rewrite this
        continent.GUID(target).collect { case vehicle: Vehicle =>
          changeLoadoutDeleteOldEquipment(vehicle, oldWeapons, oldInventory)
        }

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
        sessionData.updateWeaponAtSeatPosition(vehicle, seatNum)
      case None =>
        //observer: observe changes to external equipment
        oldWeapons.foreach { case (_, eguid) => sendResponse(ObjectDeleteMessage(eguid, unk1=0)) }
    }
  }

  private def startPlayerSeatedInVehicle(vehicle: Vehicle): Unit = {
    val vehicle_guid = vehicle.GUID
    sessionData.playerActionsToCancel()
    sessionData.terminals.CancelAllProximityUnits()
    sessionData.vehicles.serverVehicleControlVelocity = Some(0)
    sendResponse(PlanetsideAttributeMessage(vehicle_guid, attribute_type=22, attribute_value=1L)) //mount points off
    sendResponse(PlanetsideAttributeMessage(player.GUID, attribute_type=21, vehicle_guid)) //ownership
    vehicle.MountPoints.find { case (_, mp) => mp.seatIndex == 0 }.collect {
      case (mountPoint, _) => vehicle.Actor ! Mountable.TryMount(player, mountPoint)
    }
  }
}

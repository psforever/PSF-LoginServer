// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.objects.equipment.{JammableMountedWeapons, JammableUnit}
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.{GlobalDefinitions, Player, Vehicle, Vehicles}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.packet.game._
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.vehicle.{VehicleResponse, VehicleServiceResponse}
import net.psforever.types.{BailType, ChatMessageType, PlanetSideGUID, Vector3}

import scala.concurrent.duration._

class SessionVehicleHandlers(
                              val sessionData: SessionData,
                              avatarActor: typed.ActorRef[AvatarActor.Command],
                              galaxyService: ActorRef,
                              implicit val context: ActorContext
                            ) extends CommonSessionInterfacingFuncs {
  /**
   * na
   *
   * @param toChannel na
   * @param guid      na
   * @param reply     na
   */
  def handle(toChannel: String, guid: PlanetSideGUID, reply: VehicleResponse.Response): Unit = {
    val tplayer_guid = if (player.HasGUID) player.GUID else PlanetSideGUID(0)
    reply match {
      case VehicleResponse.AttachToRails(vehicle_guid, pad_guid) =>
        sendResponse(ObjectAttachMessage(pad_guid, vehicle_guid, 3))

      case VehicleResponse.ChildObjectState(object_guid, pitch, yaw) =>
        if (tplayer_guid != guid) {
          sendResponse(ChildObjectStateMessage(object_guid, pitch, yaw))
        }

      case VehicleResponse.ConcealPlayer(player_guid) =>
        sendResponse(GenericObjectActionMessage(player_guid, 9))

      case VehicleResponse.DismountVehicle(bailType, wasKickedByDriver) =>
        if (tplayer_guid != guid) {
          sendResponse(DismountVehicleMsg(guid, bailType, wasKickedByDriver))
        }

      case VehicleResponse.DeployRequest(object_guid, state, unk1, unk2, pos) =>
        if (tplayer_guid != guid) {
          sendResponse(DeployRequestMessage(guid, object_guid, state, unk1, unk2, pos))
        }

      case VehicleResponse.DetachFromRails(vehicle_guid, pad_guid, pad_position, pad_orientation_z) =>
        val pad = continent.GUID(pad_guid).get.asInstanceOf[VehicleSpawnPad].Definition
        sendResponse(
          ObjectDetachMessage(
            pad_guid,
            vehicle_guid,
            pad_position + Vector3.z(pad.VehicleCreationZOffset),
            pad_orientation_z + pad.VehicleCreationZOrientOffset
          )
        )

      case VehicleResponse.EquipmentInSlot(pkt) =>
        if (tplayer_guid != guid) {
          sendResponse(pkt)
        }

      case VehicleResponse.FrameVehicleState(vguid, u1, pos, oient, vel, u2, u3, u4, is_crouched, u6, u7, u8, u9, uA) =>
        if (tplayer_guid != guid) {
          sendResponse(FrameVehicleStateMessage(vguid, u1, pos, oient, vel, u2, u3, u4, is_crouched, u6, u7, u8, u9, uA))
        }

      case VehicleResponse.GenericObjectAction(object_guid, action) =>
        if (tplayer_guid != guid) {
          sendResponse(GenericObjectActionMessage(object_guid, action))
        }

      case VehicleResponse.HitHint(source_guid) =>
        if (player.isAlive) {
          sendResponse(HitHint(source_guid, player.GUID))
        }

      case VehicleResponse.InventoryState(obj, parent_guid, start, con_data) =>
        if (tplayer_guid != guid) {
          //TODO prefer ObjectDetachMessage, but how to force ammo pools to update properly?
          val obj_guid = obj.GUID
          sendResponse(ObjectDeleteMessage(obj_guid, 0))
          sendResponse(
            ObjectCreateDetailedMessage(
              obj.Definition.ObjectId,
              obj_guid,
              ObjectCreateMessageParent(parent_guid, start),
              con_data
            )
          )
        }

      case VehicleResponse.KickPassenger(_, wasKickedByDriver, vehicle_guid) =>
        //seat number (first field) seems to be correct if passenger is kicked manually by driver
        //but always seems to return 4 if user is kicked by mount permissions changing
        sendResponse(DismountVehicleMsg(guid, BailType.Kicked, wasKickedByDriver))
        if (tplayer_guid == guid) {
          val typeOfRide = continent.GUID(vehicle_guid) match {
            case Some(obj: Vehicle) =>
              sessionData.UnaccessContainer(obj)
              s"the ${obj.Definition.Name}'s seat by ${obj.OwnerName.getOrElse("the pilot")}"
            case _ =>
              s"${player.Sex.possessive} ride"
          }
          log.info(s"${player.Name} has been kicked from $typeOfRide!")
        }

      case VehicleResponse.InventoryState2(obj_guid, parent_guid, value) =>
        if (tplayer_guid != guid) {
          sendResponse(InventoryStateMessage(obj_guid, 0, parent_guid, value))
        }

      case VehicleResponse.LoadVehicle(vehicle, vtype, vguid, vdata) =>
        //this is not be suitable for vehicles with people who are seated in it before it spawns (if that is possible)
        if (tplayer_guid != guid) {
          sendResponse(ObjectCreateMessage(vtype, vguid, vdata))
          Vehicles.ReloadAccessPermissions(vehicle, player.Name)
        }

      case VehicleResponse.MountVehicle(vehicle_guid, seat) =>
        if (tplayer_guid != guid) {
          sendResponse(ObjectAttachMessage(vehicle_guid, guid, seat))
        }

      case VehicleResponse.Ownership(vehicleGuid) =>
        if (tplayer_guid == guid) { // Only the player that owns this vehicle needs the ownership packet
          avatarActor ! AvatarActor.SetVehicle(Some(vehicleGuid))
          sendResponse(PlanetsideAttributeMessage(tplayer_guid, 21, vehicleGuid))
        }

      case VehicleResponse.PlanetsideAttribute(vehicle_guid, attribute_type, attribute_value) =>
        if (tplayer_guid != guid) {
          sendResponse(PlanetsideAttributeMessage(vehicle_guid, attribute_type, attribute_value))
        }

      case VehicleResponse.ResetSpawnPad(pad_guid) =>
        sendResponse(GenericObjectActionMessage(pad_guid, 23))

      case VehicleResponse.RevealPlayer(player_guid) =>
        sendResponse(GenericObjectActionMessage(player_guid, 10))

      case VehicleResponse.SeatPermissions(vehicle_guid, seat_group, permission) =>
        if (tplayer_guid != guid) {
          sendResponse(PlanetsideAttributeMessage(vehicle_guid, seat_group, permission))
        }

      case VehicleResponse.StowEquipment(vehicle_guid, slot, item_type, item_guid, item_data) =>
        if (tplayer_guid != guid) {
          //TODO prefer ObjectAttachMessage, but how to force ammo pools to update properly?
          sendResponse(
            ObjectCreateDetailedMessage(item_type, item_guid, ObjectCreateMessageParent(vehicle_guid, slot), item_data)
          )
        }

      case VehicleResponse.UnloadVehicle(_, vehicle_guid) =>
        sendResponse(ObjectDeleteMessage(vehicle_guid, 0))

      case VehicleResponse.UnstowEquipment(item_guid) =>
        if (tplayer_guid != guid) {
          //TODO prefer ObjectDetachMessage, but how to force ammo pools to update properly?
          sendResponse(ObjectDeleteMessage(item_guid, 0))
        }

      case VehicleResponse.VehicleState(
      vehicle_guid,
      unk1,
      pos,
      ang,
      vel,
      unk2,
      unk3,
      unk4,
      wheel_direction,
      unk5,
      unk6
      ) =>
        if (tplayer_guid != guid) {
          sendResponse(
            VehicleStateMessage(vehicle_guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel_direction, unk5, unk6)
          )
          if (player.VehicleSeated.contains(vehicle_guid)) {
            player.Position = pos
          }
        }
      case VehicleResponse.SendResponse(msg) =>
        sendResponse(msg)

      case VehicleResponse.UpdateAmsSpawnPoint(list) =>
        sessionData.spawn.amsSpawnPoints = list.filter(tube => tube.Faction == player.Faction)
        sessionData.spawn.DrawCurrentAmsSpawnPoint()

      case VehicleResponse.TransferPassengerChannel(old_channel, temp_channel, vehicle, vehicle_to_delete) =>
        if (tplayer_guid != guid) {
          sessionData.zoning.interstellarFerry = Some(vehicle)
          sessionData.zoning.interstellarFerryTopLevelGUID = Some(vehicle_to_delete)
          continent.VehicleEvents ! Service.Leave(
            Some(old_channel)
          ) //old vehicle-specific channel (was s"${vehicle.Actor}")
          galaxyService ! Service.Join(temp_channel) //temporary vehicle-specific channel
          log.debug(s"TransferPassengerChannel: ${player.Name} now subscribed to $temp_channel for vehicle gating")
        }

      case VehicleResponse.KickCargo(vehicle, speed, delay) =>
        if (player.VehicleSeated.nonEmpty && sessionData.spawn.deadState == DeadState.Alive) {
          if (speed > 0) {
            val strafe =
              if (Vehicles.CargoOrientation(vehicle) == 1) 2
              else 1
            val reverseSpeed =
              if (strafe > 1) 0
              else speed
            //strafe or reverse, not both
            sessionData.vehicles.serverVehicleControlVelocity = Some(reverseSpeed)
            sendResponse(ServerVehicleOverrideMsg(lock_accelerator=true, lock_wheel=true, reverse=true, unk4=false, 0, strafe, reverseSpeed, Some(0)))
            import scala.concurrent.ExecutionContext.Implicits.global
            context.system.scheduler.scheduleOnce(
              delay milliseconds,
              context.self,
              VehicleServiceResponse(toChannel, PlanetSideGUID(0), VehicleResponse.KickCargo(vehicle, 0, delay))
            )
          } else {
            sessionData.vehicles.serverVehicleControlVelocity = None
            sendResponse(ServerVehicleOverrideMsg(lock_accelerator=false,lock_wheel=false, reverse=false, unk4=false, 0, 0, 0, None))
          }
        }

      case VehicleResponse.StartPlayerSeatedInVehicle(vehicle, _) =>
        val vehicle_guid = vehicle.GUID
        sessionData.PlayerActionsToCancel()
        sessionData.vehicles.serverVehicleControlVelocity = Some(0)
        sessionData.terminals.CancelAllProximityUnits()
        if (player.VisibleSlots.contains(player.DrawnSlot)) {
          player.DrawnSlot = Player.HandsDownSlot
          sendResponse(ObjectHeldMessage(player.GUID, Player.HandsDownSlot, unk1 = true))
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.SendResponse(player.GUID, ObjectHeldMessage(player.GUID, player.LastDrawnSlot, unk1 = false))
          )
        }
        sendResponse(PlanetsideAttributeMessage(vehicle_guid, 22, 1L)) //mount points off
        sendResponse(PlanetsideAttributeMessage(player.GUID, 21, vehicle_guid)) //ownership
        avatarActor ! AvatarActor.UpdatePurchaseTime(vehicle.Definition)
        vehicle.MountPoints.find { case (_, mp) => mp.seatIndex == 0 } match {
          case Some((mountPoint, _)) => vehicle.Actor ! Mountable.TryMount(player, mountPoint)
          case _ => ;
        }

      case VehicleResponse.PlayerSeatedInVehicle(vehicle, _) =>
        val vehicle_guid = vehicle.GUID
        sendResponse(PlanetsideAttributeMessage(vehicle_guid, 22, 0L)) //mount points on
        Vehicles.ReloadAccessPermissions(vehicle, player.Name)
        sessionData.vehicles.ServerVehicleLock(vehicle)

      case VehicleResponse.ServerVehicleOverrideStart(vehicle, _) =>
        val vdef = vehicle.Definition
        sessionData.vehicles.ServerVehicleOverride(vehicle, vdef.AutoPilotSpeed1, if (GlobalDefinitions.isFlightVehicle(vdef)) 1 else 0)

      case VehicleResponse.ServerVehicleOverrideEnd(vehicle, _) =>
        sessionData.vehicles.DriverVehicleControl(vehicle, vehicle.Definition.AutoPilotSpeed2)

      case VehicleResponse.PeriodicReminder(VehicleSpawnPad.Reminders.Blocked, data) =>
        sendResponse(ChatMsg(
          ChatMessageType.CMT_OPEN,
          wideContents=true,
          "",
          s"The vehicle spawn where you placed your order is blocked. ${data.getOrElse("")}",
          None
        ))

      case VehicleResponse.PeriodicReminder(_, data) =>
        val (isType, flag, msg): (ChatMessageType, Boolean, String) = data match {
          case Some(msg: String)
            if msg.startsWith("@") => (ChatMessageType.UNK_227, false, msg)
          case Some(msg: String) => (ChatMessageType.CMT_OPEN, true, msg)
          case _ => (ChatMessageType.CMT_OPEN, true, "Your vehicle order has been cancelled.")
        }
        sendResponse(ChatMsg(isType, flag, "", msg, None))

      case VehicleResponse.ChangeLoadout(target, old_weapons, added_weapons, old_inventory, new_inventory) =>
        //TODO when vehicle weapons can be changed without visual glitches, rewrite this
        continent.GUID(target) match {
          case Some(vehicle: Vehicle) =>
            if (player.avatar.vehicle.contains(target)) {
              import net.psforever.login.WorldSession.boolToInt
              //owner: must unregister old equipment, and register and install new equipment
              (old_weapons ++ old_inventory).foreach {
                case (obj, eguid) =>
                  sendResponse(ObjectDeleteMessage(eguid, 0))
                  TaskWorkflow.execute(GUIDTask.unregisterEquipment(continent.GUID, obj))
              }
              sessionData.ApplyPurchaseTimersBeforePackingLoadout(player, vehicle, added_weapons ++ new_inventory)
              //jammer or unjamm new weapons based on vehicle status
              val vehicleJammered = vehicle.Jammed
              added_weapons
                .map {
                  _.obj
                }
                .collect {
                  case jamItem: JammableUnit if jamItem.Jammed != vehicleJammered =>
                    jamItem.Jammed = vehicleJammered
                    JammableMountedWeapons.JammedWeaponStatus(vehicle.Zone, jamItem, vehicleJammered)
                }
            } else if (sessionData.accessedContainer.map { _.GUID }.contains(target)) {
              //external participant: observe changes to equipment
              (old_weapons ++ old_inventory).foreach { case (_, eguid) => sendResponse(ObjectDeleteMessage(eguid, 0)) }
            }
            vehicle.PassengerInSeat(player) match {
              case Some(seatNum) =>
                //participant: observe changes to equipment
                (old_weapons ++ old_inventory).foreach {
                  case (_, eguid) => sendResponse(ObjectDeleteMessage(eguid, 0))
                }
                sessionData.UpdateWeaponAtSeatPosition(vehicle, seatNum)
              case None =>
                //observer: observe changes to external equipment
                old_weapons.foreach { case (_, eguid) => sendResponse(ObjectDeleteMessage(eguid, 0)) }
            }
          case _ => ;
        }

      case _ => ;
    }
  }
}

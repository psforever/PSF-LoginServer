// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, typed}
import scala.concurrent.duration._
//
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Player, Vehicle, Vehicles}
import net.psforever.objects.definition.{BasicDefinition, ObjectDefinition}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalMech
import net.psforever.objects.serverobject.turret.{FacilityTurret, WeaponTurret}
import net.psforever.objects.vehicles.AccessPermissionGroup
import net.psforever.packet.game.{ChatMsg, DelayedPathMountMsg, DismountVehicleMsg, GenericObjectActionMessage, ObjectAttachMessage, ObjectDetachMessage, PlanetsideAttributeMessage, PlayerStasisMessage, PlayerStateShiftMessage, ShiftState}
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{BailType, ChatMessageType, PlanetSideGUID}

class SessionMountHandlers(
                            val sessionData: SessionData,
                            avatarActor: typed.ActorRef[AvatarActor.Command],
                            implicit val context: ActorContext
                          ) extends CommonSessionInterfacingFunctionality {
  /**
   * na
   *
   * @param tplayer na
   * @param reply   na
   */
  def handle(tplayer: Player, reply: Mountable.Exchange): Unit = {
    reply match {
      case Mountable.CanMount(obj: ImplantTerminalMech, seat_number, _) =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        log.info(s"${player.Name} mounts an implant terminal")
        sessionData.terminals.CancelAllProximityUnits()
        MountingAction(tplayer, obj, seat_number)
        sessionData.keepAliveFunc = sessionData.KeepAlivePersistence

      case Mountable.CanMount(obj: Vehicle, seat_number, _) if obj.Definition == GlobalDefinitions.orbital_shuttle =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the orbital shuttle")
        sessionData.terminals.CancelAllProximityUnits()
        MountingAction(tplayer, obj, seat_number)
        sessionData.keepAliveFunc = sessionData.KeepAlivePersistence

      case Mountable.CanMount(obj: Vehicle, seat_number, _) =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the ${obj.Definition.Name} in ${
          obj.SeatPermissionGroup(seat_number) match {
            case Some(AccessPermissionGroup.Driver) => "the driver seat"
            case Some(seatType) => s"a $seatType seat (#$seat_number)"
            case None => "a seat"
          }
        }")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionData.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, 0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        if (obj.Definition == GlobalDefinitions.ant) {
          sendResponse(PlanetsideAttributeMessage(obj_guid, 45, obj.NtuCapacitorScaled))
        }
        if (obj.Definition.MaxCapacitor > 0) {
          sendResponse(PlanetsideAttributeMessage(obj_guid, 113, obj.Capacitor))
        }
        if (seat_number == 0) {
          if (obj.Definition == GlobalDefinitions.quadstealth) {
            //wraith cloak state matches the cloak state of the driver
            //phantasm doesn't uncloak if the driver is uncloaked and no other vehicle cloaks
            obj.Cloaked = tplayer.Cloaked
          }
          sendResponse(GenericObjectActionMessage(obj_guid, 11))
        } else if (obj.WeaponControlledFromSeat(seat_number).isEmpty) {
          sessionData.keepAliveFunc = sessionData.KeepAlivePersistence
        }
        sessionData.AccessContainer(obj)
        sessionData.UpdateWeaponAtSeatPosition(obj, seat_number)
        MountingAction(tplayer, obj, seat_number)

      case Mountable.CanMount(obj: FacilityTurret, seat_number, _) =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        if (!obj.isUpgrading) {
          log.info(s"${player.Name} mounts the ${obj.Definition.Name}")
          if (obj.Definition == GlobalDefinitions.vanu_sentry_turret) {
            obj.Zone.LocalEvents ! LocalServiceMessage(obj.Zone.id, LocalAction.SetEmpire(obj.GUID, player.Faction))
          }
          sendResponse(PlanetsideAttributeMessage(obj.GUID, 0, obj.Health))
          sessionData.UpdateWeaponAtSeatPosition(obj, seat_number)
          MountingAction(tplayer, obj, seat_number)
        } else {
          log.warn(
            s"MountVehicleMsg: ${tplayer.Name} wants to mount turret ${obj.GUID.guid}, but needs to wait until it finishes updating"
          )
        }

      case Mountable.CanMount(obj: PlanetSideGameObject with WeaponTurret, seat_number, _) =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the ${obj.Definition.asInstanceOf[BasicDefinition].Name}")
        sendResponse(PlanetsideAttributeMessage(obj.GUID, 0, obj.Health))
        sessionData.UpdateWeaponAtSeatPosition(obj, seat_number)
        MountingAction(tplayer, obj, seat_number)

      case Mountable.CanMount(obj: Mountable, _, _) =>
        log.warn(s"MountVehicleMsg: $obj is some mountable object and nothing will happen for ${player.Name}")

      case Mountable.CanDismount(obj: ImplantTerminalMech, seat_num, _) =>
        log.info(s"${tplayer.Name} dismounts the implant terminal")
        DismountAction(tplayer, obj, seat_num)

      case Mountable.CanDismount(obj: Vehicle, seat_num, mount_point)
        if obj.Definition == GlobalDefinitions.orbital_shuttle =>
        val pguid = player.GUID
        if (obj.MountedIn.nonEmpty) {
          //dismount to hart lobby
          log.info(s"${tplayer.Name} dismounts the orbital shuttle into the lobby")
          val sguid = obj.GUID
          val (pos, zang) = Vehicles.dismountShuttle(obj, mount_point)
          tplayer.Position = pos
          sendResponse(DelayedPathMountMsg(pguid, sguid, 60, u2=true))
          continent.LocalEvents ! LocalServiceMessage(
            continent.id,
            LocalAction.SendResponse(ObjectDetachMessage(sguid, pguid, pos, 0, 0, zang))
          )
        } else {
          log.info(s"${player.Name} is prepped for dropping")
          //get ready for orbital drop
          DismountAction(tplayer, obj, seat_num)
          continent.actor ! ZoneActor.RemoveFromBlockMap(player) //character doesn't need it
          //DismountAction(...) uses vehicle service, so use that service to coordinate the remainder of the messages
          continent.VehicleEvents ! VehicleServiceMessage(
            player.Name,
            VehicleAction.SendResponse(Service.defaultPlayerGUID, PlayerStasisMessage(pguid)) //the stasis message
          )
          //when the player dismounts, they will be positioned where the shuttle was when it disappeared in the sky
          //the player will fall to the ground and is perfectly vulnerable in this state
          //additionally, our player must exist in the current zone
          //having no in-game avatar target will throw us out of the map screen when deploying and cause softlock
          continent.VehicleEvents ! VehicleServiceMessage(
            player.Name,
            VehicleAction.SendResponse(
              Service.defaultPlayerGUID,
              PlayerStateShiftMessage(ShiftState(0, obj.Position, obj.Orientation.z, None)) //cower in the shuttle bay
            )
          )
          continent.VehicleEvents ! VehicleServiceMessage(
            continent.id,
            VehicleAction.SendResponse(pguid, GenericObjectActionMessage(pguid, 9)) //conceal the player
          )
        }
        sessionData.keepAliveFunc = sessionData.zoning.NormalKeepAlive

      case Mountable.CanDismount(obj: Vehicle, seat_num, _) if obj.Definition == GlobalDefinitions.droppod =>
        log.info(s"${tplayer.Name} has landed on ${continent.id}")
        sessionData.UnaccessContainer(obj)
        DismountAction(tplayer, obj, seat_num)
        obj.Actor ! Vehicle.Deconstruct()

      case Mountable.CanDismount(obj: Vehicle, seat_num, _) =>
        val player_guid: PlanetSideGUID = tplayer.GUID
        if (player_guid == player.GUID) {
          //disembarking self
          log.info(s"${player.Name} dismounts the ${obj.Definition.Name}'s ${
            obj.SeatPermissionGroup(seat_num) match {
              case Some(AccessPermissionGroup.Driver) => "driver seat"
              case Some(seatType) => s"$seatType seat (#$seat_num)"
              case None => "seat"
            }
          }")
          sessionData.vehicles.ConditionalDriverVehicleControl(obj)
          sessionData.UnaccessContainer(obj)
          DismountAction(tplayer, obj, seat_num)
        } else {
          continent.VehicleEvents ! VehicleServiceMessage(
            continent.id,
            VehicleAction.KickPassenger(player_guid, seat_num, unk2=true, obj.GUID)
          )
        }

      case Mountable.CanDismount(obj: PlanetSideGameObject with WeaponTurret, seat_num, _) =>
        log.info(s"${tplayer.Name} dismounts a ${obj.Definition.asInstanceOf[ObjectDefinition].Name}")
        DismountAction(tplayer, obj, seat_num)

      case Mountable.CanDismount(obj: Mountable, _, _) =>
        log.warn(s"DismountVehicleMsg: $obj is some dismountable object but nothing will happen for ${player.Name}")

      case Mountable.CanNotMount(obj: Vehicle, mount_point) =>
        log.warn(s"MountVehicleMsg: ${tplayer.Name} attempted to mount $obj's mount $mount_point, but was not allowed")
        obj.GetSeatFromMountPoint(mount_point) match {
          case Some(seatNum) if obj.SeatPermissionGroup(seatNum).contains(AccessPermissionGroup.Driver) =>
            sendResponse(
              ChatMsg(ChatMessageType.CMT_OPEN, wideContents=false, "", "You are not the driver of this vehicle.", None)
            )
          case _ =>
        }

      case Mountable.CanNotMount(obj: Mountable, mount_point) =>
        log.warn(s"MountVehicleMsg: ${tplayer.Name} attempted to mount $obj's mount $mount_point, but was not allowed")

      case Mountable.CanNotDismount(obj, seat_num) =>
        log.warn(
          s"DismountVehicleMsg: ${tplayer.Name} attempted to dismount $obj's mount $seat_num, but was not allowed"
        )
    }
  }

  /**
   * Common activities/procedure when a player mounts a valid object.
   * @param tplayer the player
   * @param obj the mountable object
   * @param seatNum the mount into which the player is mounting
   */
  def MountingAction(tplayer: Player, obj: PlanetSideGameObject with Mountable, seatNum: Int): Unit = {
    val player_guid: PlanetSideGUID = tplayer.GUID
    val obj_guid: PlanetSideGUID    = obj.GUID
    sessionData.PlayerActionsToCancel()
    avatarActor ! AvatarActor.DeactivateActiveImplants()
    avatarActor ! AvatarActor.SuspendStaminaRegeneration(3 seconds)
    sendResponse(ObjectAttachMessage(obj_guid, player_guid, seatNum))
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.MountVehicle(player_guid, obj_guid, seatNum)
    )
  }

  /**
   * Common activities/procedure when a player dismounts a valid mountable object.
   * @param tplayer the player
   * @param obj the mountable object
   * @param seatNum the mount out of which which the player is disembarking
   */
  def DismountAction(tplayer: Player, obj: PlanetSideGameObject with Mountable, seatNum: Int): Unit = {
    val player_guid: PlanetSideGUID = tplayer.GUID
    sessionData.keepAliveFunc = sessionData.zoning.NormalKeepAlive
    val bailType = if (tplayer.BailProtection) {
      BailType.Bailed
    } else {
      BailType.Normal
    }
    sendResponse(DismountVehicleMsg(player_guid, bailType, wasKickedByDriver = false))
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.DismountVehicle(player_guid, bailType, unk2=false)
    )
  }
}

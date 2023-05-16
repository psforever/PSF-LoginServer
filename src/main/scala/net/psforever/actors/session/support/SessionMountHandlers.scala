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
import net.psforever.types.{BailType, ChatMessageType, PlanetSideGUID, Vector3}

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
      case Mountable.CanMount(obj: ImplantTerminalMech, seatNumber, _) =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        log.info(s"${player.Name} mounts an implant terminal")
        sessionData.terminals.CancelAllProximityUnits()
        MountingAction(tplayer, obj, seatNumber)
        sessionData.keepAliveFunc = sessionData.keepAlivePersistence

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if obj.Definition == GlobalDefinitions.orbital_shuttle =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the orbital shuttle")
        sessionData.terminals.CancelAllProximityUnits()
        MountingAction(tplayer, obj, seatNumber)
        sessionData.keepAliveFunc = sessionData.keepAlivePersistence

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if obj.Definition == GlobalDefinitions.ant =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the driver seat of the ${obj.Definition.Name}")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionData.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=45, obj.NtuCapacitorScaled))
        sendResponse(GenericObjectActionMessage(obj_guid, code=11))
        sessionData.accessContainer(obj)
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if obj.Definition == GlobalDefinitions.quadstealth =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the driver seat of the ${obj.Definition.Name}")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionData.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        //exclusive to the wraith, cloak state matches the cloak state of the driver
        //phantasm doesn't uncloak if the driver is uncloaked and no other vehicle cloaks
        obj.Cloaked = tplayer.Cloaked
        sendResponse(GenericObjectActionMessage(obj_guid, code=11))
        sessionData.accessContainer(obj)
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if seatNumber == 0 && obj.Definition.MaxCapacitor > 0 =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the driver seat of the ${obj.Definition.Name}")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionData.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=113, obj.Capacitor))
        sendResponse(GenericObjectActionMessage(obj_guid, code=11))
        sessionData.accessContainer(obj)
        sessionData.updateWeaponAtSeatPosition(obj, seatNumber)
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if seatNumber == 0 =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the driver seat of the ${obj.Definition.Name}")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionData.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sendResponse(GenericObjectActionMessage(obj_guid, code=11))
        sessionData.accessContainer(obj)
        sessionData.updateWeaponAtSeatPosition(obj, seatNumber)
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if obj.Definition.MaxCapacitor > 0 =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts ${
          obj.SeatPermissionGroup(seatNumber) match {
            case Some(seatType) => s"a $seatType seat (#$seatNumber)"
            case None => "a seat"
          }
        } of the ${obj.Definition.Name}")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionData.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=113, obj.Capacitor))
        sessionData.accessContainer(obj)
        sessionData.updateWeaponAtSeatPosition(obj, seatNumber)
        sessionData.keepAliveFunc = sessionData.keepAlivePersistence
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _) =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the ${
          obj.SeatPermissionGroup(seatNumber) match {
            case Some(seatType) => s"a $seatType seat (#$seatNumber)"
            case None => "a seat"
          }
        } of the ${obj.Definition.Name}")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionData.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sessionData.accessContainer(obj)
        sessionData.updateWeaponAtSeatPosition(obj, seatNumber)
        sessionData.keepAliveFunc = sessionData.keepAlivePersistence
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: FacilityTurret, seatNumber, _)
        if obj.Definition == GlobalDefinitions.vanu_sentry_turret =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the ${obj.Definition.Name}")
        obj.Zone.LocalEvents ! LocalServiceMessage(obj.Zone.id, LocalAction.SetEmpire(obj.GUID, player.Faction))
        sendResponse(PlanetsideAttributeMessage(obj.GUID, attribute_type=0, obj.Health))
        sessionData.updateWeaponAtSeatPosition(obj, seatNumber)
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: FacilityTurret, seatNumber, _)
        if !obj.isUpgrading =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the ${obj.Definition.Name}")
        sendResponse(PlanetsideAttributeMessage(obj.GUID, attribute_type=0, obj.Health))
        sessionData.updateWeaponAtSeatPosition(obj, seatNumber)
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: FacilityTurret, _, _) =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.warn(
          s"MountVehicleMsg: ${tplayer.Name} wants to mount turret ${obj.GUID.guid}, but needs to wait until it finishes updating"
        )

      case Mountable.CanMount(obj: PlanetSideGameObject with WeaponTurret, seatNumber, _) =>
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the ${obj.Definition.asInstanceOf[BasicDefinition].Name}")
        sendResponse(PlanetsideAttributeMessage(obj.GUID, attribute_type=0, obj.Health))
        sessionData.updateWeaponAtSeatPosition(obj, seatNumber)
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Mountable, _, _) =>
        log.warn(s"MountVehicleMsg: $obj is some mountable object and nothing will happen for ${player.Name}")

      case Mountable.CanDismount(obj: ImplantTerminalMech, seatNum, _) =>
        log.info(s"${tplayer.Name} dismounts the implant terminal")
        DismountAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(obj: Vehicle, _, mountPoint)
        if obj.Definition == GlobalDefinitions.orbital_shuttle && obj.MountedIn.nonEmpty =>
        //dismount to hart lobby
        val pguid = player.GUID
        log.info(s"${tplayer.Name} dismounts the orbital shuttle into the lobby")
        val sguid = obj.GUID
        val (pos, zang) = Vehicles.dismountShuttle(obj, mountPoint)
        tplayer.Position = pos
        sendResponse(DelayedPathMountMsg(pguid, sguid, u1=60, u2=true))
        continent.LocalEvents ! LocalServiceMessage(
          continent.id,
          LocalAction.SendResponse(ObjectDetachMessage(sguid, pguid, pos, roll=0, pitch=0, zang))
        )
        sessionData.keepAliveFunc = sessionData.zoning.NormalKeepAlive

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if obj.Definition == GlobalDefinitions.orbital_shuttle =>
        //get ready for orbital drop
        val pguid = player.GUID
        val events = continent.VehicleEvents
        log.info(s"${player.Name} is prepped for dropping")
        DismountAction(tplayer, obj, seatNum)
        continent.actor ! ZoneActor.RemoveFromBlockMap(player) //character doesn't need it
        //DismountAction(...) uses vehicle service, so use that service to coordinate the remainder of the messages
        events ! VehicleServiceMessage(
          player.Name,
          VehicleAction.SendResponse(Service.defaultPlayerGUID, PlayerStasisMessage(pguid)) //the stasis message
        )
        //when the player dismounts, they will be positioned where the shuttle was when it disappeared in the sky
        //the player will fall to the ground and is perfectly vulnerable in this state
        //additionally, our player must exist in the current zone
        //having no in-game avatar target will throw us out of the map screen when deploying and cause softlock
        events ! VehicleServiceMessage(
          player.Name,
          VehicleAction.SendResponse(
            Service.defaultPlayerGUID,
            PlayerStateShiftMessage(ShiftState(unk=0, obj.Position, obj.Orientation.z, vel=None)) //cower in the shuttle bay
          )
        )
        events ! VehicleServiceMessage(
          continent.id,
          VehicleAction.SendResponse(pguid, GenericObjectActionMessage(pguid, code=9)) //conceal the player
        )
        sessionData.keepAliveFunc = sessionData.zoning.NormalKeepAlive

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if obj.Definition == GlobalDefinitions.droppod =>
        log.info(s"${tplayer.Name} has landed on ${continent.id}")
        sessionData.unaccessContainer(obj)
        DismountAction(tplayer, obj, seatNum)
        obj.Actor ! Vehicle.Deconstruct()

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if tplayer.GUID == player.GUID =>
        //disembarking self
        log.info(s"${player.Name} dismounts the ${obj.Definition.Name}'s ${
          obj.SeatPermissionGroup(seatNum) match {
            case Some(AccessPermissionGroup.Driver) => "driver seat"
            case Some(seatType) => s"$seatType seat (#$seatNum)"
            case None => "seat"
          }
        }")
        sessionData.vehicles.ConditionalDriverVehicleControl(obj)
        sessionData.unaccessContainer(obj)
        DismountVehicleAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(obj: Vehicle, seat_num, _) =>
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.KickPassenger(tplayer.GUID, seat_num, unk2=true, obj.GUID)
        )

      case Mountable.CanDismount(obj: PlanetSideGameObject with WeaponTurret, seatNum, _) =>
        log.info(s"${tplayer.Name} dismounts a ${obj.Definition.asInstanceOf[ObjectDefinition].Name}")
        DismountAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(obj: Mountable, _, _) =>
        log.warn(s"DismountVehicleMsg: $obj is some dismountable object but nothing will happen for ${player.Name}")

      case Mountable.CanNotMount(obj: Vehicle, mountPoint) =>
        log.warn(s"MountVehicleMsg: ${tplayer.Name} attempted to mount $obj's mount $mountPoint, but was not allowed")
        obj.GetSeatFromMountPoint(mountPoint).collect {
          case seatNum if obj.SeatPermissionGroup(seatNum).contains(AccessPermissionGroup.Driver) =>
            sendResponse(
              ChatMsg(ChatMessageType.CMT_OPEN, wideContents=false, recipient="", "You are not the driver of this vehicle.", note=None)
            )
        }

      case Mountable.CanNotMount(obj: Mountable, mountPoint) =>
        log.warn(s"MountVehicleMsg: ${tplayer.Name} attempted to mount $obj's mount $mountPoint, but was not allowed")

      case Mountable.CanNotDismount(obj, seatNum) =>
        log.warn(s"DismountVehicleMsg: ${tplayer.Name} attempted to dismount $obj's mount $seatNum, but was not allowed")
    }
  }

  /**
   * Common activities/procedure when a player mounts a valid object.
   * @param tplayer the player
   * @param obj the mountable object
   * @param seatNum the mount into which the player is mounting
   */
  def MountingAction(tplayer: Player, obj: PlanetSideGameObject with Mountable, seatNum: Int): Unit = {
    val playerGuid: PlanetSideGUID = tplayer.GUID
    val objGuid: PlanetSideGUID    = obj.GUID
    sessionData.playerActionsToCancel()
    avatarActor ! AvatarActor.DeactivateActiveImplants()
    avatarActor ! AvatarActor.SuspendStaminaRegeneration(3.seconds)
    sendResponse(ObjectAttachMessage(objGuid, playerGuid, seatNum))
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.MountVehicle(playerGuid, objGuid, seatNum)
    )
  }

  /**
   * Common activities/procedure when a player dismounts a valid mountable object.
   * @param tplayer the player
   * @param obj the mountable object
   * @param seatNum the mount out of which which the player is disembarking
   */
  def DismountVehicleAction(tplayer: Player, obj: PlanetSideGameObject with Mountable, seatNum: Int): Unit = {
    DismountAction(tplayer, obj, seatNum)
    //until vehicles maintain synchronized momentum without a driver
    obj match {
      case v: Vehicle
        if seatNum == 0 && Vector3.MagnitudeSquared(v.Velocity.getOrElse(Vector3.Zero)) > 0f =>
        sessionData.vehicles.serverVehicleControlVelocity.collect { _ =>
          sessionData.vehicles.ServerVehicleOverrideStop(v)
        }
        v.Velocity = Vector3.Zero
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.VehicleState(
            tplayer.GUID,
            v.GUID,
            unk1 = 0,
            v.Position,
            v.Orientation,
            vel = None,
            v.Flying,
            unk3 = 0,
            unk4 = 0,
            wheel_direction = 15,
            unk5 = false,
            unk6 = v.Cloaked
          )
        )
      case _ => ()
    }
  }

  /**
   * Common activities/procedure when a player dismounts a valid mountable object.
   * @param tplayer the player
   * @param obj the mountable object
   * @param seatNum the mount out of which which the player is disembarking
   */
  def DismountAction(tplayer: Player, obj: PlanetSideGameObject with Mountable, seatNum: Int): Unit = {
    val playerGuid: PlanetSideGUID = tplayer.GUID
    sessionData.keepAliveFunc = sessionData.zoning.NormalKeepAlive
    val bailType = if (tplayer.BailProtection) {
      BailType.Bailed
    } else {
      BailType.Normal
    }
    sendResponse(DismountVehicleMsg(playerGuid, bailType, wasKickedByDriver = false))
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.DismountVehicle(playerGuid, bailType, unk2 = false)
    )
  }
}

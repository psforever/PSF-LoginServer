// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import akka.actor.ActorContext
import net.psforever.actors.session.support.{MountHandlerFunctions, SessionData, SessionMountHandlers}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Player, Vehicle, Vehicles}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.environment.interaction.ResetAllEnvironmentInteractions
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.structures.WarpGate
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalMech
import net.psforever.objects.serverobject.turret.{FacilityTurret, WeaponTurret}
import net.psforever.objects.vehicles.AccessPermissionGroup
import net.psforever.objects.vital.InGameHistory
import net.psforever.packet.game.{ChatMsg, DelayedPathMountMsg, DismountVehicleCargoMsg, DismountVehicleMsg, GenericObjectActionMessage, MountVehicleCargoMsg, MountVehicleMsg, ObjectDetachMessage, PlanetsideAttributeMessage, PlayerStasisMessage, PlayerStateShiftMessage, ShiftState}
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{BailType, ChatMessageType, DriveState, PlanetSideGUID, Vector3}

object MountHandlerLogic {
  def apply(ops: SessionMountHandlers): MountHandlerLogic = {
    new MountHandlerLogic(ops, ops.context)
  }
}

class MountHandlerLogic(val ops: SessionMountHandlers, implicit val context: ActorContext) extends MountHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  /* packets */

  def handleMountVehicle(pkt: MountVehicleMsg): Unit = {
    //can only mount vehicle when not in csr spectator mode
    if (!player.spectator) {
      ops.handleMountVehicle(pkt)
    }
  }

  def handleDismountVehicle(pkt: DismountVehicleMsg): Unit = {
    //can't do this if we're not in vehicle, so also not csr spectator
    ops.handleDismountVehicle(pkt.copy(bailType = BailType.Bailed))
  }

  def handleMountVehicleCargo(pkt: MountVehicleCargoMsg): Unit = {
    //can't do this if we're not in vehicle, so also not csr spectator
    ops.handleMountVehicleCargo(pkt)
  }

  def handleDismountVehicleCargo(pkt: DismountVehicleCargoMsg): Unit = {
    //can't do this if we're not in vehicle, so also not csr spectator
    ops.handleDismountVehicleCargo(pkt.copy(bailed = true))
  }

  /* response handlers */

  /**
   * na
   *
   * @param tplayer na
   * @param reply   na
   */
  def handle(tplayer: Player, reply: Mountable.Exchange): Unit = {
    reply match {
      case Mountable.CanMount(obj: ImplantTerminalMech, seatNumber, _) =>
        sessionLogic.zoning.CancelZoningProcess()
        sessionLogic.terminals.CancelAllProximityUnits()
        ops.MountingAction(tplayer, obj, seatNumber)
        sessionLogic.keepAliveFunc = sessionLogic.keepAlivePersistenceFunc

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if obj.Definition == GlobalDefinitions.orbital_shuttle =>
        sessionLogic.zoning.CancelZoningProcess()
        sessionLogic.terminals.CancelAllProximityUnits()
        ops.MountingAction(tplayer, obj, seatNumber)
        sessionLogic.keepAliveFunc = sessionLogic.keepAlivePersistenceFunc

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if obj.Definition == GlobalDefinitions.ant =>
        sessionLogic.zoning.CancelZoningProcess()
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionLogic.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=45, obj.NtuCapacitorScaled))
        sendResponse(GenericObjectActionMessage(obj_guid, code=11))
        sessionLogic.general.accessContainer(obj)
        tplayer.Actor ! ResetAllEnvironmentInteractions
        ops.MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if obj.Definition == GlobalDefinitions.quadstealth =>
        sessionLogic.zoning.CancelZoningProcess()
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionLogic.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        //exclusive to the wraith, cloak state matches the cloak state of the driver
        //phantasm doesn't uncloak if the driver is uncloaked and no other vehicle cloaks
        obj.Cloaked = tplayer.Cloaked
        sendResponse(GenericObjectActionMessage(obj_guid, code=11))
        sessionLogic.general.accessContainer(obj)
        tplayer.Actor ! ResetAllEnvironmentInteractions
        ops.MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if seatNumber == 0 && obj.Definition.MaxCapacitor > 0 =>
        sessionLogic.zoning.CancelZoningProcess()
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionLogic.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=113, obj.Capacitor))
        sendResponse(GenericObjectActionMessage(obj_guid, code=11))
        sessionLogic.general.accessContainer(obj)
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        tplayer.Actor ! ResetAllEnvironmentInteractions
        ops.MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if seatNumber == 0 =>
        sessionLogic.zoning.CancelZoningProcess()
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionLogic.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sendResponse(GenericObjectActionMessage(obj_guid, code=11))
        sessionLogic.general.accessContainer(obj)
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        tplayer.Actor ! ResetAllEnvironmentInteractions
        ops.MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if obj.Definition.MaxCapacitor > 0 =>
        sessionLogic.zoning.CancelZoningProcess()
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionLogic.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=113, obj.Capacitor))
        sessionLogic.general.accessContainer(obj)
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        sessionLogic.keepAliveFunc = sessionLogic.keepAlivePersistenceFunc
        tplayer.Actor ! ResetAllEnvironmentInteractions
        ops.MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _) =>
        sessionLogic.zoning.CancelZoningProcess()
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionLogic.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sessionLogic.general.accessContainer(obj)
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        sessionLogic.keepAliveFunc = sessionLogic.keepAlivePersistenceFunc
        tplayer.Actor ! ResetAllEnvironmentInteractions
        ops.MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: FacilityTurret, seatNumber, _)
        if obj.Definition == GlobalDefinitions.vanu_sentry_turret =>
        sessionLogic.zoning.CancelZoningProcess()
        obj.Zone.LocalEvents ! LocalServiceMessage(obj.Zone.id, LocalAction.SetEmpire(obj.GUID, player.Faction))
        sendResponse(PlanetsideAttributeMessage(obj.GUID, attribute_type=0, obj.Health))
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        ops.MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: FacilityTurret, seatNumber, _)
        if !obj.isUpgrading || System.currentTimeMillis() - obj.CheckTurretUpgradeTime >= 1500L =>
        obj.setMiddleOfUpgrade(false)
        sessionLogic.zoning.CancelZoningProcess()
        sendResponse(PlanetsideAttributeMessage(obj.GUID, attribute_type=0, obj.Health))
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        ops.MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: FacilityTurret, _, _) =>
        sessionLogic.zoning.CancelZoningProcess()
        log.warn(
          s"MountVehicleMsg: ${tplayer.Name} wants to mount turret ${obj.GUID.guid}, but needs to wait until it finishes updating"
        )

      case Mountable.CanMount(obj: PlanetSideGameObject with FactionAffinity with WeaponTurret with InGameHistory, seatNumber, _) =>
        sessionLogic.zoning.CancelZoningProcess()
        sendResponse(PlanetsideAttributeMessage(obj.GUID, attribute_type=0, obj.Health))
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        ops.MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Mountable, _, _) =>
        log.warn(s"MountVehicleMsg: $obj is some kind of mountable object but nothing will happen for ${player.Name}")

      case Mountable.CanDismount(obj: ImplantTerminalMech, seatNum, _) =>
        ops.DismountAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(obj: Vehicle, _, mountPoint)
        if obj.Definition == GlobalDefinitions.orbital_shuttle && obj.MountedIn.nonEmpty =>
        //dismount to hart lobby
        val pguid = player.GUID
        val sguid = obj.GUID
        val (pos, zang) = Vehicles.dismountShuttle(obj, mountPoint)
        tplayer.Position = pos
        sendResponse(DelayedPathMountMsg(pguid, sguid, u1=60, u2=true))
        continent.LocalEvents ! LocalServiceMessage(
          continent.id,
          LocalAction.SendResponse(ObjectDetachMessage(sguid, pguid, pos, roll=0, pitch=0, zang))
        )
        sessionLogic.keepAliveFunc = sessionLogic.zoning.NormalKeepAlive

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if obj.Definition == GlobalDefinitions.orbital_shuttle =>
        //get ready for orbital drop
        val pguid = player.GUID
        val events = continent.VehicleEvents
        ops.DismountAction(tplayer, obj, seatNum)
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
        sessionLogic.keepAliveFunc = sessionLogic.zoning.NormalKeepAlive

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if obj.Definition == GlobalDefinitions.droppod =>
        sessionLogic.general.unaccessContainer(obj)
        ops.DismountAction(tplayer, obj, seatNum)
        obj.Actor ! Vehicle.Deconstruct()

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if tplayer.GUID == player.GUID &&
          obj.isFlying &&
          obj.SeatPermissionGroup(seatNum).contains(AccessPermissionGroup.Driver) =>
        // Deconstruct the vehicle if the driver has bailed out and the vehicle is capable of flight
        //todo: implement auto landing procedure if the pilot bails but passengers are still present instead of deconstructing the vehicle
        //todo: continue flight path until aircraft crashes if no passengers present (or no passenger seats), then deconstruct.
        //todo: kick cargo passengers out. To be added after PR #216 is merged
        ops.DismountVehicleAction(tplayer, obj, seatNum)
        obj.Actor ! Vehicle.Deconstruct(None) //immediate deconstruction

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if tplayer.GUID == player.GUID =>
        ops.DismountVehicleAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(obj: Vehicle, seat_num, _) =>
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.KickPassenger(tplayer.GUID, seat_num, unk2=true, obj.GUID)
        )

      case Mountable.CanDismount(obj: PlanetSideGameObject with PlanetSideGameObject with Mountable with FactionAffinity with InGameHistory, seatNum, _) =>
        ops.DismountAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(obj: Mountable, _, _) =>
        log.warn(s"DismountVehicleMsg: $obj is some dismountable object but nothing will happen for ${player.Name}")

      case Mountable.CanNotMount(obj: Vehicle, seatNumber) =>
        log.warn(s"MountVehicleMsg: ${tplayer.Name} attempted to mount $obj's seat $seatNumber, but was not allowed")
        obj.GetSeatFromMountPoint(seatNumber).collect {
          case seatNum if obj.SeatPermissionGroup(seatNum).contains(AccessPermissionGroup.Driver) =>
            sendResponse(
              ChatMsg(ChatMessageType.CMT_OPEN, wideContents=false, recipient="", "You are not the driver of this vehicle.", note=None)
            )
        }

      case Mountable.CanNotMount(obj: Mountable, seatNumber) =>
        log.warn(s"MountVehicleMsg: ${tplayer.Name} attempted to mount $obj's seat $seatNumber, but was not allowed")

      case Mountable.CanNotDismount(obj: Vehicle, _, BailType.Normal)
        if obj.DeploymentState == DriveState.AutoPilot =>
        if (!player.spectator) {
          sendResponse(ChatMsg(ChatMessageType.UNK_224, "@SA_CannotDismountAtThisTime"))
        }

      case Mountable.CanNotDismount(obj: Vehicle, _, BailType.Bailed)
        if obj.Definition == GlobalDefinitions.droppod =>
        if (!player.spectator) {
          sendResponse(ChatMsg(ChatMessageType.UNK_224, "@CannotBailFromDroppod"))
        }

      case Mountable.CanNotDismount(obj: Vehicle, _, BailType.Bailed)
        if obj.DeploymentState == DriveState.AutoPilot =>
        if (!player.spectator) {
          sendResponse(ChatMsg(ChatMessageType.UNK_224, "@SA_CannotBailAtThisTime"))
        }

      case Mountable.CanNotDismount(obj: Vehicle, _, BailType.Bailed)
        if obj.Health <= (obj.MaxHealth * .35).round && GlobalDefinitions.isFlightVehicle(obj.Definition) =>
        sendResponse(ChatMsg(ChatMessageType.UNK_224, "@BailingMechanismFailure_Pilot"))

      case Mountable.CanNotDismount(obj: Vehicle, _, BailType.Bailed)
        if GlobalDefinitions.isFlightVehicle(obj.Definition) && {
          continent
            .blockMap
            .sector(obj)
            .buildingList
            .exists {
              case wg: WarpGate =>
                Vector3.DistanceSquared(obj.Position, wg.Position) < math.pow(wg.Definition.SOIRadius, 2)
              case _ =>
                false
            }
        } =>
        if (!player.spectator) {
          sendResponse(ChatMsg(ChatMessageType.UNK_227, "@Vehicle_CannotBailInWarpgateEnvelope"))
        }

      case Mountable.CanNotDismount(obj: Vehicle, _, _)
        if obj.isMoving(test = 1f) =>
        ops.handleDismountVehicle(DismountVehicleMsg(player.GUID, BailType.Bailed, wasKickedByDriver=true))
        if (!player.spectator) {
          sendResponse(ChatMsg(ChatMessageType.UNK_224, "@TooFastToDismount"))
        }

      case Mountable.CanNotDismount(obj, seatNum, _) =>
        log.warn(s"DismountVehicleMsg: ${tplayer.Name} attempted to dismount $obj's mount $seatNum, but was not allowed")
    }
  }

  /* support functions */
}

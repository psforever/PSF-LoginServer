// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.ActorContext
import net.psforever.actors.session.support.{LocalHandlerFunctions, SessionData, SessionLocalHandlers}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.vehicles.MountableWeapons
import net.psforever.objects.{BoomerDeployable, ExplosiveDeployable, TelepadDeployable, Tool, TurretDeployable}
import net.psforever.packet.game.{ChatMsg, DeployableObjectsInfoMessage, GenericActionMessage, GenericObjectActionMessage, GenericObjectStateMsg, HackMessage, HackState, HackState1, InventoryStateMessage, ObjectAttachMessage, ObjectCreateMessage, ObjectDeleteMessage, ObjectDetachMessage, OrbitalShuttleTimeMsg, PadAndShuttlePair, PlanetsideAttributeMessage, ProximityTerminalUseMessage, SetEmpireMessage, TriggerEffectMessage, TriggerSoundMessage, TriggeredSound, VehicleStateMessage}
import net.psforever.services.{InterstellarClusterService, Service}
import net.psforever.services.local.LocalResponse
import net.psforever.types.{ChatMessageType, PlanetSideGUID, SpawnGroup}

object LocalHandlerLogic {
  def apply(ops: SessionLocalHandlers): LocalHandlerLogic = {
    new LocalHandlerLogic(ops, ops.context)
  }
}

class LocalHandlerLogic(val ops: SessionLocalHandlers, implicit val context: ActorContext) extends LocalHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  /* messages */

  def handleTurretDeployableIsDismissed(obj: TurretDeployable): Unit = {
    ops.handleTurretDeployableIsDismissed(obj)
  }

  def handleDeployableIsDismissed(obj: Deployable): Unit = {
    ops.handleDeployableIsDismissed(obj)
  }

  /* response handlers */

  /**
   * na
   * @param toChannel na
   * @param guid      na
   * @param reply     na
   */
  def handle(toChannel: String, guid: PlanetSideGUID, reply: LocalResponse.Response): Unit = {
    val resolvedPlayerGuid = if (player.HasGUID) {
      player.GUID
    } else {
      Service.defaultPlayerGUID
    }
    val isNotSameTarget = resolvedPlayerGuid != guid
    reply match {
      case LocalResponse.DeployableMapIcon(behavior, deployInfo) if isNotSameTarget =>
        sendResponse(DeployableObjectsInfoMessage(behavior, deployInfo))

      case LocalResponse.DeployableUIFor(item) =>
        sessionLogic.general.updateDeployableUIElements(avatar.deployables.UpdateUIElement(item))

      case LocalResponse.Detonate(dguid, _: BoomerDeployable) =>
        sendResponse(TriggerEffectMessage(dguid, "detonate_boomer"))
        sendResponse(PlanetsideAttributeMessage(dguid, attribute_type=29, attribute_value=1))
        sendResponse(ObjectDeleteMessage(dguid, unk1=0))

      case LocalResponse.Detonate(dguid, _: ExplosiveDeployable) =>
        sendResponse(GenericObjectActionMessage(dguid, code=19))
        sendResponse(PlanetsideAttributeMessage(dguid, attribute_type=29, attribute_value=1))
        sendResponse(ObjectDeleteMessage(dguid, unk1=0))

      case LocalResponse.Detonate(_, obj) =>
        log.warn(s"LocalResponse.Detonate: ${obj.Definition.Name} not configured to explode correctly")

      case LocalResponse.DoorOpens(doorGuid) if isNotSameTarget =>
        val pos = player.Position.xy
        val range = ops.doorLoadRange()
        val foundDoor = continent
          .blockMap
          .sector(pos, range)
          .amenityList
          .collect { case door: Door => door }
          .find(_.GUID == doorGuid)
        val doorExistsInRange: Boolean = foundDoor.nonEmpty
        if (doorExistsInRange) {
          sendResponse(GenericObjectStateMsg(doorGuid, state=16))
        }

      case LocalResponse.DoorCloses(doorGuid) => //door closes for everyone
        sendResponse(GenericObjectStateMsg(doorGuid, state=17))

      case LocalResponse.EliminateDeployable(obj: TurretDeployable, dguid, _, _) if obj.Destroyed =>
        sendResponse(ObjectDeleteMessage(dguid, unk1=0))

      case LocalResponse.EliminateDeployable(obj: TurretDeployable, dguid, pos, _) =>
        obj.Destroyed = true
        ops.DeconstructDeployable(
          obj,
          dguid,
          pos,
          obj.Orientation,
          deletionType= if (obj.MountPoints.isEmpty) { 2 } else { 1 }
        )

      case LocalResponse.EliminateDeployable(obj: ExplosiveDeployable, dguid, _, _)
        if obj.Destroyed || obj.Jammed || obj.Health == 0 =>
        sendResponse(ObjectDeleteMessage(dguid, unk1=0))

      case LocalResponse.EliminateDeployable(obj: ExplosiveDeployable, dguid, pos, effect) =>
        obj.Destroyed = true
        ops.DeconstructDeployable(obj, dguid, pos, obj.Orientation, effect)

      case LocalResponse.EliminateDeployable(obj: TelepadDeployable, dguid, _, _) if obj.Active && obj.Destroyed =>
        //if active, deactivate
        obj.Active = false
        ops.deactivateTelpadDeployableMessages(dguid)
        //standard deployable elimination behavior
        sendResponse(ObjectDeleteMessage(dguid, unk1=0))

      case LocalResponse.EliminateDeployable(obj: TelepadDeployable, dguid, pos, _) if obj.Active =>
        //if active, deactivate
        obj.Active = false
        ops.deactivateTelpadDeployableMessages(dguid)
        //standard deployable elimination behavior
        obj.Destroyed = true
        ops.DeconstructDeployable(obj, dguid, pos, obj.Orientation, deletionType=2)

      case LocalResponse.EliminateDeployable(obj: TelepadDeployable, dguid, _, _) if obj.Destroyed =>
        //standard deployable elimination behavior
        sendResponse(ObjectDeleteMessage(dguid, unk1=0))

      case LocalResponse.EliminateDeployable(obj: TelepadDeployable, dguid, pos, _) =>
        //standard deployable elimination behavior
        obj.Destroyed = true
        ops.DeconstructDeployable(obj, dguid, pos, obj.Orientation, deletionType=2)

      case LocalResponse.EliminateDeployable(obj, dguid, _, _) if obj.Destroyed =>
        sendResponse(ObjectDeleteMessage(dguid, unk1=0))

      case LocalResponse.EliminateDeployable(obj, dguid, pos, effect) =>
        obj.Destroyed = true
        ops.DeconstructDeployable(obj, dguid, pos, obj.Orientation, effect)

      case LocalResponse.SendHackMessageHackCleared(targetGuid, unk1, unk2) =>
        sendResponse(HackMessage(HackState1.Unk0, targetGuid, guid, progress=0, unk1.toFloat, HackState.HackCleared, unk2))

      case LocalResponse.HackObject(targetGuid, unk1, unk2) =>
        sessionLogic.general.hackObject(targetGuid, unk1, unk2)

      case LocalResponse.PlanetsideAttribute(targetGuid, attributeType, attributeValue) =>
        sessionLogic.general.sendPlanetsideAttributeMessage(targetGuid, attributeType, attributeValue)

      case LocalResponse.GenericObjectAction(targetGuid, actionNumber) =>
        sendResponse(GenericObjectActionMessage(targetGuid, actionNumber))

      case LocalResponse.GenericActionMessage(actionNumber) =>
        sendResponse(GenericActionMessage(actionNumber))

      case LocalResponse.ChatMessage(msg) =>
        sendResponse(msg)

      case LocalResponse.SendPacket(packet) =>
        sendResponse(packet)

      case LocalResponse.LluSpawned(llu) =>
        // Create LLU on client
        sendResponse(ObjectCreateMessage(
          llu.Definition.ObjectId,
          llu.GUID,
          llu.Definition.Packet.ConstructorData(llu).get
        ))
        sendResponse(TriggerSoundMessage(TriggeredSound.LLUMaterialize, llu.Position, unk=20, volume=0.8000001f))

      case LocalResponse.LluDespawned(lluGuid, position) =>
        sendResponse(TriggerSoundMessage(TriggeredSound.LLUDeconstruct, position, unk=20, volume=0.8000001f))
        sendResponse(ObjectDeleteMessage(lluGuid, unk1=0))
        // If the player was holding the LLU, remove it from their tracked special item slot
        sessionLogic.general.specialItemSlotGuid.collect { case guid if guid == lluGuid =>
          sessionLogic.general.specialItemSlotGuid = None
          player.Carrying = None
        }

      case LocalResponse.ObjectDelete(objectGuid, unk) if isNotSameTarget =>
        sendResponse(ObjectDeleteMessage(objectGuid, unk))

      case LocalResponse.ProximityTerminalEffect(object_guid, true) =>
        sendResponse(ProximityTerminalUseMessage(Service.defaultPlayerGUID, object_guid, unk=true))

      case LocalResponse.ProximityTerminalEffect(objectGuid, false) =>
        sendResponse(ProximityTerminalUseMessage(Service.defaultPlayerGUID, objectGuid, unk=false))
        sessionLogic.terminals.ForgetAllProximityTerminals(objectGuid)

      case LocalResponse.RouterTelepadMessage(msg) =>
        sendResponse(ChatMsg(ChatMessageType.UNK_229, wideContents=false, recipient="", msg, note=None))

      case LocalResponse.RouterTelepadTransport(passengerGuid, srcGuid, destGuid) =>
        sessionLogic.general.useRouterTelepadEffect(passengerGuid, srcGuid, destGuid)

      case LocalResponse.SendResponse(msg) =>
        sendResponse(msg)

      case LocalResponse.SetEmpire(objectGuid, empire) =>
        sendResponse(SetEmpireMessage(objectGuid, empire))

      case LocalResponse.ShuttleEvent(ev) =>
        val msg = OrbitalShuttleTimeMsg(
          ev.u1,
          ev.u2,
          ev.t1,
          ev.t2,
          ev.t3,
          pairs=ev.pairs.map { case ((a, b), c) => PadAndShuttlePair(a, b, c) }
        )
        sendResponse(msg)

      case LocalResponse.ShuttleDock(pguid, sguid, slot) =>
        sendResponse(ObjectAttachMessage(pguid, sguid, slot))

      case LocalResponse.ShuttleUndock(pguid, sguid, pos, orient) =>
        sendResponse(ObjectDetachMessage(pguid, sguid, pos, orient))

      case LocalResponse.ShuttleState(sguid, pos, orient, state) =>
        sendResponse(VehicleStateMessage(sguid, unk1=0, pos, orient, vel=None, Some(state), unk3=0, unk4=0, wheel_direction=15, is_decelerating=false, is_cloaked=false))

      case LocalResponse.ToggleTeleportSystem(router, systemPlan) =>
        sessionLogic.general.toggleTeleportSystem(router, systemPlan)

      case LocalResponse.TriggerEffect(targetGuid, effect, effectInfo, triggerLocation) =>
        sendResponse(TriggerEffectMessage(targetGuid, effect, effectInfo, triggerLocation))

      case LocalResponse.TriggerSound(sound, pos, unk, volume) =>
        sendResponse(TriggerSoundMessage(sound, pos, unk, volume))

      case LocalResponse.UpdateForceDomeStatus(buildingGuid, true) =>
        sendResponse(GenericObjectActionMessage(buildingGuid, 11))

      case LocalResponse.UpdateForceDomeStatus(buildingGuid, false) =>
        sendResponse(GenericObjectActionMessage(buildingGuid, 12))

      case LocalResponse.RechargeVehicleWeapon(vehicleGuid, weaponGuid) if resolvedPlayerGuid == guid =>
        continent.GUID(vehicleGuid)
          .collect { case vehicle: MountableWeapons => (vehicle, vehicle.PassengerInSeat(player)) }
          .collect { case (vehicle, Some(seat_num)) => vehicle.WeaponControlledFromSeat(seat_num) }
          .getOrElse(Set.empty)
          .collect { case weapon: Tool if weapon.GUID == weaponGuid =>
            sendResponse(InventoryStateMessage(weapon.AmmoSlot.Box.GUID, weapon.GUID, weapon.Magazine))
          }

      case LocalResponse.ForceZoneChange(zone) =>
        //todo we might be able to piggyback this for squad recalls later
        if(session.zone eq zone) {
          sessionLogic.zoning.zoneReload = true
          zone.AvatarEvents ! Service.Leave()
          zone.LocalEvents ! Service.Leave()
          zone.VehicleEvents ! Service.Leave()
          zone.AvatarEvents ! Service.Join(player.Name) //must manually restore this subscriptions
          sessionLogic.zoning.spawn.handleNewPlayerLoaded(player) //will restart subscriptions and dispatch a LoadMapMessage
        } else {
          import akka.actor.typed.scaladsl.adapter._
          sessionLogic.cluster ! InterstellarClusterService.GetRandomSpawnPoint(
            zone.Number,
            player.Faction,
            Seq(SpawnGroup.Facility, SpawnGroup.Tower, SpawnGroup.AMS),
            context.self
          )
        }

      case _ => ()
    }
  }

  /* support functions */
}

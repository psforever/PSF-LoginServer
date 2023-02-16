// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.ActorContext
import net.psforever.objects.ce.Deployable
import net.psforever.objects.vehicles.MountableWeapons
import net.psforever.objects._
import net.psforever.packet.game.PlanetsideAttributeEnum.PlanetsideAttributeEnum
import net.psforever.packet.game._
import net.psforever.services.Service
import net.psforever.services.local.LocalResponse
import net.psforever.types.{ChatMessageType, PlanetSideGUID, Vector3}

class SessionLocalHandlers(
                            val sessionData: SessionData,
                            implicit val context: ActorContext
                          ) extends CommonSessionInterfacingFunctionality {
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
        sessionData.updateDeployableUIElements(avatar.deployables.UpdateUIElement(item))

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
        sendResponse(GenericObjectStateMsg(doorGuid, state=16))

      case LocalResponse.DoorCloses(doorGuid) => //door closes for everyone
        sendResponse(GenericObjectStateMsg(doorGuid, state=17))

      case LocalResponse.EliminateDeployable(obj: TurretDeployable, dguid, _, _) if obj.Destroyed =>
        sendResponse(ObjectDeleteMessage(dguid, unk1=0))

      case LocalResponse.EliminateDeployable(obj: TurretDeployable, dguid, pos, _) =>
        obj.Destroyed = true
        DeconstructDeployable(
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
        DeconstructDeployable(obj, dguid, pos, obj.Orientation, effect)

      case LocalResponse.EliminateDeployable(obj: TelepadDeployable, dguid, _, _) if obj.Active && obj.Destroyed =>
        //if active, deactivate
        obj.Active = false
        sendResponse(GenericObjectActionMessage(dguid, code=29))
        sendResponse(GenericObjectActionMessage(dguid, code=30))
        //standard deployable elimination behavior
        sendResponse(ObjectDeleteMessage(dguid, unk1=0))

      case LocalResponse.EliminateDeployable(obj: TelepadDeployable, dguid, pos, _) if obj.Active =>
        //if active, deactivate
        obj.Active = false
        sendResponse(GenericObjectActionMessage(dguid, code=29))
        sendResponse(GenericObjectActionMessage(dguid, code=30))
        //standard deployable elimination behavior
        obj.Destroyed = true
        DeconstructDeployable(obj, dguid, pos, obj.Orientation, deletionType=2)

      case LocalResponse.EliminateDeployable(obj: TelepadDeployable, dguid, _, _) if obj.Destroyed =>
        //standard deployable elimination behavior
        sendResponse(ObjectDeleteMessage(dguid, unk1=0))

      case LocalResponse.EliminateDeployable(obj: TelepadDeployable, dguid, pos, _) =>
        //standard deployable elimination behavior
        obj.Destroyed = true
        DeconstructDeployable(obj, dguid, pos, obj.Orientation, deletionType=2)

      case LocalResponse.EliminateDeployable(obj, dguid, _, _) if obj.Destroyed =>
        sendResponse(ObjectDeleteMessage(dguid, unk1=0))

      case LocalResponse.EliminateDeployable(obj, dguid, pos, effect) =>
        obj.Destroyed = true
        DeconstructDeployable(obj, dguid, pos, obj.Orientation, effect)

      case LocalResponse.SendHackMessageHackCleared(targetGuid, unk1, unk2) =>
        sendResponse(HackMessage(unk1=0, targetGuid, guid, progress=0, unk1, HackState.HackCleared, unk2))

      case LocalResponse.HackObject(targetGuid, unk1, unk2) =>
        HackObject(targetGuid, unk1, unk2)

      case LocalResponse.SendPlanetsideAttributeMessage(targetGuid, attributeType, attributeValue) =>
        SendPlanetsideAttributeMessage(targetGuid, attributeType, attributeValue)

      case LocalResponse.SendGenericObjectActionMessage(targetGuid, actionNumber) =>
        sendResponse(GenericObjectActionMessage(targetGuid, actionNumber))

      case LocalResponse.SendGenericActionMessage(actionNumber) =>
        sendResponse(GenericActionMessage(actionNumber))

      case LocalResponse.SendChatMsg(msg) =>
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
        sessionData.specialItemSlotGuid.foreach { case guid if guid == lluGuid =>
          sessionData.specialItemSlotGuid = None
          player.Carrying = None
        }

      case LocalResponse.ObjectDelete(objectGuid, unk) if isNotSameTarget =>
        sendResponse(ObjectDeleteMessage(objectGuid, unk))

      case LocalResponse.ProximityTerminalEffect(object_guid, true) =>
        sendResponse(ProximityTerminalUseMessage(Service.defaultPlayerGUID, object_guid, unk=true))

      case LocalResponse.ProximityTerminalEffect(objectGuid, false) =>
        sendResponse(ProximityTerminalUseMessage(Service.defaultPlayerGUID, objectGuid, unk=false))
        sessionData.terminals.ForgetAllProximityTerminals(objectGuid)

      case LocalResponse.RouterTelepadMessage(msg) =>
        sendResponse(ChatMsg(ChatMessageType.UNK_229, wideContents=false, recipient="", msg, note=None))

      case LocalResponse.RouterTelepadTransport(passengerGuid, srcGuid, destGuid) =>
        sessionData.useRouterTelepadEffect(passengerGuid, srcGuid, destGuid)

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
        sessionData.toggleTeleportSystem(router, systemPlan)

      case LocalResponse.TriggerEffect(targetGuid, effect, effectInfo, triggerLocation) =>
        sendResponse(TriggerEffectMessage(targetGuid, effect, effectInfo, triggerLocation))

      case LocalResponse.TriggerSound(sound, pos, unk, volume) =>
        sendResponse(TriggerSoundMessage(sound, pos, unk, volume))

      case LocalResponse.UpdateForceDomeStatus(buildingGuid, activated) if activated =>
        sendResponse(GenericObjectActionMessage(buildingGuid, 11))

      case LocalResponse.UpdateForceDomeStatus(buildingGuid, _) =>
        sendResponse(GenericObjectActionMessage(buildingGuid, 12))

      case LocalResponse.RechargeVehicleWeapon(vehicleGuid, weaponGuid) if resolvedPlayerGuid == guid =>
        continent.GUID(vehicleGuid).collect { case vehicle: MountableWeapons =>
          vehicle.PassengerInSeat(player).collect { seat_num =>
            vehicle.WeaponControlledFromSeat(seat_num).collect { case weapon: Tool if weapon.GUID == weaponGuid =>
              sendResponse(InventoryStateMessage(weapon.AmmoSlot.Box.GUID, weapon.GUID, weapon.Magazine))
            }
          }
        }

      case _ => ()
    }
  }

  /**
   * Common behavior for deconstructing deployables in the game environment.
   * @param obj the deployable
   * @param guid the globally unique identifier for the deployable
   * @param pos the previous position of the deployable
   * @param orient the previous orientation of the deployable
   * @param deletionType the value passed to `ObjectDeleteMessage` concerning the deconstruction animation
   */
  def DeconstructDeployable(
                             obj: Deployable,
                             guid: PlanetSideGUID,
                             pos: Vector3,
                             orient: Vector3,
                             deletionType: Int
                           ): Unit = {
    sendResponse(TriggerEffectMessage("spawn_object_failed_effect", pos, orient))
    sendResponse(PlanetsideAttributeMessage(guid, 29, 1)) //make deployable vanish
    sendResponse(ObjectDeleteMessage(guid, deletionType))
  }

  /**
   * na
   * @param targetGuid na
   * @param unk1 na
   * @param unk2 na
   */
  def HackObject(targetGuid: PlanetSideGUID, unk1: Long, unk2: Long): Unit = {
    sendResponse(HackMessage(unk1=0, targetGuid, player_guid=Service.defaultPlayerGUID, progress=100, unk1, HackState.Hacked, unk2))
  }

  /**
   * Send a PlanetsideAttributeMessage packet to the client
   * @param targetGuid The target of the attribute
   * @param attributeType The attribute number
   * @param attributeValue The attribute value
   */
  def SendPlanetsideAttributeMessage(
                                      targetGuid: PlanetSideGUID,
                                      attributeType: PlanetsideAttributeEnum,
                                      attributeValue: Long
                                    ): Unit = {
    sendResponse(PlanetsideAttributeMessage(targetGuid, attributeType, attributeValue))
  }
}

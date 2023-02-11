// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.ActorContext
import net.psforever.objects.ce.Deployable
import net.psforever.objects.vehicles.MountableWeapons
import net.psforever.objects._
import net.psforever.packet.game.PlanetsideAttributeEnum.PlanetsideAttributeEnum
import net.psforever.packet.game._
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
    val tplayer_guid = if (player.HasGUID) { player.GUID }
    else { PlanetSideGUID(0) }
    reply match {
      case LocalResponse.DeployableMapIcon(behavior, deployInfo) =>
        if (tplayer_guid != guid) {
          sendResponse(DeployableObjectsInfoMessage(behavior, deployInfo))
        }

      case LocalResponse.DeployableUIFor(item) =>
        sessionData.updateDeployableUIElements(avatar.deployables.UpdateUIElement(item))

      case LocalResponse.Detonate(dguid, _: BoomerDeployable) =>
        sendResponse(TriggerEffectMessage(dguid, "detonate_boomer"))
        sendResponse(PlanetsideAttributeMessage(dguid, 29, 1))
        sendResponse(ObjectDeleteMessage(dguid, 0))

      case LocalResponse.Detonate(dguid, _: ExplosiveDeployable) =>
        sendResponse(GenericObjectActionMessage(dguid, 19))
        sendResponse(PlanetsideAttributeMessage(dguid, 29, 1))
        sendResponse(ObjectDeleteMessage(dguid, 0))

      case LocalResponse.Detonate(_, obj) =>
        log.warn(s"LocalResponse.Detonate: ${obj.Definition.Name} not configured to explode correctly")

      case LocalResponse.DoorOpens(door_guid) =>
        if (tplayer_guid != guid) {
          sendResponse(GenericObjectStateMsg(door_guid, 16))
        }

      case LocalResponse.DoorCloses(door_guid) => //door closes for everyone
        sendResponse(GenericObjectStateMsg(door_guid, 17))

      case LocalResponse.EliminateDeployable(obj: TurretDeployable, dguid, pos, _) =>
        if (obj.Destroyed) {
          sendResponse(ObjectDeleteMessage(dguid, 0))
        } else {
          obj.Destroyed = true
          DeconstructDeployable(
            obj,
            dguid,
            pos,
            obj.Orientation,
            if (obj.MountPoints.isEmpty) 2 else 1
          )
        }

      case LocalResponse.EliminateDeployable(obj: ExplosiveDeployable, dguid, pos, effect) =>
        if (obj.Destroyed || obj.Jammed || obj.Health == 0) {
          sendResponse(ObjectDeleteMessage(dguid, 0))
        } else {
          obj.Destroyed = true
          DeconstructDeployable(obj, dguid, pos, obj.Orientation, effect)
        }

      case LocalResponse.EliminateDeployable(obj: TelepadDeployable, dguid, pos, _) =>
        //if active, deactivate
        if (obj.Active) {
          obj.Active = false
          sendResponse(GenericObjectActionMessage(dguid, 29))
          sendResponse(GenericObjectActionMessage(dguid, 30))
        }
        //standard deployable elimination behavior
        if (obj.Destroyed) {
          sendResponse(ObjectDeleteMessage(dguid, 0))
        } else {
          obj.Destroyed = true
          DeconstructDeployable(obj, dguid, pos, obj.Orientation, deletionType = 2)
        }

      case LocalResponse.EliminateDeployable(obj, dguid, pos, effect) =>
        if (obj.Destroyed) {
          sendResponse(ObjectDeleteMessage(dguid, 0))
        } else {
          obj.Destroyed = true
          DeconstructDeployable(obj, dguid, pos, obj.Orientation, effect)
        }

      case LocalResponse.SendHackMessageHackCleared(target_guid, unk1, unk2) =>
        sendResponse(HackMessage(0, target_guid, guid, 0, unk1, HackState.HackCleared, unk2))

      case LocalResponse.HackObject(target_guid, unk1, unk2) =>
        HackObject(target_guid, unk1, unk2)

      case LocalResponse.SendPlanetsideAttributeMessage(target_guid, attribute_number, attribute_value) =>
        SendPlanetsideAttributeMessage(target_guid, attribute_number, attribute_value)

      case LocalResponse.SendGenericObjectActionMessage(target_guid, action_number) =>
        sendResponse(GenericObjectActionMessage(target_guid, action_number))

      case LocalResponse.SendGenericActionMessage(action_number) =>
        sendResponse(GenericActionMessage(action_number))

      case LocalResponse.SendChatMsg(msg) =>
        sendResponse(msg)

      case LocalResponse.SendPacket(packet) =>
        sendResponse(packet)

      case LocalResponse.LluSpawned(llu) =>
        // Create LLU on client
        sendResponse(
          ObjectCreateMessage(
            llu.Definition.ObjectId,
            llu.GUID,
            llu.Definition.Packet.ConstructorData(llu).get
          )
        )

        sendResponse(TriggerSoundMessage(TriggeredSound.LLUMaterialize, llu.Position, unk = 20, 0.8000001f))

      case LocalResponse.LluDespawned(llu) =>
        sendResponse(TriggerSoundMessage(TriggeredSound.LLUDeconstruct, llu.Position, unk = 20, 0.8000001f))
        sendResponse(ObjectDeleteMessage(llu.GUID, 0))
        // If the player was holding the LLU, remove it from their tracked special item slot
        sessionData.specialItemSlotGuid match {
          case Some(guid) =>
            if (guid == llu.GUID) {
              sessionData.specialItemSlotGuid = None
              player.Carrying = None
            }
          case _ => ;
        }

      case LocalResponse.ObjectDelete(object_guid, unk) =>
        if (tplayer_guid != guid) {
          sendResponse(ObjectDeleteMessage(object_guid, unk))
        }

      case LocalResponse.ProximityTerminalEffect(object_guid, true) =>
        sendResponse(ProximityTerminalUseMessage(PlanetSideGUID(0), object_guid, unk=true))

      case LocalResponse.ProximityTerminalEffect(object_guid, false) =>
        sendResponse(ProximityTerminalUseMessage(PlanetSideGUID(0), object_guid, unk=false))
        sessionData.terminals.ForgetAllProximityTerminals(object_guid)

      case LocalResponse.RouterTelepadMessage(msg) =>
        sendResponse(ChatMsg(ChatMessageType.UNK_229, wideContents=false, "", msg, None))

      case LocalResponse.RouterTelepadTransport(passenger_guid, src_guid, dest_guid) =>
        sessionData.useRouterTelepadEffect(passenger_guid, src_guid, dest_guid)

      case LocalResponse.SendResponse(msg) =>
        sendResponse(msg)

      case LocalResponse.SetEmpire(object_guid, empire) =>
        sendResponse(SetEmpireMessage(object_guid, empire))

      case LocalResponse.ShuttleEvent(ev) =>
        val msg = OrbitalShuttleTimeMsg(
          ev.u1,
          ev.u2,
          ev.t1,
          ev.t2,
          ev.t3,
          ev.pairs.map { case ((a, b), c) => PadAndShuttlePair(a, b, c) }
        )
        sendResponse(msg)

      case LocalResponse.ShuttleDock(pguid, sguid, slot) =>
        sendResponse(ObjectAttachMessage(pguid, sguid, slot))

      case LocalResponse.ShuttleUndock(pguid, sguid, pos, orient) =>
        sendResponse(ObjectDetachMessage(pguid, sguid, pos, orient))

      case LocalResponse.ShuttleState(sguid, pos, orient, state) =>
        sendResponse(VehicleStateMessage(sguid, 0, pos, orient, None, Some(state), 0, 0, 15, is_decelerating=false, is_cloaked=false))

      case LocalResponse.ToggleTeleportSystem(router, system_plan) =>
        sessionData.toggleTeleportSystem(router, system_plan)

      case LocalResponse.TriggerEffect(target_guid, effect, effectInfo, triggerLocation) =>
        sendResponse(TriggerEffectMessage(target_guid, effect, effectInfo, triggerLocation))

      case LocalResponse.TriggerSound(sound, pos, unk, volume) =>
        sendResponse(TriggerSoundMessage(sound, pos, unk, volume))

      case LocalResponse.UpdateForceDomeStatus(building_guid, activated) =>
        if (activated) {
          sendResponse(GenericObjectActionMessage(building_guid, 11))
        } else {
          sendResponse(GenericObjectActionMessage(building_guid, 12))
        }

      case LocalResponse.RechargeVehicleWeapon(vehicle_guid, weapon_guid) =>
        if (tplayer_guid == guid) {
          continent.GUID(vehicle_guid) match {
            case Some(vehicle: MountableWeapons) =>
              vehicle.PassengerInSeat(player) match {
                case Some(seat_num: Int) =>
                  vehicle.WeaponControlledFromSeat(seat_num) foreach {
                    case weapon: Tool if weapon.GUID == weapon_guid =>
                      sendResponse(InventoryStateMessage(weapon.AmmoSlot.Box.GUID, weapon.GUID, weapon.Magazine))
                    case _ => ;
                  }
                case _ => ;
              }
            case _ => ;
          }
        }

      case _ => ;
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
   * @param target_guid na
   * @param unk1 na
   * @param unk2 na
   */
  def HackObject(target_guid: PlanetSideGUID, unk1: Long, unk2: Long): Unit = {
    sendResponse(HackMessage(0, target_guid, PlanetSideGUID(0), 100, unk1, HackState.Hacked, unk2))
  }

  /**
   * Send a PlanetsideAttributeMessage packet to the client
   * @param target_guid The target of the attribute
   * @param attribute_number The attribute number
   * @param attribute_value The attribute value
   */
  def SendPlanetsideAttributeMessage(
                                      target_guid: PlanetSideGUID,
                                      attribute_number: PlanetsideAttributeEnum,
                                      attribute_value: Long
                                    ): Unit = {
    sendResponse(PlanetsideAttributeMessage(target_guid, attribute_number, attribute_value))
  }
}

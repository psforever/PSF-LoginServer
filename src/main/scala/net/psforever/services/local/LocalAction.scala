// Copyright (c) 2017-2026 PSForever
package net.psforever.services.local

import net.psforever.objects.{PlanetSideGameObject, TelepadDeployable, Vehicle}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.objects.vehicles.Utility
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{ChatMsg, DeployableInfo, DeploymentAction, GenericAction, HackState7, ObjectCreateMessage, TriggeredEffect, TriggeredEffectLocation, TriggeredSound}
import net.psforever.services.base.messages.SendResponse
import net.psforever.services.base.{EventMessage, EventResponse, SelfRespondingEvent}
import net.psforever.services.hart.HartTimer.OrbitalShuttleEvent
import net.psforever.types.{PlanetSideGUID, Vector3}

object LocalAction {
  sealed trait IsADoorMessage extends SelfRespondingEvent

  sealed trait IsAHackMessage extends SelfRespondingEvent

  final case class DeployItem(item: Deployable) extends EventMessage {
    def response(): EventResponse = {
      val definition = item.Definition
      val objectData = definition.Packet.ConstructorData(item).get
      SendResponse(ObjectCreateMessage(definition.ObjectId, item.GUID, objectData))
    }
  }

  final case class DeployableMapIcon(behavior: DeploymentAction.Value, deployInfo: DeployableInfo) extends SelfRespondingEvent

  final case class DeployableUIFor(obj: DeployedItem.Value) extends SelfRespondingEvent

  final case class Detonate(guid: PlanetSideGUID, obj: PlanetSideGameObject) extends SelfRespondingEvent

  final case class DoorOpens(continent: Zone, door: Door) extends IsADoorMessage

  final case class DoorCloses(door_guid: PlanetSideGUID) extends IsADoorMessage

  final case class EliminateDeployable(
                                        obj: Deployable,
                                        object_guid: PlanetSideGUID,
                                        pos: Vector3,
                                        deletionEffect: Int
                                      ) extends SelfRespondingEvent

  final case class HackClear(
                              target_guid: PlanetSideGUID,
                              unk1: Long,
                              unk2: HackState7 = HackState7.Unk8
                            ) extends IsAHackMessage

  final case class HackObject(target_guid: PlanetSideGUID, unk1: Long, unk2: HackState7) extends IsAHackMessage

  final case class LluSpawned(llu: CaptureFlag) extends SelfRespondingEvent

  final case class LluDespawned(
                                 guid: PlanetSideGUID,
                                 position: Vector3
                               ) extends SelfRespondingEvent

  final case class ChatMessage(msg: ChatMsg) extends SelfRespondingEvent

  final case class GenericActionMessage(action_num: GenericAction) extends SelfRespondingEvent

  final case class ProximityTerminalAction(
                                            terminal: Terminal with ProximityUnit,
                                            target: PlanetSideGameObject
                                          ) extends EventResponse

  final case class ProximityTerminalEffect(
                                            object_guid: PlanetSideGUID,
                                            effectState: Boolean
                                          ) extends SelfRespondingEvent

  final case class RouterTelepadMessage(msg: String) extends SelfRespondingEvent

  final case class RouterTelepadTransport(
                                           passenger_guid: PlanetSideGUID,
                                           src_guid: PlanetSideGUID,
                                           dest_guid: PlanetSideGUID
                                         ) extends SelfRespondingEvent

  final case class ShuttleDock(pad_guid: PlanetSideGUID, shuttle_guid: PlanetSideGUID, toSlot: Int) extends SelfRespondingEvent

  final case class ShuttleUndock(
                                  pad_guid: PlanetSideGUID,
                                  shuttle_guid: PlanetSideGUID,
                                  pos: Vector3, orient: Vector3
                                ) extends SelfRespondingEvent

  final case class ShuttleEvent(ev: OrbitalShuttleEvent)  extends SelfRespondingEvent

  final case class ShuttleState(guid: PlanetSideGUID, pos: Vector3, orientation: Vector3, state: Int) extends SelfRespondingEvent

  final case class StartRouterInternalTelepad(
                                               router_guid: PlanetSideGUID,
                                               obj_guid: PlanetSideGUID,
                                               obj: Utility.InternalTelepad
                                             ) extends SelfRespondingEvent

  final case class ToggleTeleportSystem(
                                         router: Vehicle,
                                         systemPlan: Option[(Utility.InternalTelepad, TelepadDeployable)]
                                       ) extends SelfRespondingEvent

  final case class TriggerEffectAtLocation(
                                            target: PlanetSideGUID,
                                            effect: String,
                                            effectInfo: Option[TriggeredEffect] = None,
                                            triggeredLocation: Option[TriggeredEffectLocation] = None
                                          ) extends EventResponse

  final case class TriggerEffect(effect: String, target: PlanetSideGUID) extends EventMessage {
    def response(): EventResponse = {
      TriggerEffectAtLocation(target, effect)
    }
  }

  final case class TriggerEffectInfo(target: PlanetSideGUID, effect: String, unk1: Boolean, unk2: Long) extends EventMessage {
    def response(): EventResponse = {
      TriggerEffectAtLocation(target, effect, Some(TriggeredEffect(unk1, unk2)))
    }
  }

  final case class TriggerEffectLocation(
                                          effect: String,
                                          pos: Vector3,
                                          orient: Vector3
                                        ) extends EventMessage {
    def response(): EventResponse = {
      TriggerEffectAtLocation(PlanetSideGUID(0), effect, None, Some(TriggeredEffectLocation(pos, orient)))
    }
  }

  final case class TriggerSound(
                                 sound: TriggeredSound.Value,
                                 pos: Vector3,
                                 unk: Int,
                                 volume: Float
                               ) extends SelfRespondingEvent

  final case class UpdateForceDomeStatus(
                                          building_guid: PlanetSideGUID,
                                          activated: Boolean
                                        ) extends SelfRespondingEvent

  final case class RechargeVehicleWeapon(
                                          mountable_guid: PlanetSideGUID,
                                          weapon_guid: PlanetSideGUID
                                        ) extends SelfRespondingEvent

  final case class ForceZoneChange(zone: Zone) extends SelfRespondingEvent
}

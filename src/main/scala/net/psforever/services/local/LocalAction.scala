// Copyright (c) 2017-2026 PSForever
package net.psforever.services.local

import net.psforever.objects.{PlanetSideGameObject, TelepadDeployable, Vehicle}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.objects.vehicles.Utility
import net.psforever.objects.zones.Zone
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.GenericObjectActionEnum.GenericObjectActionEnum
import net.psforever.packet.game.PlanetsideAttributeEnum.PlanetsideAttributeEnum
import net.psforever.packet.game.{ChatMsg, DeployableInfo, DeploymentAction, GenericAction, HackState7, ObjectCreateMessage, TriggeredEffect, TriggeredEffectLocation, TriggeredSound}
import net.psforever.services.base.{EventMessage, EventResponse, SelfResponseMessage}
import net.psforever.services.hart.HartTimer.OrbitalShuttleEvent
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}

object LocalAction {
  trait Action extends EventMessage {
    def response(): EventResponse = null
  }

  final case class DeployItem(item: Deployable) extends EventMessage {
    def response(): EventResponse = {
      val definition = item.Definition
      val objectData = definition.Packet.ConstructorData(item).get
      LocalAction.SendResponse(ObjectCreateMessage(definition.ObjectId, item.GUID, objectData))
    }
  }

  final case class DeployableMapIcon(behavior: DeploymentAction.Value, deployInfo: DeployableInfo) extends SelfResponseMessage

  final case class DeployableUIFor(obj: DeployedItem.Value) extends SelfResponseMessage

  final case class Detonate(guid: PlanetSideGUID, obj: PlanetSideGameObject) extends SelfResponseMessage

  final case class DoorOpens(continent: Zone, door: Door) extends SelfResponseMessage

  final case class DoorCloses(door_guid: PlanetSideGUID) extends SelfResponseMessage

  final case class DoorSlamsShut(door: Door) extends EventMessage {
    def response(): EventResponse = {
      //todo important! doorCloser ! SupportActor.HurrySpecific(List(door), zone)
      DoorCloses(door.GUID)
    }
  }

  final case class EliminateDeployable(
                                        obj: Deployable,
                                        object_guid: PlanetSideGUID,
                                        pos: Vector3,
                                        deletionEffect: Int
                                      ) extends SelfResponseMessage

  final case class SendHackMessageHackCleared(
                                               target_guid: PlanetSideGUID,
                                               unk1: Long,
                                               unk2: HackState7
                                             ) extends SelfResponseMessage

  final case class HackClear(
                              target: PlanetSideServerObject,
                              unk1: Long,
                              unk2: HackState7 = HackState7.Unk8
                            ) extends EventMessage {
    def response(): EventResponse = {
      SendHackMessageHackCleared(target.GUID, unk1, unk2)
    }
  }

  final case class HackObject(target_guid: PlanetSideGUID, unk1: Long, unk2: HackState7) extends EventResponse

  final case class HackTemporarily(
                                    continent: Zone,
                                    target: PlanetSideServerObject,
                                    hackValue: Long,
                                    hackClearValue: Long,
                                    duration: Int,
                                    unk2: HackState7 = HackState7.Unk8
                                  ) extends EventMessage {
    def response(): EventResponse = {
      HackObject(target.GUID, hackValue, unk2)
    }
  }

  final case class LluSpawned(llu: CaptureFlag) extends SelfResponseMessage

  final case class LluDespawned(
                                 guid: PlanetSideGUID,
                                 position: Vector3
                               ) extends SelfResponseMessage

  final case class PlanetsideAttribute(
                                        target_guid: PlanetSideGUID,
                                        attribute_number: PlanetsideAttributeEnum,
                                        attribute_value: Long
                                      ) extends SelfResponseMessage

  final case class ChatMessage(msg: ChatMsg) extends SelfResponseMessage

  final case class GenericObjectAction(
                                        target_guid: PlanetSideGUID,
                                        action_number: GenericObjectActionEnum
                                      ) extends SelfResponseMessage

  final case class GenericActionMessage(action_num: GenericAction) extends SelfResponseMessage

  final case class ObjectDelete(item_guid: PlanetSideGUID, unk: Int) extends EventResponse

  final case class ProximityTerminalAction(
                                            terminal: Terminal with ProximityUnit,
                                            target: PlanetSideGameObject
                                          ) extends EventResponse

  final case class ProximityTerminalEffect(
                                            object_guid: PlanetSideGUID,
                                            effectState: Boolean
                                          ) extends SelfResponseMessage

  final case class RouterTelepadMessage(msg: String) extends SelfResponseMessage

  final case class RouterTelepadTransport(
                                           passenger_guid: PlanetSideGUID,
                                           src_guid: PlanetSideGUID,
                                           dest_guid: PlanetSideGUID
                                         ) extends SelfResponseMessage

  final case class SendResponse(pkt: PlanetSideGamePacket) extends SelfResponseMessage

  final case class SetEmpire(object_guid: PlanetSideGUID, empire: PlanetSideEmpire.Value) extends SelfResponseMessage

  final case class ShuttleDock(pad_guid: PlanetSideGUID, shuttle_guid: PlanetSideGUID, toSlot: Int) extends SelfResponseMessage

  final case class ShuttleUndock(
                                  pad_guid: PlanetSideGUID,
                                  shuttle_guid: PlanetSideGUID,
                                  pos: Vector3, orient: Vector3
                                ) extends SelfResponseMessage

  final case class ShuttleEvent(ev: OrbitalShuttleEvent)  extends SelfResponseMessage

  final case class ShuttleState(guid: PlanetSideGUID, pos: Vector3, orientation: Vector3, state: Int) extends SelfResponseMessage

  final case class StartRouterInternalTelepad(
                                               router_guid: PlanetSideGUID,
                                               obj_guid: PlanetSideGUID,
                                               obj: Utility.InternalTelepad
                                             ) extends SelfResponseMessage

  final case class ToggleTeleportSystem(
                                         router: Vehicle,
                                         systemPlan: Option[(Utility.InternalTelepad, TelepadDeployable)]
                                       ) extends SelfResponseMessage

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
                               ) extends SelfResponseMessage

  final case class UpdateForceDomeStatus(
                                          building_guid: PlanetSideGUID,
                                          activated: Boolean
                                        ) extends SelfResponseMessage

  final case class RechargeVehicleWeapon(
                                          mountable_guid: PlanetSideGUID,
                                          weapon_guid: PlanetSideGUID
                                        ) extends SelfResponseMessage

  final case class ForceZoneChange(zone: Zone) extends SelfResponseMessage
}

// Copyright (c) 2017 PSForever
package net.psforever.services.local

import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.objects.vehicles.Utility
import net.psforever.objects.zones.Zone
import net.psforever.objects.{PlanetSideGameObject, TelepadDeployable, Vehicle}
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.GenericObjectActionEnum.GenericObjectActionEnum
import net.psforever.packet.game.PlanetsideAttributeEnum.PlanetsideAttributeEnum
import net.psforever.packet.game.{ChatMsg, DeployableInfo, DeploymentAction, GenericAction, HackState7, TriggeredSound}
import net.psforever.services.base.{EventMessage, EventResponse}
import net.psforever.services.hart.HartTimer.OrbitalShuttleEvent
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}

final case class LocalServiceMessage(forChannel: String, actionMessage: LocalAction.Action)

object LocalServiceMessage {
  final case class Deployables(msg: Any)
}

object LocalAction {
  trait Action extends EventMessage {
    def response(): EventResponse = null
  }

  final case class DeployItem(item: Deployable) extends Action
  final case class DeployableMapIcon(
                                      player_guid: PlanetSideGUID,
                                      behavior: DeploymentAction.Value,
                                      deployInfo: DeployableInfo
                                    )                                                  extends Action
  final case class DeployableUIFor(obj: DeployedItem.Value)                            extends Action
  final case class Detonate(guid: PlanetSideGUID, obj: PlanetSideGameObject)           extends Action
  final case class DoorOpens(player_guid: PlanetSideGUID, continent: Zone, door: Door) extends Action
  final case class DoorCloses(player_guid: PlanetSideGUID, door_guid: PlanetSideGUID)  extends Action
  final case class DoorSlamsShut(door: Door)                                           extends Action
  final case class EliminateDeployable(
                                        obj: Deployable,
                                        object_guid: PlanetSideGUID,
                                        pos: Vector3,
                                        deletionEffect: Int
                                      )                                                extends Action
  final case class HackClear(player_guid: PlanetSideGUID, target: PlanetSideServerObject, unk1: Long, unk2: HackState7 = HackState7.Unk8)
      extends Action
  final case class HackTemporarily(
      player_guid: PlanetSideGUID,
      continent: Zone,
      target: PlanetSideServerObject,
      hackValue: Long,
      hackClearValue: Long,
      duration: Int,
      unk2: HackState7 = HackState7.Unk8
  ) extends Action
  final case class ClearTemporaryHack(player_guid: PlanetSideGUID, target: PlanetSideServerObject with Hackable)
      extends Action

  final case class ResecureCaptureTerminal(target: CaptureTerminal, hacker: PlayerSource) extends Action
  final case class StartCaptureTerminalHack(target: CaptureTerminal) extends Action
  final case class LluCaptured(llu: CaptureFlag) extends Action
  final case class LluLost(llu: CaptureFlag) extends Action
  final case class LluSpawned(player_guid: PlanetSideGUID, llu: CaptureFlag) extends Action
  final case class LluDespawned(player_guid: PlanetSideGUID, guid: PlanetSideGUID, position: Vector3) extends Action

  final case class SendPacket(packet: PlanetSideGamePacket) extends Action
  final case class SendPlanetsideAttributeMessage(
      player_guid: PlanetSideGUID,
      target: PlanetSideGUID,
      attribute_number: PlanetsideAttributeEnum,
      attribute_value: Long
  ) extends Action
  final case class SendGenericObjectActionMessage(
     player_guid: PlanetSideGUID,
     target: PlanetSideGUID,
     action_number: GenericObjectActionEnum
   ) extends Action

  final case class SendChatMsg(
     player_guid: PlanetSideGUID,
     msg: ChatMsg
   ) extends Action

  final case class SendGenericActionMessage(
    player_guid: PlanetSideGUID,
    action_number: GenericAction
  ) extends Action
  final case class RouterTelepadMessage(msg: String)                                      extends Action
  final case class RouterTelepadTransport(
      player_guid: PlanetSideGUID,
      passenger_guid: PlanetSideGUID,
      src_guid: PlanetSideGUID,
      dest_guid: PlanetSideGUID
  )                                                                                       extends Action
  final case class SendResponse(pkt: PlanetSideGamePacket)                                extends Action
  final case class SetEmpire(object_guid: PlanetSideGUID, empire: PlanetSideEmpire.Value) extends Action
  final case class ShuttleDock(pad_guid: PlanetSideGUID, shuttle_guid: PlanetSideGUID, toSlot: Int)   extends Action
  final case class ShuttleUndock(
      pad_guid: PlanetSideGUID,
      shuttle_guid: PlanetSideGUID,
      pos: Vector3, orient: Vector3
  ) extends Action
  final case class ShuttleEvent(ev: OrbitalShuttleEvent)                                              extends Action
  final case class ShuttleState(guid: PlanetSideGUID, pos: Vector3, orientation: Vector3, state: Int) extends Action
  final case class StartRouterInternalTelepad(
    router_guid: PlanetSideGUID,
    obj_guid: PlanetSideGUID,
    obj: Utility.InternalTelepad
  ) extends Action
  final case class ToggleTeleportSystem(
      player_guid: PlanetSideGUID,
      router: Vehicle,
      systemPlan: Option[(Utility.InternalTelepad, TelepadDeployable)]
  )                                                                                                   extends Action
  final case class TriggerEffect(player_guid: PlanetSideGUID, effect: String, target: PlanetSideGUID) extends Action
  final case class TriggerEffectInfo(
      player_guid: PlanetSideGUID,
      effect: String,
      target: PlanetSideGUID,
      unk1: Boolean,
      unk2: Long
  ) extends Action
  final case class TriggerEffectLocation(player_guid: PlanetSideGUID, effect: String, pos: Vector3, orient: Vector3)
      extends Action
  final case class TriggerSound(
      player_guid: PlanetSideGUID,
      sound: TriggeredSound.Value,
      pos: Vector3,
      unk: Int,
      volume: Float
  ) extends Action
  final case class UpdateForceDomeStatus(player_guid: PlanetSideGUID, building_guid: PlanetSideGUID, activated: Boolean)
      extends Action
  final case class RechargeVehicleWeapon(
      player_guid: PlanetSideGUID,
      mountable_guid: PlanetSideGUID,
      weapon_guid: PlanetSideGUID
  ) extends Action
  final case class ForceZoneChange(zone: Zone) extends Action
}

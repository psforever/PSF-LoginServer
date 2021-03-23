// Copyright (c) 2017 PSForever
package net.psforever.services.local

import net.psforever.objects.{PlanetSideGameObject, TelepadDeployable, Vehicle}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.objects.vehicles.Utility
import net.psforever.packet.game.PlanetsideAttributeEnum.PlanetsideAttributeEnum
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import net.psforever.services.GenericEventBusMsg
import net.psforever.services.hart.HartTimer.OrbitalShuttleEvent

final case class LocalServiceResponse(
    channel: String,
    avatar_guid: PlanetSideGUID,
    replyMessage: LocalResponse.Response
) extends GenericEventBusMsg

object LocalResponse {
  trait Response

  final case class AlertDestroyDeployable(obj: PlanetSideGameObject with Deployable)             extends Response
  final case class DeployableMapIcon(action: DeploymentAction.Value, deployInfo: DeployableInfo) extends Response
  final case class Detonate(guid: PlanetSideGUID, obj: PlanetSideGameObject)                     extends Response
  final case class DoorOpens(door_guid: PlanetSideGUID)                                          extends Response
  final case class DoorCloses(door_guid: PlanetSideGUID)                                         extends Response
  final case class EliminateDeployable(
      obj: PlanetSideGameObject with Deployable,
      object_guid: PlanetSideGUID,
      pos: Vector3
  )                                                                                extends Response
  final case class SendHackMessageHackCleared(target_guid: PlanetSideGUID, unk1: Long, unk2: Long)  extends Response
  final case class HackObject(target_guid: PlanetSideGUID, unk1: Long, unk2: Long) extends Response
  final case class SendPlanetsideAttributeMessage(target_guid: PlanetSideGUID, attribute_number: PlanetsideAttributeEnum, attribute_value: Long)
      extends Response
  final case class ObjectDelete(item_guid: PlanetSideGUID, unk: Int) extends Response
  final case class ProximityTerminalAction(terminal: Terminal with ProximityUnit, target: PlanetSideGameObject)
      extends Response
  final case class ProximityTerminalEffect(object_guid: PlanetSideGUID, effectState: Boolean) extends Response
  final case class RouterTelepadMessage(msg: String)                                          extends Response
  final case class RouterTelepadTransport(
      passenger_guid: PlanetSideGUID,
      src_guid: PlanetSideGUID,
      dest_guid: PlanetSideGUID
  )                                                                                       extends Response
  final case class SendResponse(pkt: PlanetSideGamePacket)                                extends Response
  final case class SetEmpire(object_guid: PlanetSideGUID, empire: PlanetSideEmpire.Value) extends Response
  final case class ShuttleDock(pad_guid: PlanetSideGUID, shuttle_guid: PlanetSideGUID, toSlot: Int)   extends Response
  final case class ShuttleUndock(
      pad_guid: PlanetSideGUID,
      shuttle_guid: PlanetSideGUID,
      pos: Vector3, orient: Vector3
  ) extends Response
  final case class ShuttleEvent(ev: OrbitalShuttleEvent)                                              extends Response
  final case class ShuttleState(guid: PlanetSideGUID, pos: Vector3, orientation: Vector3, state: Int) extends Response
  final case class ToggleTeleportSystem(
      router: Vehicle,
      systemPlan: Option[(Utility.InternalTelepad, TelepadDeployable)]
  ) extends Response
  final case class TriggerEffect(
      target: PlanetSideGUID,
      effect: String,
      effectInfo: Option[TriggeredEffect] = None,
      triggeredLocation: Option[TriggeredEffectLocation] = None
  )                                                                                                   extends Response
  final case class TriggerSound(sound: TriggeredSound.Value, pos: Vector3, unk: Int, volume: Float)   extends Response
  final case class UpdateForceDomeStatus(building_guid: PlanetSideGUID, activated: Boolean)           extends Response
  final case class RechargeVehicleWeapon(mountable_guid: PlanetSideGUID, weapon_guid: PlanetSideGUID) extends Response
}

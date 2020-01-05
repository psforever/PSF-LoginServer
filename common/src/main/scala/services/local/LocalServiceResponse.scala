// Copyright (c) 2017 PSForever
package services.local

import net.psforever.objects.{PlanetSideGameObject, TelepadDeployable, Vehicle}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.objects.vehicles.Utility
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideEmpire, Vector3}
import services.GenericEventBusMsg

final case class LocalServiceResponse(toChannel : String,
                                      avatar_guid : PlanetSideGUID,
                                      replyMessage : LocalResponse.Response
                                     ) extends GenericEventBusMsg

object LocalResponse {
  trait Response

  final case class AlertDestroyDeployable(obj : PlanetSideGameObject with Deployable) extends Response
  final case class DeployableMapIcon(action : DeploymentAction.Value, deployInfo : DeployableInfo) extends Response
  final case class Detonate(guid : PlanetSideGUID, obj : PlanetSideGameObject) extends Response
  final case class DoorOpens(door_guid : PlanetSideGUID) extends Response
  final case class DoorCloses(door_guid : PlanetSideGUID) extends Response
  final case class EliminateDeployable(obj : PlanetSideGameObject with Deployable, object_guid : PlanetSideGUID, pos : Vector3) extends Response
  final case class HackClear(target_guid : PlanetSideGUID, unk1 : Long, unk2 : Long) extends Response
  final case class HackObject(target_guid : PlanetSideGUID, unk1 : Long, unk2 : Long) extends Response
  final case class HackCaptureTerminal(target_guid : PlanetSideGUID, unk1 : Long, unk2 : Long, isResecured: Boolean) extends Response
  final case class ObjectDelete(item_guid : PlanetSideGUID, unk : Int) extends Response
  final case class ProximityTerminalAction(terminal : Terminal with ProximityUnit, target : PlanetSideGameObject) extends Response
  final case class ProximityTerminalEffect(object_guid : PlanetSideGUID, effectState : Boolean) extends Response
  final case class RouterTelepadMessage(msg : String) extends Response
  final case class RouterTelepadTransport(passenger_guid : PlanetSideGUID, src_guid : PlanetSideGUID, dest_guid : PlanetSideGUID) extends Response
  final case class SetEmpire(object_guid: PlanetSideGUID, empire: PlanetSideEmpire.Value) extends Response
  final case class ToggleTeleportSystem(router : Vehicle, systemPlan : Option[(Utility.InternalTelepad, TelepadDeployable)]) extends Response
  final case class TriggerEffect(target: PlanetSideGUID, effect: String, effectInfo: Option[TriggeredEffect] = None, triggeredLocation: Option[TriggeredEffectLocation] = None) extends Response
  final case class TriggerSound(sound : TriggeredSound.Value, pos : Vector3, unk : Int, volume : Float) extends Response
  final case class UpdateForceDomeStatus(building_guid : PlanetSideGUID, activated : Boolean) extends Response
}

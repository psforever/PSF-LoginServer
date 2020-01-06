// Copyright (c) 2017 PSForever
package services.local

import net.psforever.objects.{PlanetSideGameObject, TelepadDeployable, Vehicle}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.terminals.CaptureTerminal
import net.psforever.objects.vehicles.Utility
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{DeployableInfo, DeploymentAction, TriggeredSound}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}

final case class LocalServiceMessage(forChannel : String, actionMessage : LocalAction.Action)

object LocalServiceMessage {
  final case class Deployables(msg : Any)

  final case class Telepads(msg : Any)
}

object LocalAction {
  trait Action

  final case class AlertDestroyDeployable(player_guid : PlanetSideGUID, obj : PlanetSideGameObject with Deployable) extends Action
  final case class DeployableMapIcon(player_guid : PlanetSideGUID, behavior : DeploymentAction.Value, deployInfo : DeployableInfo) extends Action
  final case class Detonate(guid : PlanetSideGUID, obj : PlanetSideGameObject) extends Action
  final case class DoorOpens(player_guid : PlanetSideGUID, continent : Zone, door : Door) extends Action
  final case class DoorCloses(player_guid : PlanetSideGUID, door_guid : PlanetSideGUID) extends Action
  final case class HackClear(player_guid : PlanetSideGUID, target : PlanetSideServerObject, unk1 : Long, unk2 : Long = 8L) extends Action
  final case class HackTemporarily(player_guid : PlanetSideGUID, continent : Zone, target : PlanetSideServerObject, unk1 : Long, duration: Int, unk2 : Long = 8L) extends Action
  final case class ClearTemporaryHack(player_guid: PlanetSideGUID, target: PlanetSideServerObject with Hackable) extends Action
  final case class HackCaptureTerminal(player_guid : PlanetSideGUID, continent : Zone, target : CaptureTerminal, unk1 : Long, unk2 : Long = 8L, isResecured : Boolean) extends Action
  final case class RouterTelepadTransport(player_guid : PlanetSideGUID, passenger_guid : PlanetSideGUID, src_guid : PlanetSideGUID, dest_guid : PlanetSideGUID) extends Action
  final case class SetEmpire(object_guid: PlanetSideGUID, empire: PlanetSideEmpire.Value) extends Action
  final case class ToggleTeleportSystem(player_guid : PlanetSideGUID, router : Vehicle, systemPlan : Option[(Utility.InternalTelepad, TelepadDeployable)]) extends Action
  final case class TriggerEffect(player_guid : PlanetSideGUID, effect : String, target : PlanetSideGUID) extends Action
  final case class TriggerEffectInfo(player_guid : PlanetSideGUID, effect : String, target : PlanetSideGUID, unk1 : Boolean, unk2 : Long) extends Action
  final case class TriggerEffectLocation(player_guid : PlanetSideGUID, effect : String, pos : Vector3, orient : Vector3) extends Action
  final case class TriggerSound(player_guid : PlanetSideGUID, sound : TriggeredSound.Value, pos : Vector3, unk : Int, volume : Float) extends Action
  final case class UpdateForceDomeStatus(player_guid : PlanetSideGUID, building_guid : PlanetSideGUID, activated : Boolean) extends Action
}

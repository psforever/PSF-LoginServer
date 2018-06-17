// Copyright (c) 2017 PSForever
package services.local

import net.psforever.packet.game.{PlanetSideGUID, TriggeredSound}
import net.psforever.types.{PlanetSideEmpire, Vector3}

object LocalResponse {
  trait Response

  final case class DoorOpens(door_guid : PlanetSideGUID) extends Response
  final case class DoorCloses(door_guid : PlanetSideGUID) extends Response
  final case class HackClear(target_guid : PlanetSideGUID, unk1 : Long, unk2 : Long) extends Response
  final case class HackObject(target_guid : PlanetSideGUID, unk1 : Long, unk2 : Long) extends Response
  final case class HackCaptureTerminal(target_guid : PlanetSideGUID, unk1 : Long, unk2 : Long, isResecured: Boolean) extends Response
  final case class ProximityTerminalEffect(object_guid : PlanetSideGUID, effectState : Boolean) extends Response
  final case class TriggerSound(sound : TriggeredSound.Value, pos : Vector3, unk : Int, volume : Float) extends Response
  final case class SetEmpire(object_guid: PlanetSideGUID, empire: PlanetSideEmpire.Value) extends Response
}

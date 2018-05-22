// Copyright (c) 2017 PSForever
package services.local

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{PlanetSideGUID, TriggeredSound}
import net.psforever.types.Vector3

object LocalAction {
  trait Action

  final case class DoorOpens(player_guid : PlanetSideGUID, continent : Zone, door : Door) extends Action
  final case class DoorCloses(player_guid : PlanetSideGUID, door_guid : PlanetSideGUID) extends Action
  final case class DropItem(player_guid : PlanetSideGUID, item : Equipment) extends Action
  final case class HackClear(player_guid : PlanetSideGUID, target : PlanetSideServerObject, unk1 : Long, unk2 : Long = 8L) extends Action
  final case class HackTemporarily(player_guid : PlanetSideGUID, continent : Zone, target : PlanetSideServerObject, unk1 : Long, unk2 : Long = 8L) extends Action
  final case class ProximityTerminalEffect(player_guid : PlanetSideGUID, object_guid : PlanetSideGUID, effectState : Boolean) extends Action
  final case class TriggerSound(player_guid : PlanetSideGUID, sound : TriggeredSound.Value, pos : Vector3, unk : Int, volume : Float) extends Action
}

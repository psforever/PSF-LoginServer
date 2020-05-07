// Copyright (c) 2017 PSForever
package services.chat

import net.psforever.objects.zones.Zone
import net.psforever.packet.game.ChatMsg
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}

object ChatAction {
  sealed trait Action

  final case class Local(player_guid : PlanetSideGUID, player_name : String, continent : Zone, player_pos : Vector3, player_faction : PlanetSideEmpire.Value, msg : ChatMsg) extends Action
  final case class Tell(player_guid : PlanetSideGUID, player_name : String, msg : ChatMsg) extends Action
  final case class Broadcast(player_guid : PlanetSideGUID, player_name : String, continent : Zone, player_pos : Vector3, player_faction : PlanetSideEmpire.Value, msg : ChatMsg) extends Action
  final case class Voice(player_guid : PlanetSideGUID, player_name : String, continent : Zone, player_pos : Vector3, player_faction : PlanetSideEmpire.Value, msg : ChatMsg) extends Action
  final case class Note(player_guid : PlanetSideGUID, player_name : String, msg : ChatMsg) extends Action
  final case class Squad(player_guid : PlanetSideGUID, player_name : String, continent : Zone, player_pos : Vector3, player_faction : PlanetSideEmpire.Value, msg : ChatMsg) extends Action
  final case class Platoon(player_guid : PlanetSideGUID, player_name : String, continent : Zone, player_pos : Vector3, player_faction : PlanetSideEmpire.Value, msg : ChatMsg) extends Action
  final case class Command(player_guid : PlanetSideGUID, player_name : String, continent : Zone, player_pos : Vector3, player_faction : PlanetSideEmpire.Value, msg : ChatMsg) extends Action
  final case class GM(player_guid : PlanetSideGUID, player_name : String, msg : ChatMsg) extends Action
}
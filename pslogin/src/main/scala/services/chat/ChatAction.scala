// Copyright (c) 2017 PSForever
package services.chat

import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{ChatMsg, PlanetSideGUID}

object ChatAction {
  trait Action

  final case class Local(player_guid : PlanetSideGUID, player_name : String, continent : Zone, msg : ChatMsg) extends Action
  final case class Tell(player_guid : PlanetSideGUID, player_name : String, msg : ChatMsg) extends Action
  final case class Broadcast(player_guid : PlanetSideGUID, continent : Zone, msg : ChatMsg) extends Action
  final case class Voice(player_guid : PlanetSideGUID, player_name : String, continent : Zone, msg : ChatMsg) extends Action
  final case class Note(player_guid : PlanetSideGUID, player_name : String, msg : ChatMsg) extends Action
}

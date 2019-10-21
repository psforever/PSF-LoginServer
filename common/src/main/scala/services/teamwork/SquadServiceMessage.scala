// Copyright (c) 2019 PSForever
package services.teamwork

import net.psforever.objects.Player
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{PlanetSideGUID, SquadAction => PacketSquadAction, WaypointEventAction, WaypointInfo}
import net.psforever.types.{SquadRequestType, SquadWaypoints, Vector3}

final case class SquadServiceMessage(tplayer : Player, zone : Zone, actionMessage : Any)

object SquadServiceMessage {
  final case class RecoverSquadMembership()
}

object SquadAction {
  sealed trait Action

  final case class InitSquadList() extends Action
  final case class InitCharId() extends Action

  final case class Definition(guid : PlanetSideGUID, line : Int, action : PacketSquadAction) extends Action
  final case class Membership(request_type : SquadRequestType.Value, unk2 : Long, unk3 : Option[Long], player_name : String, unk5 : Option[Option[String]]) extends Action
  final case class Waypoint(event_type : WaypointEventAction.Value, waypoint_type : SquadWaypoints.Value, unk : Option[Long], waypoint_info : Option[WaypointInfo]) extends Action
  final case class Update(char_id : Long, health : Int, max_health : Int, armor : Int, max_armor : Int, pos : Vector3, zone_number : Int) extends Action
}

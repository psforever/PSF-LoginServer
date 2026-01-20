// Copyright (c) 2019 PSForever
package net.psforever.services.teamwork

import net.psforever.objects.Player
import net.psforever.objects.avatar.Certification
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{WaypointEventAction, WaypointInfo, SquadAction => PacketSquadAction}
import net.psforever.services.base.{EventMessage, EventResponse}
import net.psforever.types.{PlanetSideGUID, SquadRequestType, SquadWaypoint, Vector3}

final case class SquadServiceMessage(tplayer: Player, zone: Zone, actionMessage: Any)

object SquadServiceMessage {

  final case class RecoverSquadMembership()
}

object SquadAction {
  sealed trait Action extends EventMessage {
    def response(): EventResponse = null
  }

  final case class InitSquadList()    extends Action
  final case class InitCharId()       extends Action
  final case class ReloadDecoration() extends Action

  final case class Definition(guid: PlanetSideGUID, line: Int, action: PacketSquadAction) extends Action
  final case class Membership(
      request_type: SquadRequestType.Value,
      unk2: Long,
      unk3: Option[Long],
      player_name: String,
      unk5: Option[Option[String]]
  ) extends Action
  final case class Waypoint(
      event_type: WaypointEventAction.Value,
      waypoint_type: SquadWaypoint,
      unk: Option[Long],
      waypoint_info: Option[WaypointInfo]
  ) extends Action
  final case class Update(
      char_id: Long,
      guid: PlanetSideGUID,
      health: Int,
      max_health: Int,
      armor: Int,
      max_armor: Int,
      certifications: Set[Certification],
      pos: Vector3,
      zone_number: Int
  ) extends Action
}

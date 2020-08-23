// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{SquadWaypoints, Vector3}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Actions that can be requested of the specific waypoint.
  */
object WaypointEventAction extends Enumeration {
  type Type = Value

  val Add, Unknown1, Remove, Unknown3 //unconfirmed
  = Value

  implicit val codec: Codec[WaypointEventAction.Value] = PacketHelpers.createEnumerationCodec(enum = this, uint2)
}

/**
  * na
  * @param zone_number the zone
  * @param pos the continental map coordinate location of the waypoint;
  *            the z-coordinate is almost always 0.0
  */
final case class WaypointInfo(zone_number: Int, pos: Vector3)

/**
  * na
  * @param request_type the action to be performed
  * @param char_id the unique id of player setting the waypoint
  * @param waypoint_type the waypoint being updated;
  *                      0-3 for the standard squad waypoints numbered "1-4";
  *                      4 for the squad leader experience waypoint;
  *                      cycles through 0-3 continuously
  *
  * @param unk4 na
  * @param waypoint_info essential data about the waypoint
  */
final case class SquadWaypointRequest(
    request_type: WaypointEventAction.Value,
    char_id: Long,
    waypoint_type: SquadWaypoints.Value,
    unk4: Option[Long],
    waypoint_info: Option[WaypointInfo]
) extends PlanetSideGamePacket {
  type Packet = SquadWaypointRequest
  def opcode = GamePacketOpcode.SquadWaypointRequest
  def encode = SquadWaypointRequest.encode(this)
}

object SquadWaypointRequest extends Marshallable[SquadWaypointRequest] {
  def Add(char_id: Long, waypoint_type: SquadWaypoints.Value, waypoint: WaypointInfo): SquadWaypointRequest =
    SquadWaypointRequest(WaypointEventAction.Add, char_id, waypoint_type, None, Some(waypoint))

  def Unknown1(char_id: Long, waypoint_type: SquadWaypoints.Value, unk_a: Long): SquadWaypointRequest =
    SquadWaypointRequest(WaypointEventAction.Unknown1, char_id, waypoint_type, Some(unk_a), None)

  def Remove(char_id: Long, waypoint_type: SquadWaypoints.Value): SquadWaypointRequest =
    SquadWaypointRequest(WaypointEventAction.Remove, char_id, waypoint_type, None, None)

  private val waypoint_codec: Codec[WaypointInfo] = (
    ("zone_number" | uint16L) ::
      ("pos" | Vector3.codec_pos)
  ).xmap[WaypointInfo](
    {
      case zone_number :: pos :: HNil => WaypointInfo(zone_number, pos)
    },
    {
      case WaypointInfo(zone_number, pos) => zone_number :: pos.xy :: HNil
    }
  )

  implicit val codec: Codec[SquadWaypointRequest] = (
    ("request_type" | WaypointEventAction.codec) >>:~ { request_type =>
      ("char_id" | uint32L) ::
        ("waypoint_type" | SquadWaypoints.codec) ::
        ("unk4" | conditional(request_type == WaypointEventAction.Unknown1, uint32L)) ::
        ("waypoint" | conditional(request_type == WaypointEventAction.Add, waypoint_codec))
    }
  ).exmap[SquadWaypointRequest](
    {
      case WaypointEventAction.Add :: char_id :: waypoint_type :: None :: Some(waypoint) :: HNil =>
        Attempt.Successful(SquadWaypointRequest(WaypointEventAction.Add, char_id, waypoint_type, None, Some(waypoint)))

      case WaypointEventAction.Unknown1 :: char_id :: waypoint_type :: Some(d) :: None :: HNil =>
        Attempt.Successful(SquadWaypointRequest(WaypointEventAction.Unknown1, char_id, waypoint_type, Some(d), None))

      case request_type :: char_id :: waypoint_type :: None :: None :: HNil =>
        Attempt.Successful(SquadWaypointRequest(request_type, char_id, waypoint_type, None, None))

      case data =>
        Attempt.Failure(Err(s"unexpected format while decoding - $data"))
    },
    {
      case SquadWaypointRequest(WaypointEventAction.Add, char_id, waypoint_type, None, Some(waypoint)) =>
        Attempt.Successful(WaypointEventAction.Add :: char_id :: waypoint_type :: None :: Some(waypoint) :: HNil)

      case SquadWaypointRequest(WaypointEventAction.Unknown1, char_id, waypoint_type, Some(d), None) =>
        Attempt.Successful(WaypointEventAction.Unknown1 :: char_id :: waypoint_type :: Some(d) :: None :: HNil)

      case SquadWaypointRequest(request_type, char_id, waypoint_type, None, None) =>
        Attempt.Successful(request_type :: char_id :: waypoint_type :: None :: None :: HNil)

      case data: SquadWaypointRequest =>
        Attempt.Failure(Err(s"unexpected format while encoding - $data"))
    }
  )
}

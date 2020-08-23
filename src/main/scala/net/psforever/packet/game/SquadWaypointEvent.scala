// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{SquadWaypoints, Vector3}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

final case class WaypointEvent(zone_number: Int, pos: Vector3, unk: Int)

final case class SquadWaypointEvent(
    event_type: WaypointEventAction.Value,
    unk: Int,
    char_id: Long,
    waypoint_type: SquadWaypoints.Value,
    unk5: Option[Long],
    waypoint_info: Option[WaypointEvent]
) extends PlanetSideGamePacket {
  type Packet = SquadWaypointEvent
  def opcode = GamePacketOpcode.SquadWaypointEvent
  def encode = SquadWaypointEvent.encode(this)
}

object SquadWaypointEvent extends Marshallable[SquadWaypointEvent] {
  def Add(unk: Int, char_id: Long, waypoint_type: SquadWaypoints.Value, waypoint: WaypointEvent): SquadWaypointEvent =
    SquadWaypointEvent(WaypointEventAction.Add, unk, char_id, waypoint_type, None, Some(waypoint))

  def Unknown1(unk: Int, char_id: Long, waypoint_type: SquadWaypoints.Value, unk_a: Long): SquadWaypointEvent =
    SquadWaypointEvent(WaypointEventAction.Unknown1, unk, char_id, waypoint_type, Some(unk_a), None)

  def Remove(unk: Int, char_id: Long, waypoint_type: SquadWaypoints.Value): SquadWaypointEvent =
    SquadWaypointEvent(WaypointEventAction.Remove, unk, char_id, waypoint_type, None, None)

  private val waypoint_codec: Codec[WaypointEvent] = (
    ("zone_number" | uint16L) ::
      ("pos" | Vector3.codec_pos) ::
      ("unk" | uint(3))
  ).as[WaypointEvent]

  implicit val codec: Codec[SquadWaypointEvent] = (
    ("event_type" | WaypointEventAction.codec) >>:~ { event_type =>
      ("unk" | uint16L) ::
        ("char_id" | uint32L) ::
        ("waypoint_type" | SquadWaypoints.codec) ::
        ("unk5" | conditional(event_type == WaypointEventAction.Unknown1, uint32L)) ::
        ("waypoint_info" | conditional(event_type == WaypointEventAction.Add, waypoint_codec))
    }
  ).exmap[SquadWaypointEvent](
    {
      case WaypointEventAction.Add :: a :: char_id :: waypoint_type :: None :: Some(waypoint) :: HNil =>
        Attempt.Successful(SquadWaypointEvent(WaypointEventAction.Add, a, char_id, waypoint_type, None, Some(waypoint)))

      case WaypointEventAction.Unknown1 :: a :: char_id :: waypoint_type :: Some(d) :: None :: HNil =>
        Attempt.Successful(SquadWaypointEvent(WaypointEventAction.Unknown1, a, char_id, waypoint_type, Some(d), None))

      case event_type :: b :: char_id :: waypoint_type :: None :: None :: HNil =>
        Attempt.Successful(SquadWaypointEvent(event_type, b, char_id, waypoint_type, None, None))

      case data =>
        Attempt.Failure(Err(s"unexpected format for $data"))
    },
    {
      case SquadWaypointEvent(WaypointEventAction.Add, a, char_id, waypoint_type, None, Some(waypoint)) =>
        Attempt.Successful(WaypointEventAction.Add :: a :: char_id :: waypoint_type :: None :: Some(waypoint) :: HNil)

      case SquadWaypointEvent(WaypointEventAction.Unknown1, a, char_id, waypoint_type, Some(d), None) =>
        Attempt.Successful(WaypointEventAction.Unknown1 :: a :: char_id :: waypoint_type :: Some(d) :: None :: HNil)

      case SquadWaypointEvent(event_type, b, char_id, waypoint_type, None, None) =>
        Attempt.Successful(event_type :: b :: char_id :: waypoint_type :: None :: None :: HNil)

      case data =>
        Attempt.Failure(Err(s"unexpected format for $data"))
    }
  )
}

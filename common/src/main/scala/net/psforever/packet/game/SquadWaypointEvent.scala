// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

final case class WaypointEvent(unk1 : Int,
                               pos : Vector3,
                               unk2 : Int)

final case class SquadWaypointEvent(unk1 : Int,
                                    unk2 : Int,
                                    unk3 : Long,
                                    unk4 : Int,
                                    unk5 : Option[Long],
                                    unk6 : Option[WaypointEvent])
  extends PlanetSideGamePacket {
  type Packet = SquadWaypointEvent
  def opcode = GamePacketOpcode.SquadWaypointEvent
  def encode = SquadWaypointEvent.encode(this)
}

object SquadWaypointEvent extends Marshallable[SquadWaypointEvent] {
  def apply(unk1 : Int, unk2 : Int, unk3 : Long, unk4 : Int, unk_a : Long) : SquadWaypointEvent =
    SquadWaypointEvent(unk1, unk2, unk3, unk4, Some(unk_a), None)

  def apply(unk1 : Int, unk2 : Int, unk3 : Long, unk4 : Int, unk_a : Int, pos : Vector3, unk_b : Int) : SquadWaypointEvent =
    SquadWaypointEvent(unk1, unk2, unk3, unk4, None, Some(WaypointEvent(unk_a, pos, unk_b)))

  def apply(unk1 : Int, unk2 : Int, unk3 : Long, unk4 : Int) : SquadWaypointEvent =
    SquadWaypointEvent(unk1, unk2, unk3, unk4, None, None)

  private val waypoint_codec : Codec[WaypointEvent] = (
    ("unk1" | uint16L) ::
      ("pos" | Vector3.codec_pos) ::
      ("unk2" | uint(3))
    ).as[WaypointEvent]

  implicit val codec : Codec[SquadWaypointEvent] = (
    ("unk1" | uint2) >>:~ { unk1 =>
      ("unk2" | uint16L) ::
      ("unk3" | uint32L) ::
        ("unk4" | uint8L) ::
        ("unk5" | conditional(unk1 == 1, uint32L)) ::
        ("unk6" | conditional(unk1 == 0, waypoint_codec))
    }
  ).exmap[SquadWaypointEvent] (
    {
      case 0 :: a :: b :: c :: None :: Some(d) :: HNil =>
        Attempt.Successful(SquadWaypointEvent(0, a, b, c, None, Some(d)))

      case 1 :: a :: b :: c :: Some(d) :: None :: HNil =>
        Attempt.Successful(SquadWaypointEvent(1, a, b, c, Some(d), None))

      case a :: b :: c :: d :: None :: None :: HNil =>
        Attempt.Successful(SquadWaypointEvent(a, b, c, d, None, None))

      case n :: _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.Failure(Err(s"unexpected format for unk1 - $n"))
    },
    {
      case SquadWaypointEvent(0, a, b, c, None, Some(d)) =>
        Attempt.Successful(0 :: a :: b :: c :: None :: Some(d) :: HNil)

      case SquadWaypointEvent(1, a, b, c, Some(d), None) =>
        Attempt.Successful(1 :: a :: b :: c :: Some(d) :: None :: HNil)

      case SquadWaypointEvent(a, b, c, d, None, None) =>
        Attempt.Successful(a :: b :: c :: d :: None :: None :: HNil)

      case SquadWaypointEvent(n, _, _, _, _, _) =>
        Attempt.Failure(Err(s"unexpected format for unk1 - $n"))
    }
  )
}

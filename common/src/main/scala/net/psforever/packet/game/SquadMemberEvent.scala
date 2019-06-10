// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

final case class SquadMemberEvent(unk1 : Int,
                                  unk2 : Int,
                                  unk3 : Long,
                                  unk4 : Int,
                                  unk5 : Option[String],
                                  unk6 : Option[Int],
                                  unk7 : Option[Long])
  extends PlanetSideGamePacket {
  type Packet = SquadMemberEvent
  def opcode = GamePacketOpcode.SquadMemberEvent
  def encode = SquadMemberEvent.encode(this)
}

object SquadMemberEvent extends Marshallable[SquadMemberEvent] {
  def apply(unk1 : Int, unk2 : Int, unk3 : Long, unk4 : Int) : SquadMemberEvent =
    SquadMemberEvent(unk1, unk2, unk3, unk4, None, None, None)

  def apply(unk2 : Int, unk3 : Long, unk4 : Int, unk5 : String, unk6 : Int, unk7 : Long) : SquadMemberEvent =
    SquadMemberEvent(0, unk2, unk3, unk4, Some(unk5), Some(unk6), Some(unk7))

  def apply(unk2 : Int, unk3 : Long, unk4 : Int, unk6 : Int) : SquadMemberEvent =
    SquadMemberEvent(3, unk2, unk3, unk4, None, Some(unk6), None)

  def apply(unk2 : Int, unk3 : Long, unk4 : Int, unk7 : Long) : SquadMemberEvent =
    SquadMemberEvent(4, unk2, unk3, unk4, None, None, Some(unk7))

  implicit val codec : Codec[SquadMemberEvent] = (
    ("unk1" | uintL(3)) >>:~ { unk1 =>
      ("unk2" | uint16L) ::
        ("unk3" | uint32L) ::
        ("unk4" | uintL(4)) ::
        conditional(unk1 == 0, "unk5" | PacketHelpers.encodedWideStringAligned(1)) ::
        conditional(unk1 == 0 || unk1 == 3, "unk6" | uint16L) ::
        conditional(unk1 == 0 || unk1 == 4, "unk7" | uint32L)
    }).exmap[SquadMemberEvent] (
    {
      case unk1 :: unk2 :: unk3 :: unk4 :: unk5 :: unk6 :: unk7 :: HNil =>
        Attempt.Successful(SquadMemberEvent(unk1, unk2, unk3, unk4, unk5, unk6, unk7))
    },
    {
      case data @ SquadMemberEvent(0, unk2, unk3, unk4, Some(unk5), Some(unk6), Some(unk7)) =>
        Attempt.Successful(0 :: unk2 :: unk3 :: unk4 :: Some(unk5) :: Some(unk6) :: Some(unk7) :: HNil)
      case data @ SquadMemberEvent(3, unk2, unk3, unk4, None, Some(unk6), None) =>
        Attempt.Successful(3 :: unk2 :: unk3 :: unk4 :: None :: Some(unk6) :: None :: HNil)
      case data @ SquadMemberEvent(4, unk2, unk3, unk4, None, None, Some(unk7)) =>
        Attempt.Successful(4 :: unk2 :: unk3 :: unk4 :: None :: None :: Some(unk7) :: HNil)
      case data @ SquadMemberEvent(unk1, unk2, unk3, unk4, None, None, None) =>
        Attempt.Successful(unk1 :: unk2 :: unk3 :: unk4 :: None :: None :: None :: HNil)
      case data =>
        Attempt.Failure(Err(s"SquadMemberEvent can not encode with this pattern - $data"))
    }
  )
}

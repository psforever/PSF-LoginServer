// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

object MemberEvent extends Enumeration {
  type Type = Value

  val
  Add,
  Remove,
  Unknown2,
  UpdateZone,
  Unknown4
    = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint(3))
}

final case class SquadMemberEvent(unk1 : Int,
                                  unk2 : Int,
                                  char_id : Long,
                                  member_position : Int,
                                  player_name : Option[String],
                                  zone_number : Option[Int],
                                  unk7 : Option[Long])
  extends PlanetSideGamePacket {
  type Packet = SquadMemberEvent
  def opcode = GamePacketOpcode.SquadMemberEvent
  def encode = SquadMemberEvent.encode(this)
}

object SquadMemberEvent extends Marshallable[SquadMemberEvent] {
  def apply(unk1 : Int, unk2 : Int, char_id : Long, member_position : Int) : SquadMemberEvent =
    SquadMemberEvent(unk1, unk2, char_id, member_position, None, None, None)

  def apply(unk2 : Int, char_id : Long, member_position : Int, player_name : String, zone_number : Int, unk7 : Long) : SquadMemberEvent =
    SquadMemberEvent(0, unk2, char_id, member_position, Some(player_name), Some(zone_number), Some(unk7))

  def apply(unk2 : Int, char_id : Long, member_position : Int, zone_number : Int) : SquadMemberEvent =
    SquadMemberEvent(3, unk2, char_id, member_position, None, Some(zone_number), None)

  def apply(unk2 : Int, char_id : Long, member_position : Int, unk7 : Long) : SquadMemberEvent =
    SquadMemberEvent(4, unk2, char_id, member_position, None, None, Some(unk7))

  implicit val codec : Codec[SquadMemberEvent] = (
    ("unk1" | uint(3)) >>:~ { unk1 =>
      ("unk2" | uint16L) ::
        ("char_id" | uint32L) ::
        ("member_position" | uint4) ::
        conditional(unk1 == 0, "player_name" | PacketHelpers.encodedWideStringAligned(1)) ::
        conditional(unk1 == 0 || unk1 == 3, "zone_number" | uint16L) ::
        conditional(unk1 == 0 || unk1 == 4, "unk7" | uint32L)
    }).exmap[SquadMemberEvent] (
    {
      case unk1 :: unk2 :: char_id :: member_position :: player_name :: zone_number :: unk7 :: HNil =>
        Attempt.Successful(SquadMemberEvent(unk1, unk2, char_id, member_position, player_name, zone_number, unk7))
    },
    {
      case data @ SquadMemberEvent(0, unk2, char_id, member_position, Some(player_name), Some(zone_number), Some(unk7)) =>
        Attempt.Successful(0 :: unk2 :: char_id :: member_position :: Some(player_name) :: Some(zone_number) :: Some(unk7) :: HNil)
      case data @ SquadMemberEvent(3, unk2, char_id, member_position, None, Some(zone_number), None) =>
        Attempt.Successful(3 :: unk2 :: char_id :: member_position :: None :: Some(zone_number) :: None :: HNil)
      case data @ SquadMemberEvent(4, unk2, char_id, member_position, None, None, Some(unk7)) =>
        Attempt.Successful(4 :: unk2 :: char_id :: member_position :: None :: None :: Some(unk7) :: HNil)
      case data @ SquadMemberEvent(unk1, unk2, char_id, member_position, None, None, None) =>
        Attempt.Successful(unk1 :: unk2 :: char_id :: member_position :: None :: None :: None :: HNil)
      case data =>
        Attempt.Failure(Err(s"SquadMemberEvent can not encode with this pattern - $data"))
    }
  )
}

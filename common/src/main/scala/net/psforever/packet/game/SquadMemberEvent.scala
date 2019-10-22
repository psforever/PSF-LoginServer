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
  Promote,
  UpdateZone,
  Unknown4
    = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(enum = this, uint(bits = 3))
}

final case class SquadMemberEvent(action : MemberEvent.Value,
                                  unk2 : Int,
                                  char_id : Long,
                                  position : Int,
                                  player_name : Option[String],
                                  zone_number : Option[Int],
                                  unk7 : Option[Long])
  extends PlanetSideGamePacket {
  type Packet = SquadMemberEvent
  def opcode = GamePacketOpcode.SquadMemberEvent
  def encode = SquadMemberEvent.encode(this)
}

object SquadMemberEvent extends Marshallable[SquadMemberEvent] {
  def apply(action : MemberEvent.Value, unk2 : Int, char_id : Long, position : Int) : SquadMemberEvent =
    SquadMemberEvent(action, unk2, char_id, position, None, None, None)

  def Add(unk2 : Int, char_id : Long, position : Int, player_name : String, zone_number : Int, unk7 : Long) : SquadMemberEvent =
    SquadMemberEvent(MemberEvent.Add, unk2, char_id, position, Some(player_name), Some(zone_number), Some(unk7))

  def Remove(unk2 : Int, char_id : Long, position : Int) : SquadMemberEvent =
    SquadMemberEvent(MemberEvent.Remove, unk2, char_id, position, None, None, None)

  def Promote(unk2 : Int, char_id : Long) : SquadMemberEvent =
    SquadMemberEvent(MemberEvent.Promote, unk2, char_id, 0, None, None, None)

  def UpdateZone(unk2 : Int, char_id : Long, position : Int, zone_number : Int) : SquadMemberEvent =
    SquadMemberEvent(MemberEvent.UpdateZone, unk2, char_id, position, None, Some(zone_number), None)

  def Unknown4(unk2 : Int, char_id : Long, position : Int, unk7 : Long) : SquadMemberEvent =
    SquadMemberEvent(MemberEvent.Unknown4, unk2, char_id, position, None, None, Some(unk7))

  implicit val codec : Codec[SquadMemberEvent] = (
    ("action" | MemberEvent.codec) >>:~ { action =>
      ("unk2" | uint16L) ::
        ("char_id" | uint32L) ::
        ("position" | uint4) ::
        conditional(action == MemberEvent.Add, "player_name" | PacketHelpers.encodedWideStringAligned(1)) ::
        conditional(action == MemberEvent.Add || action == MemberEvent.UpdateZone, "zone_number" | uint16L) ::
        conditional(action == MemberEvent.Add || action == MemberEvent.Unknown4, "unk7" | uint32L)
    }).exmap[SquadMemberEvent] (
    {
      case action :: unk2 :: char_id :: member_position :: player_name :: zone_number :: unk7 :: HNil =>
        Attempt.Successful(SquadMemberEvent(action, unk2, char_id, member_position, player_name, zone_number, unk7))
    },
    {
      case SquadMemberEvent(MemberEvent.Add, unk2, char_id, member_position, Some(player_name), Some(zone_number), Some(unk7)) =>
        Attempt.Successful(MemberEvent.Add :: unk2 :: char_id :: member_position :: Some(player_name) :: Some(zone_number) :: Some(unk7) :: HNil)
      case SquadMemberEvent(MemberEvent.UpdateZone, unk2, char_id, member_position, None, Some(zone_number), None) =>
        Attempt.Successful(MemberEvent.UpdateZone :: unk2 :: char_id :: member_position :: None :: Some(zone_number) :: None :: HNil)
      case SquadMemberEvent(MemberEvent.Unknown4, unk2, char_id, member_position, None, None, Some(unk7)) =>
        Attempt.Successful(MemberEvent.Unknown4 :: unk2 :: char_id :: member_position :: None :: None :: Some(unk7) :: HNil)
      case SquadMemberEvent(action, unk2, char_id, member_position, None, None, None) =>
        Attempt.Successful(action :: unk2 :: char_id :: member_position :: None :: None :: None :: HNil)
      case data =>
        Attempt.Failure(Err(s"SquadMemberEvent can not encode with this pattern - $data"))
    }
  )
}

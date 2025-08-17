// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMemberEvent(
  unk00: Int,
  outfit_id: Long,
  unk3: Int, // OMR(Unk1) target_guid
  unk5: Int, // OMR(Unk1) unk3
  member_name: String,
  unk8: Int,
  unk9: Int,
  unk10: Int,
  unk11: Int,
  unk12: Int,
  unk13: Int,
  unk14: Int,
  unk15: Int,
  unk16: Int,
) extends PlanetSideGamePacket {
  type Packet = OutfitMemberEvent
  def opcode = GamePacketOpcode.OutfitMemberEvent
  def encode = OutfitMemberEvent.encode(this)
}

object OutfitMemberEvent extends Marshallable[OutfitMemberEvent] {
  implicit val codec: Codec[OutfitMemberEvent] = (
    ("unk00" | uintL(2)) ::
    ("outfit_id" | uint32L) ::
      ("unk3" | uint16L) :: // OMR(Unk1) unk2
      ("unk5" | uint16L) ::
      ("member_name" | PacketHelpers.encodedWideStringAligned(6)) ::
      ("unk8" | uint8L) ::
      ("unk9" | uint8L) ::
      ("unk10" | uint8L) ::
      ("unk11" | uint8L) ::
      ("unk12" | uint8L) ::
      ("unk13" | uint8L) ::
      ("unk14" | uint8L) ::
      ("unk15" | uint8L) ::
      ("unk16" | uint8L)
    ).xmap[OutfitMemberEvent](
    {
      case unk00 :: outfit_id :: u3 :: u5 :: member_name :: u8 :: u9 :: u10 :: u11 :: u12 :: u13 :: u14 :: u15 :: u16 :: HNil =>
        OutfitMemberEvent(unk00, outfit_id, u3, u5, member_name, u8, u9, u10, u11, u12, u13, u14, u15, u16)
    },
    {
      case OutfitMemberEvent(unk00, outfit_id, u3, u5, member_name, u8, u9, u10, u11, u12, u13, u14, u15, u16) =>
        unk00 :: outfit_id :: u3 :: u5 :: member_name :: u8 :: u9 :: u10 :: u11 :: u12 :: u13 :: u14 :: u15 :: u16 :: HNil
    }
  )
}

// Copyright (c) 2024 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMemberEvent(
  unk00: Int,
  outfit_id: PlanetSideGUID,
  unk1: Int,
  unk2: Int,
  unk3: Int,
  member_name: String,
  unk7: Int,
  unk8: Int,
  unk9: Int,
  unk10: Int,
  unk12: Int,
  unk13: Int,
  unk14: Int,
  unk15: Int,
) extends PlanetSideGamePacket {
  type Packet = OutfitMemberEvent
  def opcode = GamePacketOpcode.OutfitMemberEvent
  def encode = OutfitMemberEvent.encode(this)
}

object OutfitMemberEvent extends Marshallable[OutfitMemberEvent] {
  implicit val codec: Codec[OutfitMemberEvent] = (
    ("unk00" | uintL(2)) ::
    ("outfit_id" | PlanetSideGUID.codec) ::
      ("unk1" | uint16L) ::
      ("unk2" | uint16L) ::
      ("unk3" | uint16L) ::
      ("member_name" | PacketHelpers.encodedWideStringAligned(6)) ::
      ("unk7" | uint8L) ::
      ("unk8" | uint8L) ::
      ("unk9" | uint16L) ::
      ("unk10" | uint8L) ::
      ("unk12" | uint8L) ::
      ("unk13" | uint8L) ::
      ("unk14" | uint8L) ::
      ("unk15" | uint8L)
    ).xmap[OutfitMemberEvent](
    {
      case unk00 :: outfit_id :: u1 :: u2 :: u3 :: member_name :: u7 :: u8 :: u9 :: u10 :: u12 :: u13 :: u14 :: u15 :: HNil =>
        OutfitMemberEvent(unk00, outfit_id, u1, u2, u3, member_name, u7, u8, u9, u10, u12, u13, u14, u15)
    },
    {
      case OutfitMemberEvent(unk00, outfit_id, u1, u2, u3, member_name, u7, u8, u9, u10, u12, u13, u14, u15) =>
        unk00 :: outfit_id :: u1 :: u2 :: u3 :: member_name :: u7 :: u8 :: u9 :: u10 :: u12 :: u13 :: u14 :: u15 :: HNil
    }
  )
}

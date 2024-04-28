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
  unk4: Int,
  unk5: Int,
  unk6: Int,
  member_name: String,
  unk7: Int,
  unk8: Int,
  unk9: Int,
  unk10: Int,
  unk11: Int,
) extends PlanetSideGamePacket {
  type Packet = OutfitMemberEvent
  def opcode = GamePacketOpcode.OutfitMemberEvent
  def encode = OutfitMemberEvent.encode(this)
}

object OutfitMemberEvent extends Marshallable[OutfitMemberEvent] {
  implicit val codec: Codec[OutfitMemberEvent] = (
    ("unk00" | uintL(2)) ::
    ("outfit_id" | PlanetSideGUID.codec) ::
      ("unk1" | uint8L) ::
      ("unk2" | uint8L) ::
      ("unk3" | uint8L) ::
      ("unk4" | uint8L) ::
      ("unk5" | uint8L) ::
      ("unk6" | uint8L) ::
      ("member_name" | PacketHelpers.encodedWideStringAligned(6)) ::
      ("unk7" | uint16L) ::
      ("unk8" | uint16L) ::
      ("unk9" | uint16L) ::
      ("unk10" | uint16L) ::
      ("unk11" | uint8L)
    ).xmap[OutfitMemberEvent](
    {
      case unk00 :: outfit_id :: u1 :: u2 :: u3 :: u4 :: u5 :: u6 :: member_name :: u7 :: u8 :: u9 :: u10 :: u11 :: HNil =>
        OutfitMemberEvent(unk00, outfit_id, u1, u2, u3, u4, u5, u6, member_name, u7, u8, u9, u10, u11)
    },
    {
      case OutfitMemberEvent(unk00, outfit_id, u1, u2, u3, u4, u5, u6, member_name, u7, u8, u9, u10, u11) =>
        unk00 :: outfit_id :: u1 :: u2 :: u3 :: u4 :: u5 :: u6 :: member_name :: u7 :: u8 :: u9 :: u10 :: u11 :: HNil
    }
  )
}

// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMembershipResponse(
    packet_type: OutfitMembershipResponse.PacketType.Type,
    unk0: Int,
    unk1: Int,
    outfit_id: Long,
    target_id: Long,
    str1: String,
    str2: String,
    flag: Boolean
  ) extends PlanetSideGamePacket {
  type Packet = OutfitMembershipResponse

  def opcode: Type = GamePacketOpcode.OutfitMembershipResponse

  def encode: Attempt[BitVector] = OutfitMembershipResponse.encode(this)
}

object OutfitMembershipResponse extends Marshallable[OutfitMembershipResponse] {

  object PacketType extends Enumeration {
    type Type = Value

    val CreateResponse: PacketType.Value = Value(0)
    val Unk1: PacketType.Value = Value(1) // Info: Player has been invited / response to OutfitMembershipRequest Unk2 for that player
    val Unk2: PacketType.Value = Value(2) // Invited / Accepted / Added
    val Unk3: PacketType.Value = Value(3)
    val Unk4: PacketType.Value = Value(4)
    val Unk5: PacketType.Value = Value(5)
    val Unk6: PacketType.Value = Value(6) // 6 and 7 seen as failed decodes, validity unknown
    val Unk7: PacketType.Value = Value(7)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  implicit val codec: Codec[OutfitMembershipResponse] = (
    ("packet_type" | PacketType.codec) ::
    ("unk0" | uintL(5)) ::
    ("unk1" | uintL(3)) ::
    ("outfit_id" | uint32L) ::
    ("target_id" | uint32L) ::
    ("str1" | PacketHelpers.encodedWideStringAligned(5)) ::
    ("str2" | PacketHelpers.encodedWideString) ::
    ("flag" | bool)
  ).xmap[OutfitMembershipResponse](
    {
      case packet_type :: u0 :: u1 :: outfit_id :: target_id :: str1 :: str2 :: flag :: HNil =>
        OutfitMembershipResponse(packet_type, u0, u1, outfit_id, target_id, str1, str2, flag)
    },
    {
      case OutfitMembershipResponse(packet_type, u0, u1, outfit_id, target_id, str1, str2, flag) =>
        packet_type :: u0 :: u1 :: outfit_id :: target_id :: str1 :: str2 :: flag :: HNil
    }
  )
}

// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMemberUpdate(
  outfit_guid: Long,
  unk1: Int,
  unk2: Int,
  unk3: Int,
  unk4: Int,
  unk5: Int,
) extends PlanetSideGamePacket {
  type Packet = OutfitMemberUpdate
  def opcode = GamePacketOpcode.OutfitMemberUpdate
  def encode = OutfitMemberUpdate.encode(this)
}

object OutfitMemberUpdate extends Marshallable[OutfitMemberUpdate] {
  implicit val codec: Codec[OutfitMemberUpdate] = (
    ("outfit_guid" | uint32L) ::
      ("unk1" | uint8L) ::
      ("unk2" | uint8L) ::
      ("unk3" | uint8L) ::
      ("unk4" | uint8L) ::
      ("unk5" | uint8L)
    ).xmap[OutfitMemberUpdate](
    {
      case outfit_guid :: u1 :: u2 :: u3 :: u4 :: u5 :: HNil =>
        OutfitMemberUpdate(outfit_guid, u1, u2, u3, u4, u5)
    },
    {
      case OutfitMemberUpdate(outfit_guid, u1, u2, u3, u4, u5) =>
        outfit_guid :: u1 :: u2 :: u3 :: u4 :: u5 :: HNil
    }
  )
}

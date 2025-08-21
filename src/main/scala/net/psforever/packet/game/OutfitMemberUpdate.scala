// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMemberUpdate(
  outfit_guid: Long,
  char_id: Long,
  rank: Int, // 0-7
  unk1: Int,
) extends PlanetSideGamePacket {
  type Packet = OutfitMemberUpdate
  def opcode = GamePacketOpcode.OutfitMemberUpdate
  def encode = OutfitMemberUpdate.encode(this)
}

object OutfitMemberUpdate extends Marshallable[OutfitMemberUpdate] {
  implicit val codec: Codec[OutfitMemberUpdate] = (
    ("outfit_guid" | uint32L) ::
      ("char_id" | uint32L) ::
      ("rank" | uint(3)) ::
      ("unk1" | uint(5))
    ).xmap[OutfitMemberUpdate](
    {
      case outfit_guid :: char_id :: rank :: u1 :: HNil =>
        OutfitMemberUpdate(outfit_guid, char_id, rank, u1)
    },
    {
      case OutfitMemberUpdate(outfit_guid, char_id, rank, u1) =>
        outfit_guid :: char_id :: rank :: u1 :: HNil
    }
  )
}

// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMemberUpdate(
    outfit_id: Long,
    char_id: Long,
    rank: Int, // 0-7
    flag: Boolean,
) extends PlanetSideGamePacket {
  type Packet = OutfitMemberUpdate
  def opcode = GamePacketOpcode.OutfitMemberUpdate
  def encode = OutfitMemberUpdate.encode(this)
}

object OutfitMemberUpdate extends Marshallable[OutfitMemberUpdate] {
  implicit val codec: Codec[OutfitMemberUpdate] = (
    ("outfit_id" | uint32L) ::
    ("char_id" | uint32L) ::
    ("rank" | uint(3)) ::
    ("flag" | bool)
  ).xmap[OutfitMemberUpdate](
    {
      case outfit_id :: char_id :: rank :: flag :: HNil =>
        OutfitMemberUpdate(outfit_id, char_id, rank, flag)
    },
    {
      case OutfitMemberUpdate(outfit_id, char_id, rank, flag) =>
        outfit_id :: char_id :: rank :: flag :: HNil
    }
  )
}

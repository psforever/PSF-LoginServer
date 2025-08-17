// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMemberUpdate(
  outfit_guid: Long,
  avatar_guid: PlanetSideGUID,
  unk3: Int,
) extends PlanetSideGamePacket {
  type Packet = OutfitMemberUpdate
  def opcode = GamePacketOpcode.OutfitMemberUpdate
  def encode = OutfitMemberUpdate.encode(this)
}

object OutfitMemberUpdate extends Marshallable[OutfitMemberUpdate] {
  implicit val codec: Codec[OutfitMemberUpdate] = (
    ("outfit_guid" | uint32L) ::
      ("avatar_guid" | PlanetSideGUID.codec) ::
      ("unk3" | uint8L)
    ).xmap[OutfitMemberUpdate](
    {
      case outfit_guid :: u2 :: u3 :: HNil =>
        OutfitMemberUpdate(outfit_guid, u2, u3)
    },
    {
      case OutfitMemberUpdate(outfit_guid, u2, u3) =>
        outfit_guid :: u2 :: u3 :: HNil
    }
  )
}

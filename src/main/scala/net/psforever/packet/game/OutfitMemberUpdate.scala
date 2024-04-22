// Copyright (c) 2024 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMemberUpdate(outfit_id: PlanetSideGUID, unk1: Int, unk2: Int, unk3: Int) extends PlanetSideGamePacket {
  type Packet = OutfitMemberUpdate
  def opcode = GamePacketOpcode.OutfitMemberUpdate
  def encode = OutfitMemberUpdate.encode(this)
}

object OutfitMemberUpdate extends Marshallable[OutfitMemberUpdate] {
  implicit val codec: Codec[OutfitMemberUpdate] = (
    ("outfit_id" | PlanetSideGUID.codec) ::
      ("unk1" | uint16L) ::
      ("unk2" | uint16L) ::
      ("unk3" | uint8L)
    ).xmap[OutfitMemberUpdate](
    {
      case u0 :: u1 :: u2 :: u3 :: HNil =>
        OutfitMemberUpdate(u0, u1, u2, u3)
    },
    {
      case OutfitMemberUpdate(u0, u1, u2, u3) =>
        u0 :: u1 :: u2 :: u3 :: HNil
    }
  )
//  ).as[OutfitMemberUpdate]
}

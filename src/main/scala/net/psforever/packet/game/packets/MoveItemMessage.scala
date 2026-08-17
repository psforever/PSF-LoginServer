// Copyright (c) 2017 PSForever
package net.psforever.packet.game.packets

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec}

final case class MoveItemMessage(
    itemGuid: PlanetSideGUID,
    avatarGuid1: PlanetSideGUID,
    avatarGuid2: PlanetSideGUID,
    dest: Int,
    quantity: Int
) extends PlanetSideGamePacket {
  type Packet = MoveItemMessage

  def opcode: Type = GamePacketOpcode.MoveItemMessage

  def encode: Attempt[BitVector] = MoveItemMessage.encode(this)
}

object MoveItemMessage extends Marshallable[MoveItemMessage] {
  implicit val codec: Codec[MoveItemMessage] = (
    ("item_guid" | PlanetSideGUID.codec) ::
      ("avatar_guid_1" | PlanetSideGUID.codec) ::
      ("avatar_guid_2" | PlanetSideGUID.codec) ::
      ("dest" | uint16L) ::
      ("quantity" | uint16L)
  ).as[MoveItemMessage]
}

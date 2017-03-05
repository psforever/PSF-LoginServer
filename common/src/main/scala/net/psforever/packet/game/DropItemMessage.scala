// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class DropItemMessage(item_guid : PlanetSideGUID)
  extends PlanetSideGamePacket {
  type Packet = DropItemMessage
  def opcode = GamePacketOpcode.DropItemMessage
  def encode = DropItemMessage.encode(this)
}

object DropItemMessage extends Marshallable[DropItemMessage] {
  implicit val codec : Codec[DropItemMessage] = (
      ("item_guid" | PlanetSideGUID.codec)
    ).as[DropItemMessage]
}

// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class RequestDestroyMessage(object_guid : PlanetSideGUID)
  extends PlanetSideGamePacket {
  type Packet = RequestDestroyMessage
  def opcode = GamePacketOpcode.RequestDestroyMessage
  def encode = RequestDestroyMessage.encode(this)
}

object RequestDestroyMessage extends Marshallable[RequestDestroyMessage] {
  implicit val codec : Codec[RequestDestroyMessage] = (
      ("object_guid" | PlanetSideGUID.codec)
    ).as[RequestDestroyMessage]
}

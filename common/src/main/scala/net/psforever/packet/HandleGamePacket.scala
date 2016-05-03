// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import scodec.bits.ByteVector
import scodec.Codec
import scodec.codecs._

final case class HandleGamePacket(packet : ByteVector)
  extends PlanetSideControlPacket {
  def opcode = ControlPacketOpcode.HandleGamePacket
  def encode = throw new Exception("This packet type should never be encoded")
}

object HandleGamePacket extends Marshallable[HandleGamePacket] {
  implicit val codec : Codec[HandleGamePacket] = bytes.as[HandleGamePacket].decodeOnly
}
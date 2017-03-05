// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

final case class HandleGamePacket(packet : ByteVector)
  extends PlanetSideControlPacket {
  def opcode = ControlPacketOpcode.HandleGamePacket
  def encode = throw new Exception("This packet type should never be encoded")
}

object HandleGamePacket extends Marshallable[HandleGamePacket] {
  implicit val codec : Codec[HandleGamePacket] = bytes.as[HandleGamePacket].decodeOnly
}
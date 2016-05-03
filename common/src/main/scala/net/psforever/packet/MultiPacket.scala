// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import scodec.bits.ByteVector
import scodec.Codec
import scodec.codecs._
import scodec.bits._

final case class MultiPacket(packets : Vector[ByteVector])
  extends PlanetSideControlPacket {
  type Packet = MultiPacket
  def opcode = ControlPacketOpcode.MultiPacket
  def encode = MultiPacket.encode(this)
}

object MultiPacket extends Marshallable[MultiPacket] {
  implicit val codec : Codec[MultiPacket] = ("packets" | vector(variableSizeBytes(uint8L, bytes))).as[MultiPacket]
}
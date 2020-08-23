// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

final case class MultiPacket(packets: Vector[ByteVector]) extends PlanetSideControlPacket {
  type Packet = MultiPacket
  def opcode = ControlPacketOpcode.MultiPacket
  def encode = MultiPacket.encode(this)
}

object MultiPacket extends Marshallable[MultiPacket] {
  implicit val codec: Codec[MultiPacket] = ("packets" | vector(variableSizeBytes(uint8L, bytes))).as[MultiPacket]
}

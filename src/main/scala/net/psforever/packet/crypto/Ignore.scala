// Copyright (c) 2021 PSForever
package net.psforever.packet.crypto

import net.psforever.packet.{CryptoPacketOpcode, Marshallable, PlanetSideCryptoPacket}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

final case class Ignore(data: ByteVector) extends PlanetSideCryptoPacket {
  type Packet = Ignore
  def opcode = CryptoPacketOpcode.Ignore
  def encode = Ignore.encode(this)
}

object Ignore extends Marshallable[Ignore] {
  implicit val codec: Codec[Ignore] = ("data" | bytes).as[Ignore]
}

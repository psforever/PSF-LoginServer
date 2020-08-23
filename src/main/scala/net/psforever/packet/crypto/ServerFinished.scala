// Copyright (c) 2017 PSForever
package net.psforever.packet.crypto

import net.psforever.packet.{CryptoPacketOpcode, Marshallable, PlanetSideCryptoPacket}
import scodec.Codec
import scodec.bits.{ByteVector, _}
import scodec.codecs._

final case class ServerFinished(challengeResult: ByteVector) extends PlanetSideCryptoPacket {
  type Packet = ServerFinished
  def opcode = CryptoPacketOpcode.ServerFinished
  def encode = ServerFinished.encode(this)
}

object ServerFinished extends Marshallable[ServerFinished] {
  implicit val codec: Codec[ServerFinished] = (
    ("unknown" | constant(hex"0114".bits)) ::
      ("challenge_result" | bytes(0xc))
  ).as[ServerFinished]
}

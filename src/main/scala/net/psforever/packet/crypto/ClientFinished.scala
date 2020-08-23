// Copyright (c) 2017 PSForever
package net.psforever.packet.crypto

import net.psforever.packet.{CryptoPacketOpcode, Marshallable, PlanetSideCryptoPacket}
import scodec.Codec
import scodec.bits.{ByteVector, _}
import scodec.codecs._

final case class ClientFinished(pubKey: ByteVector, challengeResult: ByteVector) extends PlanetSideCryptoPacket {
  type Packet = ClientFinished
  def opcode = CryptoPacketOpcode.ClientFinished
  def encode = ClientFinished.encode(this)
}

object ClientFinished extends Marshallable[ClientFinished] {
  implicit val codec: Codec[ClientFinished] = (
    ("obj_type?" | constant(hex"10".bits)) ::
      ("pub_key_len" | constant(hex"1000")) ::
      ("pub_key" | bytes(16)) ::
      ("unknown" | constant(hex"0114".bits)) ::
      ("challenge_result" | bytes(0xc))
  ).as[ClientFinished]
}

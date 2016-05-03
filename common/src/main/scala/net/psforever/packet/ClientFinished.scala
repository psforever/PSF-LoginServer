// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import scodec.bits.ByteVector
import scodec.Codec
import scodec.codecs._
import scodec.bits._

final case class ClientFinished(pubKey : ByteVector, challengeResult: ByteVector)
  extends PlanetSideCryptoPacket {
  type Packet = ClientFinished
  def opcode = CryptoPacketOpcode.ClientFinished
  def encode = ClientFinished.encode(this)
}

object ClientFinished extends Marshallable[ClientFinished] {
  implicit val codec : Codec[ClientFinished] = (
    ("obj_type?" | constant(hex"10".bits)) ::
      ("pub_key_len" | constant(hex"1000")) ::
      ("pub_key" | bytes(16)) ::
      ("unknown" | constant(hex"0114".bits)) ::
      ("challenge_result" | bytes(0xc))
    ).as[ClientFinished]
}
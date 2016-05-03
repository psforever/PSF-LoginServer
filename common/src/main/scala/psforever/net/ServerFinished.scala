// Copyright (c) 2016 PSForever.net to present
package psforever.net

import scodec.bits.ByteVector
import scodec.Codec
import scodec.codecs._
import scodec.bits._

final case class ServerFinished(challengeResult : ByteVector)
  extends PlanetSideCryptoPacket {
  type Packet = ServerFinished
  def opcode = CryptoPacketOpcode.ServerFinished
  def encode = ServerFinished.encode(this)
}

object ServerFinished extends Marshallable[ServerFinished] {
  implicit val codec : Codec[ServerFinished] = (
    ("unknown" | constant(hex"0114".bits)) ::
      ("challenge_result" | bytes(0xc))
    ).as[ServerFinished]
}
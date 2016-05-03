// Copyright (c) 2016 PSForever.net to present
package psforever.net

import scodec.bits.ByteVector

import scodec.Codec
import scodec.codecs._
import scodec.bits._

final case class ClientChallengeXchg(time : Long, challenge : ByteVector, p : ByteVector, g : ByteVector)
  extends PlanetSideCryptoPacket {
  def opcode = CryptoPacketOpcode.ClientChallengeXchg
  def encode = ClientChallengeXchg.encode(this)
}

object ClientChallengeXchg extends Marshallable[ClientChallengeXchg] {
  implicit val codec: Codec[ClientChallengeXchg] = (
    ("unknown" | constant(1)) ::
      ("unknown" | constant(1)) ::
      ("client_time" | uint32L) ::
      ("challenge" | bytes(12)) ::
      ("end_chal?" | constant(0)) ::
      ("objects?" | constant(1)) ::
      ("object_type?" | constant(hex"0002".bits)) ::
      ("unknown" | constant(hex"ff240000".bits)) ::
      ("P_len" | constant(hex"1000".bits)) ::
      ("P" | bytes(16)) ::
      ("G_len" | constant(hex"1000".bits)) ::
      ("G" | bytes(16)) ::
      ("end?" | constant(0)) ::
      ("end?" | constant(0)) ::
      ("objects?" | constant(1)) ::
      ("unknown" | constant(hex"03070000".bits)) ::
      ("end?" | constant(0))
    ).as[ClientChallengeXchg]
}
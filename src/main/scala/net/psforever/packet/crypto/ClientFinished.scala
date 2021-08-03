// Copyright (c) 2017 PSForever
package net.psforever.packet.crypto

import net.psforever.packet.{CryptoPacketOpcode, Marshallable, PlanetSideCryptoPacket}
import scodec.Attempt.Successful
import scodec.Codec
import scodec.bits.{ByteVector, _}
import scodec.codecs._
import shapeless.{::, HNil}

final case class ClientFinished(
                                 obj_type: Int,
                                 pubKey: ByteVector,
                                 challengeResult: ByteVector
                               ) extends PlanetSideCryptoPacket {
  type Packet = ClientFinished
  def opcode = CryptoPacketOpcode.ClientFinished
  def encode = ClientFinished.encode(this)
}

object ClientFinished extends Marshallable[ClientFinished] {
  def apply(pubKey: ByteVector, challengeResult: ByteVector): ClientFinished =
    ClientFinished(16, pubKey, challengeResult)

  implicit val codec: Codec[ClientFinished] = (
    ("obj_type?" | uint8) ::
      ("pub_key_len" | constant(hex"1000")) ::
      ("pub_key" | bytes(16)) ::
      ("unknown" | constant(hex"0114".bits)) ::
      ("challenge_result" | bytes(0xc))
  ).exmap[ClientFinished](
    {
      case 16 :: () :: c :: () :: e :: HNil =>
        Successful(ClientFinished(16, c, e))
      case a :: () :: c :: () :: e :: HNil =>
        org.log4s.getLogger("ClientFinished").warn(s"obj_type?: expected 16 but got $a; attempting to bypass")
        Successful(ClientFinished(a, c, e))
    },
    {
      case ClientFinished(a, c, e) =>
        Successful(a :: () :: c :: () :: e :: HNil)
    }
  )
}

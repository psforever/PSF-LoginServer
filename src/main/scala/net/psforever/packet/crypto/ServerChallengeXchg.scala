// Copyright (c) 2017 PSForever
package net.psforever.packet.crypto

import net.psforever.packet.{CryptoPacketOpcode, Marshallable, PlanetSideCryptoPacket}
import scodec.Codec
import scodec.bits.{ByteVector, _}
import scodec.codecs._

final case class ServerChallengeXchg(time: Long, challenge: ByteVector, pubKey: ByteVector)
    extends PlanetSideCryptoPacket {
  type Packet = ServerChallengeXchg
  def opcode = CryptoPacketOpcode.ServerChallengeXchg
  def encode = ServerChallengeXchg.encode(this)
}

object ServerChallengeXchg extends Marshallable[ServerChallengeXchg] {
  def getCompleteChallenge(time: Long, rest: ByteVector): ByteVector =
    uint32L.encode(time).require.toByteVector ++ rest

  implicit val codec: Codec[ServerChallengeXchg] = (
    ("unknown" | constant(2)) ::
      ("unknown" | constant(1)) ::
      ("server_time" | uint32L) ::
      ("challenge" | bytes(0xc)) ::
      ("end?" | constant(0)) ::
      ("objects" | constant(1)) ::
      ("unknown" | constant(hex"03070000000c00".bits)) ::
      ("pub_key_len" | constant(hex"1000")) ::
      ("pub_key" | bytes(16)) ::
      ("unknown" | constant(0x0e))
  ).as[ServerChallengeXchg]
}

package net.psforever.packet

import scodec.bits.BitVector
import scodec.{Attempt, DecodeResult, Err}

// This isn't actually used as an opcode (i.e not serialized)
object CryptoPacketOpcode extends Enumeration {
  type Type = Value
  val Ignore, ClientChallengeXchg, ServerChallengeXchg, ClientFinished, ServerFinished = Value

  def getPacketDecoder(opcode: CryptoPacketOpcode.Type): (BitVector) => Attempt[DecodeResult[PlanetSideCryptoPacket]] =
    opcode match {
      case ClientChallengeXchg => crypto.ClientChallengeXchg.decode
      case ServerChallengeXchg => crypto.ServerChallengeXchg.decode
      case ServerFinished      => crypto.ServerFinished.decode
      case ClientFinished      => crypto.ClientFinished.decode
      case default =>
        (a: BitVector) =>
          Attempt.failure(
            Err(s"Could not find a marshaller for crypto packet ${opcode}")
              .pushContext("get_marshaller")
          )
    }
}

// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import scodec.bits.BitVector
import scodec.{Err, DecodeResult, Attempt}

// this isnt actually used as an opcode (i.e not serialized)
object CryptoPacketOpcode extends Enumeration {
  type Type = Value
  val Ignore, ClientChallengeXchg, ServerChallengeXchg,
    ClientFinished, ServerFinished = Value

  def getPacketDecoder(opcode : CryptoPacketOpcode.Type) : (BitVector) => Attempt[DecodeResult[PlanetSideCryptoPacket]] = {
    import net.psforever

    opcode match {
      case ClientChallengeXchg => psforever.packet.ClientChallengeXchg.decode
      case ServerChallengeXchg => psforever.packet.ServerChallengeXchg.decode
      case ServerFinished => psforever.packet.ServerFinished.decode
      case ClientFinished => psforever.packet.ClientFinished.decode
      case default => (a : BitVector) => Attempt.failure(Err(s"Could not find a marshaller for crypto packet ${opcode}")
        .pushContext("get_marshaller"))
    }
  }
}

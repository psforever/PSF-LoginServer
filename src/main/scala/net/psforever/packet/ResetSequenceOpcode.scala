// Copyright (c) 2020 PSForever
package net.psforever.packet

import scodec.bits.BitVector
import scodec.{Attempt, Codec, DecodeResult, Err}
import scodec.codecs.uint8L

object ResetSequenceOpcode extends Enumeration(1) {
  type Type = Value
  val ResetSequence = Value

  def getPacketDecoder(opcode: ResetSequenceOpcode.Type): BitVector => Attempt[DecodeResult[PlanetSideResetSequencePacket]] =
    opcode match {
      case ResetSequence => reset.ResetSequence.decode
      case _ =>
        (_: BitVector) =>
          Attempt.failure(
            Err(s"Could not find a marshaller for reset sequence packet $opcode")
              .pushContext("get_marshaller")
          )
    }

  implicit val codec: Codec[this.Value] = PacketHelpers.createEnumerationCodec(this, uint8L)
}

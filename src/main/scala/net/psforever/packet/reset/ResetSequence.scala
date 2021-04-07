// Copyright (c) 2020 PSForever
package net.psforever.packet.reset

import net.psforever.packet.{Marshallable, PacketHelpers, PlanetSideResetSequencePacket, ResetSequenceOpcode}
import scodec.{Attempt, Codec}
import scodec.bits.BitVector

final case class ResetSequence()
  extends PlanetSideResetSequencePacket {
  type Packet = ResetSequence
  def opcode: ResetSequenceOpcode.Type = ResetSequenceOpcode.ResetSequence
  def encode: Attempt[BitVector]       = ResetSequence.encode(this)
}

object ResetSequence extends Marshallable[ResetSequence] {
  implicit val codec: Codec[ResetSequence] = PacketHelpers.emptyCodec[ResetSequence](ResetSequence())
}

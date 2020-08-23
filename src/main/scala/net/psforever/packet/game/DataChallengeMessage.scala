// Copyright (c) 2020 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.{Attempt, Codec}
import scodec.bits.BitVector

/**
  * Our packet captures contain no examples of `DataChallengeMessage`.
  * @param attribute na
  * @param value na
  */
final case class DataChallengeMessage(attribute: String, value: Long) extends PlanetSideGamePacket {
  type Packet = DataChallengeMessage
  def opcode: GamePacketOpcode.Value = GamePacketOpcode.DataChallengeMessage
  def encode: Attempt[BitVector]     = DataChallengeMessage.encode(this)
}

object DataChallengeMessage extends Marshallable[DataChallengeMessage] {
  implicit val codec: Codec[DataChallengeMessage] = DataChallenge.codec.as[DataChallengeMessage]
}

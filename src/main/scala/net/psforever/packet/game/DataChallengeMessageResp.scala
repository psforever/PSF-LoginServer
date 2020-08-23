// Copyright (c) 2020 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.{Attempt, Codec}
import scodec.bits.BitVector

/**
  * Our packet captures contain no examples of `DataChallengeMessageResp`.
  * @param attribute na
  * @param value na
  */
final case class DataChallengeMessageResp(attribute: String, value: Long) extends PlanetSideGamePacket {
  type Packet = DataChallengeMessageResp
  def opcode: GamePacketOpcode.Value = GamePacketOpcode.DataChallengeMessageResp
  def encode: Attempt[BitVector]     = DataChallengeMessageResp.encode(this)
}

object DataChallengeMessageResp extends Marshallable[DataChallengeMessageResp] {
  implicit val codec: Codec[DataChallengeMessageResp] = DataChallenge.codec.as[DataChallengeMessageResp]
}

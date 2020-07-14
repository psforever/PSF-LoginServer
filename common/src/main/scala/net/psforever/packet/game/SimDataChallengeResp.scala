// Copyright (c) 2020 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.{Attempt, Codec}
import scodec.bits.BitVector
import scodec.codecs._

/**
  * na
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  */
final case class SimDataChallengeResp(unk1: List[Long], unk2: List[Long], unk3: Boolean) extends PlanetSideGamePacket {
  type Packet = SimDataChallengeResp
  def opcode: GamePacketOpcode.Value = GamePacketOpcode.SimDataChallengeResp
  def encode: Attempt[BitVector]     = SimDataChallengeResp.encode(this)
}

object SimDataChallengeResp extends Marshallable[SimDataChallengeResp] {
  implicit val codec: Codec[SimDataChallengeResp] = (
    ("unk1" | listOfN(uint16L, ulongL(bits = 32))) ::
      ("unk2" | listOfN(uint16L, ulongL(bits = 32))) ::
      ("unk3" | bool)
  ).as[SimDataChallengeResp]
}

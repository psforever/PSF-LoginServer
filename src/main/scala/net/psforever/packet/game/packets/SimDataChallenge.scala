// Copyright (c) 2020 PSForever
package net.psforever.packet.game.packets

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec}

/**
  * na
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  * @param unk5 na
  */
final case class SimDataChallenge(unk1: List[Long], unk2: Boolean, unk3: Int, unk4: Long, unk5: Boolean)
    extends PlanetSideGamePacket {
  type Packet = SimDataChallenge
  def opcode: GamePacketOpcode.Value = GamePacketOpcode.SimDataChallenge
  def encode: Attempt[BitVector]     = SimDataChallenge.encode(this)
}

object SimDataChallenge extends Marshallable[SimDataChallenge] {
  implicit val codec: Codec[SimDataChallenge] = (
    ("unk1" | listOfN(uint16L, ulongL(bits = 32))) ::
      ("unk2" | bool) ::
      ("unk3" | uint8) ::
      ("unk4" | ulongL(bits = 32)) ::
      ("unk5" | bool)
  ).as[SimDataChallenge]
}

// Copyright (c) 2026 PSForever
package net.psforever.packet.game.packets

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.{Attempt, Codec}
import scodec.bits.BitVector
import scodec.codecs._

/**
 * na
 * @param u1 na
 * @param u2 na
 * @param u3 na
 */
final case class ModuleLimitsMessage(
                                      u1: Int,
                                      u2: Int,
                                      u3: Int
                                    ) extends PlanetSideGamePacket {
  type Packet = ModuleLimitsMessage
  def opcode: Type = GamePacketOpcode.ModuleLimitsMessage
  def encode: Attempt[BitVector] = ModuleLimitsMessage.encode(this)
}

object ModuleLimitsMessage extends Marshallable[ModuleLimitsMessage] {
  implicit val codec: Codec[ModuleLimitsMessage] = (
    ("u1" | uint8) ::
      ("u2" | uint(bits = 6)) ::
      ("u3" | uint(bits = 3))
    ).as[ModuleLimitsMessage]
}

// Copyright (c) 2024 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._

final case class DebugDrawMessage(
                                   unk1: Int,
                                   unk2: Long,
                                   unk3: Long,
                                   unk4: Long,
                                   unk5: List[Vector3]
                                 )
  extends PlanetSideGamePacket {
  type Packet = DebugDrawMessage
  def opcode: Type = GamePacketOpcode.DebugDrawMessage
  def encode: Attempt[BitVector] = DebugDrawMessage.encode(this)
}

object DebugDrawMessage extends Marshallable[DebugDrawMessage] {
  implicit val codec: Codec[DebugDrawMessage] = (
    ("unk1" | uint(bits = 3)) ::
      ("unk2" | ulongL(bits = 32)) ::
      ("unk3" | ulongL(bits = 32)) ::
      ("unk4" | ulongL(bits = 32)) ::
      ("unk5" | listOfN(uint2, Vector3.codec_pos))
    ).as[DebugDrawMessage]
}

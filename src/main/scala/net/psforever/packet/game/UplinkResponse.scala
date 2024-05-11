// Copyright (c) 2024 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._

final case class UplinkResponse(
                                unk1: Int,
                                unk2: Int
                              ) extends PlanetSideGamePacket {
  type Packet = UplinkResponse
  def opcode: Type = GamePacketOpcode.UplinkResponse
  def encode: Attempt[BitVector] = UplinkResponse.encode(this)
}

object UplinkResponse extends Marshallable[UplinkResponse] {
  implicit val codec: Codec[UplinkResponse] = (
    ("unk1" | uint(bits = 3)) ::
      ("unk2" | uint4)
    ).as[UplinkResponse]
}

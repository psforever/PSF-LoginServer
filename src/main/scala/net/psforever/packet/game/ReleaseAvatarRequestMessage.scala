// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec

/** Packet send by client when clic on button after death
  * https://streamable.com/4r16m
  */

final case class ReleaseAvatarRequestMessage() extends PlanetSideGamePacket {
  type Packet = ReleaseAvatarRequestMessage
  def opcode = GamePacketOpcode.ReleaseAvatarRequestMessage
  def encode = ReleaseAvatarRequestMessage.encode(this)
}

object ReleaseAvatarRequestMessage extends Marshallable[ReleaseAvatarRequestMessage] {
  implicit val codec: Codec[ReleaseAvatarRequestMessage] = PacketHelpers.emptyCodec(ReleaseAvatarRequestMessage())
}

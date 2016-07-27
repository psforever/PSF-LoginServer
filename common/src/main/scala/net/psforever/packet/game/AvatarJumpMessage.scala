// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import scodec.bits._

final case class AvatarJumpMessage(state : Boolean)
  extends PlanetSideGamePacket {
  type Packet = AvatarJumpMessage
  def opcode = GamePacketOpcode.AvatarJumpMessage
  def encode = AvatarJumpMessage.encode(this)
}

object AvatarJumpMessage extends Marshallable[AvatarJumpMessage] {
  implicit val codec : Codec[AvatarJumpMessage] = (
    ("state" | bool)
    ).as[AvatarJumpMessage]
}
// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Tell the server that the player is is jumping.
  * This will allow it to coordinate animation with other clients.<br>
  * <br>
  * During the jump, the avatar's "height" coordinate does change, as reported in `PlayerStateMessageUpstream`.
  * `PlayerStateMessage`, however, can depict another player with a proper jumping animation without the explicit coordinate change.
  * The server must probably account for the distance to the ground when passing along data somehow.<br>
  * <br>
  * Exploration:<br>
  * Is `state` ever not `true`?
  * @param state true
  */
final case class AvatarJumpMessage(state: Boolean) extends PlanetSideGamePacket {
  type Packet = AvatarJumpMessage
  def opcode = GamePacketOpcode.AvatarJumpMessage
  def encode = AvatarJumpMessage.encode(this)
}

object AvatarJumpMessage extends Marshallable[AvatarJumpMessage] {
  implicit val codec: Codec[AvatarJumpMessage] = ("state" | bool).as[AvatarJumpMessage]
}

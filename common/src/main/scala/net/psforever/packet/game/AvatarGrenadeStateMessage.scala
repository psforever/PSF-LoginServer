// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Report the state of the "grenade throw" animation for this player.<br>
  * <br>
  * States:<br>
  * 1 - prepare for throwing (grenade held back over shoulder)<br>
  * 2 - throwing (grenade released overhand)<br>
  * <br>
  * Exploration:<br>
  * How many grenade states are possible?
  * @param player_guid the player
  * @param state the animation state
  */
final case class AvatarGrenadeStateMessage(player_guid : PlanetSideGUID,
                                           state : Int)
  extends PlanetSideGamePacket {
  type Packet = AvatarGrenadeStateMessage
  def opcode = GamePacketOpcode.AvatarGrenadeStateMessage
  def encode = AvatarGrenadeStateMessage.encode(this)
}

object AvatarGrenadeStateMessage extends Marshallable[AvatarGrenadeStateMessage] {
  implicit val codec : Codec[AvatarGrenadeStateMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("state" | uint8L)
    ).as[AvatarGrenadeStateMessage]
}

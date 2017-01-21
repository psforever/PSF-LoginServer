// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param player_guid the player
  * @param stasis whether or not the player is held in stasis
  */
final case class PlayerStasisMessage(player_guid : PlanetSideGUID,
                                     stasis : Boolean)
  extends PlanetSideGamePacket {
  type Packet = PlayerStasisMessage
  def opcode = GamePacketOpcode.PlayerStasisMessage
  def encode = PlayerStasisMessage.encode(this)
}

object PlayerStasisMessage extends Marshallable[PlayerStasisMessage] {
  implicit val codec : Codec[PlayerStasisMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("stasis" | bool)
    ).as[PlayerStasisMessage]
}

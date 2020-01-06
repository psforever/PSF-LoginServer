// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Causes the avatar to be prepped for drop pod use.<br>
  * <br>
  * This packet is dispatched from the server to all occupants of the HART shuttle when it has completed its take-off animation.
  * When received by the client, that player's avatar is "removed from the world" and the Interstellar Map is displayed for them.
  * The "Launch Drop Pod" button window is also made visible.
  * Selecting individual continental maps for viewing and clicking on the landmasses has the expected behavior for drop pods implementation.<br>
  * <br>
  * Being seated on the HART shuttle at the time, a player's avatar does not physically exist when the packet is received.
  * If the packet is received while the player is outside of the HART shuttle, the state of their avatar is not known to them.
  * "Removed from the world" merely implies that one can not leave the Interstellar Map once it has been displayed.
  * According to packet capture, their avatar is not explicitly deconstructed until the dropped-into map is loaded.<br>
  * <br>
  * When the packet is received on one's client, but is addressed to another player, nothing seems to happen to that player.
  * If that player's model is outside of the HART, it will not deconstruct.
  * Only the client's avatar can be affected by this packet.
  * @param player_guid the player
  * @param stasis `true` by default;
  *               nothing when `false` (?)
  */
final case class PlayerStasisMessage(player_guid : PlanetSideGUID,
                                     stasis : Boolean = true)
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

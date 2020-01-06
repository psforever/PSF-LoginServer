// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{GrenadeState, PlanetSideGUID}
import scodec.Codec
import scodec.codecs._

/**
  * Report the state of the grenade throw animation for this player.
  * The default state is "held at side," though the client's avatar never has to announce this.<br>
  * <br>
  * The throwing animation has a minor timing glitch.
  * Causing another player to raise his arm will always result in that arm being lowered a few seconds later.
  * This is as opposed to the client's avatar, who can seem to hold a grenade in the "prepare to throw" state indefinitely.
  * If the avatar looks away from a player whose grenade arm is up ("prepare to throw"), however, when they look back at the player,
  * his grenade arm will occasionally have been lowered ("held at side") again before it would normally be lowered.<br>
  * <br>
  * A client will dispatch state 'Primed' and state 'Thrown' for the avatar's actions.
  * A client will only react temporarily for another character other than the avatar when the given a state 'Primed'.
  * If that internal state is not changed, however, that other character will not respond to any subsequent 'Primed' state.
  * (This may also be a glitch.)<br>
  * <br>
  * States:<br>
  * `
  * 1 - prepare to throw (grenade held back over shoulder)<br>
  * 2 - throwing (grenade released overhand and then reset) (avatar only)<br>
  * `
  * @param player_guid the player
  * @param state the animation state
  */
final case class AvatarGrenadeStateMessage(player_guid : PlanetSideGUID,
                                           state : GrenadeState.Value)
  extends PlanetSideGamePacket {
  type Packet = AvatarGrenadeStateMessage
  def opcode = GamePacketOpcode.AvatarGrenadeStateMessage
  def encode = AvatarGrenadeStateMessage.encode(this)
}

object AvatarGrenadeStateMessage extends Marshallable[AvatarGrenadeStateMessage] {
  implicit val codec : Codec[AvatarGrenadeStateMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("state" | GrenadeState.codec)
    ).as[AvatarGrenadeStateMessage]
}

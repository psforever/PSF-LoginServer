// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Inform the client how many battle experience points (BEP) the player currently has earned.<br>
  * <br>
  * The amount of `experience` earned is an accumulating value.
  * Whenever the server sends this packet, the value of this field is equal to the player's current total BEP.
  * Each packet updates to a higher BEP score and the client occasionally reports of the difference as an event message.
  * "You have been awarded `x` battle experience points."
  * Milestone notifications that occur due to BEP gain, e.g., rank progression, will trigger naturally as the client is updated.<br>
  * <br>
  * It is possible to award more battle experience than is necessary to progress one's character to the highest battle rank.
  * (This must be accomplished in a single event packet.)
  * Only the most significant notifications will be displayed in that case.
  * If the BEP has been modified, there will be an extra three bits to indicate which message to display.<br>
  * <br>
  * Messages:<br>
  * 0 - Normal<br>
  * 1 - Normal (repeat?)<br>
  * 2 - Support bonus ("due to support activity")<br>
  * 4 - Rabbit bonus ("+25% rabbit Bonus")<br>
  * (Support message has priority over Rabbit message.)<br>
  * <br>
  * Exploration:<br>
  * `msg = 1` probably does not do the same thing as `mod = 0`.
  * @param player_guid the player
  * @param experience the current total experience
  * @param msg modifies the awarded experience message
  */
final case class BattleExperienceMessage(player_guid : PlanetSideGUID,
                                         experience : Long,
                                         msg : Int)
  extends PlanetSideGamePacket {
  type Packet = BattleExperienceMessage
  def opcode = GamePacketOpcode.BattleExperienceMessage
  def encode = BattleExperienceMessage.encode(this)
}

object BattleExperienceMessage extends Marshallable[BattleExperienceMessage] {
  implicit val codec : Codec[BattleExperienceMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("experience" | uint32L) ::
      ("msg" | uintL(3))
    ).as[BattleExperienceMessage]
}

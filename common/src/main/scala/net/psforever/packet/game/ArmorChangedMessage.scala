// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Force a player model to change its exo-suit.
  * Set all GUI elements and functional elements to be associated with that type of exo-suit.
  * Inventory and holster contents are discarded.<br>
  * <br>
  * Due to the way armor is handled internally, a player of one faction may not spawn in the exo-suit of another faction.
  * That style of exo-suit is never available through this packet.
  * As MAX units do not get their weapon by default, all the MAX values produce the same faction-appropriate mechanized exo-suit body visually.
  * (The MAX weapons are supplied in subsequent packets.)<br>
  * <br>
  * All values in between the ones indicated (below) dress the player in the lesser suit, e.g., 1F is Agile, 21 is Reinforced.
  * 44, 48, and 4C are distinguished in that those are the prescribed values for terminal-swapped MAX exo-suits.<br>
  * <br>
  * `
  * 00 - Agile (0)<br>
  * 20 - Reinforced (32)<br>
  * 40 - MAX (64)<br>
  * 44 - AI MAX (68)<br>
  * 48 - AV MAX (72)<br>
  * 4C - AA MAX (76)<br>
  * 60 - Infiltration (96)<br>
  * 80 - Standard (128)
  * `
  * @param player_guid the player
  * @param armor the type of exo-suit
  */
final case class ArmorChangedMessage(player_guid : PlanetSideGUID,
                                    armor : Int)
  extends PlanetSideGamePacket {
  type Packet = ArmorChangedMessage
  def opcode = GamePacketOpcode.ArmorChangedMessage
  def encode = ArmorChangedMessage.encode(this)
}

object ArmorChangedMessage extends Marshallable[ArmorChangedMessage] {
  implicit val codec : Codec[ArmorChangedMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("armor" | uint8L)
    ).as[ArmorChangedMessage]
}

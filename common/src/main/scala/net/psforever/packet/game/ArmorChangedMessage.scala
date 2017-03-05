// Copyright (c) 2017 PSForever
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
  * (The MAX weapons are supplied in subsequent packets.)
  * `
  * 0, 0 - Agile<br>
  * 1, 0 - Reinforced<br>
  * 2, 0 - MAX<br>
  * 2, 1 - AI MAX<br>
  * 2, 2 - AV MAX<br>
  * 2, 3 - AA MAX<br>
  * 3, 0 - Infiltration<br>
  * 4, 0 - Standard
  * `
  * @param player_guid the player
  * @param armor the type of exo-suit
  * @param subtype the exo-suit subtype, if any
  */
final case class ArmorChangedMessage(player_guid : PlanetSideGUID,
                                    armor : Int,
                                    subtype : Int)
  extends PlanetSideGamePacket {
  type Packet = ArmorChangedMessage
  def opcode = GamePacketOpcode.ArmorChangedMessage
  def encode = ArmorChangedMessage.encode(this)
}

object ArmorChangedMessage extends Marshallable[ArmorChangedMessage] {
  implicit val codec : Codec[ArmorChangedMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("armor" | uintL(3)) ::
      ("subtype" | uintL(3))
    ).as[ArmorChangedMessage]
}

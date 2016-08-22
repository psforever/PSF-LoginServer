// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Force a player to change their exo-suit.
  * This one command sets personal armor value, weapon slots, GUI elements, and other such things, related to the exo-suit being worn.<br>
  * <br>
  * Due to the way MAXes are handled internally, a player of one faction may not spawn in the MAX suit of another faction.
  * MAXes do not get their weapon by default.
  * It is created by subsequent ObjectCreatedMessage and an ObjectHeldMessage pairs (two for TR?).
  * Consequentially, the three MAX values all produce the same mechanized exo-suit body visually.<br>
  * <br>
  * `
  * - Standard:     hex 00, dec  0<br>
  * - Agile:        hex 00, dex  0<br>
  * - Reinforced:   hex 00, dex  0<br>
  * - MAX AI:       hex 44, dec 68<br>
  * - MAX AV:       hex 48, dec 72<br>
  * - MAX AA:       hex 4C, dec 76<br>
  * - Infiltration: hex 60, dec 96<br>
  * `
  *
  * @param player_guid the player
  * @param armor the type of armor to change into (if a MAX, you will not have your weapons)
  */
//sendResponse(PacketCoding.CreateGamePacket(0, ArmorChangedMessage(PlanetSideGUID(guid),68)))
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
      ("time" | uint8L)
    ).as[ArmorChangedMessage]
}

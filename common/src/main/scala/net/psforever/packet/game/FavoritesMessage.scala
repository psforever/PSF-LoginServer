// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Load the designator for an entry in the player's favorites list.<br>
  * <br>
  * This entry defines a loadout but does not directly associate itself with the configurations and contents of the loadout.
  * It is just the user-defined name that appears on a "Favorites" tab list and can be selected.
  * A subsequent server request - `ItemTransactionMessage` - must be made to retrieve the said configuration and content for loading.
  * Multiple separated favorites lists are present in the game.
  * All entries are prepended with their destination list, which determines what kind of terminal or menu allows them to be viewable.<br>
  * <br>
  * Infantry equipment favorites are appended with a code for the type of exo-suit that they will load on a player.
  * This does not match the same two field numbering system as in `ArmorChangedMessage` packets.
  * Subtypes are not considered separately.<br>
  * <br>
  * Lists:<br>
  * `
  * 0 - Equipment Terminal (infantry)<br>
  * 1 - Repair/Rearm (standard vehicles)<br>
  * `
  * <br>
  * Armors:<br>
  * `
  * 1 - Agile<br>
  * 2 - Reinforced<br>
  * 4 - AA MAX<br>
  * 5 - AI MAX<br>
  * 6 - AV MAX<br>
  * `
  * <br>
  * Exploration 1:<br>
  * The identifier for the list is two bits so four separated lists of Favorites are supportable.
  * Two of the lists are common enough and we can assume one of the others is related to Battleframe Robotics.
  * What, if anything, is the fourth indexed list?<br>
  * <br>
  * Exploration 2:<br>
  * There are three unaccounted exo-suit indices - '0', '3', and '7' - and two specific kinds of exo-suit that are not defined - Infiltration and Standard.
  * It is possible that one of the indices also defines the generic MAX (see `ArmorChangedMessage`).
  * Which exo-suit is associated with which index?<br>
  * <br>
  * Exploration 3:<br>
  * The `armor` parameter coincides with the type of exo-suit maintained by the loadout.
  * Does this value help the client judge when a favorite can be selected by checking against certifications for exo-suit permission?
  * (If so, why isn't it working?)
  * @param list the destination list
  * @param player_guid the player
  * @param line the zero-indexed line number of this entry in its list
  * @param label the identifier for this entry
  * @param armor the type of exo-suit, if an Infantry loadout
  */
final case class FavoritesMessage(list : Int,
                                  player_guid : PlanetSideGUID,
                                  line : Int,
                                  label : String,
                                  armor : Option[Int] = None)
  extends PlanetSideGamePacket {
  type Packet = FavoritesMessage
  def opcode = GamePacketOpcode.FavoritesMessage
  def encode = FavoritesMessage.encode(this)
}

object FavoritesMessage extends Marshallable[FavoritesMessage] {
  implicit val codec : Codec[FavoritesMessage] = (
    ("list" | uintL(2)) >>:~ { value =>
      ("player_guid" | PlanetSideGUID.codec) ::
        ("line" | uintL(4)) ::
        ("label" | PacketHelpers.encodedWideStringAligned(2)) ::
        conditional(value == 0, "armor" | uintL(3))
    }).as[FavoritesMessage]
}

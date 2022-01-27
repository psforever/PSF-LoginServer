// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{LoadoutType, PlanetSideGUID}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Load the designator for an entry in the player's favorites list.<br>
  * <br>
  * This entry defines a user-defined loadout label that appears on a "Favorites" tab list and can be selected.
  * A subsequent server request - `ItemTransactionMessage` - must be made to retrieve the said loadout contents.
  * Multiple separated favorites lists are present in the game.
  * All entries are prepended with their destination list which indicates how from how that list is viewable.
  * Different lists also have different numbers of available lines to store loadout entries.<br>
  * <br>
  * Infantry equipment favorites are appended with a code for the type of exo-suit that they will load on a player.
  * This does not match the same two field numbering system as in `ArmorChangedMessage` packets.<br>
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
  * Exploration:<br>
  * There are three unaccounted exo-suit indices - 0, 3, and 7;
  * and, there are two specific kinds of exo-suit that are not defined - Infiltration and Standard.
  * It is possible that one of the indices also defines the generic MAX (see `ArmorChangedMessage`).
  * Which exo-suit is associated with which index?
  * @param list the destination list
  * @param player_guid the player
  * @param line the zero-indexed line number of this entry in its list
  * @param label the identifier for this entry
  * @param armor_type the type of exo-suit, if an Infantry loadout;
  *                   the type of battleframe, if a Battleframe loadout;
  *                   `None`, if just a Vehicle loadout
  */
final case class FavoritesMessage(
    list: LoadoutType.Value,
    player_guid: PlanetSideGUID,
    line: Int,
    label: String,
    armor_type: Option[Int]
) extends PlanetSideGamePacket {
  type Packet = FavoritesMessage
  def opcode = GamePacketOpcode.FavoritesMessage
  def encode = FavoritesMessage.encode(this)
}

object FavoritesMessage extends Marshallable[FavoritesMessage] {
  /**
    * Overloaded constructor.
    * @param list the destination list
    * @param player_guid the player
    * @param line the zero-indexed line number of this entry in its list
    * @param label the identifier for this entry
    * @param armor the type of exo-suit, if an Infantry loadout
    * @return a `FavoritesMessage` object
    */
  def apply(
      list: LoadoutType.Value,
      player_guid: PlanetSideGUID,
      line: Int,
      label: String,
      armor: Int
  ): FavoritesMessage = {
    FavoritesMessage(list, player_guid, line, label, Some(armor))
  }

  /**
    * Overloaded constructor, for vehicle loadouts specifically.
    * @param list the destination list
    * @param player_guid the player
    * @param line the zero-indexed line number of this entry in its list
    * @param label the identifier for this entry
    * @return a `FavoritesMessage` object
    */
  def apply(list: LoadoutType.Value, player_guid: PlanetSideGUID, line: Int, label: String): FavoritesMessage = {
    FavoritesMessage(list, player_guid, line, label, None)
  }

  /**
    * Overloaded constructor for infantry loadouts.
    * @param player_guid the player
    * @param line the zero-indexed line number of this entry in its list
    * @param label the identifier for this entry
    * @param armor the type of exo-suit
    * @return a `FavoritesMessage` object
    */
  def Infantry(
                player_guid: PlanetSideGUID,
                line: Int,
                label: String,
                armor: Int
              ): FavoritesMessage = {
    FavoritesMessage(LoadoutType.Infantry, player_guid, line, label, Some(armor))
  }

  /**
    * Overloaded constructor for vehicle loadouts.
    * @param player_guid the player
    * @param line the zero-indexed line number of this entry in its list
    * @param label the identifier for this entry
    * @return a `FavoritesMessage` object
    */
  def Vehicle(
               player_guid: PlanetSideGUID,
               line: Int,
               label: String
             ): FavoritesMessage = {
    FavoritesMessage(LoadoutType.Vehicle, player_guid, line, label, None)
  }

  /**
    * Overloaded constructor for battleframe loadouts.
    * @param player_guid the player
    * @param line the zero-indexed line number of this entry in its list
    * @param label the identifier for this entry
    * @param subtype the type of battleframe unit
    * @return a `FavoritesMessage` object
    */
  def Battleframe(
                   player_guid: PlanetSideGUID,
                   line: Int,
                   label: String,
                   subtype: Int
                 ): FavoritesMessage = {
    FavoritesMessage(LoadoutType.Battleframe, player_guid, line, label, Some(subtype))
  }

  implicit val codec: Codec[FavoritesMessage] = (("list" | LoadoutType.codec) >>:~ { value =>
    ("player_guid" | PlanetSideGUID.codec) ::
      ("line" | uint4L) ::
      ("label" | PacketHelpers.encodedWideStringAligned(adjustment = 2)) ::
      ("armor_type" | conditional(value != LoadoutType.Vehicle,
        {
          if (value == LoadoutType.Infantry) uint(bits = 3)
          else uint4
        }
      ))
  }).xmap[FavoritesMessage](
    {
      case lst :: guid :: ln :: str :: arm :: HNil =>
        FavoritesMessage(lst, guid, ln, str, arm)
    },
    {
      case FavoritesMessage(lst, guid, ln, str, arm) =>
        val armset = if (lst != LoadoutType.Vehicle && arm.isEmpty) { Some(0) } else { arm }
        lst :: guid :: ln :: str :: armset :: HNil
    }
  )
}

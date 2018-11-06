// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.codecs._
import scodec.Codec
import shapeless.{::, HNil}

/**
  * A representation of an `avatar` player for the `ObjectCreateDetailedMessage` packet.
  * As an avatar, the character created by this data is expected to be controllable by the client that gets sent this data.<br>
  * <br>
  * Divisions exist to make the data more manageable.
  * The first division defines the player's location within the game coordinate system.
  * The second division defines features of the `avatar`
  * that are shared by both the `ObjectCreateDetailedMessage` version of a controlled player character (this)
  * and the `ObjectCreateMessage` version of a player character.
  * The third field expands on the nature of the character and this avatar's campaign.
  * Expansive information about previous interactions, the contents of their inventory, and equipment permissions are included.<br>
  * <br>
  * The presence or absence of position data as the first division creates a cascading effect
  * causing all of fields in the other two divisions to gain offsets.
  * These offsets exist in the form of `String` and `List` padding.
  * @see `CharacterAppearanceData`
  * @see `DetailedCharacterData`
  * @see `InventoryData`
  * @see `DrawnSlot`
  * @param pos the optional position of the character in the world environment
  * @param basic_appearance common fields regarding the the character's appearance
  * @param character_data the class-specific data that discusses the character
  * @param position_defined used to seed the state of the optional position fields
  * @param inventory the player's full or partial (holsters-only) inventory
  * @param drawn_slot the holster that is depicted as exposed, or "drawn"
  */
final case class DetailedPlayerData(pos : Option[PlacementData],
                                    basic_appearance : CharacterAppearanceData,
                                    character_data : DetailedCharacterData,
                                    inventory : Option[InventoryData],
                                    drawn_slot : DrawnSlot.Value)
                                   (position_defined : Boolean) extends ConstructorData {
  override def bitsize : Long = {
    //factor guard bool values into the base size, not its corresponding optional field
    val posSize : Long = if(pos.isDefined) { pos.get.bitsize } else { 0L }
    val appSize : Long = basic_appearance.bitsize
    val charSize = character_data.bitsize
    val inventorySize : Long = if(inventory.isDefined) { inventory.get.bitsize } else { 0L }
    5L + posSize + appSize + charSize + inventorySize
  }
}

object DetailedPlayerData extends Marshallable[DetailedPlayerData] {
  /**
    * Overloaded constructor that ignores the coordinate information but includes the inventory.
    * It passes information between the three major divisions for the purposes of offset calculations.
    * This constructor should be used for players that are mounted.
    * @param basic_appearance a curried function for the common fields regarding the the character's appearance
    * @param character_data a curried function for the class-specific data that explains about the character
    * @param inventory the player's full or partial (holsters-only) inventory
    * @param drawn_slot the holster that is depicted as exposed, or "drawn";
    *                   technically, always `DrawnSlot.None`, but the field is preserved to maintain similarity
    * @return a `DetailedPlayerData` object
    */
  def apply(basic_appearance : (Int)=>CharacterAppearanceData, character_data : (Option[Int])=>DetailedCharacterData, inventory : InventoryData, drawn_slot : DrawnSlot.Value) : DetailedPlayerData = {
    val appearance = basic_appearance(5)
    DetailedPlayerData(None, appearance, character_data(appearance.altModelBit), Some(inventory), drawn_slot)(false)
  }

  /**
    * Overloaded constructor that ignores the coordinate information and the inventory.
    * It passes information between the three major divisions for the purposes of offset calculations.
    * This constructor should be used for players that are mounted.
    * @param basic_appearance a curried function for the common fields regarding the the character's appearance
    * @param character_data a curried function for the class-specific data that explains about the character
    * @param drawn_slot the holster that is depicted as exposed, or "drawn;"
    *                   technically, always `DrawnSlot.None`, but the field is preserved to maintain similarity
    * @return a `DetailedPlayerData` object
    */
  def apply(basic_appearance : (Int)=>CharacterAppearanceData, character_data : (Option[Int])=>DetailedCharacterData, drawn_slot : DrawnSlot.Value) : DetailedPlayerData = {
    val appearance = basic_appearance(5)
    DetailedPlayerData(None, appearance, character_data(appearance.altModelBit), None, drawn_slot)(false)
  }

  /**
    * Overloaded constructor that includes the coordinate information and the inventory.
    * It passes information between the three major divisions for the purposes of offset calculations.
    * This constructor should be used for players that are standing apart from other containers.
    * @param pos the optional position of the character in the world environment
    * @param basic_appearance a curried function for the common fields regarding the the character's appearance
    * @param character_data a curried function for the class-specific data that explains about the character
    * @param inventory the player's full or partial (holsters-only) inventory
    * @param drawn_slot the holster that is depicted as exposed, or "drawn"
    * @return a `DetailedPlayerData` object
    */
  def apply(pos : PlacementData, basic_appearance : (Int)=>CharacterAppearanceData, character_data : (Option[Int])=>DetailedCharacterData, inventory : InventoryData, drawn_slot : DrawnSlot.Value) : DetailedPlayerData = {
    val appearance = basic_appearance(PlayerData.PaddingOffset(Some(pos)))
    DetailedPlayerData(Some(pos), appearance, character_data(appearance.altModelBit), Some(inventory), drawn_slot)(true)
  }

  /**
    * Overloaded constructor that includes the coordinate information but ignores the inventory.
    * It passes information between the three major divisions for the purposes of offset calculations.
    * This constructor should be used for players that are standing apart from other containers.
    * @param pos the optional position of the character in the world environment
    * @param basic_appearance a curried function for the common fields regarding the the character's appearance
    * @param character_data a curried function for the class-specific data that explains about the character
    * @param drawn_slot the holster that is depicted as exposed, or "drawn"
    * @return a `DetailedPlayerData` object
    */
  def apply(pos : PlacementData, basic_appearance : (Int)=>CharacterAppearanceData, character_data : (Option[Int])=>DetailedCharacterData, drawn_slot : DrawnSlot.Value) : DetailedPlayerData = {
    val appearance = basic_appearance(PlayerData.PaddingOffset(Some(pos)))
    DetailedPlayerData(Some(pos), appearance, character_data(appearance.altModelBit), None, drawn_slot)(true)
  }

  def codec(position_defined : Boolean) : Codec[DetailedPlayerData] = (
    conditional(position_defined, "pos" | PlacementData.codec) >>:~ { pos =>
      ("basic_appearance" | CharacterAppearanceData.codec(PlayerData.PaddingOffset(pos))) >>:~ { app =>
        ("character_data" | DetailedCharacterData.codec(app.altModelBit)) ::
          optional(bool, "inventory" | InventoryData.codec_detailed) ::
          ("drawn_slot" | DrawnSlot.codec) ::
          bool //usually false
      }
    }).xmap[DetailedPlayerData] (
    {
      case pos :: app :: data :: inv :: hand :: _ :: HNil =>
        DetailedPlayerData(pos, app, data, inv, hand)(pos.isDefined)
    },
    {
      case DetailedPlayerData(pos, app, data, inv, hand) =>
        pos :: app :: data :: inv :: hand :: false :: HNil
    }
  )

  implicit val codec : Codec[DetailedPlayerData] = codec(false)
}

// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.codecs._
import scodec.Codec
import shapeless.{::, HNil}

/**
  * A representation of another player's character for the `ObjectCreateDetailedMessage` packet.
  * In general, this packet is used to describe other players.<br>
  * <br>
  * Divisions exist to make the data more manageable.
  * The first division defines the player's location within the game coordinate system.
  * The second division defines features of the character
  * that are shared by both the `ObjectCreateDetailedMessage` version of a controlled player character
  * and the `ObjectCreateMessage` version of a player character (this).
  * The third field provides further information on the appearance of the player character, albeit condensed.
  * One of the most compact forms of a player character description is transcribed using this information.<br>
  * <br>
  * The presence or absence of position data as the first division creates a cascading effect
  * causing all of fields in the other two divisions to gain offsets.
  * These offsets exist in the form of `String` and `List` padding.
  * @see `CharacterData`<br>
  *        `InventoryData`<br>
  *        `DrawnSlot`
  * @param pos the optional position of the character in the world environment
  * @param basic_appearance common fields regarding the the character's appearance
  * @param character_data the class-specific data that explains about the character
  * @param inventory the player's inventory;
  *                  typically, only the tools and weapons in the equipment holster slots
  * @param drawn_slot the holster that is initially drawn
  * @param position_defined used by the `Codec` to seed the state of the optional `pos` field
  */
final case class PlayerData(pos : Option[PlacementData],
                            basic_appearance : CharacterAppearanceData,
                            character_data : CharacterData,
                            inventory : Option[InventoryData],
                            drawn_slot : DrawnSlot.Value)
                           (position_defined : Boolean) extends ConstructorData {
  override def bitsize : Long = {
    val posSize : Long = if(pos.isDefined) { pos.get.bitsize } else { 0 }
    val appSize : Long = basic_appearance.bitsize
    val charSize = character_data.bitsize
    val inventorySize : Long = if(inventory.isDefined) { inventory.get.bitsize } else { 0L }
    5L + posSize + appSize + charSize + inventorySize
  }
}

object PlayerData extends Marshallable[PlayerData] {
  /**
    * Overloaded constructor that ignores the coordinate information.
    * It passes information between the three major divisions for the purposes of offset calculations.
    * @param basic_appearance a curried function for the common fields regarding the the character's appearance
    * @param character_data a curried function for the class-specific data that explains about the character
    * @param inventory the player's inventory
    * @param drawn_slot the holster that is initially drawn
    * @return a `PlayerData` object
    */
  def apply(basic_appearance : (Int)=>CharacterAppearanceData, character_data : (Boolean)=>CharacterData, inventory : InventoryData, drawn_slot : DrawnSlot.Type) : PlayerData = {
    val appearance = basic_appearance(0)
    PlayerData(None, appearance, character_data(appearance.backpack), Some(inventory), drawn_slot)(false)
  }
  /** */
  def apply(basic_appearance : (Int)=>CharacterAppearanceData, character_data : (Boolean)=>CharacterData, hand_held : DrawnSlot.Type) : PlayerData = {
    val appearance = basic_appearance(0)
    PlayerData(None, appearance, character_data(appearance.backpack), None, hand_held)(false)
  }
  /**
    * Overloaded constructor that includes the coordinate information.
    * It passes information between the three major divisions for the purposes of offset calculations.
    * @param pos the optional position of the character in the world environment
    * @param basic_appearance a curried function for the common fields regarding the the character's appearance
    * @param character_data a curried function for the class-specific data that explains about the character
    * @param inventory the player's inventory
    * @param drawn_slot the holster that is initially drawn
    * @return a `PlayerData` object
    */
  def apply(pos : PlacementData, basic_appearance : (Int)=>CharacterAppearanceData, character_data : (Boolean)=>CharacterData, inventory : InventoryData, drawn_slot : DrawnSlot.Type) : PlayerData = {
    val appearance = basic_appearance( placementOffset(Some(pos)) )
    PlayerData(Some(pos), appearance, character_data(appearance.backpack), Some(inventory), drawn_slot)(true)
  }
  /** */
  def apply(pos : PlacementData, basic_appearance : (Int)=>CharacterAppearanceData, character_data : (Boolean)=>CharacterData, hand_held : DrawnSlot.Type) : PlayerData = {
    val appearance = basic_appearance( placementOffset(Some(pos)) )
    PlayerData(Some(pos), appearance, character_data(appearance.backpack), None, hand_held)(true)
  }

  /**
    * Determine the padding offset for a subsequent field given the existence of `PlacementData`.
    * The padding will always be a number 0-7.
    * @see `PlacemtnData`
    * @param pos the optional `PlacementData` object that creates the shift in bits
    * @return the pad length in bits
    */
  def placementOffset(pos : Option[PlacementData]) : Int = {
    if(pos.isEmpty) {
      0
    }
    else if(pos.get.vel.isDefined) {
      2
    }
    else {
      4
    }
  }

  def codec(position_defined : Boolean) : Codec[PlayerData] = (
    conditional(position_defined, "pos" | PlacementData.codec) >>:~ { pos =>
      ("basic_appearance" | CharacterAppearanceData.codec(placementOffset(pos))) >>:~ { app =>
        ("character_data" | CharacterData.codec(app.backpack)) ::
          optional(bool, "inventory" | InventoryData.codec) ::
          ("drawn_slot" | DrawnSlot.codec) ::
          bool //usually false
      }
    }).xmap[PlayerData] (
    {
      case pos :: app :: data :: inv :: hand :: _ :: HNil =>
        PlayerData(pos, app, data, inv, hand)(pos.isDefined)
    },
    {
      case PlayerData(pos, app, data, inv, hand) =>
        pos :: app :: data :: inv :: hand :: false :: HNil
    }
  )

  implicit val codec : Codec[PlayerData] = codec(false)
}

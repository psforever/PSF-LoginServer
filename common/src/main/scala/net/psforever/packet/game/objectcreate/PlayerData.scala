// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.newcodecs._
import net.psforever.packet.Marshallable
import scodec.codecs._
import scodec.Codec
import shapeless.{::, HNil}

/**
  * A representation of another player's character for the `ObjectCreateMessage` packet.
  * In general, this packet is used to describe other players.<br>
  * <br>
  * Divisions exist to make the data more manageable.
  * The first division defines the player's location within the game coordinate system.
  * The second division defines features of the character
  * that are shared by both the `ObjectCreateDetailedMessage` version of a controlled player character
  * and the `ObjectCreateMessage` version of a player character (this).
  * The third field provides further information on the appearance of the player character, albeit condensed.
  * The fourth field involves the player's `Equipment` holsters and their inventory.
  * The hand that the player has exposed is last.
  * One of the most compact forms of a player character description is transcribed using this information.<br>
  * <br>
  * The presence or absence of position data as the first division creates a cascading effect
  * causing all of fields in the other two divisions to gain offset values.
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
    //factor guard bool values into the base size, not its corresponding optional field
    val posSize : Long = if(pos.isDefined) { pos.get.bitsize } else { 0L }
    val appSize : Long = basic_appearance.bitsize
    val charSize = character_data.bitsize
    val inventorySize : Long = if(inventory.isDefined) { inventory.get.bitsize } else { 0L }
    5L + posSize + appSize + charSize + inventorySize
  }
}

object PlayerData extends Marshallable[PlayerData] {
  /**
    * Overloaded constructor that ignores the coordinate information but includes the inventory.
    * It passes information between the three major divisions for the purposes of offset calculations.
    * This constructor should be used for players that are mounted.
    * @param basic_appearance a curried function for the common fields regarding the the character's appearance
    * @param character_data a curried function for the class-specific data that explains about the character
    * @param inventory the player's inventory
    * @param drawn_slot the holster that is initially drawn;
    *                   technically, always `DrawnSlot.None`, but the field is preserved to maintain similarity
    * @return a `PlayerData` object
    */
  def apply(basic_appearance : (Int)=>CharacterAppearanceData, character_data : (Boolean,Boolean)=>CharacterData, inventory : InventoryData, drawn_slot : DrawnSlot.Type) : PlayerData = {
    val appearance = basic_appearance(5)
    PlayerData(None, appearance, character_data(appearance.a.altModel, true), Some(inventory), drawn_slot)(false)
  }
  /**
    * Overloaded constructor that ignores the coordinate information and the inventory.
    * It passes information between the three major divisions for the purposes of offset calculations.
    * This constructor should be used for players that are mounted.
    * @param basic_appearance a curried function for the common fields regarding the the character's appearance
    * @param character_data a curried function for the class-specific data that explains about the character
    * @param drawn_slot the holster that is initially drawn;
    *                   technically, always `DrawnSlot.None`, but the field is preserved to maintain similarity
    * @return a `PlayerData` object
    */
  def apply(basic_appearance : (Int)=>CharacterAppearanceData, character_data : (Boolean,Boolean)=>CharacterData, drawn_slot : DrawnSlot.Type) : PlayerData = {
    val appearance = basic_appearance(5)
    PlayerData(None, appearance, character_data(appearance.a.altModel, true), None, drawn_slot)(false)
  }

  /**
    * Overloaded constructor that includes the coordinate information and the inventory.
    * It passes information between the three major divisions for the purposes of offset calculations.
    * This constructor should be used for players that are standing apart from other containers.
    * @param pos the optional position of the character in the world environment
    * @param basic_appearance a curried function for the common fields regarding the the character's appearance
    * @param character_data a curried function for the class-specific data that explains about the character
    * @param inventory the player's inventory
    * @param drawn_slot the holster that is initially drawn
    * @return a `PlayerData` object
    */
  def apply(pos : PlacementData, basic_appearance : (Int)=>CharacterAppearanceData, character_data : (Boolean,Boolean)=>CharacterData, inventory : InventoryData, drawn_slot : DrawnSlot.Type) : PlayerData = {
    val appearance = basic_appearance( PaddingOffset(Some(pos)) )
    PlayerData(Some(pos), appearance, character_data(appearance.a.altModel, false), Some(inventory), drawn_slot)(true)
  }
  /**
    * Overloaded constructor that includes the coordinate information but ignores the inventory.
    * It passes information between the three major divisions for the purposes of offset calculations.
    * This constructor should be used for players that are standing apart from other containers.
    * @param pos the optional position of the character in the world environment
    * @param basic_appearance a curried function for the common fields regarding the the character's appearance
    * @param character_data a curried function for the class-specific data that explains about the character
    * @param drawn_slot the holster that is initially drawn
    * @return a `PlayerData` object
    */
  def apply(pos : PlacementData, basic_appearance : (Int)=>CharacterAppearanceData, character_data : (Boolean,Boolean)=>CharacterData, drawn_slot : DrawnSlot.Type) : PlayerData = {
    val appearance = basic_appearance( PaddingOffset(Some(pos)) )
    PlayerData(Some(pos), appearance, character_data(appearance.a.altModel, false), None, drawn_slot)(true)
  }

  /**
    * Determine the padding offset for a subsequent field given the existence of `PlacementData`.
    * With the `PlacementData` objects, a question of the optional velocity field also exists.<br>
    * <br>
    * With just `PlacementData`, the bit distance to the name field is 164 (padding: 4 bits).
    * With `PlacementData` with velocity, the bit distance to the name field is 206 (padding: 2 bits).
    * Without `PlacementData`, the distance to the name field is either 107 or 115 (padding: 5 bits).
    * The padding will always be a number 0-7.
    * @see `PlacementData`
    * @param pos the optional `PlacementData` object that creates the shift in bits
    * @return the pad length in bits
    */
  def PaddingOffset(pos : Option[PlacementData]) : Int = {
    /*
    The `ObjectCreateMessage` length is either 32 + 12 + 16 + 81 - 141 - with `PlacementData`,
    with an additional +42 - 183 - with the optional velocity field,
    or 32 + 12 + 16 + 16 + 8/16 - 84/92 - without any `PlacementData`.
    23 is the distance of all the fields before the player's `name` field in `CharacterAppearanceData`.
     */
    pos match {
      case Some(place) =>
        if(place.vel.isDefined) { 2 } else { 4 }
      case None =>
        5 //with ObjectCreateMessageParent data
    }
  }

  /**
    * Find the number of trailing bits that need to be added to make the current value perfectly divisible by eight.
    * @param length the current length of a stream
    * @return the number of bits needed to pad it
    */
  def ByteAlignmentPadding(length : Long) : Int = {
    val pad = (length - math.floor(length / 8) * 8).toInt
      if(pad > 0) {
        8 - pad
      }
      else {
        0
      }
  }

  /**
    * This `Codec` is generic.
    * However, it should not be used to translate a `Player` object
    * in the middle of translating that `Player`'s mounting object.
    * The offset value is calculated internally.
    * @param position_defined this entry has `PlacementData` that defines position, orientation, and, optionally, motion
    * @return a `Codec` that translates a `PlayerData` object
    */
  def codec(position_defined : Boolean) : Codec[PlayerData] = (
    conditional(position_defined, "pos" | PlacementData.codec) >>:~ { pos =>
      ("basic_appearance" | CharacterAppearanceData.codec(PaddingOffset(pos))) >>:~ { app =>
        ("character_data" | newcodecs.binary_choice(position_defined,
          CharacterData.codec(app.b.backpack),
          CharacterData.codec_seated(app.b.backpack))) ::
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

  /**
    * This `Codec` is exclusively for translating a `Player` object
    * while that `Player` object is encountered in the process of translating its mounting object.
    * In other words, the player is "seated" or "mounted."
    * @see `CharacterAppearanceData.codec`
    * @param offset the padding for the player's name field
    * @return a `Codec` that translates a `PlayerData` object
    */
  def codec(offset : Int) : Codec[PlayerData] = (
    ("basic_appearance" | CharacterAppearanceData.codec(offset)) >>:~ { app =>
      ("character_data" | CharacterData.codec_seated(app.b.backpack)) ::
        optional(bool, "inventory" | InventoryData.codec) ::
        ("drawn_slot" | DrawnSlot.codec) ::
        bool //usually false
      }
    ).xmap[PlayerData] (
    {
      case app :: data :: inv :: hand :: _ :: HNil =>
        PlayerData(None, app, data, inv, hand)(false)
    },
    {
      case PlayerData(None, app, data, inv, hand) =>
        app :: data :: inv :: hand :: false :: HNil
    }
  )

  implicit val codec : Codec[PlayerData] = codec(false)
}

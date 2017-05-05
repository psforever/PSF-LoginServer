// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the avatar portion of `ObjectCreateDetailedMessage` packet data.
  * This densely-packed information outlines most of the specifics required to depict a character as an avatar.<br>
  * <br>
  * As an avatar, the character created by this data is expected to be controllable by the client that gets sent this data.
  * It goes into depth about information related to the given character in-game career that is not revealed to other players.<br>
  * <br>
  * Divisions exist to make the data more manageable.
  * The first division of data only manages the general appearance of the player's in-game model.
  * The second division (currently, the fields actually in this class) manages the status of the character as an avatar.
  * In general, it passes more thorough data about the character that the client can display to the owner of the client.
  * For example, health is a full number, rather than a percentage.
  * Just as prominent is the list of first time events and the list of completed tutorials.
  * The third subdivision is also exclusive to avatar-prepared characters and contains (omitted).
  * The fourth is the inventory (composed of `Direct`-type objects).<br>
  * <br>
  * Exploration:<br>
  * Lots of analysis needed for the remainder of the byte data.
  * @param appearance data about the avatar's basic aesthetics
  * @param healthMax for `x / y` of hitpoints, this is the avatar's `y` value;
  *                  range is 0-65535
  * @param health for `x / y` of hitpoints, this is the avatar's `x` value;
  *               range is 0-65535
  * @param armor for `x / y` of armor points, this is the avatar's `x` value;
  *              range is 0-65535;
  *              the avatar's `y` armor points is tied to their exo-suit type
  * @param unk1 na;
  *             defaults to 1
  * @param unk2 na;
  *             defaults to 7
  * @param unk3 na;
  *             defaults to 7
  * @param staminaMax for `x / y` of stamina points, this is the avatar's `y` value;
  *                   range is 0-65535
  * @param stamina for `x / y` of stamina points, this is the avatar's `x` value;
  *                range is 0-65535
  * @param unk4 na;
  *             defaults to 28
  * @param unk5 na;
  *             defaults to 4
  * @param unk6 na;
  *             defaults to 44
  * @param unk7 na;
  *              defaults to 84
  * @param unk8 na;
  *              defaults to 104
  * @param unk9 na;
  *              defaults to 1900
  * @param firstTimeEvents the list of first time events performed by this avatar;
  *                        the size field is a 32-bit number;
  *                        the first entry may be padded
  * @param tutorials the list of tutorials completed by this avatar;
  *                  the size field is a 32-bit number;
  *                  the first entry may be padded
  * @param inventory the avatar's inventory
  * @param drawn_slot the holster that is initially drawn
  * @see `CharacterAppearanceData`
  * @see `CharacterData`
  * @see `InventoryData`
  * @see `DrawnSlot`
  */
final case class DetailedCharacterData(appearance : CharacterAppearanceData,
                                       healthMax : Int,
                                       health : Int,
                                       armor : Int,
                                       unk1 : Int, //1
                                       unk2 : Int, //7
                                       unk3 : Int, //7
                                       staminaMax : Int,
                                       stamina : Int,
                                       unk4 : Int, //28
                                       unk5 : Int, //4
                                       unk6 : Int, //44
                                       unk7 : Int, //84
                                       unk8 : Int, //104
                                       unk9 : Int, //1900
                                       firstTimeEvents : List[String],
                                       tutorials : List[String],
                                       inventory : Option[InventoryData],
                                       drawn_slot : DrawnSlot.Value = DrawnSlot.None
                               ) extends ConstructorData {

  override def bitsize : Long = {
    //factor guard bool values into the base size, not its corresponding optional field
    val appearanceSize = appearance.bitsize
    val fteLen = firstTimeEvents.size //fte list
    var eventListSize : Long = 32L + DetailedCharacterData.ftePadding(fteLen)
    for(str <- firstTimeEvents) {
      eventListSize += StreamBitSize.stringBitSize(str)
    }
    val tutLen = tutorials.size //tutorial list
    var tutorialListSize : Long = 32L + DetailedCharacterData.tutPadding(fteLen, tutLen)
    for(str <- tutorials) {
      tutorialListSize += StreamBitSize.stringBitSize(str)
    }
    var inventorySize : Long = 0L //inventory
    if(inventory.isDefined) {
      inventorySize = inventory.get.bitsize
    }
    713L + appearanceSize + eventListSize + tutorialListSize + inventorySize
  }
}

object DetailedCharacterData extends Marshallable[DetailedCharacterData] {
  /**
    * Overloaded constructor for `DetailedCharacterData` that skips all the unknowns by assigning defaulted values.
    * It also allows for a not-optional inventory.
    * @param appearance data about the avatar's basic aesthetics
    * @param healthMax for `x / y` of hitpoints, this is the avatar's `y` value
    * @param health for `x / y` of hitpoints, this is the avatar's `x` value
    * @param armor for `x / y` of armor points, this is the avatar's `x` value
    * @param staminaMax for `x / y` of stamina points, this is the avatar's `y` value
    * @param stamina for `x / y` of stamina points, this is the avatar's `x` value
    * @param firstTimeEvents the list of first time events performed by this avatar
    * @param tutorials the list of tutorials completed by this avatar
    * @param inventory the avatar's inventory
    * @param drawn_slot the holster that is initially drawn
    * @return a `DetailedCharacterData` object
  */
  def apply(appearance : CharacterAppearanceData, healthMax : Int, health : Int, armor : Int, staminaMax : Int, stamina : Int, firstTimeEvents : List[String], tutorials : List[String], inventory : InventoryData, drawn_slot : DrawnSlot.Value) : DetailedCharacterData =
    new DetailedCharacterData(appearance, healthMax, health, armor, 1, 7, 7, staminaMax, stamina, 28, 4, 44, 84, 104, 1900, firstTimeEvents, tutorials, Some(inventory), drawn_slot)

  /**
    * Overloaded constructor for `DetailedCharacterData` that allows for a not-optional inventory.
    * @param appearance data about the avatar's basic aesthetics
    * @param healthMax for `x / y` of hitpoints, this is the avatar's `y` value
    * @param health for `x / y` of hitpoints, this is the avatar's `x` value
    * @param armor for `x / y` of armor points, this is the avatar's `x` value
    * @param unk1 na
    * @param unk2 na
    * @param unk3 na
    * @param staminaMax for `x / y` of stamina points, this is the avatar's `y` value
    * @param stamina for `x / y` of stamina points, this is the avatar's `x` value
    * @param unk4 na
    * @param unk5 na
    * @param unk6 na
    * @param unk7 na
    * @param unk8 na
    * @param unk9 na
    * @param firstTimeEvents the list of first time events performed by this avatar
    * @param tutorials the list of tutorials completed by this avatar
    * @param inventory the avatar's inventory
    * @param drawn_slot the holster that is initially drawn
    * @return a `DetailedCharacterData` object
    */
  def apply(appearance : CharacterAppearanceData, healthMax : Int, health : Int, armor : Int, unk1 : Int, unk2 : Int, unk3 : Int, staminaMax : Int, stamina : Int, unk4 : Int, unk5 : Int, unk6 : Int, unk7 : Int, unk8 : Int, unk9 : Int, firstTimeEvents : List[String], tutorials : List[String], inventory : InventoryData, drawn_slot : DrawnSlot.Value) : DetailedCharacterData =
    new DetailedCharacterData(appearance, healthMax, health, armor, unk1, unk2, unk3, staminaMax, stamina, unk4, unk5, unk6, unk7, unk8, unk9, firstTimeEvents, tutorials, Some(inventory), drawn_slot)

  /**
    * Get the padding of the first entry in the first time events list.
    * The padding will always be a number 0-7.
    * @param len the length of the list
    * @return the pad length in bits
    */
  private def ftePadding(len : Long) : Int = {
    //TODO the parameters for this function are not correct
    //TODO the proper padding length should reflect all variability in the stream prior to this point
    if(len > 0) {
      5
    }
    else
      0
  }

  /**
    * Get the padding of the first entry in the completed tutorials list.
    * The padding will always be a number 0-7.<br>
    * <br>
    * The tutorials list follows the first time event list and that contains byte-aligned strings too.
    * While there will be more to the padding, this other list is important.
    * Any elements in that list causes the automatic byte-alignment of this list's first entry.
    * @param len the length of the list
    * @return the pad length in bits
    */
  private def tutPadding(len : Long, len2 : Long) : Int = {
    if(len > 0) //automatic alignment from previous List
      0
    else if(len2 > 0) //need to align for elements
      5
    else //both lists are empty
      0
  }

  implicit val codec : Codec[DetailedCharacterData] = (
    ("appearance" | CharacterAppearanceData.codec) ::
      ignore(160) ::
      ("healthMax" | uint16L) ::
      ("health" | uint16L) ::
      ignore(1) ::
      ("armor" | uint16L) ::
      ignore(9) ::
      ("unk1" | uint8L) ::
      ignore(8) ::
      ("unk2" | uint4L) ::
      ("unk3" | uintL(3)) ::
      ("staminaMax" | uint16L) ::
      ("stamina" | uint16L) ::
      ignore(149) ::
      ("unk4" | uint16L) ::
      ("unk5" | uint8L) ::
      ("unk6" | uint8L) ::
      ("unk7" | uint8L) ::
      ("unk8" | uint8L) ::
      ("unk9" | uintL(12)) ::
      ignore(19) ::
      (("firstTimeEvent_length" | uint32L) >>:~ { len =>
        conditional(len > 0, "firstTimeEvent_firstEntry" | PacketHelpers.encodedStringAligned( ftePadding(len) )) ::
          ("firstTimeEvent_list" | PacketHelpers.listOfNSized(len - 1, PacketHelpers.encodedString)) ::
          (("tutorial_length" | uint32L) >>:~ { len2 =>
            conditional(len2 > 0, "tutorial_firstEntry" | PacketHelpers.encodedStringAligned( tutPadding(len, len2) )) ::
              ("tutorial_list" | PacketHelpers.listOfNSized(len2 - 1, PacketHelpers.encodedString)) ::
              ignore(207) ::
              optional(bool, "inventory" | InventoryData.codec_detailed) ::
              ("drawn_slot" | DrawnSlot.codec) ::
              bool //usually false
        })
      })
    ).exmap[DetailedCharacterData] (
    {
      case app :: _ :: b :: c :: _ :: d :: _ :: e :: _ :: f :: g :: h :: i :: _ :: j :: k :: l :: m :: n :: o :: _ :: _ :: q :: r :: _ :: t :: u :: _ :: v :: w :: false :: HNil =>
        //prepend the displaced first elements to their lists
        val fteList : List[String] = if(q.isDefined) { q.get :: r } else r
        val tutList : List[String] = if(t.isDefined) { t.get :: u } else u
        Attempt.successful(DetailedCharacterData(app, b, c, d, e, f, g, h, i, j, k, l, m, n, o, fteList, tutList, v, w))
    },
    {
      case DetailedCharacterData(app, b, c, d, e, f, g, h, i, j, k, l, m, n, o, fteList, tutList, p, q) =>
        //shift the first elements off their lists
        var fteListCopy = fteList
        var firstEvent : Option[String] = None
        if(fteList.nonEmpty) {
          firstEvent = Some(fteList.head)
          fteListCopy = fteList.drop(1)
        }
        var tutListCopy = tutList
        var firstTutorial : Option[String] = None
        if(tutList.nonEmpty) {
          firstTutorial = Some(tutList.head)
          tutListCopy = tutList.drop(1)
        }
        Attempt.successful(app :: () :: b :: c :: () :: d :: () :: e :: () :: f :: g :: h :: i :: () :: j :: k :: l :: m :: n :: o :: () :: fteList.size.toLong :: firstEvent :: fteListCopy :: tutList.size.toLong :: firstTutorial :: tutListCopy :: () :: p :: q :: false :: HNil)
    }
  )
}

// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

final case class BattleRankFieldData(field00 : Int,
                                     field01 : Int,
                                     field02 : Int,
                                     field03 : Int,
                                     field04 : Int,
                                     field05 : Int,
                                     field06 : Int,
                                     field07 : Option[Int] = None,
                                     field08 : Option[Int] = None,
                                     field09 : Option[Int] = None,
                                     field0A : Option[Int] = None,
                                     field0B : Option[Int] = None,
                                     field0C : Option[Int] = None,
                                     field0D : Option[Int] = None,
                                     field0E : Option[Int] = None,
                                     field0F : Option[Int] = None,
                                     field10 : Option[Int] = None) extends StreamBitSize {
  override def bitsize : Long = {
    val extraFieldSize : Long = if(field10.isDefined) {
      70L
    }
    else if(field0E.isDefined) {
      50L
    }
    else if(field09.isDefined) {
      10L
    }
    else {
      0L
    }
    55L + extraFieldSize
  }
}

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
  * @param brFields na
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
                                       bep : Int,
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
                                       brFields : BattleRankFieldData,
                                       firstTimeEvents : List[String],
                                       tutorials : List[String],
                                       inventory : Option[InventoryData],
                                       drawn_slot : DrawnSlot.Value = DrawnSlot.None
                                      ) extends ConstructorData {

  override def bitsize : Long = {
    //factor guard bool values into the base size, not its corresponding optional field
    val appearanceSize = appearance.bitsize
    val brFieldSize = brFields.bitsize
    val fteLen = firstTimeEvents.size //fte list
    var eventListSize : Long = 32L + DetailedCharacterData.ftePadding(fteLen, bep)
    for(str <- firstTimeEvents) {
      eventListSize += StreamBitSize.stringBitSize(str)
    }
    val tutLen = tutorials.size //tutorial list
    var tutorialListSize : Long = 32L + DetailedCharacterData.tutPadding(fteLen, tutLen, bep)
    for(str <- tutorials) {
      tutorialListSize += StreamBitSize.stringBitSize(str)
    }
    var inventorySize : Long = 0L //inventory
    if(inventory.isDefined) {
      inventorySize = inventory.get.bitsize
    }
    658L + appearanceSize + brFieldSize + eventListSize + tutorialListSize + inventorySize
  }
}

object DetailedCharacterData extends Marshallable[DetailedCharacterData] {
//  /**
//    * Overloaded constructor for `DetailedCharacterData` that skips all the unknowns by assigning defaulted values.
//    * It also allows for a not-optional inventory.
//    * @param appearance data about the avatar's basic aesthetics
//    * @param healthMax for `x / y` of hitpoints, this is the avatar's `y` value
//    * @param health for `x / y` of hitpoints, this is the avatar's `x` value
//    * @param armor for `x / y` of armor points, this is the avatar's `x` value
//    * @param staminaMax for `x / y` of stamina points, this is the avatar's `y` value
//    * @param stamina for `x / y` of stamina points, this is the avatar's `x` value
//    * @param firstTimeEvents the list of first time events performed by this avatar
//    * @param tutorials the list of tutorials completed by this avatar
//    * @param inventory the avatar's inventory
//    * @param drawn_slot the holster that is initially drawn
//    * @return a `DetailedCharacterData` object
//    */
//  def apply(appearance : CharacterAppearanceData, bep : Int, healthMax : Int, health : Int, armor : Int, staminaMax : Int, stamina : Int, firstTimeEvents : List[String], tutorials : List[String], inventory : InventoryData, drawn_slot : DrawnSlot.Value) : DetailedCharacterData =
//    new DetailedCharacterData(appearance, bep, healthMax, health, armor, 1, 7, 7, staminaMax, stamina, 28, 4, 44, 84, 104, 1900, firstTimeEvents, tutorials, Some(inventory), drawn_slot)

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
    * @param firstTimeEvents the list of first time events performed by this avatar
    * @param tutorials the list of tutorials completed by this avatar
    * @param inventory the avatar's inventory
    * @param drawn_slot the holster that is initially drawn
    * @return a `DetailedCharacterData` object
    */
  def apply(appearance : CharacterAppearanceData, bep : Int, healthMax : Int, health : Int, armor : Int, unk1 : Int, unk2 : Int, unk3 : Int, staminaMax : Int, stamina : Int, unk4 : Int, unk5 : Int, unk6 : BattleRankFieldData, firstTimeEvents : List[String], tutorials : List[String], inventory : InventoryData, drawn_slot : DrawnSlot.Value) : DetailedCharacterData =
    new DetailedCharacterData(appearance, bep, healthMax, health, armor, unk1, unk2, unk3, staminaMax, stamina, unk4, unk5, unk6, firstTimeEvents, tutorials, Some(inventory), drawn_slot)

  private val br1FieldCodec : Codec[BattleRankFieldData] = ( // +0u
    ("f1" | uint8L) ::
      ("f2" | uint8L) ::
      ("f3" | uint8L) ::
      ("f4" | uint8L) ::
      ("f5" | uint8L) ::
      ("f6" | uint8L) ::
      ("f7" | uintL(7))
  ).exmap[BattleRankFieldData] (
    {
      case f1 :: f2 :: f3 :: f4 :: f5 :: f6 :: f7 :: HNil =>
        Attempt.successful(BattleRankFieldData(f1, f2, f3, f4, f5, f6, f7))
    },
    {
      case BattleRankFieldData(f1, f2, f3, f4, f5, f6, f7, _, _, _, _, _, _, _, _, _, _) =>
        Attempt.successful(f1 :: f2 :: f3 :: f4 :: f5 :: f6 :: f7 :: HNil)
    }
  )

  private val br6FieldCodec : Codec[BattleRankFieldData] = ( //+10u
    ("f1" | uint8L) ::
      ("f2" | uint8L) ::
      ("f3" | uint8L) ::
      ("f4" | uint8L) ::
      ("f5" | uint8L) ::
      ("f6" | uint8L) ::
      ("f7" | uint8L) ::
      ("f8" | uint8L) ::
      ("f9" | bool)
    ).exmap[BattleRankFieldData] (
    {
      case f1 :: f2 :: f3 :: f4 :: f5 :: f6 :: f7 :: f8 :: f9 :: HNil =>
        val f9Int : Int = if(f9) { 1 } else { 0 }
        Attempt.successful(BattleRankFieldData(f1, f2, f3, f4, f5, f6, f7, Some(f8), Some(f9Int)))
    },
    {
      case BattleRankFieldData(f1, f2, f3, f4, f5, f6, f7, Some(f8), Some(f9), _, _, _, _, _, _, _, _) =>
        val f9Bool : Boolean = if(f9 == 0) { false } else { true }
        Attempt.successful(f1 :: f2 :: f3 :: f4 :: f5 :: f6 :: f7 :: f8 :: f9Bool :: HNil)
      case _ =>
        Attempt.failure(Err("expected battle rank 6 field data"))
    }
  )

  private val br12FieldCodec : Codec[BattleRankFieldData] = ( //+52u
    ("f1" | uint8L) ::
      ("f2" | uint8L) ::
      ("f3" | uint8L) ::
      ("f4" | uint8L) ::
      ("f5" | uint8L) ::
      ("f6" | uint8L) ::
      ("f7" | uint8L) ::
      ("f8" | uint8L) ::
      ("f9" | uint8L) ::
      ("fA" | uint8L) ::
      ("fB" | uint8L) ::
      ("fC" | uint8L) ::
      ("fD" | uint8L) ::
      ("fE" | uintL(3))
    ).exmap[BattleRankFieldData] (
    {
      case f1 :: f2 :: f3 :: f4 :: f5 :: f6 :: f7 :: f8 :: f9 :: fa :: fb :: fc :: fd :: fe :: HNil =>
        Attempt.successful(BattleRankFieldData(f1, f2, f3, f4, f5, f6, f7, Some(f8), Some(f9), Some(fa), Some(fb), Some(fc), Some(fd), Some(fe)))
    },
    {
      case BattleRankFieldData(f1, f2, f3, f4, f5, f6, f7, Some(f8), Some(f9), Some(fa), Some(fb), Some(fc), Some(fd), Some(fe), _, _, _) =>
        Attempt.successful(f1 :: f2 :: f3 :: f4 :: f5 :: f6 :: f7 :: f8 :: f9 :: fa :: fb :: fc :: fd :: fe :: HNil)
      case _ =>
        Attempt.failure(Err("expected battle rank 12 field data"))
    }
  )

  private val br18FieldCodec : Codec[BattleRankFieldData] = ( //+70u
    ("f01" | uint8L) ::
      ("f02" | uint8L) ::
      ("f03" | uint8L) ::
      ("f04" | uint8L) ::
      ("f05" | uint8L) ::
      ("f06" | uint8L) ::
      ("f07" | uint8L) ::
      ("f08" | uint8L) ::
      ("f09" | uint8L) ::
      ("f0A" | uint8L) ::
      ("f0B" | uint8L) ::
      ("f0C" | uint8L) ::
      ("f0D" | uint8L) ::
      ("f0E" | uint8L) ::
      ("f0F" | uint8L) ::
      ("f10" | uintL(5))
    ).exmap[BattleRankFieldData] (
    {
      case f01 :: f02 :: f03 :: f04 :: f05 :: f06 :: f07 :: f08 :: f09 :: f0a :: f0b :: f0c :: f0d :: f0e :: f0f :: f10 :: HNil =>
        Attempt.successful(BattleRankFieldData(f01, f02, f03, f04, f05, f06, f07, Some(f08), Some(f09), Some(f0a), Some(f0b), Some(f0c), Some(f0d), Some(f0e), Some(f0f), Some(f10)))
    },
    {
      case BattleRankFieldData(f01, f02, f03, f04, f05, f06, f07, Some(f08), Some(f09), Some(f0a), Some(f0b), Some(f0c), Some(f0d), Some(f0e), Some(f0f), Some(f10), _) =>
        Attempt.successful(f01 :: f02 :: f03 :: f04 :: f05 :: f06 :: f07 :: f08 :: f09 :: f0a :: f0b :: f0c :: f0d :: f0e :: f0f :: f10 :: HNil)
      case _ =>
        Attempt.failure(Err("expected battle rank 18 field data"))
    }
  )

  /**
    * na
    * @param bep the battle experience points
    * @return the appropriate `Codec` for the fields representing a player with the implied battle rank
    */
  private def selectBattleRankFieldCodec(bep : Int) : Codec[BattleRankFieldData] = {
    if(bep > 754370) {
      br18FieldCodec
    }
    else if(bep > 197753) {
      br12FieldCodec
    }
    else if(bep > 29999) {
      br6FieldCodec
    }
    else {
      br1FieldCodec
    }
  }

  /**
    * The padding value of the first entry in either of two byte-aligned `List` structures.
    * @param bep the battle experience points
    * @return the pad length in bits `n < 8`
    */
  private def bepFieldPadding(bep : Int) : Int = {
    if(bep > 754370) { //BR18+
      7
    }
    else if(bep > 197753) { //BR12+
      1
    }
    else if(bep > 29999) { //BR6+
      3
    }
    else { //BR1+
      5
    }
  }

  /**
    * Get the padding of the first entry in the first time events list.
    * @param len the length of the list
    * @param bep the battle experience points
    * @return the pad length in bits `n < 8`
    */
  private def ftePadding(len : Long, bep : Int) : Int = {
    //TODO the parameters for this function are not correct
    //TODO the proper padding length should reflect all variability in the stream prior to this point
    if(len > 0) {
      bepFieldPadding(bep)
    }
    else {
      0
    }
  }

  /**
    * Get the padding of the first entry in the completed tutorials list.<br>
    * <br>
    * The tutorials list follows the first time event list and also contains byte-aligned strings.
    * If the both lists are populated or empty at the same time, the first entry will not need padding.
    * If the first time events list is unpopulated, but this list is populated, the first entry will need padding bits.
    * @param len the length of the list
    * @param bep the battle experience points
    * @return the pad length in bits `n < 8`
    */
  private def tutPadding(len : Long, len2 : Long, bep : Int) : Int = {
    if(len > 0) {
      //automatic alignment from previous List
      0
    }
    else if(len2 > 0) {
      //need to align for elements
      bepFieldPadding(bep)
    }
    else {
      //both lists are empty
      0
    }
  }

  implicit val codec : Codec[DetailedCharacterData] = (
    ("appearance" | CharacterAppearanceData.codec) ::
      (("bep" | uint24L) >>:~ { bep =>
        ignore(136) ::
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
          ("brFields" | selectBattleRankFieldCodec(bep)) :: //TODO do this for all these fields until their bits are better defined
          (("firstTimeEvent_length" | uint32L) >>:~ { len =>
            conditional(len > 0, "firstTimeEvent_firstEntry" | PacketHelpers.encodedStringAligned(ftePadding(len, bep))) ::
              ("firstTimeEvent_list" | PacketHelpers.listOfNSized(len - 1, PacketHelpers.encodedString)) ::
              (("tutorial_length" | uint32L) >>:~ { len2 =>
                conditional(len2 > 0, "tutorial_firstEntry" | PacketHelpers.encodedStringAligned(tutPadding(len, len2, bep))) ::
                  ("tutorial_list" | PacketHelpers.listOfNSized(len2 - 1, PacketHelpers.encodedString)) ::
                  ignore(207) ::
                  optional(bool, "inventory" | InventoryData.codec_detailed) ::
                  ("drawn_slot" | DrawnSlot.codec) ::
                  bool //usually false
              })
          })
      })
    ).exmap[DetailedCharacterData] (
    {
      case app :: bep :: _ :: hpmax :: hp :: _ :: armor :: _ :: u1 :: _ :: u2 :: u3 :: stamax :: stam :: _ :: u4 :: u5 :: brFields :: _ :: fte0 :: fte1 :: _ :: tut0 :: tut1 :: _ :: inv :: drawn :: false :: HNil =>
        //prepend the displaced first elements to their lists
        val fteList : List[String] = if(fte0.isDefined) { fte0.get +: fte1 } else fte1
        val tutList : List[String] = if(tut0.isDefined) { tut0.get +: tut1 } else tut1
        Attempt.successful(DetailedCharacterData(app, bep, hpmax, hp, armor, u1, u2, u3, stamax, stam, u4, u5, brFields, fteList, tutList, inv, drawn))
    },
    {
      case DetailedCharacterData(app, bep, hpmax, hp, armor, u1, u2, u3, stamax, stam, u4, u5, brFields, fteList, tutList, inv, drawn) =>
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
        Attempt.successful(app :: bep :: () :: hpmax :: hp :: () :: armor :: () :: u1 :: () :: u2 :: u3 :: stamax :: stam :: () :: u4 :: u5 :: brFields :: fteList.size.toLong :: firstEvent :: fteListCopy :: tutList.size.toLong :: firstTutorial :: tutListCopy :: () :: inv :: drawn :: false :: HNil)
    }
  )
}

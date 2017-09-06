// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.types.ImplantType
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

import scala.annotation.tailrec

/**
  * An entry in the `List` of valid implant slots in `DetailedCharacterData`.
  * "`activation`" is not necessarily the best word for it ...
  * @param implant the type of implant
  * @param activation na
  * @see `ImplantType`
  */
final case class ImplantEntry(implant : ImplantType.Value,
                              activation : Option[Int]) extends StreamBitSize {
  override def bitsize : Long = {
    val activationSize = if(activation.isDefined) { 12L } else { 5L }
    5L + activationSize
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
  * It is shared between `DetailedCharacterData` avatars and `CharacterData` player characters.
  * The second division (of fields) manages the status of the character as an avatar.
  * In general, it passes more thorough data about the character that the client can display to the owner of the client.
  * For example, health is a full number, rather than a percentage.
  * Just as prominent is the list of first time events and the list of completed tutorials.
  * The third subdivision is also exclusive to avatar-prepared characters and contains (omitted).
  * The fourth is the inventory (composed of `Direct`-type objects).
  * @param appearance data about the avatar's basic aesthetics
  * @param bep the avatar's battle experience points, which determines his Battle Rank
  * @param cep the avatar's command experience points, which determines his Command Rank
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
  * @param certs the `List` of active certifications
  * @param implants the `List` of implant slots currently possessed by this avatar
  * @param firstTimeEvents the list of first time events performed by this avatar;
  *                        the size field is a 32-bit number;
  *                        the first entry may be padded
  * @param tutorials the `List` of tutorials completed by this avatar;
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
                                       bep : Long,
                                       cep : Long,
                                       healthMax : Int,
                                       health : Int,
                                       armor : Int,
                                       unk1 : Int, //1
                                       unk2 : Int, //7
                                       unk3 : Int, //7
                                       staminaMax : Int,
                                       stamina : Int,
                                       certs : List[Int],
                                       implants : List[ImplantEntry],
                                       firstTimeEvents : List[String],
                                       tutorials : List[String],
                                       inventory : Option[InventoryData],
                                       drawn_slot : DrawnSlot.Value = DrawnSlot.None
                                      ) extends ConstructorData {

  override def bitsize : Long = {
    //factor guard bool values into the base size, not corresponding optional fields, unless contained or enumerated
    val appearanceSize = appearance.bitsize
    val varBit : Option[Int] = CharacterAppearanceData.altModelBit(appearance)
    val certSize = (certs.length + 1) * 8 //cert list
    var implantSize : Long = 0L //implant list
    for(entry <- implants) {
      implantSize += entry.bitsize
    }
    val implantPadding = DetailedCharacterData.implantFieldPadding(implants, varBit)
    val fteLen = firstTimeEvents.size //fte list
    var eventListSize : Long = 32L + DetailedCharacterData.ftePadding(fteLen, implantPadding)
    for(str <- firstTimeEvents) {
      eventListSize += StreamBitSize.stringBitSize(str)
    }
    val tutLen = tutorials.size //tutorial list
    var tutorialListSize : Long = 32L + DetailedCharacterData.tutPadding(fteLen, tutLen, implantPadding)
    for(str <- tutorials) {
      tutorialListSize += StreamBitSize.stringBitSize(str)
    }
    val inventorySize : Long = if(inventory.isDefined) { //inventory
      inventory.get.bitsize
    }
    else {
      0L
    }
    649L + appearanceSize + certSize + implantSize + eventListSize + tutorialListSize + inventorySize
  }
}

object DetailedCharacterData extends Marshallable[DetailedCharacterData] {
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
    * @param certs na
    * @param firstTimeEvents the list of first time events performed by this avatar
    * @param tutorials the list of tutorials completed by this avatar
    * @param inventory the avatar's inventory
    * @param drawn_slot the holster that is initially drawn
    * @return a `DetailedCharacterData` object
    */
  def apply(appearance : CharacterAppearanceData, bep : Long, cep : Long, healthMax : Int, health : Int, armor : Int, unk1 : Int, unk2 : Int, unk3 : Int, staminaMax : Int, stamina : Int, certs : List[Int], implants : List[ImplantEntry], firstTimeEvents : List[String], tutorials : List[String], inventory : InventoryData, drawn_slot : DrawnSlot.Value) : DetailedCharacterData =
    new DetailedCharacterData(appearance, bep, cep, healthMax, health, armor, unk1, unk2, unk3, staminaMax, stamina, certs, implants, firstTimeEvents, tutorials, Some(inventory), drawn_slot)

  /**
    * `Codec` for entires in the list of implants.
    */
  private val implant_entry_codec : Codec[ImplantEntry] = (
    ("implant" | ImplantType.codec) ::
      (bool >>:~ { guard =>
        newcodecs.binary_choice(guard, uintL(5), uintL(12)).hlist
      })
  ).xmap[ImplantEntry] (
    {
      case implant :: true :: _ :: HNil =>
        ImplantEntry(implant, None)

      case implant :: false :: extra :: HNil =>
        ImplantEntry(implant, Some(extra))
    },
    {
      case ImplantEntry(implant, None) =>
        implant :: true :: 0 :: HNil

      case ImplantEntry(implant, Some(extra)) =>
        implant :: false :: extra :: HNil
    }
  )

  /**
    * A player's battle rank, determined by their battle experience points, determines how many implants to which they have access.
    * Starting with "no implants" at BR1, a player earns one at each of the three ranks: BR6, BR12, and BR18.
    * @param bep battle experience points
    * @return the number of accessible implant slots
    */
  private def numberOfImplantSlots(bep : Long) : Int = {
    if(bep > 754370) { //BR18+
      3
    }
    else if(bep > 197753) { //BR12+
      2
    }
    else if(bep > 29999) { //BR6+
      1
    }
    else { //BR1+
      0
    }
  }

  /**
    * The padding value of the first entry in either of two byte-aligned `List` structures.
    * @param implants implant entries
    * @return the pad length in bits `0 <= n < 8`
    */
  private def implantFieldPadding(implants : List[ImplantEntry], varBit : Option[Int] = None) : Int = {
    val base : Int = 5 //the offset with no implant entries
    val baseOffset : Int = base - varBit.getOrElse(0)
    val resultA = if(baseOffset < 0) { 8 - baseOffset } else { baseOffset % 8 }

    var implantOffset : Int = 0
    implants.foreach({entry =>
      implantOffset += entry.bitsize.toInt
    })
    val resultB : Int = resultA - (implantOffset % 8)
    if(resultB < 0) { 8 - resultB } else { resultB }
  }

  /**
    * Players with certain battle rank will always have a certain number of implant slots.
    * The encoding requires it.
    * Pad empty slots onto the end of a list of
    * @param size the required number of implant slots
    * @param list the `List` of implant slots
    * @return a fully-populated (or over-populated) `List` of implant slots
    * @see `ImplantEntry`
    */
  @tailrec private def recursiveEnsureImplantSlots(size : Int, list : List[ImplantEntry] = Nil) : List[ImplantEntry] = {
    if(list.length >= size) {
      list
    }
    else {
      recursiveEnsureImplantSlots(size, list :+ ImplantEntry(ImplantType.None, None))
    }
  }

  /**
    * Get the padding of the first entry in the first time events list.
    * @param len the length of the first time events list
    * @param implantPadding the padding that resulted from implant entries
    * @return the pad length in bits `0 <= n < 8`
    */
  private def ftePadding(len : Long, implantPadding : Int) : Int = {
    //TODO the proper padding length should reflect all variability in the stream prior to this point
    if(len > 0) {
      implantPadding
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
    * @param len the length of the first time events list
    * @param len2 the length of the tutorial list
    * @param implantPadding the padding that resulted from implant entries
    * @return the pad length in bits `n < 8`
    */
  private def tutPadding(len : Long, len2 : Long, implantPadding : Int) : Int = {
    if(len > 0) {
      0 //automatic alignment from previous List
    }
    else if(len2 > 0) {
      implantPadding //need to align for elements
    }
    else {
      0 //both lists are empty
    }
  }

  implicit val codec : Codec[DetailedCharacterData] = (
    ("appearance" | CharacterAppearanceData.codec) >>:~ { app =>
      ("bep" | uint32L) >>:~ { bep =>
        ("cep" | uint32L) ::
          ignore(96) ::
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
          ignore(147) ::
          ("certs" | listOfN(uint8L, uint8L)) ::
          optional(bool, uint32L) :: //ask about sample CCRIDER
          ignore(4) ::
          (("implants" | PacketHelpers.listOfNSized(numberOfImplantSlots(bep), implant_entry_codec)) >>:~ { implants =>
            ignore(12) ::
              (("firstTimeEvent_length" | uint32L) >>:~ { len =>
                conditional(len > 0, "firstTimeEvent_firstEntry" | PacketHelpers.encodedStringAligned(ftePadding(len, implantFieldPadding(implants, CharacterAppearanceData.altModelBit(app))))) ::
                  ("firstTimeEvent_list" | PacketHelpers.listOfNSized(len - 1, PacketHelpers.encodedString)) ::
                  (("tutorial_length" | uint32L) >>:~ { len2 =>
                    conditional(len2 > 0, "tutorial_firstEntry" | PacketHelpers.encodedStringAligned(tutPadding(len, len2, implantFieldPadding(implants, CharacterAppearanceData.altModelBit(app))))) ::
                      ("tutorial_list" | PacketHelpers.listOfNSized(len2 - 1, PacketHelpers.encodedString)) ::
                      ignore(207) ::
                      optional(bool, "inventory" | InventoryData.codec_detailed) ::
                      ("drawn_slot" | DrawnSlot.codec) ::
                      bool //usually false
                  })
              })
          })
      }
    }
    ).exmap[DetailedCharacterData] (
    {
      case app :: bep :: cep :: _ :: hpmax :: hp :: _ :: armor :: _ :: u1 :: _ :: u2 :: u3 :: stamax :: stam :: _ :: certs :: _ :: _  :: implants :: _ :: _ :: fte0 :: fte1 :: _ :: tut0 :: tut1 :: _ :: inv :: drawn :: false :: HNil =>
        //prepend the displaced first elements to their lists
        val fteList : List[String] = if(fte0.isDefined) { fte0.get +: fte1 } else fte1
        val tutList : List[String] = if(tut0.isDefined) { tut0.get +: tut1 } else tut1
        Attempt.successful(DetailedCharacterData(app, bep, cep, hpmax, hp, armor, u1, u2, u3, stamax, stam, certs, implants, fteList, tutList, inv, drawn))
    },
    {
      case DetailedCharacterData(app, bep, cep, hpmax, hp, armor, u1, u2, u3, stamax, stam, certs, implants, fteList, tutList, inv, drawn) =>
        val implantCapacity : Int = numberOfImplantSlots(bep)
        val implantList = if(implants.length > implantCapacity) {
          implants.slice(0, implantCapacity)
        }
        else {
          recursiveEnsureImplantSlots(implantCapacity, implants)
        }
        //shift the first elements off their lists
        var fteListCopy = fteList
        var firstEvent : Option[String] = None
        if(fteList.nonEmpty) {
          firstEvent = Some(fteList.head)
          fteListCopy = fteList.tail
        }
        var tutListCopy = tutList
        var firstTutorial : Option[String] = None
        if(tutList.nonEmpty) {
          firstTutorial = Some(tutList.head)
          tutListCopy = tutList.tail
        }
        Attempt.successful(app :: bep :: cep :: () :: hpmax :: hp :: () :: armor :: () :: u1 :: () :: u2 :: u3 :: stamax :: stam :: () :: certs :: None :: () :: implantList :: () :: fteList.size.toLong :: firstEvent :: fteListCopy :: tutList.size.toLong :: firstTutorial :: tutListCopy :: () :: inv :: drawn :: false :: HNil)
    }
  )
}

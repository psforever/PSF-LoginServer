// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.types.{CertificationType, ImplantType}
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

import scala.annotation.tailrec

/**
  * An entry in the `List` of valid implant slots in `DetailedCharacterData`.
  * `activation`, if defined, indicates the time remaining (in seconds?) before an implant becomes usable.
  * @param implant the type of implant
  * @param activation the activation timer;
  *                   technically, this is "unconfirmed"
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
  * A representation of a portion of an avatar's `ObjectCreateDetailedMessage` packet data.<br>
  * <br>
  * This densely-packed information outlines most of the specifics required to depict a character as an avatar.
  * It goes into depth about information related to the given character in-game career that is not revealed to other players.
  * To be specific, it passes more thorough data about the character that the client can display to the owner of the client.
  * For example, health is a full number, rather than a percentage.
  * Just as prominent is the list of first time events and the list of completed tutorials.
  * Additionally, a full inventory, as opposed to the initial five weapon slots.
  * @param bep the avatar's battle experience points, which determines his Battle Rank
  * @param cep the avatar's command experience points, which determines his Command Rank
  * @param healthMax for `x / y` of hitpoints, this is the avatar's `y` value;
  *                  range is 0-65535
  * @param health for `x / y` of hitpoints, this is the avatar's `x` value;
  *               range is 0-65535
  * @param armor for `x / y` of armor points, this is the avatar's `x` value;
  *              range is 0-65535;
  *              the avatar's `y` armor points is tied to their exo-suit type
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
  * @param cosmetics optional decorative features that are added to the player's head model by console/chat commands;
  *                  they become available at battle rank 24;
  *                  these flags do not exist if they are not applicable
  * @see `CharacterData`<br>
  *       `CertificationType`
  */
final case class DetailedCharacterData(bep : Long,
                                       cep : Long,
                                       healthMax : Int,
                                       health : Int,
                                       armor : Int,
                                       staminaMax : Int,
                                       stamina : Int,
                                       certs : List[CertificationType.Value],
                                       implants : List[ImplantEntry],
                                       firstTimeEvents : List[String],
                                       tutorials : List[String],
                                       cosmetics : Option[Cosmetics])
                                      (pad_length : Option[Int]) extends ConstructorData {

  override def bitsize : Long = {
    //factor guard bool values into the base size, not corresponding optional fields, unless contained or enumerated
    val certSize = (certs.length + 1) * 8 //cert list
    var implantSize : Long = 0L //implant list
    for(entry <- implants) {
      implantSize += entry.bitsize
    }
    val implantPadding = DetailedCharacterData.implantFieldPadding(implants, pad_length)
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
    val br24 = DetailedCharacterData.isBR24(bep) //character is at least BR24
    val extraBitSize : Long = if(br24) { 33L } else { 46L }
    val cosmeticsSize : Long = if(br24) { cosmetics.get.bitsize } else { 0L }
    598L + certSize + implantSize + eventListSize + extraBitSize + cosmeticsSize + tutorialListSize
  }
}

object DetailedCharacterData extends Marshallable[DetailedCharacterData] {
  /**
    * `Codec` for entries in the `List` of implants.
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
  def numberOfImplantSlots(bep : Long) : Int = {
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
    if(resultB < 0) { 8 + resultB } else { resultB }
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

  def isBR24(bep : Long) : Boolean = bep > 2286230

  def codec(pad_length : Option[Int]) : Codec[DetailedCharacterData] = (
    ("bep" | uint32L) >>:~ { bep =>
      ("cep" | uint32L) ::
        uint32L ::
        uint32L ::
        uint32L ::
        ("healthMax" | uint16L) ::
        ("health" | uint16L) ::
        ignore(1) ::
        ("armor" | uint16L) ::
        uint32 :: //TODO switch endianness
        ("staminaMax" | uint16L) ::
        ("stamina" | uint16L) ::
        ignore(147) ::
        ("certs" | listOfN(uint8L, CertificationType.codec)) ::
        optional(bool, uint32L) :: //ask about sample CCRIDER
        ignore(4) ::
        (("implants" | PacketHelpers.listOfNSized(numberOfImplantSlots(bep), implant_entry_codec)) >>:~ { implants =>
          ignore(12) ::
            (("firstTimeEvent_length" | uint32L) >>:~ { len =>
              conditional(len > 0, "firstTimeEvent_firstEntry" | PacketHelpers.encodedStringAligned(ftePadding(len, implantFieldPadding(implants, pad_length)))) ::
                ("firstTimeEvent_list" | PacketHelpers.listOfNSized(len - 1, PacketHelpers.encodedString)) ::
                (("tutorial_length" | uint32L) >>:~ { len2 =>
                  conditional(len2 > 0, "tutorial_firstEntry" | PacketHelpers.encodedStringAligned(tutPadding(len, len2, implantFieldPadding(implants, pad_length)))) ::
                    ("tutorial_list" | PacketHelpers.listOfNSized(len2 - 1, PacketHelpers.encodedString)) ::
                    ignore(160) ::
                    (bool >>:~ { br24 => //BR24+
                      newcodecs.binary_choice(br24, ignore(33), ignore(46)) ::
                        conditional(br24, Cosmetics.codec)
                    })
                })
            })
        })
    }
    ).exmap[DetailedCharacterData] (
    {
      case bep :: cep :: 0 :: 0 :: 0 :: hpmax :: hp :: _ :: armor :: 32831L :: stamax :: stam :: _ :: certs :: _ :: _  :: implants :: _ :: _ :: fte0 :: fte1 :: _ :: tut0 :: tut1 :: _ :: _ :: _ :: cosmetics :: HNil =>
        //prepend the displaced first elements to their lists
        val fteList : List[String] = if(fte0.isDefined) { fte0.get +: fte1 } else { fte1 }
        val tutList : List[String] = if(tut0.isDefined) { tut0.get +: tut1 } else { tut1 }
        Attempt.successful(new DetailedCharacterData(bep, cep, hpmax, hp, armor, stamax, stam, certs, implants, fteList, tutList, cosmetics)(pad_length))
    },
    {
      case DetailedCharacterData(bep, cep, hpmax, hp, armor, stamax, stam, certs, implants, fteList, tutList, cos) =>
        val implantCapacity : Int = numberOfImplantSlots(bep)
        val implantList = if(implants.length > implantCapacity) {
          implants.slice(0, implantCapacity)
        }
        else {
          recursiveEnsureImplantSlots(implantCapacity, implants)
        }
        //shift the first elements off their lists
        val (firstEvent, fteListCopy) = fteList match {
          case (f : String) +: Nil => (Some(f), Nil)
          case ((f : String) +: (rest : List[String])) => (Some(f), rest)
          case Nil => (None, Nil)
        }
        val (firstTutorial, tutListCopy) = tutList match {
          case (f : String) +: Nil => (Some(f), Nil)
          case ((f : String) +: (rest : List[String])) => (Some(f), rest)
          case Nil => (None, Nil)
        }
        val br24 : Boolean = isBR24(bep)
        val cosmetics : Option[Cosmetics] = if(br24) { cos } else { None }
        Attempt.successful(bep :: cep :: 0L :: 0L :: 0L :: hpmax :: hp :: () :: armor :: 32831L :: stamax :: stam :: () :: certs :: None :: () :: implantList :: () :: fteList.size.toLong :: firstEvent :: fteListCopy :: tutList.size.toLong :: firstTutorial :: tutListCopy :: () :: br24 :: () :: cosmetics :: HNil)
    }
  )

  implicit val codec : Codec[DetailedCharacterData] = codec(None)
}

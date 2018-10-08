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
    val activationSize = if(activation.isDefined) { 8L } else { 1L }
    9L + activationSize
  }
}

final case class DCDExtra1(unk1 : String,
                           unk2 : Int) extends StreamBitSize {
  override def bitsize : Long = 16L + StreamBitSize.stringBitSize(unk1)
}

final case class DCDExtra2(unk1 : Int,
                           unk2 : Int) extends StreamBitSize {
  override def bitsize : Long = 13L
}

/**
  * A representation of a portion of an avatar's `ObjectCreateDetailedMessage` packet data.<br>
  * <br>
  * This densely-packed information outlines most of the specifics required to depict a character as an avatar.
  * It goes into depth about information related to the given character in-game career that is not revealed to other players.
  * To be specific, it passes more thorough data about the character that the client can display to the owner of the client.
  * For example, health is a full number, rather than a percentage, as is the case with `CharacterData`.
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
                                       unk1 : Option[Long],
                                       implants : List[ImplantEntry],
                                       unk2 : List[DCDExtra1],
                                       unk3 : List[DCDExtra1],
                                       firstTimeEvents : List[String],
                                       tutorials : List[String],
                                       cosmetics : Option[Cosmetics])
                                      (pad_length : Option[Int]) extends ConstructorData {

  override def bitsize : Long = {
    //factor guard bool values into the base size, not corresponding optional fields, unless contained or enumerated
    //cert list
    val certSize = (certs.length + 1) * 8
    //unk1
    val unk1Size = if(unk1.isDefined) { 32L } else { 0L }
    //implant list
    var implantSize : Long = 0L
    for(entry <- implants) {
      implantSize += entry.bitsize
    }
    val implantPadding = DetailedCharacterData.implantFieldPadding(implants, pad_length)
    //fte list
    val fteLen = firstTimeEvents.size
    var eventListSize : Long = 32L + DetailedCharacterData.ftePadding(fteLen, implantPadding)
    for(str <- firstTimeEvents) {
      eventListSize += StreamBitSize.stringBitSize(str)
    }
    //unk2, unk3, TODO padding
    val unk2Len = unk2.size
    val unk3Len = unk3.size
    val unkAllLen = unk2Len + unk3Len
    val unk2_3ListSize : Long = 16L + (if(unk2Len > 0) {
      unkAllLen * unk2.head.bitsize
    }
    else if(unk3Len > 0) {
      unkAllLen * unk3.head.bitsize
    }
    else {
      0
    })
    //tutorial list
    val tutLen = tutorials.size
    var tutorialListSize : Long = 32L + DetailedCharacterData.tutPadding(fteLen, tutLen, implantPadding)
    for(str <- tutorials) {
      tutorialListSize += StreamBitSize.stringBitSize(str)
    }
    //character is at least BR24
    val br24 = DetailedCharacterData.isBR24(bep)
    val extraBitSize : Long = if(br24) { 0L } else { 13L }
    //TODO DCDExtra2
    //TODO last List of String values, and padding
    val cosmeticsSize : Long = if(br24) { cosmetics.get.bitsize } else { 0L }
    615L + certSize + unk1Size + implantSize + eventListSize + unk2_3ListSize + tutorialListSize + extraBitSize + cosmeticsSize
  }
}

object DetailedCharacterData extends Marshallable[DetailedCharacterData] {
  def apply(bep : Long,
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
            cosmetics : Option[Cosmetics]) : Option[Int]=>DetailedCharacterData = {
    DetailedCharacterData(bep, cep, healthMax, health, armor, staminaMax, stamina, certs, None, implants, Nil, Nil, firstTimeEvents, tutorials, cosmetics)
  }

  /**
    * `Codec` for entries in the `List` of implants.
    */
  private val implant_entry_codec : Codec[ImplantEntry] = (
    ("implant" | uint8L) ::
      (bool >>:~ { guard =>
        newcodecs.binary_choice(guard, uint(1), uint8L).hlist
      })
  ).xmap[ImplantEntry] (
    {
      case implant :: true :: _ :: HNil =>
          ImplantEntry(ImplantType(implant), None) //TODO catch potential NoSuchElementException?

      case implant :: false :: extra :: HNil =>
        ImplantEntry(ImplantType(implant), Some(extra)) //TODO catch potential NoSuchElementException?
    },
    {
      case ImplantEntry(implant, None) =>
        implant.id :: true :: 0 :: HNil

      case ImplantEntry(implant, Some(extra)) =>
        implant.id :: false :: extra :: HNil
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

  private def dcd_list_codec(pad : Int) : Codec[List[DCDExtra1]] = (
    uint8 >>:~ { size =>
      conditional(size > 0, dcd_extra1_codec(pad)) ::
        PacketHelpers.listOfNSized(size - 1, dcd_extra1_codec(0))
    }
    ).xmap[List[DCDExtra1]] (
    {
      case _ :: Some(first) :: Nil :: HNil =>
        List(first)
      case _ :: Some(first) :: rest :: HNil =>
        first +: rest
      case _ :: None :: _ :: HNil =>
        List()
    },
    {
      case List() =>
        0 :: None :: Nil :: HNil
      case contents =>
        contents.length :: contents.headOption :: contents.tail :: HNil
    }
  )

  private def dcd_extra1_codec(pad : Int) : Codec[DCDExtra1] = (
    ("unk1" | PacketHelpers.encodedStringAligned(pad)) ::
      ("unk2" | uint16L)
    ).xmap[DCDExtra1] (
    {
      case unk1 :: unk2 :: HNil =>
        DCDExtra1(unk1, unk2)
    },
    {
      case DCDExtra1(unk1, unk2) =>
        unk1.slice(0, 80) :: unk2 :: HNil //max 80 characters
    }
  )

  private def eventsListCodec(padFunc : (Long)=>Int) : Codec[List[String]] = (
    uint32L >>:~ { size =>
      conditional(size > 0, PacketHelpers.encodedStringAligned(padFunc(size))) ::
        PacketHelpers.listOfNSized(size - 1, PacketHelpers.encodedString)
    }
    ).xmap[List[String]] (
    {
      case _ :: Some(first) :: Nil :: HNil =>
        List(first)
      case _ :: Some(first) :: rest :: HNil =>
        first +: rest
      case _ :: None :: _ :: HNil =>
        List()
    },
    {
      case List() =>
        0 :: None :: Nil :: HNil
      case contents =>
        contents.length :: contents.headOption :: contents.tail :: HNil
    }
  )

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
    * A variant of `ftePadding` where the length of the list has been uncurried.
    * @see `ftePadding(Int)(Long)`
    */
  private def ftePadding(len : Long, implantPadding : Int) : Int = {
    //TODO the proper padding length should reflect all variability in the stream prior to this point
    ftePadding(implantPadding)(len)
  }

  /**
    * Get the padding of the first entry in the first time events list.
    * @see `ftePadding(Long, Int)`
    * @param len the length of the first time events list
    * @param implantPadding the padding that resulted from implant entries
    * @return the pad length in bits `0 <= n < 8`
    */
  private def ftePadding(implantPadding : Int)(len : Long) : Int = {
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
    * @see `tutPadding(Long, Long, Int)`
    * @param len the length of the first time events list
    * @param implantPadding the padding that resulted from implant entries
    * @param len2 the length of the tutorial list, curried
    * @return the pad length in bits `n < 8`
    */
  private def tutPadding(len : Long, implantPadding : Int)(len2 : Long) : Int = {
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

  /**
    * A variant of `tutPadding` where the length of the second list has been uncurried.
    * @see `tutPadding(Long, Int)(Long)`
    */
  private def tutPadding(len : Long, len2 : Long, implantPadding : Int) : Int = tutPadding(len, implantPadding)(len2)

  def isBR24(bep : Long) : Boolean = bep > 2286230

  private val dcd_extra2_codec : Codec[DCDExtra2] = (
    uint(5) ::
      uint8L
  ).as[DCDExtra2]

  def codec(pad_length : Option[Int]) : Codec[DetailedCharacterData] = (
    ("bep" | uint32L) >>:~ { bep =>
      ("cep" | uint32L) ::
        uint32L ::
        uint32L ::
        uint32L ::
        ("healthMax" | uint16L) ::
        ("health" | uint16L) ::
        bool ::
        ("armor" | uint16L) ::
        uint32 :: //endianness is important here
        ("staminaMax" | uint16L) ::
        ("stamina" | uint16L) ::
        uint16L ::
        uint(3) ::
        uint32L ::
        PacketHelpers.listOfNSized(6, uint16L) ::
        ("certs" | listOfN(uint8L, CertificationType.codec)) ::
        optional(bool, "unk1" | uint32L) :: //ask about sample CCRIDER
        (("implants" | PacketHelpers.listOfNSized(numberOfImplantSlots(bep), implant_entry_codec)) >>:~ { implants =>
          ("unk2" | dcd_list_codec(0)) :: //TODO pad value
            ("unk3" | dcd_list_codec(0)) :: //TODO pad value
            (("firstTimeEvents" | eventsListCodec(ftePadding(implantFieldPadding(implants, pad_length)))) >>:~ { fte =>
              ("tutorials" | eventsListCodec(tutPadding(fte.length, implantFieldPadding(implants, pad_length)))) >>:~ { _ =>
                uint32L ::
                  uint32L ::
                  uint32L ::
                  uint32L ::
                  uint32L ::
                  (bool >>:~ { br24 => //BR24+
                    conditional(!br24, dcd_extra2_codec) ::
                      listOfN(uint16L, uint32L) ::
                      listOfN(uint16L, PacketHelpers.encodedString) :: //TODO pad value
                      bool ::
                      conditional(br24, Cosmetics.codec)
                  })
                }
            })
        })
    }
    ).exmap[DetailedCharacterData] (
    {
      case o @ (bep :: cep :: 0 :: 0 :: 0 :: hpmax :: hp :: _ :: armor :: 32831L :: stamax :: stam :: 0 :: _ :: _ :: _ :: certs :: unk1 :: implants :: unk2 :: unk3 :: fteList :: tutList :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: cosmetics :: HNil) =>
        println(o)
        Attempt.successful(DetailedCharacterData(bep, cep, hpmax, hp, armor, stamax, stam, certs, unk1, implants, unk2, unk3, fteList, tutList, cosmetics)(pad_length))
    },
    {
      case DetailedCharacterData(bep, cep, hpmax, hp, armor, stamax, stam, certs, unk1, implants, unk2, unk3, fteList, tutList, cos) =>
        val implantCapacity : Int = numberOfImplantSlots(bep)
        val implantList = if(implants.length > implantCapacity) {
          implants.slice(0, implantCapacity)
        }
        else {
          recursiveEnsureImplantSlots(implantCapacity, implants)
        }
        val br24 : Boolean = isBR24(bep)
        val dcdExtra2Field : Option[DCDExtra2] = if(!br24) {
          Some(DCDExtra2(0, 0))
        }
        else {
          None
        }
        val cosmetics : Option[Cosmetics] = if(br24) { cos } else { None }
        Attempt.successful(bep :: cep :: 0L :: 0L :: 0L :: hpmax :: hp :: false :: armor :: 32831L :: stamax :: stam :: 0 :: 0 :: 0L :: List(0, 0, 0, 0, 0, 0) :: certs :: unk1 :: implantList :: unk2 :: unk3 :: fteList :: tutList :: 0L :: 0L :: 0L :: 0L :: 0L :: br24 :: dcdExtra2Field :: Nil :: Nil :: false :: cosmetics :: HNil)
    }
  )

  implicit val codec : Codec[DetailedCharacterData] = codec(None)
}

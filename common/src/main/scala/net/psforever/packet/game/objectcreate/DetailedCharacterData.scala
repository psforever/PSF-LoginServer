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
  * @param implant the type of implant
  *                technically, this is unconfirmed
  * @param active whether this implant is turned on;
  *               technically, this is unconfirmed
  * @see `ImplantType`
  */
final case class ImplantEntry(implant : ImplantType.Value,
                              initialization : Option[Int],
                              active : Boolean) extends StreamBitSize {
  override def bitsize : Long = {
    val timerSize = initialization match { case Some(_) => 8L ; case None => 1L }
    9L + timerSize
  }
}

object ImplantEntry {
  def apply(implant : ImplantType.Value, initialization : Option[Int]) : ImplantEntry = {
    ImplantEntry(implant, initialization, false)
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
  * A representation of a portion of an avatar's `ObjectCreateDetailedMessage` packet data.
  * @see `CharacterData`
  * @see `CertificationType`
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
  * @param certs the `List` of certifications
  */
final case class DetailedCharacterA(bep : Long,
                                    cep : Long,
                                    unk1 : Long,
                                    unk2 : Long,
                                    unk3 : Long,
                                    healthMax : Int,
                                    health : Int,
                                    unk4 : Boolean,
                                    armor : Int,
                                    unk5 : Long,
                                    staminaMax : Int,
                                    stamina : Int,
                                    unk6 : Int,
                                    unk7 : Int,
                                    unk8 : Long,
                                    unk9 : List[Int],
                                    certs : List[CertificationType.Value]) extends StreamBitSize {
  override def bitsize : Long = {
    val certSize : Long = certs.length * 8
    428L + certSize
  }
}

/**
  * A representation of a portion of an avatar's `ObjectCreateDetailedMessage` packet data.
  * @see `CharacterData`
  * @see `Cosmetics`
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
  */
final case class DetailedCharacterB(unk1 : Option[Long],
                                    implants : List[ImplantEntry],
                                    unk2 : List[DCDExtra1],
                                    unk3 : List[DCDExtra1],
                                    firstTimeEvents : List[String],
                                    tutorials : List[String],
                                    unk4 : Long,
                                    unk5 : Long,
                                    unk6 : Long,
                                    unk7 : Long,
                                    unk8 : Long,
                                    unk9 : Option[DCDExtra2],
                                    unkA : List[Long],
                                    unkB : List[String],
                                    unkC : Boolean,
                                    cosmetics : Option[Cosmetics])
                                   (bep : Long,
                                    pad_length : Option[Int]) extends StreamBitSize {
  override def bitsize : Long = {
    //unk1
    val unk1Size = unk1 match { case Some(_) => 32L ; case None => 0L }
    //implant list
    val implantSize : Long = implants.foldLeft(0L)(_ + _.bitsize)
    //fte list
    val fteLen = firstTimeEvents.size
    val eventListSize : Long = firstTimeEvents.foldLeft(0L)(_ + StreamBitSize.stringBitSize(_))
    //tutorial list
    val tutLen = tutorials.size
    val tutorialListSize : Long = tutorials.foldLeft(0L)(_ + StreamBitSize.stringBitSize(_))
    val unk2Len = unk2.size
    val unk3Len = unk3.size
    val unkAllLen = unk2Len + unk3Len
    val unk2_3ListSize : Long = if(unk2Len > 0) {
      unkAllLen * unk2.head.bitsize
    }
    else if(unk3Len > 0) {
      unkAllLen * unk3.head.bitsize
    }
    else {
      0L
    }
    //character is at least BR24
    val br24 = DetailedCharacterData.isBR24(bep)
    val unk9Size : Long = if(br24) { 0L } else { 13L }
    val unkASize : Long = unkA.length * 32L
    val unkBSize : Long = unkB.foldLeft(0L)(_ + StreamBitSize.stringBitSize(_))
    val cosmeticsSize : Long = if(br24) { cosmetics.get.bitsize } else { 0L }

    val paddingSize : Int =
      DetailedCharacterData.paddingCalculations(pad_length, implants, Nil)(unk2Len) + /* unk2 */
        DetailedCharacterData.paddingCalculations(pad_length, implants, List(unk2))(unk3Len) + /* unk3 */
        DetailedCharacterData.paddingCalculations(pad_length, implants, List(unk3, unk2))(fteLen) + /* firstTimeEvents */
        DetailedCharacterData.paddingCalculations(pad_length, implants, List(firstTimeEvents, unk3, unk2))(tutLen) + /* tutorials */
        DetailedCharacterData.paddingCalculations(
          DetailedCharacterData.displaceByUnk9(pad_length, unk9, 5),
          implants,
          List(DetailedCharacterData.optToList(unk9), tutorials, firstTimeEvents, unk3, unk2)
        )(unkB.length) /* unkB */
    275L + unk1Size + implantSize + eventListSize + unk2_3ListSize + tutorialListSize + unk9Size + unkASize + unkBSize + cosmeticsSize + paddingSize
  }
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
  * @see `CharacterData`
  */
final case class DetailedCharacterData(a : DetailedCharacterA,
                                       b : DetailedCharacterB)
                                      (pad_length : Option[Int]) extends ConstructorData {

  override def bitsize : Long = a.bitsize + b.bitsize
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
    val a = DetailedCharacterA(
      bep,
      cep,
      0L,
      0L,
      0L,
      healthMax, health,
      false,
      armor,
      0L,
      staminaMax, stamina,
      0,
      0,
      0L,
      List(0,0,0,0,0,0),
      certs
    )
    val b : (Long, Option[Int]) => DetailedCharacterB = DetailedCharacterB(
      None,
      implants,
      Nil,
      Nil,
      firstTimeEvents,
      tutorials,
      0L,
      0L,
      0L,
      0L,
      0L,
      None,
      Nil,
      Nil,
      false,
      cosmetics
    )
    (pad_length : Option[Int]) => DetailedCharacterData(a, b(a.bep, pad_length))(pad_length)
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
      case implant :: true :: n :: HNil => //initialized (no timer), active/inactive?
        val activeBool : Boolean = n != 0
        ImplantEntry(ImplantType(implant), None, activeBool) //TODO catch potential NoSuchElementException?

      case implant :: false :: extra :: HNil => //unintialized (timer), inactive
        ImplantEntry(ImplantType(implant), Some(extra), false) //TODO catch potential NoSuchElementException?
    },
    {
      case ImplantEntry(implant, None, n) => //initialized (no timer), active/inactive?
        val activeInt : Int = if(n) { 1 } else  { 0 }
        implant.id :: true :: activeInt :: HNil

      case ImplantEntry(implant, Some(extra), _) => //unintialized (timer), inactive
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

  private def dcd_list_codec(padFunc : (Long)=>Int) : Codec[List[DCDExtra1]] = (
    uint8 >>:~ { size =>
      conditional(size > 0, dcd_extra1_codec(padFunc(size))) ::
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

  private val dcd_extra2_codec : Codec[DCDExtra2] = (
    uint(5) ::
      uint8L
    ).as[DCDExtra2]

  private def unkBCodec(padFunc : (Long)=>Int) : Codec[List[String]] = (
    uint16L >>:~ { size =>
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

  def optToList(opt : Option[Any]) : List[Any] = opt match {
    case Some(o) => List(o)
    case None => Nil
  }

  def displaceByUnk9(start : Option[Int], test : Option[Any], value : Int) : Option[Int] = test match {
    case Some(_) =>
      Some(start.getOrElse(0) + value)
    case None =>
      start
  }

  private val displacementPerEntry : List[Int] = List(7, 0, 0, 0, 0)

  def paddingCalculations(contextOffset : Option[Int], implants : List[ImplantEntry], prevLists : List[List[Any]])(currListLen : Long) : Int = {
    paddingCalculations(3, contextOffset, implants, prevLists)(currListLen)
  }

  def paddingCalculations(base : Int, contextOffset : Option[Int], implants : List[ImplantEntry], prevLists : List[List[Any]])(currListLen : Long) : Int = {
    if(currListLen > 0) {
      //the offset with no implant entries, or bits from context
      //displacement into next byte of the content field of the first relevant string without padding
      val baseResult : Int = base + contextOffset.getOrElse(0) + implants.foldLeft(0L)(_ + _.bitsize).toInt

      val displacementResult : Int = (if(prevLists.isEmpty) {
        baseResult
      }
      else {
        //isolate the displacements that are important
        val sequentialEmptyLists : List[List[Any]] = prevLists.takeWhile(_.isEmpty)
        val offsetSlice : List[Int] = displacementPerEntry.drop(displacementPerEntry.length - sequentialEmptyLists.length)
        if(prevLists.length == sequentialEmptyLists.length) { //if all lists are empty, factor in the base displacement
          baseResult + offsetSlice.sum
        }
        else {
          offsetSlice.sum
        }
      }) % 8
      if(displacementResult != 0) {
        8 - displacementResult
      }
      else {
        0
      }
    }
    else {
      0 //if the current list has no length, there's no need to pad it
    }
  }

//  /**
//    * The padding value of the first entry in either of two byte-aligned `List` structures.
//    * @param implants implant entries
//    * @return the pad length in bits `0 <= n < 8`
//    */
//  def implantFieldPadding(implants : List[ImplantEntry], varBit : Option[Int] = None) : Int = {
//    val base : Int = 5 //the offset with no implant entries
//    val baseOffset : Int = base - varBit.getOrElse(0)
//    val resultA = if(baseOffset < 0) { 8 - baseOffset } else { baseOffset % 8 }
//
//    val implantOffset = implants.foldLeft(0L)(_ + _.bitsize).toInt
//    val resultB : Int = resultA - (implantOffset % 8)
//    if(resultB < 0) { 8 + resultB } else { resultB }
//  }

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

  def isBR24(bep : Long) : Boolean = bep > 2286230

  val a_codec : Codec[DetailedCharacterA] = (
    ("bep" | uint32L) ::
      ("cep" | uint32L) ::
      ("unk1" | uint32L) ::
      ("unk2" | uint32L) ::
      ("unk3" | uint32L) ::
      ("healthMax" | uint16L) ::
      ("health" | uint16L) ::
      ("unk4" | bool) ::
      ("armor" | uint16L) ::
      ("unk5" | uint32) :: //endianness?
      ("staminaMax" | uint16L) ::
      ("stamina" | uint16L) ::
      //TODO optional 32u-something here; see ps.c: line#1070692
      ("unk6" | uint16L) ::
      ("unk7" | uint(3)) ::
      ("unk8" | uint32L) ::
      ("unk9" | PacketHelpers.listOfNSized(6, uint16L)) :: //always 6
      ("certs" | listOfN(uint8L, CertificationType.codec))
    ).exmap[DetailedCharacterA] (
    {
      case bep :: cep :: u1 :: u2 :: u3 :: healthMax :: health :: u4 :: armor :: u5 :: staminaMax :: stamina :: u6 :: u7 :: u8 :: u9 :: certs :: HNil =>
        Attempt.successful(DetailedCharacterA(bep, cep, u1, u2, u3, healthMax, health, u4, armor, u5, staminaMax, stamina, u6, u7, u8, u9, certs))
    },
    {
      case DetailedCharacterA(bep, cep, u1, u2, u3, healthMax, health, u4, armor, u5, staminaMax, stamina, u6, u7, u8, u9, certs) =>
        Attempt.successful(
          bep :: cep :: u1 :: u2 :: u3 :: healthMax :: health :: u4 :: armor :: u5 :: staminaMax :: stamina :: u6 :: u7 :: u8 :: u9 :: certs :: HNil
        )
    }
  )

  def b_codec(bep : Long, pad_length : Option[Int]) : Codec[DetailedCharacterB] = (
    optional(bool, "unk1" | uint32L) :: //ask about sample CCRIDER
      (("implants" | PacketHelpers.listOfNSized(numberOfImplantSlots(bep), implant_entry_codec)) >>:~ { implants =>
        ("unk2" | dcd_list_codec(paddingCalculations(pad_length, implants, Nil))) >>:~ { unk2 =>
          ("unk3" | dcd_list_codec(paddingCalculations(pad_length, implants, List(unk2)))) >>:~ { unk3 =>
            ("firstTimeEvents" | eventsListCodec(paddingCalculations(pad_length, implants, List(unk3, unk2)))) >>:~ { fte =>
              ("tutorials" | eventsListCodec(paddingCalculations(pad_length, implants, List(fte, unk3, unk2)))) >>:~ { tut =>
                ("unk4" | uint32L) ::
                  ("unk5" | uint32L) ::
                  ("unk6" | uint32L) ::
                  ("unk7" | uint32L) ::
                  ("unk8" | uint32L) ::
                  (bool >>:~ { br24 => //BR24+
                    conditional(!br24, "unk9" | dcd_extra2_codec) >>:~ { unk9 =>
                      ("unkA" | listOfN(uint16L, uint32L)) ::
                        ("unkB" | unkBCodec(
                          paddingCalculations(
                            displaceByUnk9(pad_length, unk9, 5),
                            implants,
                            List(optToList(unk9), tut, fte, unk3, unk2)
                          )
                        )) ::
                        ("unkC" | bool) ::
                        conditional(br24, "cosmetics" | Cosmetics.codec)
                    }
                  })
              }
            }
          }
        }
      })
    ).exmap[DetailedCharacterB] (
    {
      case u1 :: implants :: u2 :: u3 :: fte :: tut :: u4 :: u5 :: u6 :: u7 :: u8 :: _ :: u9 :: uA :: uB :: uC :: cosmetics :: HNil =>
        Attempt.successful(
          DetailedCharacterB(u1, implants, u2, u3, fte, tut, u4, u5, u6, u7, u8, u9, uA, uB, uC, cosmetics)(bep, pad_length)
        )
    },
    {
      case DetailedCharacterB(u1, implants, u2, u3, fte, tut, u4, u5, u6, u7, u8, u9, uA, uB, uC, cosmetics) =>
        val implantCapacity : Int = numberOfImplantSlots(bep)
        val implantList = if(implants.length > implantCapacity) {
          implants.slice(0, implantCapacity)
        }
        else {
          recursiveEnsureImplantSlots(implantCapacity, implants)
        }
        val br24 : Boolean = isBR24(bep)
        val cos : Option[Cosmetics] = if(br24) { cosmetics } else { None }
        Attempt.successful(
          u1 :: implantList :: u2 :: u3 :: fte :: tut :: u4 :: u5 :: u6 :: u7 :: u8 :: br24 :: u9 :: uA :: uB :: uC :: cos :: HNil
        )
    }
  )

  def codec(pad_length : Option[Int]) : Codec[DetailedCharacterData] = (
    ("a" | a_codec) >>:~ { a =>
      ("b" | b_codec(a.bep, pad_length)).hlist
    }
    ).exmap[DetailedCharacterData] (
    {
      case a :: b :: HNil =>
        Attempt.successful(DetailedCharacterData(a, b)(pad_length))
    },
    {
      case DetailedCharacterData(a, b) =>
        Attempt.successful(a :: b :: HNil)
    }
  )

  implicit val codec : Codec[DetailedCharacterData] = codec(None)
}

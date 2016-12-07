// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.types.Vector3
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

case class CharacterAppearanceData(pos : Vector3,
                                   objYaw : Int,
                                   faction : Int,
                                   bops : Boolean,
                                   name : String,
                                   exosuit : Int,
                                   sex : Int,
                                   face1 : Int,
                                   face2 : Int,
                                   voice : Int,
                                   unk1 : Int, //0x8080
                                   unk2 : Int, //0xFFFF or 0x0
                                   unk3 : Int, //2
                                   viewPitch : Int,
                                   viewYaw : Int,
                                   ribbons : RibbonBars)

object CharacterAppearanceData extends Marshallable[CharacterAppearanceData] {
  implicit val codec : Codec[CharacterAppearanceData] = (
    ("pos" | Vector3.codec_pos) ::
      ignore(16) ::
      ("objYaw" | uint8L) ::
      ignore(1) ::
      ("faction" | uintL(2)) ::
      ("bops" | bool) ::
      ignore(20) ::
      ("name" | PacketHelpers.encodedWideStringAligned(4)) ::
      ("exosuit" | uintL(3)) ::
      ignore(2) ::
      ("sex" | uintL(2)) ::
      ("face1" | uint4L) ::
      ("face2" | uint4L) ::
      ("voice" | uintL(3)) ::
      ignore(22) ::
      ("unk1" | uint16L) ::
      ignore(42) ::
      ("unk2" | uint16L) ::
      ignore(30) ::
      ("unk3" | uint4L) ::
      ignore(24) ::
      ("viewPitch" | uint8L) ::
      ("viewYaw" | uint8L) ::
      ignore(10) ::
      ("ribbons" | RibbonBars.codec)
    ).as[CharacterAppearanceData]
}

/**
  * A representation of the avatar portion of `ObjectCreateMessage` packet data.<br>
  * <br>
  * This object is huge, representing the quantity of densely-encoded data in its packet.
  * Although the actual organization is ill-defined, the packet can be divided into seven parts.
  * The first part maintains information about the avatar as a game object in the game environment.
  * The second part maintains information as an ongoing representation of the avatar.
  * This includes fixed details like name and gender, though it also includes mutable aspects like exosuit type.
  * The third part maintains information about career in the game.
  * The fourth part maintains miscellaneous status and pose information.
  * The fifth part maintains part of the statistical information about participation in the game.
  * The sixth part maintains a stream of typically zero'd unknown information.
  * The seventh part maintains the inventory.
  * The fifth and seventh parts can inflate the size of packet significantly due to their encoding.
  * The fifth, in particular, is string data that can number in the hundreds of strings(!).<br>
  * <br>
  * Ignoring the strings, lists of strings, and the inventory, the base length of the packet is currently __1138__ bits.
  * Some undefined bits in the packet can change the length of the packet by being set or unset.
  * This will mess with the encoding and the decoding of later fields.
  * Any data that is padded for byte-alignment will also have its padding adjusted.
  * Each string adds either 8 or 16, plus an additional 8 or 16 per the number of characters.
  * For the name, that's 16 per character, a minimum of two characters, plus the (current) padding.
  * for the first time events and tutorials, that's 8 per character, plus the (current) padding of the first entry.
  * For the first time events and tutorials, however, the size of the list is always a 32-bit number.
  * The formal inventory entries are preceded by 1 absolute bit.<br>
  * <br>
  * The adjusted base length is therefore __1203__ bits (1138 + 32 + 32 + 1).
  * Of that, __720__ bits are unknown.
  * Including the values that are defaulted, __831__ bits are perfectly unknown.
  * This data is accurate as of 2016-12-07.<br>
  * <br>
  * Faction:<br>
  * `0 - Terran Republic`<br>
  * `1 - New Comglomerate`<br>
  * `2 - Vanu Sovereignty`<br>
  * <br>
  * Exosuit:<br>
  * `0 - Agile`<br>
  * `1 - Refinforced`<br>
  * `2 - Mechanized Assault`<br>
  * `3 - Infiltration`<br>
  * `4 - Standard`<br>
  * <br>
  * Sex:<br>
  * `1 - Male`<br>
  * `2 - Female`<br>
  * <br>
  * Voice:<br>
  * `&nbsp;&nbsp;&nbsp;&nbsp;MALE&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;FEMALE`<br>
  * `0 - No voice &nbsp;No voice`<br>
  * `1 - Male_1 &nbsp;&nbsp; Female_1`<br>
  * `2 - Male_2 &nbsp;&nbsp; Female_2`<br>
  * `3 - Male_3 &nbsp;&nbsp; Female_3`<br>
  * `4 - Male_4 &nbsp;&nbsp; Female_4`<br>
  * `5 - Male_5 &nbsp;&nbsp; Female_5`<br>
  * `6 - Female_1 &nbsp;No voice`<br>
  * `7 - Female_2 &nbsp;No voice`
//  * @param pos the position of the character in the world environment (in three coordinates)
//  * @param objYaw the angle with respect to the horizon towards which the object's front is facing;
//  *               every `0x1` is 2.813 degrees counter clockwise from North;
//  *               every `0x10` is 45-degrees;
//  *               it wraps at `0x80`
//  *               (note: references the avatar as a game object?)
//  * @param faction the empire to which the avatar belongs;
//  *                the value scale is different from `PlanetSideEmpire`;
//  * @param bops whether or not this avatar is enrolled in Black OPs
//  * @param name the wide character name of the avatar
//  * @param exosuit the type of exosuit the avatar will be depicted in;
//  *                for Black OPs, the agile exosuit and the reinforced exosuit are replaced with the Black OPs exosuits
//  * @param sex whether the avatar is male or female
//  * @param face1 the avatar's face, as by column number on the character creation screen
//  * @param face2 the avatar's face, as by row number on the character creation screen
//  * @param voice the avatar's voice selection
//  * @param unk1 na;
//  *             defaults to `0x8080`
//  * @param unk2 na;
//  *             defaults to `0xFFFF`;
//  *             may be `0x0`
//  * @param unk3 na;
//  *             defaults to 2
//  * @param viewPitch the angle with respect to the horizon towards which the avatar is looking;
//  *                  only supports downwards view angles;
//  *                  `0x0` is forwards-facing;
//  *                  `0x20` to `0xFF` is downwards-facing
//  * @param viewYaw the angle with respect to the ground directions towards which the avatar is looking;
//  *               every `0x1` is 2.813 degrees counter clockwise from North;
//  *               every `0x10` is 45-degrees;
//  *               it wraps at `0x80`
//  * @param ribbons the four merit commendation ribbon medals displayed on the avatar's left pauldron
//  * @param healthMax for "x / y" of hitpoints, this is the avatar's 'y' value;
//  *                  range is 0-65535
//  * @param health for "x / y" of hitpoints, this is the avatar's 'x' value;
//  *               range is 0-65535
//  * @param armor for "x / y" of armor points, this is the avatar's 'x' value;
//  *              range is 0-65535;
//  *              the avatar's "y" armor points is tied to their exosuit type
//  * @param unk4 na;
//  *             defaults to 1
//  * @param unk5 na;
//  *             defaults to 7
//  * @param unk6 na;
//  *             defaults to 7
//  * @param staminaMax for "x / y" of stamina points, this is the avatar's 'y' value;
//  *                   range is 0-65535
//  * @param stamina for "x / y" of stamina points, this is the avatar's 'x' value;
//  *                range is 0-65535
//  * @param unk7 na;
//  *             defaults to 28
//  * @param unk8 na;
//  *             defaults to 4
//  * @param unk9 na;
//  *             defaults to 44
//  * @param unk10 na;
//  *              defaults to 84
//  * @param unk11 na;
//  *              defaults to 104
//  * @param unk12 na;
//  *              defaults to 1900
//  * @param firstTimeEvent_length the total number of first time events performed by this avatar
//  * @param firstTimeEvent_firstEntry the separated "first entry" of the list of first time events performed by this avatar
//  * @param firstTimeEvent_list the list of first time events performed by this avatar
//  * @param tutorial_length the total number of tutorials completed by this avatar
//  * @param tutorial_firstEntry the separated "first entry" of the list of tutorials completed by this avatar
//  * @param tutorial_list the list of tutorials completed by this avatar
//  * @param inventory the avatar's inventory
  */
case class CharacterData(appearance : CharacterAppearanceData,
                         healthMax : Int,
                         health : Int,
                         armor : Int,
                         unk4 : Int, //1
                         unk5 : Int, //7
                         unk6 : Int, //7
                         staminaMax : Int,
                         stamina : Int,
                         unk7 : Int, //28
                         unk8 : Int, //4
                         unk9 : Int, //44
                         unk10 : Int, //84
                         unk11 : Int, //104
                         unk12 : Int, //1900
                         firstTimeEvents : List[String],
                         tutorials : List[String],
                         inventory : InventoryData
                        ) extends ConstructorData {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = {
//    //represents static fields (includes medals.bitsize)
//    val base : Long = 1138L //TODO ongoing analysis, this value will be subject to change
//    //name
//    val nameSize : Long = CharacterData.stringBitSize(appearance.name, 16) + 4L //plus the current padding
//    //fte_list
//    var eventListSize : Long = 32L
//    if(firstTimeEvent_firstEntry.isDefined) {
//      eventListSize += CharacterData.stringBitSize(firstTimeEvent_firstEntry.get) + 5L //plus the current padding
//      for(str <- firstTimeEvent_list) {
//        eventListSize += CharacterData.stringBitSize(str)
//      }
//    }
//    //tutorial list
//    var tutorialListSize : Long = 32L
//    for(str <- tutorial_list) {
//      tutorialListSize += CharacterData.stringBitSize(str)
//    }
//    base + nameSize + eventListSize + tutorialListSize + inventory.bitsize
    0L
  }
}

object CharacterData extends Marshallable[CharacterData] {
  /**
    * Calculate the size of a string, including the length of the "string length" field that precedes it.
    * Do not pass null-terminated strings.
    * @param str a length-prefixed string
    * @param width the width of the character encoding;
    *              defaults to the standard 8-bits
    * @return the size in bits
    */
  private def stringBitSize(str : String, width : Int = 8) : Long = {
    val strlen = str.length
    val lenSize = if(strlen > 127) 16L else 8L
    lenSize  + (strlen * width)
  }

  private def ftePadding(len : Long) : Int = {
    //TODO determine how this should be padded better
    5
  }

  private def tutListPadding(len : Long) : Int = {
    //TODO determine how this should be padded when len == 0
    if(len > 0) 0 else 0
  }

  implicit val codec : Codec[CharacterData] = (
    ("appearance" | CharacterAppearanceData.codec) ::
      ignore(160) ::
      ("healthMax" | uint16L) ::
      ("health" | uint16L) ::
      ignore(1) ::
      ("armor" | uint16L) ::
      ignore(9) ::
      ("unk4" | uint8L) ::
      ignore(8) ::
      ("unk5" | uint4L) ::
      ("unk6" | uintL(3)) ::
      ("staminaMax" | uint16L) ::
      ("stamina" | uint16L) ::
      ignore(149) ::
      ("unk7" | uint16L) ::
      ("unk8" | uint8L) ::
      ("unk9" | uint8L) ::
      ("unk10" | uint8L) ::
      ("unk11" | uint8L) ::
      ("unk12" | uintL(12)) ::
      ignore(19) ::
      (("firstTimeEvent_length" | uint32L) >>:~ { len =>
        conditional(len > 0, "firstTimeEvent_firstEntry" | PacketHelpers.encodedStringAligned( ftePadding(len) )) ::
          ("firstTimeEvent_list" | PacketHelpers.listOfNSized(len - 1, PacketHelpers.encodedString)) ::
          (("tutorial_length" | uint32L) >>:~ { len2 =>
            conditional(len2 > 0, "tutorial_firstEntry" | PacketHelpers.encodedStringAligned( tutListPadding(len) )) ::
              ("tutorial_list" | PacketHelpers.listOfNSized(len2 - 1, PacketHelpers.encodedString)) ::
              ignore(207) ::
              ("inventory" | InventoryData.codec)
        })
      })
    ).xmap[CharacterData] (
    {
      case app :: _ :: b :: c :: _ :: d :: _ :: e :: _ :: f :: g :: h :: i :: _ :: j :: k :: l :: m :: n :: o :: _ :: p :: q :: r :: s :: t :: u :: _ :: v :: HNil =>
        //prepend the displaced first elements to their lists
        val fteList : List[String] = if(q.isDefined) { q.get :: r } else r
        val tutList : List[String] = if(t.isDefined) { t.get :: u } else u
        CharacterData(app, b, c, d, e, f, g, h, i, j, k, l, m, n, o, fteList, tutList, v)
    },
    {
      case CharacterData(app, b, c, d, e, f, g, h, i, j, k, l, m, n, o, fteList, tutList, p) =>
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
        app :: () :: b :: c :: () :: d :: () :: e :: () :: f :: g :: h :: i :: () :: j :: k :: l :: m :: n :: o :: () :: fteList.size.toLong :: firstEvent :: fteListCopy :: tutList.size.toLong :: firstTutorial :: tutListCopy :: () :: p :: HNil
    }
  ).as[CharacterData]

  /**
    * Transform between CharacterData and ConstructorData.
    */
  val genericCodec : Codec[ConstructorData.genericPattern] = codec.exmap[ConstructorData.genericPattern] (
    {
      case x =>
        Attempt.successful(Some(x.asInstanceOf[ConstructorData]))
    },
    {
      case Some(x) =>
        Attempt.successful(x.asInstanceOf[CharacterData])
      case _ =>
        Attempt.failure(Err(""))
    }
  )
}

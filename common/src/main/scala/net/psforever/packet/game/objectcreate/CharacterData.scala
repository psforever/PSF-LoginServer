// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.types.Vector3
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A part of a representation of the avatar portion of `ObjectCreateMessage` packet data.<br>
  * <br>
  * This partition of the data stream contains information used to represent how the player's avatar is presented.
  * This appearance can be considered the avatar's obvious points beyond experience levels.
  * It does not include passive exo-suit upgrades, battle rank 24 cosmetics, special postures, or current equipment.
  * Those will occur later back in the main data stream.<br>
  * <br>
  * This base length of this stream is __430__ known bits, excluding the length of the name and the padding on that name.
  * Of that, __203__ bits are perfectly unknown in significance.
  * <br>
  * Faction:<br>
  * `0 - Terran Republic`<br>
  * `1 - New Conglomerate`<br>
  * `2 - Vanu Sovereignty`<br>
  * <br>
  * Exo-suit:<br>
  * `0 - Agile`<br>
  * `1 - Refinforced`<br>
  * `2 - Mechanized Assault`<br>
  * `3 - Infiltration`<br>
  * `4 - Standard`<br>
  * <br>
  * Sex:<br>
  * `0 - invalid`<br>
  * `1 - Male`<br>
  * `2 - Female`<br>
  * `3 - invalid`<br>
  * <br>
  * Voice:<br>
  * `&nbsp;&nbsp;&nbsp;&nbsp;MALE&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;FEMALE`<br>
  * `0 - no voice &nbsp;no voice`<br>
  * `1 - male_1 &nbsp;&nbsp; female_1`<br>
  * `2 - male_2 &nbsp;&nbsp; female_2`<br>
  * `3 - male_3 &nbsp;&nbsp; female_3`<br>
  * `4 - male_4 &nbsp;&nbsp; female_4`<br>
  * `5 - male_5 &nbsp;&nbsp; female_5`<br>
  * `6 - female_1 &nbsp;no voice`<br>
  * `7 - female_2 &nbsp;no voice`
  * @param pos the position of the character in the world environment (in three coordinates)
  * @param objYaw the angle with respect to the horizon towards which the object's front is facing;
  *               every `0x1` is 2.813 degrees counter clockwise from North;
  *               every `0x10` is 45-degrees;
  *               it wraps at `0x0` == `0x80` == North
  *               (note: references the avatar as a game object?)
  * @param faction the empire to which the avatar belongs;
  *                the value scale is different from `PlanetSideEmpire`
  * @param bops whether or not this avatar is enrolled in Black OPs
  * @param unk1 na;
  *             defaults to 4
  * @param name the wide character name of the avatar, minimum of two characters
  * @param exosuit the type of exosuit the avatar will be depicted in;
  *                for Black OPs, the agile exo-suit and the reinforced exo-suit are replaced with the Black OPs exo-suits
  * @param sex whether the avatar is male or female
  * @param face1 the avatar's face, as by column number on the character creation screen
  * @param face2 the avatar's face, as by row number on the character creation screen
  * @param voice the avatar's voice selection
  * @param unk2 na
  * @param unk3 na;
  *           can be missing from the stream under certain conditions;
  *           see next
  * @param unk4 na;
  *           can be missing from the stream under certain conditions;
  *           see previous
  * @param unk5 na;
  *             defaults to `0x8080`
  * @param unk6 na;
  *             defaults to `0xFFFF`;
  *             may be `0x0`
  * @param unk7 na;
  *             defaults to 2
  * @param viewPitch the angle with respect to the sky and the ground towards which the avatar is looking;
  *                  only supports downwards view angles;
  *                  `0x0` is forwards-facing;
  *                  `0x20` to `0xFF` is downwards-facing
  * @param viewYaw the angle with respect to the horizon towards which the avatar is looking;
  *               every `0x1` is 2.813 degrees counter clockwise from North;
  *               every `0x10` is 45-degrees;
  *               it wraps at `0x0` == `0x80` == North
  * @param unk8 na
  * @param ribbons the four merit commendation ribbon medals
  */
case class CharacterAppearanceData(pos : Vector3,
                                   objYaw : Int,
                                   faction : Int,
                                   bops : Boolean,
                                   unk1 : Int,
                                   name : String,
                                   exosuit : Int,
                                   sex : Int,
                                   face1 : Int,
                                   face2 : Int,
                                   voice : Int,
                                   unk2 : Int,
                                   unk3 : Int,
                                   unk4 : Int,
                                   unk5 : Int,
                                   unk6 : Int,
                                   unk7 : Int,
                                   viewPitch : Int,
                                   viewYaw : Int,
                                   unk8 : Int,
                                   ribbons : RibbonBars) extends StreamBitSize {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = {
    //TODO ongoing analysis, this value will be subject to change
    430L + CharacterData.stringBitSize(name, 16) + CharacterAppearanceData.namePadding
  }
}

object CharacterAppearanceData extends Marshallable[CharacterAppearanceData] {
  /**
    * Get the padding of the avatar's name.
    * The padding will always be a number 0-7.
    * @return the pad length in bits
    */
  private def namePadding : Int = {
    //TODO the parameters for this function are not correct
    //TODO the proper padding length should reflect all variability in the substream prior to this point
    4
  }

  implicit val codec : Codec[CharacterAppearanceData] = (
    ("pos" | Vector3.codec_pos) ::
      ignore(16) ::
      ("objYaw" | uint8L) ::
      ignore(1) ::
      ("faction" | uintL(2)) ::
      ("bops" | bool) ::
      ("unk1" | uint4L) ::
      ignore(16) ::
      ("name" | PacketHelpers.encodedWideStringAligned( namePadding )) ::
      ("exosuit" | uintL(3)) ::
      ignore(2) ::
      ("sex" | uintL(2)) ::
      ("face1" | uint4L) ::
      ("face2" | uint4L) ::
      ("voice" | uintL(3)) ::
      ("unk2" | uintL(2)) ::
      ignore(4) ::
      ("unk3" | uint8L) ::
      ("unk4" | uint8L) ::
      ("unk5" | uint16L) ::
      ignore(42) ::
      ("unk6" | uint16L) ::
      ignore(30) ::
      ("unk7" | uint4L) ::
      ignore(24) ::
      ("viewPitch" | uint8L) ::
      ("viewYaw" | uint8L) ::
      ("unk8" | uint4L) ::
      ignore(6) ::
      ("ribbons" | RibbonBars.codec)
    ).as[CharacterAppearanceData]
}

/**
  * A representation of the avatar portion of `ObjectCreateMessage` packet data.<br>
  * <br>
  * This object is huge, representing the quantity of densely-encoded data in its packet.
  * Certain bits, when set or unset, introduce or remove other bits from the packet data as well.
  * (As in: flipping a bit may create room or negate other bits from somewhere else in the data stream.
  * Not accounting for this new pattern of bits will break decoding and encoding.)
  * Due to the very real concern that bloating the constructor for this object with parameters could break the `apply` method,
  * parameters will often be composed of nested case objects that contain a group of formal parameters.
  * There are lists of byte-aligned `Strings` later-on in the packet data that will need access to these objects to calculate padding length.<br>
  * <br>
  * The first subdivision of parameters concerns the avatar's basic aesthetics, mostly.
  * (No other parts of the data divided up yet.)
  * The final sections include two lists of accredited activity performed/completed by the player.
  * The remainder of the data, following after that, can be read straight, up to and through the inventory.<br>
  * <br>
  * The base length of the stream is currently __1138__ bits, excluding `List`s and `String`s and inventory.
  * Of that, __831__ bits are perfectly unknown.
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
  *                        the size field is a 32-bit number;
  *                        the first entry may be padded
  * @param inventory the avatar's inventory
  */
case class CharacterData(appearance : CharacterAppearanceData,
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
                         inventory : InventoryData
                        ) extends ConstructorData {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = {
    //TODO ongoing analysis, this value will be subject to change
    //fte list
    val fteLen = firstTimeEvents.size
    var eventListSize : Long = 32L + CharacterData.ftePadding(fteLen)
    for(str <- firstTimeEvents) {
      eventListSize += CharacterData.stringBitSize(str)
    }
    //tutorial list
    val tutLen = tutorials.size
    var tutorialListSize : Long = 32L + CharacterData.tutPadding(fteLen, tutLen)
    for(str <- tutorials) {
      tutorialListSize += CharacterData.stringBitSize(str)
    }
    708L + appearance.bitsize + eventListSize + tutorialListSize + inventory.bitsize
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
  def stringBitSize(str : String, width : Int = 8) : Long = {
    val strlen = str.length
    val lenSize = if(strlen > 127) 16L else 8L
    lenSize  + (strlen * width)
  }

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
      1
    else //both lists are empty
      0
  }

  implicit val codec : Codec[CharacterData] = (
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
        Attempt.failure(Err("can not encode character data"))
    }
  )
}

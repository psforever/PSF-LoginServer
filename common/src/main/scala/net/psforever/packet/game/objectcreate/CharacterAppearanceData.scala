// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.types._
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A part of a representation of the avatar portion of `ObjectCreateDetailedMessage` packet data.
  * @see `CharacterData`
  * @see `DetailedCharacterData`
  * @see `ExoSuitType`
  * @param app the player's cardinal appearance settings
  * @param black_ops whether or not this avatar is enrolled in Black OPs
  * @param jammered the player has been caught in an EMP blast recently;
  *                 creates a jammered sound effect that follows the player around and can be heard by others
  * @param exosuit the type of exo-suit the avatar will be depicted in;
  *                for Black OPs, the agile exo-suit and the reinforced exo-suit are replaced with the Black OPs exo-suits
  */
final case class CharacterAppearanceA(app : BasicCharacterData,
                                      black_ops : Boolean,
                                      altModel : Boolean,
                                      unk1 : Boolean,
                                      unk2 : Option[CharacterAppearanceData.ExtraData],
                                      jammered : Boolean,
                                      exosuit : ExoSuitType.Value,
                                      unk3 : Option[Int],
                                      unk4 : Int,
                                      unk5 : Int,
                                      unk6 : Long,
                                      unk7 : Int,
                                      unk8 : Int,
                                      unk9 : Int,
                                      unkA : Int)
                                     (name_padding : Int) extends StreamBitSize {
  override def bitsize : Long = {
    //factor guard bool values into the base size, not its corresponding optional field
    val unk2Size : Long = unk2 match { case Some(n) => n.bitsize ; case None => 0L }
    val nameStringSize : Long = StreamBitSize.stringBitSize(app.name, 16) + name_padding
    val unk3Size : Long = unk3 match { case Some(_) => 32L ; case None => 0L }
    137L + unk2Size + nameStringSize + unk3Size
  }
}

/**
  * A part of a representation of the avatar portion of `ObjectCreateDetailedMessage` packet data.
  * @see `CharacterData`
  * @see `DetailedCharacterData`
  * @see `ExoSuitType`
  * @see `GrenadeState`
  * @see `RibbonBars`
  * @see `http://www.planetside-universe.com/p-outfit-decals-31.htm`
  * @param outfit_name the name of the outfit to which this player belongs;
  *                    if the option is selected, allies with see either "[`outfit_name`]" or "{No Outfit}" under the player's name
  * @param outfit_logo the decal seen on the player's exo-suit (and beret and cap) associated with the player's outfit;
  *                    if there is a variable color for that decal, the faction-appropriate one is selected
  * @param facingPitch a "pitch" angle
  * @param facingYawUpper a "yaw" angle that represents the angle of the avatar's upper body with respect to its forward-facing direction;
  *                       this number is normally 0 for forward facing;
  *                       the range is limited between approximately 61 degrees of center turned to left or right
  * @param lfs this player is looking for a squad;
  *            all allies will see the phrase "[Looking for Squad]" under the player's name
  * @param is_cloaking avatar is cloaked by virtue of an Infiltration Suit
  * @param grenade_state if the player has a grenade `Primed`;
  *                      should be `GrenadeStateState.None` if nothing special
  * @param charging_pose animation pose for both charging modules and BFR imprinting
  * @param on_zipline player's model is changed into a faction-color ball of energy, as if on a zip line
  */
final case class CharacterAppearanceB(unk0 : Long,
                                      outfit_name : String,
                                      outfit_logo : Int,
                                      unk1 : Boolean,
                                      backpack : Boolean,
                                      unk2 : Boolean,
                                      unk3 : Boolean,
                                      unk4 : Boolean,
                                      facingPitch : Float,
                                      facingYawUpper : Float,
                                      lfs : Boolean,
                                      grenade_state : GrenadeState.Value,
                                      is_cloaking : Boolean,
                                      unk5 : Boolean,
                                      unk6 : Boolean,
                                      charging_pose : Boolean,
                                      unk7 : Boolean,
                                      on_zipline : Option[CharacterAppearanceData.ZiplineData])
                                     (alt_model : Boolean, name_padding : Int) extends StreamBitSize {
  override def bitsize : Long = {
    //factor guard bool values into the base size, not its corresponding optional field
    val outfitStringSize : Long = StreamBitSize.stringBitSize(outfit_name, 16) +
      CharacterAppearanceData.outfitNamePadding //even if the outfit_name is blank, string is always padded
    val backpackSize = if(backpack) { 1L } else { 0L }
    val onZiplineSize : Long = on_zipline match { case Some(n) => n.bitsize; case None => 0 }
    70L + outfitStringSize + backpackSize + onZiplineSize
  }
}

/**
  * A part of a representation of the avatar portion of `ObjectCreateDetailedMessage` packet data.<br>
  * <br>
  * This is a shared partition of the data used to represent how the player's avatar is presented.
  * It is utilized by both `0x17 ObjectCreateMessage CharacterData` and `0x18 ObjectCreateDetailedMessage DetailedCharacterData`.
  * This can be considered the data that goes into creating the player's model.<br>
  * <br>
  * Only a few changes would occur depending on which packet would deal with the data.
  * One example is `facingYawUpper` which, when depicting avatars, can be set to represent non-trivial turning angles.
  * When depicting other players, it is limited to a small range of angles in the direction of that model's forward-facing.
  * Another example is the outfit information: not usually represented for avatars; but, always represented for other players.<br>
  * <br>
  * One way the player's model can be changed dramatically involves being depicted as "released."
  * In this form, their body appears as a backpack (or pumpkin or pastry) that can be looted for the equipment carried while alive.
  * Companion data will describe how the player is represented while he is "dead," usually a requirement for being "released."
  * Without that requirement here, it is possible to depicte the player as a "living backpack."
  * The said equipment is also defined elsewhere.
  * Another dramatic change replaces the player's model with a ball of plasma that masks the player while riding zip lines.<br>
  * <br>
  * Exploration:<br>
  * How do I crouch?
  * @see `CharacterData`
  * @see `DetailedCharacterData`
  * @param ribbons the four merit commendation ribbon medals
  */
final case class CharacterAppearanceData(a : CharacterAppearanceA,
                                         b : CharacterAppearanceB,
                                         ribbons : RibbonBars)
                                        (name_padding : Int) extends StreamBitSize {

  override def bitsize : Long = 128L + a.bitsize + b.bitsize

  /**
    * External access to the value padding on the name field.
    * The padding will always be a number 0-7.
    * @return the pad length in bits
    */
  def NamePadding : Int = name_padding

  /**
    * When a player is released-dead or attached to a zipline, their basic infantry model is replaced with a different one.
    * @return the length of the variable field that exists when using alternate models
    */
  def altModelBit : Option[Int] = CharacterAppearanceData.altModelBit(this)
}

object CharacterAppearanceData extends Marshallable[CharacterAppearanceData] {
  def apply(app : BasicCharacterData,
            black_ops : Boolean,
            jammered : Boolean,
            exosuit : ExoSuitType.Value,
            outfit_name : String,
            outfit_logo : Int,
            backpack : Boolean,
            facingPitch : Float,
            facingYawUpper : Float,
            lfs : Boolean,
            grenade_state : GrenadeState.Value,
            is_cloaking : Boolean,
            charging_pose : Boolean,
            on_zipline : Option[ZiplineData],
            ribbons : RibbonBars)(name_padding : Int) : CharacterAppearanceData = {
    val altModel : Boolean = backpack || on_zipline.isDefined
    val a = CharacterAppearanceA(
      app,
      black_ops,
      altModel,
      false,
      None,
      jammered,
      exosuit,
      None,
      0,
      0,
      0,
      0,
      0,
      0,
      0
    )(name_padding)
    val b = CharacterAppearanceB(
      outfit_name.length,
      outfit_name : String,
      outfit_logo : Int,
      false,
      backpack,
      false,
      false,
      false,
      facingPitch : Float,
      facingYawUpper : Float,
      lfs : Boolean,
      grenade_state : GrenadeState.Value,
      is_cloaking : Boolean,
      false,
      false,
      charging_pose : Boolean,
      false,
      on_zipline
    )(altModel, name_padding)
    new CharacterAppearanceData(
      a,
      b,
      ribbons
    )(name_padding)
  }

  def apply(a : Int=>CharacterAppearanceA, b : (Boolean,Int)=>CharacterAppearanceB, ribbons : RibbonBars)(name_padding : Int) : CharacterAppearanceData = {
    val first = a(name_padding)
    CharacterAppearanceData(a(name_padding), b(first.altModel, name_padding), ribbons)(name_padding)
  }

  /**
    * na
    * @param unk1 na
    * @param unk2 na
    */
  final case class ExtraData(unk1 : Boolean,
                             unk2 : Boolean) extends StreamBitSize {
    override def bitsize : Long = 2L
  }

  /**
    * na
    * @param unk1 na
    * @param unk2 na
    */
  final case class ZiplineData(unk1 : Long,
                               unk2 : Boolean) extends StreamBitSize {
    override def bitsize : Long = 33L
  }

  /**
    * When a player is released-dead or attached to a zipline, their basic infantry model is replaced with a different one.
    * In the former case, a backpack.
    * In the latter case, a ball of colored energy.
    * In this state, the length of the stream of data is modified.
    * @param app the appearance
    * @return the length of the variable field that exists when using alternate models
    */
  def altModelBit(app : CharacterAppearanceData) : Option[Int] = if(app.b.backpack || app.b.on_zipline.isDefined) {
      Some(1)
    }
    else {
      None
    }

  def namePadding(inheritPad : Int, pad : Option[ExtraData]) : Int = {
    pad match {
      case Some(n) =>
        val bitsize = n.bitsize.toInt % 8
        if(inheritPad > bitsize)
          inheritPad - bitsize
        else
          8 - bitsize
      case None =>
        inheritPad
    }
  }

  /**
    * Get the padding of the outfit's name.
    * The padding will always be a number 0-7.
    * @return the pad length in bits
    */
  def outfitNamePadding : Int = {
    6
  }

  private val extra_codec : Codec[ExtraData] = (
    ("unk1" | bool) ::
      ("unk2" | bool)
  ).as[ExtraData]

  private val zipline_codec : Codec[ZiplineData] = (
    ("unk1" | uint32L) ::
      ("unk2" | bool)
    ).as[ZiplineData]

  /**
    * na
    * @param name_padding na
    * @return na
    */
  def a_codec(name_padding : Int) : Codec[CharacterAppearanceA] = (
    ("faction" | PlanetSideEmpire.codec) ::
      ("black_ops" | bool) ::
      (("alt_model" | bool) >>:~ { alt_model => //modifies stream format (to display alternate player models)
        ("unk1" | bool) :: //serves a different internal purpose depending on the state of alt_model
          (conditional(false, "unk2" | extra_codec) >>:~ { extra => //TODO not sure what causes this branch
            ("jammered" | bool) ::
              optional(bool, "unk3" | uint16L) ::
              ("unk4" | uint16L) ::
              ("name" | PacketHelpers.encodedWideStringAligned(namePadding(name_padding, extra))) ::
              ("exosuit" | ExoSuitType.codec) ::
              ("unk5" | uint2) :: //unknown
              ("sex" | CharacterGender.codec) ::
              ("head" | uint8L) ::
              ("voice" | CharacterVoice.codec) ::
              ("unk6" | uint32L) ::
              ("unk7" | uint16L) ::
              ("unk8" | uint16L) ::
              ("unk9" | uint16L) ::
              ("unkA" | uint16L) //usually either 0 or 65535
          })
      })
    ).exmap[CharacterAppearanceA] (
    {
      case faction :: bops :: alt :: u1 :: u2 :: jamd :: u3 :: u4 :: name :: suit :: u5 :: sex :: head :: v1 :: u6 :: u7 :: u8 :: u9 :: uA :: HNil =>
        Attempt.successful(
          CharacterAppearanceA(BasicCharacterData(name, faction, sex, head, v1), bops, alt, u1, u2, jamd, suit, u3, u4, u5, u6, u7, u8, u9, uA)(name_padding)
        )

      case _ =>
        Attempt.Failure(Err("invalid character appearance data; can not encode"))
    },
    {
      case CharacterAppearanceA(BasicCharacterData(name, PlanetSideEmpire.NEUTRAL, _, _, _), _, _, _, _, _, _, _, _, _, _, _, _, _, _) =>
        Attempt.failure(Err(s"character $name's faction can not declare as neutral"))

      case CharacterAppearanceA(BasicCharacterData(name, faction, sex, head, v1), bops, alt, u1, u2, jamd, suit, u3, u4, u5, u6, u7, u8, u9, uA) =>
        Attempt.successful(
          faction :: bops :: alt :: u1 :: u2 :: jamd :: u3 :: u4 :: name :: suit :: u5 :: sex :: head :: v1 :: u6 :: u7 :: u8 :: u9 :: uA :: HNil
        )

      case _ =>
        Attempt.Failure(Err("invalid character appearance data; can not decode"))
    }
  )

  /**
    * na
    * @param alt_model na
    * @param name_padding na
    * @return na
    */
  def b_codec(alt_model : Boolean, name_padding : Int) : Codec[CharacterAppearanceB] = (
    ("unk0" | uint32L) :: //for outfit_name (below) to be visible in-game, this value should be non-zero
      ("outfit_name" | PacketHelpers.encodedWideStringAligned(outfitNamePadding)) ::
      ("outfit_logo" | uint8L) ::
      ("unk1" | bool) :: //unknown
      conditional(alt_model, "backpack" | bool) :: //alt_model flag adds this bit; see ps.c:line#1069587
      ("unk2" | bool) :: //requires alt_model flag (does NOT require health == 0)
      ("unk3" | bool) :: //stream misalignment when set
      ("unk4" | bool) :: //unknown
      ("facingPitch" | Angular.codec_pitch) ::
      ("facingYawUpper" | Angular.codec_yaw(0f)) ::
      ("lfs" | uint2) ::
      ("grenade_state" | GrenadeState.codec_2u) :: //note: bin10 and bin11 are neutral (bin00 is not defined)
      ("is_cloaking" | bool) ::
      ("unk5" | bool) :: //unknown
      ("unk6" | bool) :: //stream misalignment when set
      ("charging_pose" | bool) ::
      ("unk7" | bool) :: //alternate charging pose?
      optional(bool, "on_zipline" | zipline_codec)
    ).exmap[CharacterAppearanceB] (
    {
      case u0 :: outfit :: logo :: u1 :: bpack :: u2 :: u3 :: u4 :: facingPitch :: facingYawUpper :: lfs :: gstate :: cloaking :: u5 :: u6 :: charging :: u7 :: zipline :: HNil =>
        val lfsBool = if(lfs == 0) false else true
        val bpackBool = bpack match { case Some(_) => alt_model ; case None => false }
        Attempt.successful(
          CharacterAppearanceB(u0, outfit, logo, u1, bpackBool, u2, u3, u4, facingPitch, facingYawUpper, lfsBool, gstate, cloaking, u5, u6, charging, u7, zipline)(alt_model, name_padding)
        )
    },
    {
      case CharacterAppearanceB(u0, outfit, logo, u1, bpack, u2, u3, u4, facingPitch, facingYawUpper, lfs, gstate, cloaking, u5, u6, charging, u7, zipline) =>
        val u0Long = if(u0 == 0 && outfit.nonEmpty) {
          outfit.length.toLong
        }
        else {
          u0
        } //TODO this is a kludge; unk0 must be (some) non-zero if outfit_name is defined
        val (bpackOpt, zipOpt) = if(alt_model) {
          val bpackOpt = if(bpack) { Some(true) } else { None }
          (bpackOpt, zipline)
        }
        else {
          (None, None)
        } //alt_model must be set for either of the other two to be valid
        val lfsInt = if(lfs) { 1 } else { 0 }
        Attempt.successful(
          u0Long :: outfit :: logo :: u1 :: bpackOpt :: u2 :: u3 :: u4 :: facingPitch :: facingYawUpper :: lfsInt :: gstate :: cloaking :: u5 :: u6 :: charging :: u7 :: zipOpt :: HNil
        )
    }
  )

  def codec(name_padding : Int) : Codec[CharacterAppearanceData] = (
    ("a" | a_codec(name_padding)) >>:~ { a =>
      ("b" | b_codec(a.altModel, name_padding)) ::
        ("ribbons" | RibbonBars.codec)
    }
    ).xmap[CharacterAppearanceData] (
    {
      case a :: b :: ribbons :: HNil =>
        CharacterAppearanceData(a, b, ribbons)(name_padding)
    },
    {
      case CharacterAppearanceData(a, b, ribbons) =>
        a :: b :: ribbons :: HNil
    }
  )

  implicit val codec : Codec[CharacterAppearanceData] = codec(0)
}

// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.types._
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

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
  * @see `CharacterData`<br>
  *        `DetailedCharacterData`<br>
  *        `ExoSuitType`<br>
  *        `GrenadeState`<br>
  *        `RibbonBars`
  * @see `http://www.planetside-universe.com/p-outfit-decals-31.htm`
  * @param app the player's cardinal appearance settings
  * @param voice2 na;
  *               affects the frequency by which the character's voice is heard (somehow);
  *               commonly 3 for best results
  * @param black_ops whether or not this avatar is enrolled in Black OPs
  * @param jammered the player has been caught in an EMP blast recently;
  *                 creates a jammered sound effect that follows the player around and can be heard by others
  * @param exosuit the type of exo-suit the avatar will be depicted in;
  *                for Black OPs, the agile exo-suit and the reinforced exo-suit are replaced with the Black OPs exo-suits
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
  * @param ribbons the four merit commendation ribbon medals
  */
final case class CharacterAppearanceData(app : BasicCharacterData,
                                         voice2 : Int,
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
                                         on_zipline : Boolean,
                                         ribbons : RibbonBars)
                                        (name_padding : Int) extends StreamBitSize {

  override def bitsize : Long = {
    //factor guard bool values into the base size, not its corresponding optional field
    val nameStringSize : Long = StreamBitSize.stringBitSize(app.name, 16) + name_padding
    val outfitStringSize : Long = StreamBitSize.stringBitSize(outfit_name, 16) +
      CharacterAppearanceData.outfitNamePadding //even if the outfit_name is blank, string is always padded
    val altModelSize = CharacterAppearanceData.altModelBit(this).getOrElse(0)
    335L + nameStringSize + outfitStringSize + altModelSize //base value includes the ribbons
  }

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
  /**
    * When a player is released-dead or attached to a zipline, their basic infantry model is replaced with a different one.
    * In the former case, a backpack.
    * In the latter case, a ball of colored energy.
    * In this state, the length of the stream of data is modified.
    * @param app the appearance
    * @return the length of the variable field that exists when using alternate models
    */
  def altModelBit(app : CharacterAppearanceData) : Option[Int] = if(app.backpack || app.on_zipline) {
      Some(1)
    }
    else {
      None
    }

  /**
    * Get the padding of the outfit's name.
    * The padding will always be a number 0-7.
    * @return the pad length in bits
    */
  def outfitNamePadding : Int = {
    6
  }

  def codec(name_padding : Int) : Codec[CharacterAppearanceData] = (
    ("faction" | PlanetSideEmpire.codec) ::
      ("black_ops" | bool) ::
      (("alt_model" | bool) >>:~ { alt_model => //modifies stream format (to display alternate player models)
        ignore(1) :: //unknown
          ("jammered" | bool) ::
          bool :: //crashes client
          uint(16) :: //unknown, but usually 0
          ("name" | PacketHelpers.encodedWideStringAligned(name_padding)) ::
          ("exosuit" | ExoSuitType.codec) ::
          ignore(2) :: //unknown
          ("sex" | CharacterGender.codec) ::
          ("head" | uint8L) ::
          ("voice" | CharacterVoice.codec) ::
          ("voice2" | uint2L) ::
          ignore(78) :: //unknown
          uint16L :: //usually either 0 or 65535
          uint32L :: //for outfit_name (below) to be visible in-game, this value should be non-zero
          ("outfit_name" | PacketHelpers.encodedWideStringAligned( outfitNamePadding )) ::
          ("outfit_logo" | uint8L) ::
          ignore(1) :: //unknown
          ("backpack" | bool) :: //requires alt_model flag (does NOT require health == 0)
          bool :: //stream misalignment when set
          ("facingPitch" | Angular.codec_pitch) ::
          ("facingYawUpper" | Angular.codec_yaw(0f)) ::
          ignore(1) :: //unknown
          conditional(alt_model, bool) :: //alt_model flag adds a bit before lfs
          ignore(1) :: //an alternate lfs?
          ("lfs" | bool) ::
          ("grenade_state" | GrenadeState.codec_2u) :: //note: bin10 and bin11 are neutral (bin00 is not defined)
          ("is_cloaking" | bool) ::
          ignore(1) :: //unknown
          bool :: //stream misalignment when set
          ("charging_pose" | bool) ::
          ignore(1) :: //alternate charging pose?
          ("on_zipline" | bool) :: //requires alt_model flag
          ("ribbons" | RibbonBars.codec)
      })
    ).exmap[CharacterAppearanceData] (
    {
      case _ :: _ :: false :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: true :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: HNil |
           _ :: _ :: false :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: true :: _ :: HNil =>
        Attempt.Failure(Err("invalid character appearance data; can not encode alternate model without required bit set"))

      case faction :: bops :: _ :: _ :: jamd :: false :: 0 :: name :: suit :: _ :: sex :: head :: v1 :: v2 :: _ :: _ :: _/*has_outfit_name*/ :: outfit :: logo :: _ :: bpack :: false :: facingPitch :: facingYawUpper :: _ :: _ :: _ :: lfs :: gstate :: cloaking :: _ :: false :: charging :: _ :: zipline :: ribbons :: HNil =>
        Attempt.successful(
          CharacterAppearanceData(BasicCharacterData(name, faction, sex, head, v1), v2, bops, jamd, suit, outfit, logo, bpack, facingPitch, facingYawUpper, lfs, gstate, cloaking, charging, zipline, ribbons)(name_padding)
        )

      case _ =>
        Attempt.Failure(Err("invalid character appearance data; can not encode"))
    },
    {
      case CharacterAppearanceData(BasicCharacterData(name, PlanetSideEmpire.NEUTRAL, _, _, _), _, _, _, _, _, _, _, _, _, _, _, _, _, _, _) =>
        Attempt.failure(Err(s"character $name's faction can not declare as neutral"))

      case CharacterAppearanceData(BasicCharacterData(name, faction, sex, head, v1), v2, bops, jamd, suit, outfit, logo, bpack, facingPitch, facingYawUpper, lfs, gstate, cloaking, charging, zipline, ribbons) =>
        val has_outfit_name : Long = outfit.length.toLong //TODO this is a kludge
        var alt_model : Boolean = false
        var alt_model_extrabit : Option[Boolean] = None
        if(zipline || bpack) {
          alt_model = true
          alt_model_extrabit = Some(false)
        }
        Attempt.successful(
          faction :: bops :: alt_model :: () :: jamd :: false :: 0 :: name :: suit :: () :: sex :: head :: v1 :: v2 :: () :: 0 :: has_outfit_name :: outfit :: logo :: () :: bpack :: false :: facingPitch :: facingYawUpper :: () :: alt_model_extrabit :: () :: lfs :: gstate :: cloaking :: () :: false :: charging :: () :: zipline :: ribbons :: HNil
        )

      case _ =>
        Attempt.Failure(Err("invalid character appearance data; can not decode"))
    }
  )

  implicit val codec : Codec[CharacterAppearanceData] = codec(0)
}

// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * Values for the implant effects on a character model.
  * The effects can not be activated simultaneously.
  * In at least one case, attempting to activate multiple effects will cause the PlanetSide client to crash.<br>
  * <br>
  * `RegenEffects` is a reverse-flagged item - inactive when the corresponding bit is set.
  * For that reason, every other effect is `n`+1, while `NoEffects` is 1 and `RegenEffects` is 0.
  */
object ImplantEffects extends Enumeration {
  type Type = Value

  val SurgeEffects = Value(9)
  val PersonalShieldEffects = Value(5)
  val DarklightEffects = Value(3)
  val RegenEffects = Value(0)
  val NoEffects = Value(1)

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}

/**
  * Values for the four different color designs that impact a player's uniform.
  * Exo-suits get minor graphical updates at the following battle rank levels: seven, fourteen, and twenty-five.
  */
object UniformStyle extends Enumeration {
  type Type = Value

  val Normal = Value(0)
  val FirstUpgrade = Value(1)
  val SecondUpgrade = Value(2)
  val ThirdUpgrade = Value(4)

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uintL(3))
}

/**
  * A representation of a portion of an avatar's `ObjectCreateDetailedMessage` packet data.<br>
  * <br>
  * This densely-packed information outlines most of the specifics required to depict some other player's character.
  * Someone else decides how that character is behaving and the server tells each client how to depict that behavior.
  * For that reason, the character is mostly for presentation purposes, rather than really being fleshed-out.
  * Of the inventory for this character, only the initial five weapon slots are defined.<br>
  * <br>
  * In the "backend of the client," the character produced by this data is no different
  * from the kind of character that could be declared a given player's avatar.
  * In terms of equipment and complicated features common to an avatar character, however,
  * any user would find this character ill-equipped.
  * @param health the amount of health the player has, as a percentage of a filled bar;
  *               the bar has 85 states, with 3 points for each state;
  *               when 0% (less than 3 of 255), the player will collapse into a death pose on the ground
  * @param armor the amount of armor the player has, as a percentage of a filled bar;
  *              the bar has 85 states, with 3 points for each state
  * @param uniform_upgrade the level of upgrade to apply to the player's base uniform
  * @param command_rank the player's command rank as a number from 0 to 5;
  *                     cosmetic armor associated with the command rank will be applied automatically
  * @param implant_effects the effects of implants that can be seen on a player's character;
  *                        though many implants can be used simultaneously, only one implant effect can be applied here
  * @param cosmetics optional decorative features that are added to the player's head model by console/chat commands;
  *                  they become available at battle rank 24, but here they require the third uniform upgrade (rank 25);
  *                  these flags do not exist if they are not applicable
  * @see `DetailedCharacterData`
  */
final case class CharacterData(health : Int,
                               armor : Int,
                               uniform_upgrade : UniformStyle.Value,
                               unk : Int,
                               command_rank : Int,
                               implant_effects : Option[ImplantEffects.Value],
                               cosmetics : Option[Cosmetics])
                              (is_backpack : Boolean) extends ConstructorData {

  override def bitsize : Long = {
    //factor guard bool values into the base size, not its corresponding optional field
    val effectsSize : Long = if(implant_effects.isDefined) { 4L } else { 0L }
    val cosmeticsSize : Long = if(cosmetics.isDefined) { cosmetics.get.bitsize } else { 0L }
    27L + effectsSize + cosmeticsSize
  }
}

object CharacterData extends Marshallable[CharacterData] {
  /**
    * An overloaded constructor for `CharacterData` that allows for a not-optional inventory.
    * @param health the amount of health the player has, as a percentage of a filled bar
    * @param armor the amount of armor the player has, as a percentage of a filled bar
    * @param uniform the level of upgrade to apply to the player's base uniform
    * @param cr the player's command rank as a number from 0 to 5
    * @param implant_effects the effects of implants that can be seen on a player's character
    * @param cosmetics optional decorative features that are added to the player's head model by console/chat commands
    * //@param inv the avatar's inventory
    * //@param drawn_slot the holster that is initially drawn
    * @return a `CharacterData` object
    */
  def apply(health : Int, armor : Int, uniform : UniformStyle.Value, cr : Int, implant_effects : Option[ImplantEffects.Value], cosmetics : Option[Cosmetics]) : (Boolean)=>CharacterData =
    CharacterData(health, armor, uniform, 0, cr, implant_effects, cosmetics)

  def codec(is_backpack : Boolean) : Codec[CharacterData] = (
    ("health" | uint8L) :: //dead state when health == 0
      ("armor" | uint8L) ::
      (("uniform_upgrade" | UniformStyle.codec) >>:~ { style =>
        ignore(3) :: //unknown
          ("command_rank" | uintL(3)) ::
          bool :: //stream misalignment when != 1
          optional(bool, "implant_effects" | ImplantEffects.codec) ::
          conditional(style == UniformStyle.ThirdUpgrade, "cosmetics" | Cosmetics.codec)
      })
    ).exmap[CharacterData] (
    {
      case health :: armor :: uniform :: _ :: cr :: false :: implant_effects :: cosmetics :: HNil =>
        val newHealth = if(is_backpack) { 0 } else { health }
        Attempt.Successful(new CharacterData(newHealth, armor, uniform, 0, cr, implant_effects, cosmetics)(is_backpack))

      case _ =>
        Attempt.Failure(Err("invalid character data; can not encode"))
    },
    {
      case CharacterData(health, armor, uniform, _, cr, implant_effects, cosmetics) =>
        val newHealth = if(is_backpack) { 0 } else { health }
        Attempt.Successful(newHealth :: armor :: uniform :: () :: cr :: false :: implant_effects :: cosmetics :: HNil)

      case _ =>
        Attempt.Failure(Err("invalid character data; can not decode"))
    }
  )

  implicit val codec : Codec[CharacterData] = codec(false)
}

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
  * The different cosmetics that a player can apply to their model's head.<br>
  * <br>
  * The player gets the ability to apply these minor modifications at battle rank twenty-four, just one rank before the third uniform upgrade.
  * @param no_helmet removes the current helmet on the reinforced exo-suit and the agile exo-suit;
  *                  all other cosmetics require `no_helmet` to be `true` before they can be seen
  * @param beret player dons a beret
  * @param sunglasses player dons sunglasses
  * @param earpiece player dons an earpiece on the left
  * @param brimmed_cap player dons a cap;
  *                    the cap overrides the beret, if both are selected
  */
final case class Cosmetics(no_helmet : Boolean,
                           beret : Boolean,
                           sunglasses : Boolean,
                           earpiece : Boolean,
                           brimmed_cap : Boolean)

/**
  * A part of a representation of the avatar portion of `ObjectCreateMessage` packet data.
  * This densely-packed information outlines most of the specifics of depicting some other character.<br>
  * <br>
  * The character created by this data is treated like an NPC from the perspective of the server.
  * Someone else decides how that character is behaving and the server tells each client how to depict that behavior.
  * For that reason, the character is mostly for presentation purposes, rather than really being fleshed-out.
  * (As far as the client is concerned, nothing stops this character from being declared an "avatar."
  * A player would find such a client-controlled character lacking many important details and have poor equipment.
  * They would also be competing with some other player for input control, if they could control the character at all.)<br>
  * <br>
  * Divisions exist to make the data more manageable.
  * The first division of data only manages the general appearance of the player's in-game model.
  * The second division (currently, the fields actually in this class) manages the status of the character.
  * In general, it passes more simplified data about the character, the minimum that is necessary to explain status to some other player.
  * For example, health and armor are percentages, and are depicted as bars over the player's head near the nameplate.
  * The third is the inventory (composed of normal-type objects).
  * Rather than equipment other players would never interact with, it only comprises the contents of the five holster slots.<br>
  * <br>
  * If this player is spawned as dead - with their `health` at 0% - he will start standing and then immediately fall into a lying pose.
  * The death pose selected is randomized, can not be influenced, and is not be shared across clients.
  * @param appearance the player's cardinal appearance settings
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
  * @param inventory the avatar's inventory;
  *                  typically, only the tools and weapons in the equipment holster slots
  * @param drawn_slot the holster that is initially drawn;
  *                   defaults to `DrawnSlot.None`
  * @see `CharacterAppearanceData`
  * @see `DetailedCharacterData`
  * @see `InventoryData`
  * @see `DrawnSlot`
  */
final case class CharacterData(appearance : CharacterAppearanceData,
                               health : Int,
                               armor : Int,
                               uniform_upgrade : UniformStyle.Value,
                               command_rank : Int,
                               implant_effects : Option[ImplantEffects.Value],
                               cosmetics : Option[Cosmetics],
                               inventory : Option[InventoryData],
                               drawn_slot : DrawnSlot.Value = DrawnSlot.None
                              ) extends ConstructorData {

  override def bitsize : Long = {
    //factor guard bool values into the base size, not its corresponding optional field
    val appearanceSize : Long = appearance.bitsize
    val effectsSize : Long = if(implant_effects.isDefined) { 4L } else { 0L }
    val cosmeticsSize : Long = if(cosmetics.isDefined) { 5L } else { 0L }
    val inventorySize : Long = if(inventory.isDefined) { inventory.get.bitsize } else { 0L }
    32L + appearanceSize + effectsSize + cosmeticsSize + inventorySize
  }
}

object CharacterData extends Marshallable[CharacterData] {
  /**
    * An overloaded constructor for `CharacterData` that allows for a not-optional inventory.
    * @param appearance the player's cardinal appearance settings
    * @param health the amount of health the player has, as a percentage of a filled bar
    * @param armor the amount of armor the player has, as a percentage of a filled bar
    * @param uniform the level of upgrade to apply to the player's base uniform
    * @param cr the player's command rank as a number from 0 to 5
    * @param implant_effects the effects of implants that can be seen on a player's character
    * @param cosmetics optional decorative features that are added to the player's head model by console/chat commands
    * @param inv the avatar's inventory
    * @param drawn_slot the holster that is initially drawn
    * @return a `CharacterData` object
    */
  def apply(appearance : CharacterAppearanceData, health : Int, armor : Int, uniform : UniformStyle.Value, cr : Int, implant_effects : Option[ImplantEffects.Value], cosmetics : Option[Cosmetics], inv : InventoryData, drawn_slot : DrawnSlot.Value) : CharacterData =
    new CharacterData(appearance, health, armor, uniform, cr, implant_effects, cosmetics, Some(inv), drawn_slot)

  /**
    * Check for the bit flags for the cosmetic items.
    * These flags are only valid if the player has acquired their third uniform upgrade.
    * @see `UniformStyle.ThirdUpgrade`
    */
  private val cosmeticsCodec : Codec[Cosmetics] = (
    ("no_helmet" | bool) ::
      ("beret" | bool) ::
      ("sunglasses" | bool) ::
      ("earpiece" | bool) ::
      ("brimmed_cap" | bool)
  ).as[Cosmetics]

  implicit val codec : Codec[CharacterData] = (
    ("app" | CharacterAppearanceData.codec) ::
      ("health" | uint8L) :: //dead state when health == 0
      ("armor" | uint8L) ::
      (("uniform_upgrade" | UniformStyle.codec) >>:~ { style =>
        ignore(3) :: //unknown
          ("command_rank" | uintL(3)) ::
          bool :: //stream misalignment when != 1
          optional(bool, "implant_effects" | ImplantEffects.codec) ::
          conditional(style == UniformStyle.ThirdUpgrade, "cosmetics" | cosmeticsCodec) ::
          optional(bool, "inventory" | InventoryData.codec) ::
          ("drawn_slot" | DrawnSlot.codec) ::
          bool //usually false
      })
  ).exmap[CharacterData] (
    {
      case app :: health :: armor :: uniform :: _ :: cr :: false :: implant_effects :: cosmetics :: inv :: drawn_slot :: false :: HNil =>
        var newHealth = health
        if(app.backpack) {
          newHealth = 0
        }
        Attempt.Successful(CharacterData(app, newHealth, armor, uniform, cr, implant_effects, cosmetics, inv, drawn_slot))

      case _ =>
        Attempt.Failure(Err("invalid character data; can not encode"))
    },
    {
      case CharacterData(app, health, armor, uniform, cr, implant_effects, cosmetics, inv, drawn_slot) =>
        var newHealth = health
        if(app.backpack) {
          newHealth = 0
        }
        Attempt.Successful(app :: newHealth :: armor :: uniform :: () :: cr :: false :: implant_effects :: cosmetics :: inv :: drawn_slot :: false :: HNil)

      case _ =>
        Attempt.Failure(Err("invalid character data; can not decode"))
    }
  )
}

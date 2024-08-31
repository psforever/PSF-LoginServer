// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.objects.avatar.BattleRank
import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.types.{Cosmetic, UniformStyle}
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * Values for the implant effects on a character model.
  * The effects are not additive and this value is not a bitmask.<br>
  * <br>
  * `RegenEffects` is a reverse-flagged item - inactive when the corresponding bit is set.
  * For that reason, every other effect is `n + 1`, while `NoEffects` is `1` and `RegenEffects` is `0`.
  */
object ImplantEffects extends Enumeration {
  type Type = Value

  val SurgeEffects: ImplantEffects.Value          = Value(9)
  val PersonalShieldEffects: ImplantEffects.Value = Value(5)
  val DarklightEffects: ImplantEffects.Value      = Value(3)
  val RegenEffects: ImplantEffects.Value          = Value(0)
  val NoEffects: ImplantEffects.Value             = Value(1)

  implicit val codec: Codec[ImplantEffects.Value] = PacketHelpers.createEnumerationCodec(this, uint4)
}

/**
  * A representation of a portion of an avatar's `ObjectCreateDetailedMessage` packet data.<br>
  * <br>
  * This information outlines most of the specifics required to depict some other player's character.
  * Someone else decides how that character is behaving and the server tells each client how to depict that behavior.
  * For that reason, the character is mostly for presentation purposes, rather than really being fleshed-out.
  * Of the inventory for this character, only the initial five weapon slots are defined.
  * In the "backend of the client," the character produced by this data is no different
  * from the kind of character that could be declared a given player's avatar.
  * In terms of equipment and complicated features common to an avatar character, however,
  * any user would find this character ill-equipped.
  * @see `DetailedCharacterData`
  * @see `Cosmetics`
  * @param health the amount of health the player has, as a percentage of a filled bar;
  *               the bar has 85 states, with 3 points for each state;
  *               when 0% (less than 3 of 255), the player will collapse into a death pose on the ground;
  *               while `is_backpack == true`, `health` will always report as 0;
  *               while `is_seated == true`, `health` will (try to) report as 100
  * @param armor the amount of armor the player has, as a percentage of a filled bar;
  *              the bar has 85 states, with 3 points for each state;
  *              while `is_seated == true`, `armor` will always report as 0
  * @param uniform_upgrade the level of upgrade to apply to the player's base uniform
  * @param command_rank the player's command rank as a number from 0 to 5;
  *                     cosmetic armor associated with the command rank will be applied automatically
  * @param implant_effects the effects of implants that can be seen on a player's character;
  *                        the number of entries equates to the number of effects applied;
  *                        the maximum number of effects is three
  * @param cosmetics optional decorative features that are added to the player's head model by console/chat commands;
  *                  they become available at battle rank 24;
  *                  these flags do not exist if they are not applicable
  * @param is_backpack this player character should be depicted as a corpse;
  *                    corpses are either coffins (defunct), backpacks (normal), or a pastry (festive);
  *                    the alternate model bit should be flipped
  * @param is_seated this player character is seated in a vehicle or mounted to some other object;
  *                  alternate format for data parsing applies
  */
final case class CharacterData(
    health: Int,
    armor: Int,
    uniform_upgrade: UniformStyle,
    unk: Int,
    command_rank: Int,
    implant_effects: List[ImplantEffects.Value],
    cosmetics: Option[Set[Cosmetic]]
)(
    is_backpack: Boolean,
    is_seated: Boolean
) extends ConstructorData {

  override def bitsize: Long = {
    val seatedSize = if (is_seated) {
      0
    } else {
      16
    }
    val effectsSize: Long = implant_effects.length * 4L
    val cosmeticsSize: Long = if (cosmetics.isDefined) {
      5L
    } else {
      0L
    }
    11L + seatedSize + effectsSize + cosmeticsSize
  }
}

object CharacterData extends Marshallable[CharacterData] {

  /**
    * An overloaded constructor for `CharacterData`.
    * @param health the amount of health the player has, as a percentage of a filled bar
    * @param armor the amount of armor the player has, as a percentage of a filled bar
    * @param uniform the level of upgrade to apply to the player's base uniform
    * @param cr the player's command rank as a number from 0 to 5
    * @param implant_effects the effects of implants that can be seen on a player's character
    * @param cosmetics optional decorative features that are added to the player's head model by console/chat commands
    * @return a `CharacterData` object
    */
  def apply(
      health: Int,
      armor: Int,
      uniform: UniformStyle,
      cr: Int,
      implant_effects: List[ImplantEffects.Value],
      cosmetics: Option[Set[Cosmetic]]
  ): (Boolean, Boolean) => CharacterData =
    CharacterData(health, armor, uniform, 0, cr, implant_effects, cosmetics)

  def codec(is_backpack: Boolean): Codec[CharacterData] =
    (
      ("health" | uint8L) :: //dead state when health == 0
        ("armor" | uint8L) ::
        (("uniform_upgrade" | UniformStyle.codec) >>:~ { style =>
        uint(bits = 3) :: //uniform_upgrade is actually interpreted as a 6u field, but the lower 3u seems to be discarded
        ("command_rank" | uint(bits = 3)) ::
        ("implant_effects" | listOfN(uint2, ImplantEffects.codec)) ::
        ("cosmetics" | conditional(BattleRank.showCosmetics(style), Cosmetic.codec))
      })
    ).exmap[CharacterData](
      {
        case health :: armor :: uniform :: unk :: cr :: implant_effects :: cosmetics :: HNil =>
          val newHealth = if (is_backpack) { 0 }
          else { health }
          Attempt.Successful(
            CharacterData(
              newHealth,
              armor,
              uniform,
              unk,
              cr,
              implant_effects,
              cosmetics
            )(is_backpack, is_seated = false)
          )

        case _ =>
          Attempt.Failure(Err("invalid character data; can not encode"))
      },
      {
        case CharacterData(health, armor, uniform, unk, cr, implant_effects, cosmetics) =>
          val newHealth = if (is_backpack) { 0 }
          else { health }
          Attempt.Successful(newHealth :: armor :: uniform :: unk :: cr :: implant_effects :: cosmetics :: HNil)

        case _ =>
          Attempt.Failure(Err("invalid character data; can not decode"))
      }
    )

  def codec_seated(is_backpack: Boolean): Codec[CharacterData] =
    (
      ("uniform_upgrade" | UniformStyle.codec) >>:~ { style =>
        uint(bits = 3) :: //uniform_upgrade is actually interpreted as a 6u field, but the lower 3u seems to be discarded
        ("command_rank" | uint(bits = 3)) ::
        ("implant_effects" | listOfN(uint2, ImplantEffects.codec)) ::
        ("cosmetics" | conditional(BattleRank.showCosmetics(style), Cosmetic.codec))
      }
    ).exmap[CharacterData](
      {
        case uniform :: unk :: cr :: implant_effects :: cosmetics :: HNil =>
          Attempt.Successful(
            new CharacterData(
              health = 100,
              armor = 0,
              uniform,
              unk,
              cr,
              implant_effects,
              cosmetics
            )(is_backpack, is_seated = true)
          )

        case _ =>
          Attempt.Failure(Err("invalid character data; can not encode"))
      },
      {
        case CharacterData(_, _, uniform, unk, cr, implant_effects, cosmetics) =>
          val cos = if (BattleRank.showCosmetics(uniform)) {
            cosmetics.orElse(Some(Set[Cosmetic]()))
          } else {
            None
          }
          Attempt.Successful(uniform :: unk :: cr :: implant_effects :: cos :: HNil)

        case _ =>
          Attempt.Failure(Err("invalid character data; can not decode"))
      }
    )

  implicit val codec: Codec[CharacterData] = codec(is_backpack = false)
}

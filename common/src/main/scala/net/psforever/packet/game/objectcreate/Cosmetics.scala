// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import scodec.codecs._
import scodec.Codec

/**
  * Values for the different specific customizations available as cosmetics.<br>
  * `NoHelmet` removes the current helmet on the reinforced exo-suit and the agile exo-suit;
  *            other cosmetics require `no_helmet` to be `true` before they can be seen;
  *            `NoHelmet` does not override `Beret` or `BrimmedCap`<br>
  * `Beret` player dons a beret<br>
  * `Sunglasses` player dons sunglasses<br>
  * `Earpiece` player dons an earpiece on the left<br>
  * `BrimmedCap` player dons a cap;
  *              the cap overrides the beret, if both are selected
  */
object PersonalStyle extends Enumeration {
  val BrimmedCap = Value(1)
  val Earpiece = Value(2)
  val Sunglasses = Value(4)
  val Beret = Value(8)
  val NoHelmet = Value(16)
}

/**
  * The different cosmetics that a player can apply to their character model's head.<br>
  * <br>
  * The player gets the ability to apply these minor modifications at battle rank twenty-four, just one rank before the third uniform upgrade.
  * These flags are only valid if the player has:
  * for `DetailedCharacterData`, achieved at least battle rank twenty-four (battle experience points greater than 2286230),
  * or, for `CharacterData`, achieved at least battle rank twenty-five (acquired their third uniform upgrade).
  * `CharacterData`, as suggested, will not display these options until one battle rank after they would have become available.
  * @param pstyles a value that indicates certain cosmetic features by bitwise math
  * @see `UniformStyle`
  * @see `PersonalStyleFeatures`
  */
final case class Cosmetics(pstyles : Int) extends StreamBitSize {
  override def bitsize : Long = 5L

  /**
    * Transform the accumulated bitwise cosmetic feature integer into a group of all valid cosmetic feature values.
    * @return a group of all valid cosmetic feature values
    */
  def Styles : Set[PersonalStyle.Value] = {
    (for {
      style <- PersonalStyle.values.toList
      if (pstyles & style.id) == style.id
    } yield style) toSet
  }

  /**
    * Allocate a cosmetic feature to an existing group of cosmetic feature values if that feature is not already a member.<br>
    * `Cosmetics` is an immutable object so a new object with the additional value must be created.
    * @param pstyle the cosmetic feature value
    * @return a new `Cosmetics` object, potentially including the new cosmetic feature
    */
  def +(pstyle : PersonalStyle.Value) : Cosmetics = {
    Cosmetics(pstyles | pstyle.id)
  }

  /**
    * Revoke a cosmetic feature from an existing group of cosmetic feature values if that feature is a member.<br>
    * * `Cosmetics` is an immutable object so a new object with the value removed must be created.
    * @param pstyle the cosmetic feature value
    * @return a new `Cosmetics` object, excluding the new cosmetic feature
    */
  def -(pstyle : PersonalStyle.Value) : Cosmetics = {
    Cosmetics(pstyles - (pstyles & pstyle.id))
  }

  /**
    * Determine if this `Cosmetics` object contain the given cosmetic feature.
    * @param pstyle the cosmetic feature value
    * @return `true`, if the feature is included; `false`, otherwise
    */
  def contains(pstyle : PersonalStyle.Value) : Boolean = (pstyles & pstyle.id) == pstyle.id
}

object Cosmetics {
  /**
    * Overloaded constructor for `Cosmetics` that loads no option.
    * @return a `Cosmetics` object
    */
  def apply() : Cosmetics = Cosmetics(0)

  /**
    * Overloaded constructor for `Cosmetics` that loads a single option.
    * @param pstyle the cosmetic feature that will be valid
    * @return a `Cosmetics` object
    */
  def apply(pstyle : PersonalStyle.Value) : Cosmetics = Cosmetics(pstyle.id)

  /**
    * Overloaded constructor for `Cosmetics` that loads all options listed.
    * @param pstyle all of the cosmetic feature that will be valid
    * @return a `Cosmetics` object
    */
  def apply(pstyle : Set[PersonalStyle.Value]) : Cosmetics = {
    Cosmetics(pstyle.foldLeft(0)(_ + _.id))
  }

  /**
    * Overloaded constructor for `Cosmetics` that list all options as boolean values
    * @param no_helmet removes the current helmet on the reinforced exo-suit and the agile exo-suit
    * @param beret player dons a beret
    * @param sunglasses player dons sunglasses
    * @param earpiece player dons an earpiece on the left
    * @param brimmed_cap player dons a cap
    * @return a `Cosmetics` object
    */
  def apply(no_helmet : Boolean,
            beret : Boolean,
            sunglasses : Boolean,
            earpiece : Boolean,
            brimmed_cap : Boolean) : Cosmetics = {
    implicit def bool2int(b : Boolean) : Int = if(b) 1 else 0
    Cosmetics(
      (no_helmet * 16) +
        (beret * 8) +
        (sunglasses * 4) +
        (earpiece * 2) +
        brimmed_cap
    )
  }

  implicit val codec : Codec[Cosmetics] = uint(5).hlist.as[Cosmetics]
}

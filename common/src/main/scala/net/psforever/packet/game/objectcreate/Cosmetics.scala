// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import scodec.codecs._
import scodec.Codec

/**
  * The different cosmetics that a player can apply to their character model's head.<br>
  * <br>
  * The player gets the ability to apply these minor modifications at battle rank twenty-four, just one rank before the third uniform upgrade.
  * These flags are only valid if the player has:
  * for `DetailedCharacterData`, achieved at least battle rank twenty-four (battle experience points greater than 2286230),
  * or, for `CharacterData`, achieved at least battle rank twenty-five (acquired their third uniform upgrade).
  * `CharacterData`, as implied, will not display these options until one battle rank after they would have become available.
  * @param no_helmet removes the current helmet on the reinforced exo-suit and the agile exo-suit;
  *                  all other cosmetics require `no_helmet` to be `true` before they can be seen
  * @param beret player dons a beret
  * @param sunglasses player dons sunglasses
  * @param earpiece player dons an earpiece on the left
  * @param brimmed_cap player dons a cap;
  *                    the cap overrides the beret, if both are selected
  * @see `UniformStyle.ThirdUpgrade`
  */
final case class Cosmetics(no_helmet : Boolean,
                           beret : Boolean,
                           sunglasses : Boolean,
                           earpiece : Boolean,
                           brimmed_cap : Boolean
                          ) extends StreamBitSize {
  override def bitsize : Long = 5L
}

object Cosmetics {
  implicit val codec : Codec[Cosmetics] = (
    ("no_helmet" | bool) ::
      ("beret" | bool) ::
      ("sunglasses" | bool) ::
      ("earpiece" | bool) ::
      ("brimmed_cap" | bool)
    ).as[Cosmetics]
}

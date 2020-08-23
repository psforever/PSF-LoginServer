// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.types._

/**
  * A part of a representation of the avatar portion of `ObjectCreateMessage` packet data.<br>
  * <br>
  * This partition of the data stream contains information used to represent how the player's avatar is presented.
  * This appearance coincides with the data available from the `CharacterCreateRequestMessage` packet.
  * @see `PlanetSideEmpire`<br>
  *        `CharacterGender`
  * @param name the unique name of the avatar;
  *             minimum of two characters
  * @param faction the empire to which the avatar belongs
  * @param sex whether the avatar is `Male` or `Female`
  * @param head the avatar's face and hair;
  *             by row and column on the character creation screen, the high nibble is the row and the low nibble is the column
  * @param voice the avatar's voice selection
  */
final case class BasicCharacterData(
    name: String,
    faction: PlanetSideEmpire.Value,
    sex: CharacterGender.Value,
    head: Int,
    voice: CharacterVoice.Value
)

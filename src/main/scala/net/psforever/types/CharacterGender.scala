// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs.uint2L

/**
  * Values for two genders, Male and Female, starting at 1 = Male.
  */
object CharacterGender extends Enumeration(1) {
  type Type = Value

  val Male, Female = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint2L)
}

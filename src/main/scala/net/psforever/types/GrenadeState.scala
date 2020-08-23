// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

/**
  * An `Enumeration` of the kinds of states applicable to the grenade animation.
  */
object GrenadeState extends Enumeration(1) {
  type Type = Value

  val Primed, //avatars and other depicted player characters
  Thrown,     //avatars only
  None        //non-actionable state of rest
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint8L)

  val codec_2u = PacketHelpers.createEnumerationCodec(this, uint2L)
}

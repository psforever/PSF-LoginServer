// Copyright (c) 2020 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

/**
  * An `Enumeration` `Codec` that represents that various states of a major facility's Generator.
  */
object PlanetSideGeneratorState extends Enumeration {
  type Type = Value
  val Normal, Critical, Destroyed, Unk3 = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uintL(2))
}

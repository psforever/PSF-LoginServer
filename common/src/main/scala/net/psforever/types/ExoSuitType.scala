// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

/**
  * Values for the the different types of exo-suits that players can wear.
  */
object ExoSuitType extends Enumeration {
  type Type = Value
  val Agile,
      Reinforced,
      MAX,
      Infiltration,
      Standard= Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint(3))
}

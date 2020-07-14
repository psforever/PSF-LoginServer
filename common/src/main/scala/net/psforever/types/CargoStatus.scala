// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

object CargoStatus extends Enumeration {
  type Type = Value

  val Empty      = Value(0)
  val InProgress = Value(1)
  val UNK1       = Value(2) // to have Xtoolspar working
  val Occupied   = Value(3)

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}

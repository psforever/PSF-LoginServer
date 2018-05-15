// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs.uint2L

object LoadoutType extends Enumeration {
  type Type = Value

  val
  Infantry,
  Vehicle
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint2L)
}

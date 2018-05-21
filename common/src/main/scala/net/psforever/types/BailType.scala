// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

object BailType extends Enumeration {
  type Type = Value

  val Normal = Value(0)
  val Kicked = Value(4) // User was kicked out by vehicle owner or locked from vehicle
  val Bailed = Value(8) // User bailed out

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}

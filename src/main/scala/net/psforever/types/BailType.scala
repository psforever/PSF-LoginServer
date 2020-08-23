// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

object BailType extends Enumeration {
  type Type = Value

  val Normal = Value(0)
  val Unk1   = Value(1) // to have Xtoolspar working
  val Unk2   = Value(2) // to have Xtoolspar working
  val Unk3   = Value(3) // to have Xtoolspar working
  val Kicked = Value(4) // User was kicked out by vehicle owner or locked from vehicle
  val Unk5   = Value(5) // to have Xtoolspar working
  val Unk6   = Value(6) // to have Xtoolspar working
  val Unk7   = Value(7) // to have Xtoolspar working
  val Bailed = Value(8) // User bailed out

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}

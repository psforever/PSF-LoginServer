// Copyright (c) 2019 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

object SquadWaypoints extends Enumeration {
  type Type = Value
  val One, Two, Three, Four, ExperienceRally = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint8L)
}

// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs.uint2L

/**
  * Values for the three empires and the neutral/Black Ops group.
  */
object PlanetSideEmpire extends Enumeration {
  type Type = Value
  val TR, NC, VS, NEUTRAL = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint2L)

  def apply(id: String): PlanetSideEmpire.Value = {
    values.find(_.toString.equalsIgnoreCase(id)) match {
      case Some(faction) =>
        faction
      case None =>
        throw new NoSuchElementException(s"can not find an empire associated with $id")
    }
  }
}

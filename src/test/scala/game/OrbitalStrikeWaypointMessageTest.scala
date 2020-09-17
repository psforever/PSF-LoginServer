// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class OrbitalStrikeWaypointMessageTest extends Specification {
  val string_on  = hex"B9 46 0C AA E3 D2 2A 92 00"
  val string_off = hex"B9 46 0C 00"

  "decode (on)" in {
    PacketCoding.decodePacket(string_on).require match {
      case OrbitalStrikeWaypointMessage(guid, coords) =>
        guid mustEqual PlanetSideGUID(3142)
        coords.isDefined mustEqual true
        coords.get.x mustEqual 5518.664f
        coords.get.y mustEqual 2212.539f
      case _ =>
        ko
    }
  }

  "decode (off)" in {
    PacketCoding.decodePacket(string_off).require match {
      case OrbitalStrikeWaypointMessage(guid, coords) =>
        guid mustEqual PlanetSideGUID(3142)
        coords.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "encode (on)" in {
    val msg = OrbitalStrikeWaypointMessage(PlanetSideGUID(3142), 5518.664f, 2212.539f)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_on
  }

  "encode (off)" in {
    val msg = OrbitalStrikeWaypointMessage(PlanetSideGUID(3142))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_off
  }
}

// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{OxygenState, PlanetSideGUID}
import scodec.bits._

class OxygenStateMessageTest extends Specification {
  val string_self    = hex"78 4b00f430"
  val string_vehicle = hex"78 4b00f4385037a180"

  "decode (self)" in {
    PacketCoding.decodePacket(string_self).require match {
      case OxygenStateMessage(player, vehicle) =>
        player.guid mustEqual PlanetSideGUID(75)
        player.progress mustEqual 50.0
        player.condition mustEqual OxygenState.Suffocation

        vehicle.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (vehicle)" in {
    PacketCoding.decodePacket(string_vehicle).require match {
      case OxygenStateMessage(player, vehicle) =>
        player.guid mustEqual PlanetSideGUID(75)
        player.progress mustEqual 50.0f
        player.condition mustEqual OxygenState.Suffocation

        vehicle.isDefined mustEqual true
        val v = vehicle.get
        v.guid mustEqual PlanetSideGUID(1546)
        v.progress mustEqual 50.0f
        v.condition mustEqual OxygenState.Suffocation
      case _ =>
        ko
    }
  }

  "encode (self)" in {
    val msg = OxygenStateMessage(PlanetSideGUID(75), 50.0f)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_self
  }

  "encode (vehicle)" in {
    val msg =
      OxygenStateMessage(PlanetSideGUID(75), 50.0f, PlanetSideGUID(1546), 50.0f)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_vehicle
  }
}

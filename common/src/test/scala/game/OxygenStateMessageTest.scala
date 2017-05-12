// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class OxygenStateMessageTest extends Specification {
  val string_self = hex"78 4b00f430"
  val string_vehicle = hex"78 4b00f4385037a180"

  "decode (self)" in {
    PacketCoding.DecodePacket(string_self).require match {
      case OxygenStateMessage(guid, progress, active, veh_state) =>
        guid mustEqual PlanetSideGUID(75)
        progress mustEqual 50.0
        active mustEqual true
        veh_state.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (vehicle)" in {
    PacketCoding.DecodePacket(string_vehicle).require match {
      case OxygenStateMessage(guid, progress, active, veh_state) =>
        guid mustEqual PlanetSideGUID(75)
        progress mustEqual 50.0f
        active mustEqual true
        veh_state.isDefined mustEqual true
        veh_state.get.vehicle_guid mustEqual PlanetSideGUID(1546)
        veh_state.get.progress mustEqual 50.0f
        veh_state.get.active mustEqual true
      case _ =>
        ko
    }
  }

  "encode (self)" in {
    val msg = OxygenStateMessage(PlanetSideGUID(75), 50.0f, true)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_self
  }

  "encode (vehicle)" in {
    val msg = OxygenStateMessage(PlanetSideGUID(75), 50.0f, true, WaterloggedVehicleState(PlanetSideGUID(1546), 50.0f, true))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_vehicle
  }
}

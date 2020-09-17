// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class WarpgateRequestTest extends Specification {
  val string = hex"A4 1D00 1F00 1327 1F00 00 00" //an Extinction warp gate to a Desolation warp gate

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case WarpgateRequest(continent_guid, building_guid, dest_building_guid, dest_continent_guid, unk1, unk2) =>
        continent_guid mustEqual PlanetSideGUID(29)
        building_guid mustEqual PlanetSideGUID(31)
        dest_building_guid mustEqual PlanetSideGUID(10003)
        dest_continent_guid mustEqual PlanetSideGUID(31)
        unk1 mustEqual 0
        unk2 mustEqual 0
      case _ =>
        ko
    }
  }
  "encode" in {
    val msg = WarpgateRequest(PlanetSideGUID(29), PlanetSideGUID(31), PlanetSideGUID(10003), PlanetSideGUID(31), 0, 0)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}

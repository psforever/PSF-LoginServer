// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ZonePopulationUpdateMessageTest extends Specification {
  val string = hex"B6 0400 9E010000 8A000000 25000000 8A000000 25000000 8A000000 25000000 8A000000 25000000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ZonePopulationUpdateMessage(
            continent_guid,
            zone_queue,
            tr_queue,
            tr_pop,
            nc_queue,
            nc_pop,
            vs_queue,
            vs_pop,
            bo_queue,
            bo_pop
          ) =>
        continent_guid mustEqual 4
        zone_queue mustEqual 414
        tr_queue mustEqual 138
        tr_pop mustEqual 37
        nc_queue mustEqual 138
        nc_pop mustEqual 37
        vs_queue mustEqual 138
        vs_pop mustEqual 37
        bo_queue mustEqual 138
        bo_pop mustEqual 37
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ZonePopulationUpdateMessage(4, 414, 138, 37, 138, 37, 138, 37, 138, 37)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class BroadcastWarpgateUpdateMessageTest extends Specification {
  val string = hex"D9 0D 00 01 00 20"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case BroadcastWarpgateUpdateMessage(continent_guid, building_guid, state1, state2, state3) =>
        continent_guid mustEqual 13
        building_guid mustEqual 1
        state1 mustEqual false
        state2 mustEqual false
        state3 mustEqual true
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = BroadcastWarpgateUpdateMessage(13, 1, false, false, true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

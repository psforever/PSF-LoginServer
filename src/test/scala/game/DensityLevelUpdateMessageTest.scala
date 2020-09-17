// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class DensityLevelUpdateMessageTest extends Specification {
  val string = hex"cd 0100 1f4e 000000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case DensityLevelUpdateMessage(zone_id, building_id, unk) =>
        zone_id mustEqual 1
        building_id mustEqual 19999
        unk.length mustEqual 8
        unk.head mustEqual 0
        unk(1) mustEqual 0
        unk(2) mustEqual 0
        unk(3) mustEqual 0
        unk(4) mustEqual 0
        unk(5) mustEqual 0
        unk(6) mustEqual 0
        unk(7) mustEqual 0
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DensityLevelUpdateMessage(1, 19999, List(0, 0, 0, 0, 0, 0, 0, 0))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }

  "encode (failure; wrong number of list entries)" in {
    val msg = DensityLevelUpdateMessage(1, 19999, List(0))
    PacketCoding.encodePacket(msg).isSuccessful mustEqual false
  }

  "encode (failure; list number too big)" in {
    val msg = DensityLevelUpdateMessage(1, 19999, List(0, 0, 0, 0, 0, 0, 0, 8))
    PacketCoding.encodePacket(msg).isSuccessful mustEqual false
  }

  "encode (failure; list number too small)" in {
    val msg = DensityLevelUpdateMessage(1, 19999, List(0, 0, 0, 0, 0, -1, 0, 0))
    PacketCoding.encodePacket(msg).isSuccessful mustEqual false
  }
}

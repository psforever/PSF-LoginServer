// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ZoneInfoMessageTest extends Specification {
  val string        = hex"C6 0C 00 80 00 00 00 00"
  val string_cavern = hex"C6 1B 00 1D F9 F3 00 00"

  "decode (normal)" in {
    PacketCoding.decodePacket(string).require match {
      case ZoneInfoMessage(zone, empire_status, unk) =>
        zone mustEqual 12
        empire_status mustEqual true
        unk mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (cavern)" in {
    PacketCoding.decodePacket(string_cavern).require match {
      case ZoneInfoMessage(zone, empire_status, unk) =>
        zone mustEqual 27
        empire_status mustEqual false
        unk mustEqual 15135547
      case _ =>
        ko
    }
  }

  "encode (normal)" in {
    val msg = ZoneInfoMessage(12, true, 0)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }

  "encode (cavern)" in {
    val msg = ZoneInfoMessage(27, false, 15135547)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_cavern
  }
}

// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ZoneForcedCavernConnectionsMessageTest extends Specification {
  val string = hex"E3200040"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ZoneForcedCavernConnectionsMessage(zone, unk) =>
        zone mustEqual 32
        unk mustEqual 1
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ZoneForcedCavernConnectionsMessage(32, 1)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class ZoneForcedCavernConnectionsMessageTest extends Specification {
  val string = hex"E3200040"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ZoneForcedCavernConnectionsMessage(zone, unk) =>
        zone mustEqual PlanetSideGUID(32)
        unk mustEqual 1
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ZoneForcedCavernConnectionsMessage(PlanetSideGUID(32), 1)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

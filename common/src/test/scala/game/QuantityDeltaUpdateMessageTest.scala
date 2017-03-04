// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class QuantityDeltaUpdateMessageTest extends Specification {
  val string = hex"C4 5300 FBFFFFFF"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case QuantityDeltaUpdateMessage(item_guid, quantity) =>
        item_guid mustEqual PlanetSideGUID(83)
        quantity mustEqual -5
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = QuantityDeltaUpdateMessage(PlanetSideGUID(83), -5)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

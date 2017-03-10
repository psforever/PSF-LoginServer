// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class QuantityUpdateMessageTest extends Specification {
  val string = hex"3D 5300 7B000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case QuantityUpdateMessage(item_guid, quantity) =>
        item_guid mustEqual PlanetSideGUID(83)
        quantity mustEqual 123
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = QuantityUpdateMessage(PlanetSideGUID(83), 123)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

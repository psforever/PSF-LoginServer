// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class InventoryStateMessageTest extends Specification {
  val string = hex"38 5C0B 00 3C02 B20000000 0"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case InventoryStateMessage(object_guid, unk, inv_guid, value) =>
        object_guid mustEqual PlanetSideGUID(2908)
        unk mustEqual 0
        inv_guid mustEqual PlanetSideGUID(2800)
        value mustEqual 200
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = InventoryStateMessage(PlanetSideGUID(2908), 0, PlanetSideGUID(2800), 200)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}


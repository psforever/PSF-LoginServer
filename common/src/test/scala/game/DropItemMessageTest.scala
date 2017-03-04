// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class DropItemMessageTest extends Specification {
  val string = hex"37 4C00"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case DropItemMessage(item_guid) =>
        item_guid mustEqual PlanetSideGUID(76)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DropItemMessage(PlanetSideGUID(76))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

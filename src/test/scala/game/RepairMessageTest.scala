// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class RepairMessageTest extends Specification {
  val string = hex"4D 2709 5C000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case RepairMessage(item_guid, repair_value) =>
        item_guid mustEqual PlanetSideGUID(2343)
        repair_value mustEqual 92
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = RepairMessage(PlanetSideGUID(2343), 92)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

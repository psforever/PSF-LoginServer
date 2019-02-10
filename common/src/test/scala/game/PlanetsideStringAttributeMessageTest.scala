// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable.Specification
import net.psforever.packet.PacketCoding
import net.psforever.packet.game._
import scodec.bits._

class PlanetsideStringAttributeMessageTest extends Specification {
  val string = hex"92 fb04 00 90540068006500200042006c00610063006b00200052006100760065006e007300"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case PlanetsideStringAttributeMessage(guid, string_type, string_value) =>
        guid mustEqual PlanetSideGUID(1275)
        string_type mustEqual 0
        string_value mustEqual "The Black Ravens"
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = PlanetsideStringAttributeMessage(PlanetSideGUID(1275), 0, "The Black Ravens")
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

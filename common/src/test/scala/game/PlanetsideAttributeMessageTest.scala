// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable.Specification
import net.psforever.packet.PacketCoding
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class PlanetsideAttributeMessageTest extends Specification {
  val string = hex"2c d9040458000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case PlanetsideAttributeMessage(player_guid, attribute_type, attribute_value) =>
        player_guid mustEqual PlanetSideGUID(1241)
        attribute_type mustEqual 4
        attribute_value mustEqual 88
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = PlanetsideAttributeMessage(PlanetSideGUID(1241), 4, 88)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }

}

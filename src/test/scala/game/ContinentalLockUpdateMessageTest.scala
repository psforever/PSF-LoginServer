// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideEmpire
import scodec.bits._

class ContinentalLockUpdateMessageTest extends Specification {
  val string = hex"A8 16 00 40"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ContinentalLockUpdateMessage(continent_guid, empire) =>
        continent_guid mustEqual 22
        empire mustEqual PlanetSideEmpire.NC
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ContinentalLockUpdateMessage(22, PlanetSideEmpire.NC)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

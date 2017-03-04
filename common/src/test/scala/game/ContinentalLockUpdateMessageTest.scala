// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideEmpire
import scodec.bits._

class ContinentalLockUpdateMessageTest extends Specification {
  val string = hex"A8 16 00 40"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ContinentalLockUpdateMessage(continent_guid, empire) =>
        continent_guid mustEqual PlanetSideGUID(22)
        empire mustEqual PlanetSideEmpire.NC
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ContinentalLockUpdateMessage(PlanetSideGUID(22), PlanetSideEmpire.NC)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

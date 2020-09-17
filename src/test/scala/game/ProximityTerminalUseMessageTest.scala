// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class ProximityTerminalUseMessageTest extends Specification {
  val string = hex"C3 4B00 A700 80"
  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ProximityTerminalUseMessage(player_guid, object_guid, unk) =>
        player_guid mustEqual PlanetSideGUID(75)
        object_guid mustEqual PlanetSideGUID(167)
        unk mustEqual true
      case _ =>
        ko
    }
  }
  "encode" in {
    val msg = ProximityTerminalUseMessage(PlanetSideGUID(75), PlanetSideGUID(167), true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}

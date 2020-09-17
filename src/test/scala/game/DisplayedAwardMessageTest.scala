// Copyright (c) 2017 PSForever
package game

import net.psforever.types.{MeritCommendation, PlanetSideGUID}
import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class DisplayedAwardMessageTest extends Specification {
  val string = hex"D1 9F06 A6010000 3 0"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case DisplayedAwardMessage(player_guid, ribbon, bar) =>
        player_guid mustEqual PlanetSideGUID(1695)
        ribbon mustEqual MeritCommendation.TwoYearVS
        bar mustEqual RibbonBarsSlot.TermOfService
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DisplayedAwardMessage(PlanetSideGUID(1695), MeritCommendation.TwoYearVS, RibbonBarsSlot.TermOfService)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

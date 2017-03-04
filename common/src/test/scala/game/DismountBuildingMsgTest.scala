// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class DismountBuildingMsgTest extends Specification {
  val string = hex"7C 4B00 2E00"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case DismountBuildingMsg(player_guid, building_guid) =>
        player_guid mustEqual PlanetSideGUID(75)
        building_guid mustEqual PlanetSideGUID(46)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DismountBuildingMsg(PlanetSideGUID(75), PlanetSideGUID(46))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

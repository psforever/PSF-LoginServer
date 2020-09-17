// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class DelayedPathMountMsgTest extends Specification {
  val string = hex"5a f50583044680"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case DelayedPathMountMsg(player_guid, vehicle_guid, u3, u4) =>
        player_guid mustEqual PlanetSideGUID(1525)
        vehicle_guid mustEqual PlanetSideGUID(1155)
        u3 mustEqual 70
        u4 mustEqual true
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DelayedPathMountMsg(PlanetSideGUID(1525), PlanetSideGUID(1155), 70, true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

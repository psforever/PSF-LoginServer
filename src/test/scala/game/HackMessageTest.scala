// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class HackMessageTest extends Specification {
  // Record 62 in PSCap-hack-door-tower.gcap
  val string = hex"54 000105c3800000202fc04200000000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case HackMessage(unk1, target_guid, player_guid, progress, unk5, hack_state, unk7) =>
        unk1 mustEqual 0
        target_guid mustEqual PlanetSideGUID(1024)
        player_guid mustEqual PlanetSideGUID(3607)
        progress mustEqual 0
        unk5 mustEqual 3212836864L
        hack_state mustEqual HackState.Start
        unk7 mustEqual 8L
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = HackMessage(0, PlanetSideGUID(1024), PlanetSideGUID(3607), 0, 3212836864L, HackState.Start, 8L)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}

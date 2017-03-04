// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class BattleExperienceMessageTest extends Specification {
  val string = hex"B4 8A0A E7030000 00"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case BattleExperienceMessage(player_guid, experience, unk) =>
        player_guid mustEqual PlanetSideGUID(2698)
        experience mustEqual 999
        unk mustEqual 0
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = BattleExperienceMessage(PlanetSideGUID(2698), 999, 0)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

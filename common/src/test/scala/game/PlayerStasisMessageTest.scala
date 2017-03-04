// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class PlayerStasisMessageTest extends Specification {
  val string = hex"8A 4B00 80"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case PlayerStasisMessage(player_guid, stasis) =>
        player_guid mustEqual PlanetSideGUID(75)
        stasis mustEqual true
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = PlayerStasisMessage(PlanetSideGUID(75))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class PlayerStasisMessageTest extends Specification {
  val string = hex"8A 4B00 80"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case PlayerStasisMessage(player_guid, stasis) =>
        player_guid mustEqual PlanetSideGUID(75)
        stasis mustEqual true
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = PlayerStasisMessage(PlanetSideGUID(75))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class AvatarGrenadeStateMessageTest extends Specification {
  val string = hex"A9 DA11 01"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case AvatarGrenadeStateMessage(player_guid, state) =>
        player_guid mustEqual PlanetSideGUID(4570)
        state mustEqual GrenadeState.PRIMED
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = AvatarGrenadeStateMessage(PlanetSideGUID(4570), GrenadeState.PRIMED)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

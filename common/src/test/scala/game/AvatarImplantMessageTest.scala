// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class AvatarImplantMessageTest extends Specification {
  val string = hex"58 630C 68 80"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case AvatarImplantMessage(player_guid, unk1, unk2, implant) =>
        player_guid mustEqual PlanetSideGUID(3171)
        unk1 mustEqual 3
        unk2 mustEqual 1
        implant mustEqual ImplantType.Targeting
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = AvatarImplantMessage(PlanetSideGUID(3171), 3, 1, ImplantType.Targeting)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

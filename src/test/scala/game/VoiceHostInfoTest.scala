// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class VoiceHostInfoTest extends Specification {
  val string_info = hex"b2 4b00"

  "decode" in {
    PacketCoding.decodePacket(string_info).require match {
      case VoiceHostInfo(player, _) =>
        player mustEqual PlanetSideGUID(75)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = VoiceHostInfo(PlanetSideGUID(75), ByteVector.empty)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_info
  }
}

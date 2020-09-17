// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class VoiceHostRequestTest extends Specification {
  val string_request = hex"b0 2580 00"

  "decode" in {
    PacketCoding.decodePacket(string_request).require match {
      case VoiceHostRequest(unk, player, _) =>
        unk mustEqual false
        player mustEqual PlanetSideGUID(75)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = VoiceHostRequest(false, PlanetSideGUID(75), ByteVector.empty)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_request
  }
}

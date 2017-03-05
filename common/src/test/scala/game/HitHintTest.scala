// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class HitHintTest extends Specification {
  val string = hex"0A 460B 0100"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case HitHint(source, player) =>
        source mustEqual PlanetSideGUID(2886)
        player mustEqual PlanetSideGUID(1)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = HitHint(PlanetSideGUID(2886), PlanetSideGUID(1))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

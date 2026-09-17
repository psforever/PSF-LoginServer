// Copyright (c) 2026 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.packets.ModuleLimitsMessage
import scodec.bits._
import org.specs2.mutable.Specification

class ModuleLimitsMessageTest extends Specification {
  val string = hex"C9 FF3D80"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ModuleLimitsMessage(u1, u2, u3) =>
        u1 mustEqual 255
        u2 mustEqual 15
        u3 mustEqual 3
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ModuleLimitsMessage(255, 15, 3)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

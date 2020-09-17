// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class VoiceHostKillTest extends Specification {
  val string_kill = hex"b1"

  "decode" in {
    PacketCoding.decodePacket(string_kill).require match {
      case VoiceHostKill() =>
        ok
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = VoiceHostKill()
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_kill
  }
}

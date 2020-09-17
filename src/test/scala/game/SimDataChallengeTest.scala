// Copyright (c) 2020 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class SimDataChallengeTest extends Specification {
  val string = hex"96050067030000e9030000e10400000001000065020000808000000000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case SimDataChallenge(u1, u2, u3, u4, u5) =>
        u1 mustEqual List(871L, 1001L, 1249L, 256L, 613L)
        u2 mustEqual true
        u3 mustEqual 1
        u4 mustEqual 0L
        u5 mustEqual false
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = SimDataChallenge(List(871L, 1001L, 1249L, 256L, 613L), true, 1, 0, false)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

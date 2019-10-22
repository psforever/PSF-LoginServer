// Copyright (c) 2019 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import org.specs2.mutable._
import scodec.bits._

class SquadMemberEventTest extends Specification {
  val string = hex"7000e008545180410848006f0066004400070051150800"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case SquadMemberEvent(u1, u2, u3, u4, u5, u6, u7) =>
        u1 mustEqual MemberEvent.Add
        u2 mustEqual 7
        u3 mustEqual 42771010L
        u4 mustEqual 0
        u5.contains("HofD") mustEqual true
        u6.contains(7) mustEqual true
        u7.contains(529745L) mustEqual true
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = SquadMemberEvent(MemberEvent.Add, 7, 42771010L, 0, Some("HofD"), Some(7), Some(529745L))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}

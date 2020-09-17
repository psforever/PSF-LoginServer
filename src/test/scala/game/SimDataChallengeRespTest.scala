// Copyright (c) 2020 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class SimDataChallengeRespTest extends Specification {
  val string = hex"97050067030000e9030000e1040000000100006502000005003b3388faa52df48fb27971e7c3a9d0c109d5b03f00"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case SimDataChallengeResp(u1, u2, u3) =>
        u1 mustEqual List(871L, 1001L, 1249L, 256L, 613L)
        u2 mustEqual List(4203230011L, 2415144357L, 3882973618L, 3251677635L, 1068553481L)
        u3 mustEqual false
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = SimDataChallengeResp(
      List(871L, 1001L, 1249L, 256L, 613L),
      List(4203230011L, 2415144357L, 3882973618L, 3251677635L, 1068553481L),
      false
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

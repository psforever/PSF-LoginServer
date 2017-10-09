// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class HackMessageTest extends Specification {
  // Record 62 in PSCap-hack-door-tower.gcap
  val string = hex"54 000105c3800000202fc04200000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case HackMessage(unk1, unk2, unk3, unk4, unk5, unk6, unk7) =>
        unk1 mustEqual 0
        unk2 mustEqual 1024
        unk3 mustEqual 3607
        unk4 mustEqual 0
        unk5 mustEqual 3212836864L
        unk6 mustEqual 1
        unk7 mustEqual 8L
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = HackMessage(0,1024,3607,0,3212836864L,1,8L)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}

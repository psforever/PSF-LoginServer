// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ExperienceAddedMessageTest extends Specification {
  val string = hex"B8 04 03"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ExperienceAddedMessage(exp, unk) =>
        exp mustEqual 260 //0x104
        unk mustEqual true
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ExperienceAddedMessage(260)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

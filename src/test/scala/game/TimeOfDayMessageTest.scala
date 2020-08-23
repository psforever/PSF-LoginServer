// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class TimeOfDayMessageTest extends Specification {
  val string = hex"48 00 00 00 47 00 00 20 41"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case TimeOfDayMessage(time, unk) =>
        time mustEqual 1191182336
        unk mustEqual 1092616192
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = TimeOfDayMessage(1191182336)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

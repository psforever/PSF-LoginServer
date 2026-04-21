// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class TimeOfDayMessageTest extends Specification {
  val string = hex"48 00 00 00 47 00 00 20 41"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case TimeOfDayMessage(timeOfDay, timeSpeed) =>
        timeOfDay mustEqual 32768.0f
        timeSpeed mustEqual 10.0f
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = TimeOfDayMessage(32768.0f)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }

  val string2 = hex"48 00 00 A0 47 00 00 20 41" //22:45

  "decode2" in {
    PacketCoding.decodePacket(string2).require match {
      case TimeOfDayMessage(timeOfDay, timeSpeed) =>
        timeOfDay mustEqual 81920.0f
        timeSpeed mustEqual 10.0f
      case _ =>
        ko
    }
  }

  "encode2" in {
    val msg = TimeOfDayMessage(81920.0f)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string2
  }
}

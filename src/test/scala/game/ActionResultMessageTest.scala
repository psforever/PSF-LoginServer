// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ActionResultMessageTest extends Specification {
  val string_pass = hex"1f 80"
  val string_fail = hex"1f 0080000000"

  "decode (pass)" in {
    PacketCoding.decodePacket(string_pass).require match {
      case ActionResultMessage(okay, code) =>
        okay mustEqual true
        code mustEqual None
      case _ =>
        ko
    }
  }

  "decode (fail)" in {
    PacketCoding.decodePacket(string_fail).require match {
      case ActionResultMessage(okay, code) =>
        okay mustEqual false
        code mustEqual Some(1)
      case _ =>
        ko
    }
  }

  "encode (pass, full)" in {
    val msg = ActionResultMessage(true, None)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_pass
  }

  "encode (pass, minimal)" in {
    val msg = ActionResultMessage.Pass
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_pass
  }

  "encode (fail, full)" in {
    val msg = ActionResultMessage(false, Some(1))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_fail
  }

  "encode (fail, minimal)" in {
    val msg = ActionResultMessage.Fail(1)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_fail
  }
}

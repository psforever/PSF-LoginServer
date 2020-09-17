// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class KeepAliveMessageTest extends Specification {
  val string = hex"BA 0000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case KeepAliveMessage(code) =>
        code mustEqual 0
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = KeepAliveMessage()
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

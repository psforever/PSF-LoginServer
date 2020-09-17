// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class GenericActionMessageTest extends Specification {
  val string = hex"A7 94"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case GenericActionMessage(action) =>
        action mustEqual 37
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = GenericActionMessage(37)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

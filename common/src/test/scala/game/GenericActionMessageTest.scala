// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class GenericActionMessageTest extends Specification {
  val string = hex"A7 94"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case GenericActionMessage(action) =>
        action mustEqual 37
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = GenericActionMessage(37)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

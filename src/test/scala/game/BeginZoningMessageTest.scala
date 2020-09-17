// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class BeginZoningMessageTest extends Specification {
  val string = hex"43" //yes, just the opcode

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case BeginZoningMessage() =>
        ok
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = BeginZoningMessage()
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

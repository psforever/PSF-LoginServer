// Copyright (c) 2017 PSForever
package control

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.control._
import scodec.bits._

class ConnectionCloseTest extends Specification {
  val string = hex"001D"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ConnectionClose() =>
        ok
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ConnectionClose()
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}

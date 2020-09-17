// Copyright (c) 2017 PSForever
package control

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.control._
import scodec.bits._

class ClientStartTest extends Specification {
  val string = hex"0001 00000002 00261e27 000001f0"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ClientStart(nonce) =>
        nonce mustEqual 656287232
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ClientStart(656287232)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

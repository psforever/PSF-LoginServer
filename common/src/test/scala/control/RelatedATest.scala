// Copyright (c) 2017 PSForever
package control

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.control._
import scodec.bits._

class RelatedATest extends Specification {
  val string0 = hex"00 11 01 04"

  "decode (0)" in {
    PacketCoding.DecodePacket(string0).require match {
      case RelatedA0(slot) =>
        slot mustEqual 260
      case _ =>
        ko
    }
  }

  "encode (0)" in {
    val pkt = RelatedA0(260)
    val msg = PacketCoding.EncodePacket(pkt).require.toByteVector
    msg mustEqual string0
  }
}

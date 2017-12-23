// Copyright (c) 2017 PSForever
package control

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.control._
import scodec.bits._

class SlottedMetaAckTest extends Specification {
  val string = hex"00150da4"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case SlottedMetaAck(_, _) =>
        ko
      case RelatedB0(subslot) => //important!
        subslot mustEqual 3492
      case _ =>
        ko
    }
  }

  "encode" in {
    val pkt = SlottedMetaAck(0, 3492)
    val msg = PacketCoding.EncodePacket(pkt).require.toByteVector

    msg mustEqual string
  }
}

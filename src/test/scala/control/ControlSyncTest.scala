// Copyright (c) 2017 PSForever
package control

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.control._
import scodec.bits._

class ControlSyncTest extends Specification {
  val string = hex"0007 5268 0000004D 00000052 0000004D 0000007C 0000004D 0000000000000276 0000000000000275"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ControlSync(a, b, c, d, e, f, g, h) =>
        a mustEqual 21096
        b mustEqual 0x4d
        c mustEqual 0x52
        d mustEqual 0x4d
        e mustEqual 0x7c
        f mustEqual 0x4d
        g mustEqual 0x276
        h mustEqual 0x275
      case _ =>
        ko
    }
  }

  "encode" in {
    val encoded = PacketCoding.encodePacket(ControlSync(21096, 0x4d, 0x52, 0x4d, 0x7c, 0x4d, 0x276, 0x275)).require
    encoded.toByteVector mustEqual string
  }
}

// Copyright (c) 2017 PSForever
package control

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.control._
import scodec.bits._

class ControlSyncRespTest extends Specification {
  val string = hex"0008 5268 21392D92 0000000000000276 0000000000000275 0000000000000275 0000000000000276"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ControlSyncResp(a, b, c, d, e, f) =>
        a mustEqual 21096

        b mustEqual 0x21392d92
        c mustEqual 0x276
        d mustEqual 0x275
        e mustEqual 0x275
        f mustEqual 0x276
      case _ =>
        ko
    }
  }

  "encode" in {
    val encoded = PacketCoding.encodePacket(ControlSyncResp(21096, 0x21392d92, 0x276, 0x275, 0x275, 0x276)).require

    encoded.toByteVector mustEqual string
  }
}

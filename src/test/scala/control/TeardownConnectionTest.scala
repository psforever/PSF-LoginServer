// Copyright (c) 2017 PSForever
package control

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.control._
import scodec.bits._

class TeardownConnectionTest extends Specification {
  val string = hex"00 05 02 4F 57 17 00 06"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case TeardownConnection(nonce) =>
        nonce mustEqual 391597826
      case _ =>
        ko
    }
  }

  "encode" in {
    val encoded = PacketCoding.encodePacket(TeardownConnection(391597826)).require

    encoded.toByteVector mustEqual string
  }
}

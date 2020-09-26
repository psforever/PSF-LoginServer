// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ZoneLockInfoMesageTest extends Specification {
  val string = hex"DF 1B 00 40"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ZoneLockInfoMessage(zone, locked, unk) =>
        zone mustEqual 27
        locked mustEqual false
        unk mustEqual true
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ZoneLockInfoMessage(27, false, true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

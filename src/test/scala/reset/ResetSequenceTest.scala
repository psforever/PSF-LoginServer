// Copyright (c) 2021 PSForever
package reset

import net.psforever.packet._
import net.psforever.packet.reset.ResetSequence
import org.specs2.mutable._
import scodec.bits._

class ResetSequenceTest extends Specification {
  val string = hex"01"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ResetSequence() => ok
      case _               => ko
    }
  }

  "encode" in {
    val msg = ResetSequence()
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

// Copyright (c) 2020 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class DataChallengeMessageTest extends Specification {
  val string = hex"938673616d706c6501000000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case DataChallengeMessage(attribute, value) =>
        attribute mustEqual "sample"
        value mustEqual 1L
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DataChallengeMessage("sample", 1L)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

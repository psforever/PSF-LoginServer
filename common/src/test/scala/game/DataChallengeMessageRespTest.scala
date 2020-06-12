// Copyright (c) 2020 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class DataChallengeMessageRespTest extends Specification {
  val string = hex"948673616d706c6501000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case DataChallengeMessageResp(attribute, value) =>
        attribute mustEqual "sample"
        value mustEqual 1L
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DataChallengeMessageResp("sample", 1L)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

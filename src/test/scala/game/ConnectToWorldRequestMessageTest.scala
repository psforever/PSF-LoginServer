// Copyright (c) 2017 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import org.specs2.mutable._
import scodec.bits._

class ConnectToWorldRequestMessageTest extends Specification {
  val string =
    hex"03 8667656D696E69 0000000000000000 00000000 00000000 00000000 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  80 00 00 "

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ConnectToWorldRequestMessage(serverName, token, majorVersion, minorVersion, revision, buildDate, unk) =>
        serverName mustEqual "gemini"
        token mustEqual ""
        majorVersion mustEqual 0
        minorVersion mustEqual 0
        revision mustEqual 0
        buildDate mustEqual ""
        unk mustEqual 0
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ConnectToWorldRequestMessage("gemini", "", 0, 0, 0, "", 0)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

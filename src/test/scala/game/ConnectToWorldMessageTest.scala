// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ConnectToWorldMessageTest extends Specification {
  val string = hex"04 8667656D696E69  8C36342E33372E3135382E36393C75"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ConnectToWorldMessage(serverName, serverIp, serverPort) =>
        serverName mustEqual "gemini"
        serverIp mustEqual "64.37.158.69"
        serverPort mustEqual 30012
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ConnectToWorldMessage("gemini", "64.37.158.69", 30012)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}

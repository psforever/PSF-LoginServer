// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class DisconnectMessageTest extends Specification {
  val string = hex"B7 85 46 69 72 73 74 86 53 65 63 6F 6E 64 8E 46 69 72 73 74 20 26 20 73 65 63 6F 6E 64"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case DisconnectMessage(unk1, unk2, unk3) =>
        unk1 mustEqual "First"
        unk2 mustEqual "Second"
        unk3 mustEqual "First & second"
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DisconnectMessage("First", "Second", "First & second")
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

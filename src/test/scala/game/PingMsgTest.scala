// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class PingMsgTest extends Specification {
  val packet = hex"1a 00000000 b0360000"

  "decode" in {
    PacketCoding.decodePacket(packet).require match {
      case PingMsg(unk1, unk2) =>
        unk1 === 0
        unk2 === 14000
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = PingMsg(0, 14000)
    PacketCoding.encodePacket(msg).require.toByteVector === packet
  }
}

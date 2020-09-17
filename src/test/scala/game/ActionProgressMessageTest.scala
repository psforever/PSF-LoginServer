// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ActionProgressMessageTest extends Specification {
  val string = hex"216000000000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ActionProgressMessage(unk1, unk2) =>
        unk1 mustEqual 6
        unk2 mustEqual 0
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ActionProgressMessage(6, 0L)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

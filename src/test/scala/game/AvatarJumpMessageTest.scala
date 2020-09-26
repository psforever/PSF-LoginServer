// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class AvatarJumpMessageTest extends Specification {
  val string = hex"35 80"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case AvatarJumpMessage(state) =>
        state mustEqual true
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = AvatarJumpMessage(true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

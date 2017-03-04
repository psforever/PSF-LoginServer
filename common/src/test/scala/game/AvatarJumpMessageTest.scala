// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class AvatarJumpMessageTest extends Specification {
  val string = hex"35 80"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case AvatarJumpMessage(state) =>
        state mustEqual true
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = AvatarJumpMessage(true)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

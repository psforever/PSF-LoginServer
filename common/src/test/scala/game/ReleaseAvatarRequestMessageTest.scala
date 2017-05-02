// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits.HexStringSyntax

class ReleaseAvatarRequestMessageTest extends Specification {
  val string = hex"ac"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ReleaseAvatarRequestMessage() =>
        ok
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ReleaseAvatarRequestMessage()
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

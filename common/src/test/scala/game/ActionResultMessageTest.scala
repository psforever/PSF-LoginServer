// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ActionResultMessageTest extends Specification {
  "decode" in {
    PacketCoding.DecodePacket(hex"1f 80").require match {
      case ActionResultMessage(okay, code) =>
        okay === true
        code === None
      case _ =>
        ko
    }

    PacketCoding.DecodePacket((hex"1f".bits ++ bin"0" ++ hex"01000000".bits).toByteVector).require match {
      case ActionResultMessage(okay, code) =>
        okay === false
        code === Some(1)
      case _ =>
        ko
    }
  }

  "encode" in {
    PacketCoding.EncodePacket(ActionResultMessage(true, None)).require.toByteVector === hex"1f 80"
    PacketCoding.EncodePacket(ActionResultMessage(false, Some(1))).require.toByteVector ===
      (hex"1f".bits ++ bin"0" ++ hex"01000000".bits).toByteVector
  }
}

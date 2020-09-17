// Copyright (c) 2017 PSForever
package game

import net.psforever.types.Vector3
import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class DamageWithPositionMessageTest extends Specification {
  val string = hex"A6 11 6C2D7 65535 CA16"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case DamageWithPositionMessage(unk, pos) =>
        unk mustEqual 17
        pos.x mustEqual 3674.8438f
        pos.y mustEqual 2726.789f
        pos.z mustEqual 91.15625f
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DamageWithPositionMessage(17, Vector3(3674.8438f, 2726.789f, 91.15625f))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

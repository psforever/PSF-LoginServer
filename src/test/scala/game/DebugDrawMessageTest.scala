// Copyright (c) 2024 PSForever
package game

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.DebugDrawMessage
import net.psforever.types.Vector3
import org.specs2.mutable.Specification
import scodec.bits._

class DebugDrawMessageTest extends Specification {
  val string = hex"9c2040000000600000008000001c0010000186000800c8000f04008807d00016080578"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case DebugDrawMessage(u1, u2, u3, u4, u5) =>
        u1 mustEqual 1
        u2 mustEqual 2L
        u3 mustEqual 3L
        u4 mustEqual 4L
        u5 mustEqual List(
          Vector3(5,6,7),
          Vector3(50,60,70),
          Vector3(500,600,700)
        )
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DebugDrawMessage(1, 2L, 3L, 4L, List(Vector3(5,6,7), Vector3(50,60,70), Vector3(500,600,700)))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class DroppodFreefallingMessageTest extends Specification {
  val string = hex"68 220e 00e0b245 00c06145 00a08744 00000000 00000000 ffff79c4 0740b245 22c66145 00608144 00 67 3f 00 00 3f"

  "DroppodFreefallingMessage" should {
    "decode" in {
      PacketCoding.DecodePacket(string).require match {
        case DroppodFreefallingMessage(guid, pos, vel, pos2, orientation1, orientation2) =>
          guid mustEqual PlanetSideGUID(3618)
          pos mustEqual Vector3(5724, 3612, 1085)
          vel mustEqual Vector3(0, 0, -999.99994f)
          pos2 mustEqual Vector3(5704.0034f, 3612.3833f, 1035.0f)
          orientation1 mustEqual Vector3(0, 70.3125f, 272.8125f)
          orientation2 mustEqual Vector3(0, 0, 272.8125f)
        case _ =>
          ko
      }
    }

    "encode" in {
      val msg = DroppodFreefallingMessage(
        PlanetSideGUID(3618),
        Vector3(5724, 3612, 1085),
        Vector3(0, 0, -999.99994f),
        Vector3(5704.0034f, 3612.3833f, 1035.0f),
        Vector3(0, 70.3125f, 272.8125f), Vector3(0, 0, 272.8125f))
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string
    }
  }
}

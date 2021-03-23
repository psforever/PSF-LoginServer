// Copyright (c) 2021 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class DroppodLaunchRequestMessageTest extends Specification {
  val string = hex"53 2405050000e0b24500c06145c0"

  "DroppodLaunchRequestMessage" should {
    "decode" in {
      PacketCoding.decodePacket(string).require match {
        case DroppodLaunchRequestMessage(info, unk) =>
          info.guid mustEqual PlanetSideGUID(1316)
          info.zone_number mustEqual 5
          info.xypos mustEqual Vector3(5724, 3612, 0)
          unk mustEqual 3
        case _ =>
          ko
      }
    }

    "encode" in {
      val msg = DroppodLaunchRequestMessage(PlanetSideGUID(1316), 5, Vector3(5724, 3612, 0))
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string
    }
  }
}

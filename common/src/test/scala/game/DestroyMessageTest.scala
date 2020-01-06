// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class DestroyMessageTest extends Specification {
  val string = hex"0C 74 09 74 09 00 00 06  35 3C FF D7 26 08"

  "DestroyMessage" should {
    "decode" in {
      PacketCoding.DecodePacket(string).require match {
        case DestroyMessage(unk1, unk2, unk3, pos) =>
          unk1 mustEqual PlanetSideGUID(2420)
          unk2 mustEqual PlanetSideGUID(2420)
          unk3 mustEqual PlanetSideGUID(0)
          pos mustEqual Vector3(1642.0469f, 4091.6172f, 32.59375f)
        case _ =>
          ko
      }
    }

    "encode" in {
      val msg = DestroyMessage(PlanetSideGUID(2420), PlanetSideGUID(2420), PlanetSideGUID(0), Vector3(1642.0469f, 4091.6172f, 32.59375f))
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string
    }
  }
}

// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class DeployObjectMessageTest extends Specification {
  val string = hex"5d 740b e8030000 a644b 6e3c6 7e18 00 00 3f 01000000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case DeployObjectMessage(guid, unk1, pos, orient, unk2) =>
        guid mustEqual PlanetSideGUID(2932)
        unk1 mustEqual 1000L
        pos mustEqual Vector3(5769.297f, 3192.8594f, 97.96875f)
        orient mustEqual Vector3.z(272.8125f)
        unk2 mustEqual 1L
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DeployObjectMessage(
      PlanetSideGUID(2932),
      1000L,
      Vector3(5769.297f, 3192.8594f, 97.96875f),
      Vector3.z(272.8125f),
      1L
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

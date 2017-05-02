// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class DeployObjectMessageTest extends Specification {
  //fake data; see comments in packet; this test exists to maintain code coverage
  val string = hex"5D 0000 00000000 00000 00000 0000 00 00 00 00000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case DeployObjectMessage(guid, unk1, pos, unk2, unk3, unk4, unk5) =>
        guid mustEqual PlanetSideGUID(0)
        unk1 mustEqual 0L
        pos.x mustEqual 0f
        pos.y mustEqual 0f
        pos.z mustEqual 0f
        unk2 mustEqual 0
        unk3 mustEqual 0
        unk4 mustEqual 0
        unk5 mustEqual 0L
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DeployObjectMessage(PlanetSideGUID(0), 0L, Vector3(0f, 0f, 0f), 0, 0, 0, 0L)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

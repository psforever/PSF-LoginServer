// Copyright (c) 2020 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class ChainLashMessageTest extends Specification {
  val string1 = hex"c5 cafe708880df81e910100000043060"
  val string2 = hex"c5 5282e910100000093050"

  "decode (1)" in {
    PacketCoding.decodePacket(string1).require match {
      case ChainLashMessage(u1a, u1b, u2, u3) =>
        u1a.isEmpty mustEqual true
        u1b.contains(Vector3(7673.164f, 544.1328f, 14.984375f)) mustEqual true
        u2 mustEqual 466
        u3 mustEqual List(PlanetSideGUID(1603))
      case _ =>
        ko
    }
  }

  "decode (2)" in {
    PacketCoding.decodePacket(string2).require match {
      case ChainLashMessage(u1a, u1b, u2, u3) =>
        u1a.contains(PlanetSideGUID(1445)) mustEqual true
        u1b.isEmpty mustEqual true
        u2 mustEqual 466
        u3 mustEqual List(PlanetSideGUID(1427))
      case _ =>
        ko
    }
  }

  "encode (1)" in {
    val msg = ChainLashMessage(Vector3(7673.164f, 544.1328f, 14.984375f), 466, List(PlanetSideGUID(1603)))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string1
  }

  "encode (2)" in {
    val msg = ChainLashMessage(PlanetSideGUID(1445), 466, List(PlanetSideGUID(1427)))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string2
  }

  "encode (fail; 1)" in {
    ChainLashMessage(
      Some(PlanetSideGUID(1445)),
      Some(Vector3(7673.164f, 544.1328f, 14.984375f)),
      466,
      List(PlanetSideGUID(1427))
    ) must throwA[AssertionError]
  }

  "encode (fail; 2)" in {
    ChainLashMessage(None, None, 466, List(PlanetSideGUID(1427))) must throwA[AssertionError]
  }
}

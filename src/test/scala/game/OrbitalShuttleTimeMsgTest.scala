// Copyright (c) 2021 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class OrbitalShuttleTimeMsgTest extends Specification {
  //these are all from vs sanctuary, near HART A
  val string1 = hex"5B 6E3AAE0000200F8000400000000500D9C1051303680414480DA411B0"
  val string2 = hex"5B 72A00F8000200F8000400000000500D9C1141303680450480DA41140"
  val string3 = hex"5B 62FFFFFFFFA00F8000400000000500D9C1141303680450480DA41140"
  val string4 = hex"5B 600000000030750000400000000500D9C1141303680450480DA41140"
  val string5 = hex"5B 64F3370180200F8000400000000500D9C1141303680450480DA41140"
  val string6 = hex"5B 6953278180200F8000400000000500D9C1061303680464480DA41050"
  val string7 = hex"5B 7DCA8D8180200F8000400000000500D9C1141303680450480DA41140"

  "decode (1)" in {
    PacketCoding.decodePacket(string1).require match {
      case OrbitalShuttleTimeMsg(u1, u2, u3, u4, u5, u6, u7, u8) =>
        u1 mustEqual 3
        u2 mustEqual 3
        u3 mustEqual 4
        u4 mustEqual 23669
        u5 mustEqual 8000
        u6 mustEqual true
        u7 mustEqual 0
        u8 mustEqual List(
          PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 5),
          PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 5),
          PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 27)
        )
      case _ =>
        ko
    }
  }

  "decode (2)" in {
    PacketCoding.decodePacket(string2).require match {
      case OrbitalShuttleTimeMsg(u1, u2, u3, u4, u5, u6, u7, u8) =>
        u1 mustEqual 3
        u2 mustEqual 4
        u3 mustEqual 5
        u4 mustEqual 8000
        u5 mustEqual 8000
        u6 mustEqual true
        u7 mustEqual 0
        u8 mustEqual List(
          PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 20),
          PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 20),
          PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 20)
        )
      case _ =>
        ko
    }
  }

  "decode (3)" in {
    PacketCoding.decodePacket(string3).require match {
      case OrbitalShuttleTimeMsg(u1, u2, u3, u4, u5, u6, u7, u8) =>
        u1 mustEqual 3
        u2 mustEqual 0
        u3 mustEqual 5
        u4 mustEqual 4294967295L
        u5 mustEqual 8000
        u6 mustEqual true
        u7 mustEqual 0
        u8 mustEqual List(
          PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 20),
          PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 20),
          PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 20)
        )
      case _ =>
        ko
    }
  }

  "decode (4)" in {
    PacketCoding.decodePacket(string4).require match {
      case OrbitalShuttleTimeMsg(u1, u2, u3, u4, u5, u6, u7, u8) =>
        u1 mustEqual 3
        u2 mustEqual 0
        u3 mustEqual 0
        u4 mustEqual 0
        u5 mustEqual 60000
        u6 mustEqual true
        u7 mustEqual 0
        u8 mustEqual List(
          PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 20),
          PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 20),
          PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 20)
        )
      case _ =>
        ko
    }
  }

  "decode (5)" in {
    PacketCoding.decodePacket(string5).require match {
      case OrbitalShuttleTimeMsg(u1, u2, u3, u4, u5, u6, u7, u8) =>
        u1 mustEqual 3
        u2 mustEqual 1
        u3 mustEqual 1
        u4 mustEqual 224998
        u5 mustEqual 8000
        u6 mustEqual true
        u7 mustEqual 0
        u8 mustEqual List(
          PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 20),
          PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 20),
          PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 20)
        )
      case _ =>
        ko
    }
  }

  "decode (6)" in {
    PacketCoding.decodePacket(string6).require match {
      case OrbitalShuttleTimeMsg(u1, u2, u3, u4, u5, u6, u7, u8) =>
        u1 mustEqual 3
        u2 mustEqual 2
        u3 mustEqual 2
        u4 mustEqual 216998
        u5 mustEqual 8000
        u6 mustEqual true
        u7 mustEqual 0
        u8 mustEqual List(
          PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 6),
          PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 25),
          PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 5)
        )
      case _ =>
        ko
    }
  }

  "decode (7)" in {
    PacketCoding.decodePacket(string7).require match {
      case OrbitalShuttleTimeMsg(u1, u2, u3, u4, u5, u6, u7, u8) =>
        u1 mustEqual 3
        u2 mustEqual 7
        u3 mustEqual 3
        u4 mustEqual 203669
        u5 mustEqual 8000
        u6 mustEqual true
        u7 mustEqual 0
        u8 mustEqual List(
          PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 20),
          PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 20),
          PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 20)
        )
      case _ =>
        ko
    }
  }

  "encode (1)" in {
    val msg = OrbitalShuttleTimeMsg(3, 3, 4, 23669, 8000, true, 0, List(
      PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 5),
      PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 5),
      PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 27)
    ))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string1
  }

  "encode (2)" in {
    val msg = OrbitalShuttleTimeMsg(3, 4, 5, 8000, 8000, true, 0, List(
      PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 20),
      PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 20),
      PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 20)
    ))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string2
  }

  "encode (3)" in {
    val msg = OrbitalShuttleTimeMsg(3, 0, 5, 4294967295L, 8000, true, 0, List(
      PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 20),
      PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 20),
      PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 20)
    ))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string3
  }

  "encode (4)" in {
    val msg = OrbitalShuttleTimeMsg(3, 0, 0, 0, 60000, true, 0, List(
      PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 20),
      PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 20),
      PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 20)
    ))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string4
  }

  "encode (5)" in {
    val msg = OrbitalShuttleTimeMsg(3, 1, 1, 224998, 8000, true, 0, List(
      PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 20),
      PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 20),
      PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 20)
    ))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string5
  }

  "encode (6)" in {
    val msg = OrbitalShuttleTimeMsg(3, 2, 2, 216998, 8000, true, 0, List(
      PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 6),
      PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 25),
      PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 5)
    ))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string6
  }

  "encode (7)" in {
    val msg = OrbitalShuttleTimeMsg(3, 7, 3, 203669, 8000, true, 0, List(
      PadAndShuttlePair(PlanetSideGUID(788), PlanetSideGUID(1127), 20),
      PadAndShuttlePair(PlanetSideGUID(787), PlanetSideGUID(1128), 20),
      PadAndShuttlePair(PlanetSideGUID(786), PlanetSideGUID(1129), 20)
    ))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string7
  }
}

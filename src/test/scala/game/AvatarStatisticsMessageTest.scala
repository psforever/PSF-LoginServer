// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class AvatarStatisticsMessageTest extends Specification {
  val string_long = hex"7F 4 00000000 0"
  val string_complex =
    hex"7F 01 3C 40 20 00 00 00  C0 00 00 00 00 00 00 00  20 00 00 00 20 00 00 00  40 00 00 00 00 00 00 00  00 00 00 00"

  "decode (long)" in {
    PacketCoding.decodePacket(string_long).require match {
      case AvatarStatisticsMessage(unk, stats) =>
        unk mustEqual 2
        stats.unk1 mustEqual None
        stats.unk2 mustEqual None
        stats.unk3.length mustEqual 1
        stats.unk3.head mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (complex)" in {
    PacketCoding.decodePacket(string_complex).require match {
      case AvatarStatisticsMessage(unk, stats) =>
        unk mustEqual 0
        stats.unk1 mustEqual Some(1)
        stats.unk2 mustEqual Some(572)
        stats.unk3.length mustEqual 8
        stats.unk3.head mustEqual 1
        stats.unk3(1) mustEqual 6
        stats.unk3(2) mustEqual 0
        stats.unk3(3) mustEqual 1
        stats.unk3(4) mustEqual 1
        stats.unk3(5) mustEqual 2
        stats.unk3(6) mustEqual 0
        stats.unk3(7) mustEqual 0
      case _ =>
        ko
    }
  }

  "encode (long)" in {
    val msg = AvatarStatisticsMessage(2, Statistics(0L))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_long
  }

  "encode (complex)" in {
    val msg = AvatarStatisticsMessage(0, Statistics(1, 572, List[Long](1, 6, 0, 1, 1, 2, 0, 0)))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_complex
  }

  "encode (failure; long; missing value)" in {
    val msg = AvatarStatisticsMessage(0, Statistics(None, None, List(0L)))
    PacketCoding.encodePacket(msg).isFailure mustEqual true
  }

  "encode (failure; complex; missing value (5-bit))" in {
    val msg = AvatarStatisticsMessage(0, Statistics(None, Some(572), List[Long](1, 6, 0, 1, 1, 2, 0, 0)))
    PacketCoding.encodePacket(msg).isFailure mustEqual true
  }

  "encode (failure; complex; missing value (11-bit))" in {
    val msg = AvatarStatisticsMessage(0, Statistics(Some(1), None, List[Long](1, 6, 0, 1, 1, 2, 0, 0)))
    PacketCoding.encodePacket(msg).isFailure mustEqual true
  }

  "encode (failure; complex; wrong number of list entries)" in {
    val msg = AvatarStatisticsMessage(0, Statistics(Some(1), None, List[Long](1, 6, 0, 1)))
    PacketCoding.encodePacket(msg).isFailure mustEqual true
  }
}

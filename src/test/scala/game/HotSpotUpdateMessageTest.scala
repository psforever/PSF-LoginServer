// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class HotSpotUpdateMessageTest extends Specification {
  val stringClear = hex"9F 0500 1 000"
  val stringOne   = hex"9F 0500 1 010 002E9 00145 80000 0"
  val stringTwo   = hex"9F 0500 5 020 00D07 008CA 80000 00BEA 004C4 80000"
  val stringThree = hex"9F 0A00 4 030 00FC8 00F0A 80000 002E9 00BEA 80000 00FC8 00BEA 80000 0"

  "decode (clear)" in {
    PacketCoding.decodePacket(stringClear).require match {
      case HotSpotUpdateMessage(continent_id, unk, spots) =>
        continent_id mustEqual 5
        unk mustEqual 1
        spots.size mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (one)" in {
    PacketCoding.decodePacket(stringOne).require match {
      case HotSpotUpdateMessage(continent_id, unk, spots) =>
        continent_id mustEqual 5
        unk mustEqual 1
        spots.size mustEqual 1
        spots.head mustEqual HotSpotInfo(4700.0f, 2600.0f, 64.0f)
      case _ =>
        ko
    }
  }

  "decode (two)" in {
    PacketCoding.decodePacket(stringTwo).require match {
      case HotSpotUpdateMessage(continent_id, unk, spots) =>
        continent_id mustEqual 5
        unk mustEqual 5
        spots.size mustEqual 2
        spots.head mustEqual HotSpotInfo(4000.0f, 5400.0f, 64.0f)
        spots(1) mustEqual HotSpotInfo(5500.0f, 2200.0f, 64.0f)
      case _ =>
        ko
    }
  }

  "decode (three)" in {
    PacketCoding.decodePacket(stringThree).require match {
      case HotSpotUpdateMessage(continent_id, unk, spots) =>
        continent_id mustEqual 10
        unk mustEqual 4
        spots.size mustEqual 3
        spots.head mustEqual HotSpotInfo(4600.0f, 5600.0f, 64.0f)
        spots(1) mustEqual HotSpotInfo(4700.0f, 5500.0f, 64.0f)
        spots(2) mustEqual HotSpotInfo(4600.0f, 5500.0f, 64.0f)
      case _ =>
        ko
    }
  }

  "encode (clear)" in {
    val msg = HotSpotUpdateMessage(5, 1, Nil)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual stringClear
  }

  "encode (one)" in {
    val msg = HotSpotUpdateMessage(5, 1, List(HotSpotInfo(4700.0f, 2600.0f, 64.0f)))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual stringOne
  }

  "encode (two)" in {
    val msg =
      HotSpotUpdateMessage(5, 5, List(HotSpotInfo(4000.0f, 5400.0f, 64.0f), HotSpotInfo(5500.0f, 2200.0f, 64.0f)))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual stringTwo
  }

  "encode (three)" in {
    val msg = HotSpotUpdateMessage(
      10,
      4,
      List(
        HotSpotInfo(4600.0f, 5600.0f, 64.0f),
        HotSpotInfo(4700.0f, 5500.0f, 64.0f),
        HotSpotInfo(4600.0f, 5500.0f, 64.0f)
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual stringThree
  }
}

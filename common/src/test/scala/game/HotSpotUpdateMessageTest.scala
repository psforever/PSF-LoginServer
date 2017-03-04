// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class HotSpotUpdateMessageTest extends Specification {
  val stringClear = hex"9F 0500 1 00 0"
  val stringOne = hex"9F 0500 1 01 0 00 2E9 00 145 80000 0"
  val stringTwo = hex"9F 0500 5 02 0 00 D07 00 8CA 80000 00 BEA 00 4C4 80000"

  "decode (clear)" in {
    PacketCoding.DecodePacket(stringClear).require match {
      case HotSpotUpdateMessage(continent_guid, unk, spots) =>
        continent_guid mustEqual PlanetSideGUID(5)
        unk mustEqual 1
        spots.size mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (one)" in {
    PacketCoding.DecodePacket(stringOne).require match {
      case HotSpotUpdateMessage(continent_guid, unk, spots) =>
        continent_guid mustEqual PlanetSideGUID(5)
        unk mustEqual 1
        spots.size mustEqual 1
        spots.head.x mustEqual 4700.0f
        spots.head.y mustEqual 2600.0f
        spots.head.scale mustEqual 64.0f
      case _ =>
        ko
    }
  }

  "decode (two)" in {
    PacketCoding.DecodePacket(stringTwo).require match {
      case HotSpotUpdateMessage(continent_guid, unk, spots) =>
        continent_guid mustEqual PlanetSideGUID(5)
        unk mustEqual 5
        spots.size mustEqual 2
        spots.head.x mustEqual 4000.0f
        spots.head.y mustEqual 5400.0f
        spots.head.scale mustEqual 64.0f
        spots(1).x mustEqual 5500.0f
        spots(1).y mustEqual 2200.0f
        spots(1).scale mustEqual 64.0f
      case _ =>
        ko
    }
  }

  "encode (clear)" in {
    val msg = HotSpotUpdateMessage(PlanetSideGUID(5),1)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual stringClear
  }

  "encode (one)" in {
    val msg = HotSpotUpdateMessage(PlanetSideGUID(5),1, HotSpotInfo(4700.0f, 2600.0f, 64.0f)::Nil)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual stringOne
  }

  "encode (two)" in {
    val msg = HotSpotUpdateMessage(PlanetSideGUID(5),5, HotSpotInfo(4000.0f, 5400.0f, 64.0f)::HotSpotInfo(5500.0f, 2200.0f, 64.0f)::Nil)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual stringTwo
  }
}

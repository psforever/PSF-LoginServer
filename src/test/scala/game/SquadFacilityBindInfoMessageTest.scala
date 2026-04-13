// Copyright (c) 2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.SquadFacilityBindInfoMessage
import org.specs2.mutable._
import scodec.bits._

class SquadFacilityBindInfoMessageTest extends Specification {

  private val sample1 = hex"e2 0 38000000 00000000 00000000 0"
  private val sample2 = hex"e2 0 20000000 80000000 38000000 0"
  private val sample3 = hex"e2 0 18000000 48000000 28000000 0"

  "decode sample1" in {
    PacketCoding.decodePacket(sample1).require match {
      case SquadFacilityBindInfoMessage(unk0, squadID, mapID, zoneID) =>
        unk0 mustEqual false
        squadID mustEqual 7
        mapID mustEqual 0
        zoneID mustEqual 0
      case _ =>
        ko
    }
  }

  "encode sample1" in {
    val msg = SquadFacilityBindInfoMessage(
      unk0 = false,
      squadID = 7,
      mapID = 0,
      zoneID = 0
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample1
  }

  "decode sample2" in {
    PacketCoding.decodePacket(sample2).require match {
      case SquadFacilityBindInfoMessage(unk0, squadID, mapID, zoneID) =>
        unk0 mustEqual false
        squadID mustEqual 4
        mapID mustEqual 16
        zoneID mustEqual 7
      case _ =>
        ko
    }
  }

  "encode sample2" in {
    val msg = SquadFacilityBindInfoMessage(
      unk0 = false,
      squadID = 4,
      mapID = 16,
      zoneID = 7
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample2
  }

  "decode sample3" in {
    PacketCoding.decodePacket(sample3).require match {
      case SquadFacilityBindInfoMessage(unk0, squadID, mapID, zoneID) =>
        unk0 mustEqual false
        squadID mustEqual 3
        mapID mustEqual 9
        zoneID mustEqual 5
      case _ =>
        ko
    }
  }

  "encode sample3" in {
    val msg = SquadFacilityBindInfoMessage(
      unk0 = false,
      squadID = 3,
      mapID = 9,
      zoneID = 5
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample3
  }
}

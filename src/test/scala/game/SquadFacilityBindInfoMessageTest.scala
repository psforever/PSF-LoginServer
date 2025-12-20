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
      case SquadFacilityBindInfoMessage(unk0, unk1, unk2, unk3) =>
        unk0 mustEqual false
        unk1 mustEqual 7
        unk2 mustEqual 0
        unk3 mustEqual 0
      case _ =>
        ko
    }
  }

  "encode sample1" in {
    val msg = SquadFacilityBindInfoMessage(
      unk0 = false,
      unk1 = 7,
      unk2 = 0,
      unk3 = 0
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample1
  }

  "decode sample2" in {
    PacketCoding.decodePacket(sample2).require match {
      case SquadFacilityBindInfoMessage(unk0, unk1, unk2, unk3) =>
        unk0 mustEqual false
        unk1 mustEqual 4
        unk2 mustEqual 16
        unk3 mustEqual 7
      case _ =>
        ko
    }
  }

  "encode sample2" in {
    val msg = SquadFacilityBindInfoMessage(
      unk0 = false,
      unk1 = 4,
      unk2 = 16,
      unk3 = 7
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample2
  }

  "decode sample3" in {
    PacketCoding.decodePacket(sample3).require match {
      case SquadFacilityBindInfoMessage(unk0, unk1, unk2, unk3) =>
        unk0 mustEqual false
        unk1 mustEqual 3
        unk2 mustEqual 9
        unk3 mustEqual 5
      case _ =>
        ko
    }
  }

  "encode sample3" in {
    val msg = SquadFacilityBindInfoMessage(
      unk0 = false,
      unk1 = 3,
      unk2 = 9,
      unk3 = 5
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample3
  }
}

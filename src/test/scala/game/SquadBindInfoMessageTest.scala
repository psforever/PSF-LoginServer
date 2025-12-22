// Copyright (c) 2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.SquadBindInfoMessage
import net.psforever.packet.game.SquadBindInfoMessage.SquadBindEntry
import org.specs2.mutable._
import scodec.bits._

class SquadBindInfoMessageTest extends Specification {

  private val sample1 = hex"e0 00000000 04000000 000000000000000000000 08000000000000000000 08000000000000000000 10000000000000000000 0"
  private val sample2 = hex"e0 01000000 06000000 000000000700000007008 08000000000000000000 08000000000000000000 060000000e0000000e01 05000000000000000000 04000000038000000800 4"
  private val sample3 = hex"e0 01000000 08000000 000000000000000000000 08000000080000004004 08000000040000002002 06000000000000000000 04000000000000000000 02800000000000000000 01800000000000000000 00e0000000e0000000e0 1"
  private val sample4 = hex"e0 ffffffff 0a000000 000000000000000000000 08000000000000000000 08000000000000000000 06000000000000000000 04000000070000000c00 82800000038000000700 41800000000000000000 00e00000000000000000 00800000000000000000 00480000003800000070 040"

  "decode sample1" in {
    PacketCoding.decodePacket(sample1).require match {
      case SquadBindInfoMessage(u0, elements) =>
        u0 mustEqual 0
        elements mustEqual Vector(
          SquadBindEntry(0,0,0,false),
          SquadBindEntry(1,0,0,false),
          SquadBindEntry(2,0,0,false),
          SquadBindEntry(8,0,0,false)
        )
      case _ =>
        ko
    }
  }

  "encode sample1" in {
    val msg = SquadBindInfoMessage(
      unk0 = 0,
      elements = Vector(
        SquadBindEntry(0,0,0,false),
        SquadBindEntry(1,0,0,false),
        SquadBindEntry(2,0,0,false),
        SquadBindEntry(8,0,0,false)
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample1
  }

  "decode sample2" in {
    PacketCoding.decodePacket(sample2).require match {
      case SquadBindInfoMessage(u0, elements) =>
        u0 mustEqual 1
        elements mustEqual Vector(
          SquadBindEntry(0,7,7,true),
          SquadBindEntry(1,0,0,false),
          SquadBindEntry(2,0,0,false),
          SquadBindEntry(3,7,7,true),
          SquadBindEntry(5,0,0,false),
          SquadBindEntry(8,7,16,true)
        )
      case _ =>
        ko
    }
  }

  "encode sample2" in {
    val msg = SquadBindInfoMessage(
      unk0 = 1,
      elements = Vector(
        SquadBindEntry(0,7,7,true),
        SquadBindEntry(1,0,0,false),
        SquadBindEntry(2,0,0,false),
        SquadBindEntry(3,7,7,true),
        SquadBindEntry(5,0,0,false),
        SquadBindEntry(8,7,16,true)
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample2
  }

  "decode sample3" in {
    PacketCoding.decodePacket(sample3).require match {
      case SquadBindInfoMessage(u0, elements) =>
        u0 mustEqual 1
        elements mustEqual Vector(
          SquadBindEntry(0,0,0,false),
          SquadBindEntry(1,1,8,true),
          SquadBindEntry(2,1,8,true),
          SquadBindEntry(3,0,0,false),
          SquadBindEntry(4,0,0,false),
          SquadBindEntry(5,0,0,false),
          SquadBindEntry(6,0,0,false),
          SquadBindEntry(7,7,7,true)
        )
      case _ =>
        ko
    }
  }

  "encode sample3" in {
    val msg = SquadBindInfoMessage(
      unk0 = 1,
      elements = Vector(
        SquadBindEntry(0,0,0,false),
        SquadBindEntry(1,1,8,true),
        SquadBindEntry(2,1,8,true),
        SquadBindEntry(3,0,0,false),
        SquadBindEntry(4,0,0,false),
        SquadBindEntry(5,0,0,false),
        SquadBindEntry(6,0,0,false),
        SquadBindEntry(7,7,7,true)
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample3
  }

  "decode sample4" in {
    PacketCoding.decodePacket(sample4).require match {
      case SquadBindInfoMessage(u0, elements) =>
        u0 mustEqual -1
        elements mustEqual Vector(
          SquadBindEntry(0,0,0,false),
          SquadBindEntry(1,0,0,false),
          SquadBindEntry(2,0,0,false),
          SquadBindEntry(3,0,0,false),
          SquadBindEntry(4,7,12,true),
          SquadBindEntry(5,7,14,true),
          SquadBindEntry(6,0,0,false),
          SquadBindEntry(7,0,0,false),
          SquadBindEntry(8,0,0,false),
          SquadBindEntry(9,7,14,true)
        )
      case _ =>
        ko
    }
  }

  "encode sample4" in {
    val msg = SquadBindInfoMessage(
      unk0 = -1,
      elements = Vector(
        SquadBindEntry(0,0,0,false),
        SquadBindEntry(1,0,0,false),
        SquadBindEntry(2,0,0,false),
        SquadBindEntry(3,0,0,false),
        SquadBindEntry(4,7,12,true),
        SquadBindEntry(5,7,14,true),
        SquadBindEntry(6,0,0,false),
        SquadBindEntry(7,0,0,false),
        SquadBindEntry(8,0,0,false),
        SquadBindEntry(9,7,14,true)
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample4
  }
}

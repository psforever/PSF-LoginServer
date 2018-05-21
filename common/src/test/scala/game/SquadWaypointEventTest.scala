// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{SquadWaypointEvent, WaypointEvent}
import net.psforever.types.Vector3
import scodec.bits._

class SquadWaypointEventTest extends Specification {
  val string_1 = hex"84 82c025d9b6c04000"
  val string_2 = hex"84 8280000000000100"
  val string_3 = hex"84 00c03f1e5e808042803f3018f316800008"
  val string_4 = hex"84 40c03f1e5e80804100000000" //fabricated example

  "decode (1)" in {
    PacketCoding.DecodePacket(string_1).require match {
      case SquadWaypointEvent(unk1, unk2, unk3, unk4, unk5, unk6) =>
        unk1 mustEqual 2
        unk2 mustEqual 11
        unk3 mustEqual 31155863L
        unk4 mustEqual 0
        unk5 mustEqual None
        unk6 mustEqual None
      case _ =>
        ko
    }
  }

  "decode (2)" in {
    PacketCoding.DecodePacket(string_2).require match {
      case SquadWaypointEvent(unk1, unk2, unk3, unk4, unk5, unk6) =>
        unk1 mustEqual 2
        unk2 mustEqual 10
        unk3 mustEqual 0L
        unk4 mustEqual 4
        unk5 mustEqual None
        unk6 mustEqual None
      case _ =>
        ko
    }
  }

  "decode (3)" in {
    PacketCoding.DecodePacket(string_3).require match {
      case SquadWaypointEvent(unk1, unk2, unk3, unk4, unk5, unk6) =>
        unk1 mustEqual 0
        unk2 mustEqual 3
        unk3 mustEqual 41581052L
        unk4 mustEqual 1
        unk5 mustEqual None
        unk6 mustEqual Some(WaypointEvent(10, Vector3(3457.9688f, 5514.4688f, 0.0f), 1))
      case _ =>
        ko
    }
  }

  "decode (4)" in {
    PacketCoding.DecodePacket(string_4).require match {
      case SquadWaypointEvent(unk1, unk2, unk3, unk4, unk5, unk6) =>
        unk1 mustEqual 1
        unk2 mustEqual 3
        unk3 mustEqual 41581052L
        unk4 mustEqual 1
        unk5 mustEqual Some(4L)
        unk6 mustEqual None
      case _ =>
        ko
    }
  }

  "encode (1)" in {
    val msg = SquadWaypointEvent(2, 11, 31155863L, 0)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_1
  }

  "encode (2)" in {
    val msg = SquadWaypointEvent(2, 10, 0L, 4)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_2
  }

  "encode (3)" in {
    val msg = SquadWaypointEvent(0, 3, 41581052L, 1, 10, Vector3(3457.9688f, 5514.4688f, 0.0f), 1)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_3
  }

  "encode (4)" in {
    val msg = SquadWaypointEvent(1, 3, 41581052L, 1, 4L)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_4
  }
}

// Copyright (c) 2017 PSForever
package control

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.control.RelatedA
import scodec.bits._

class RelatedATest extends Specification {
  val string0 = hex"00 11 01 04"
  val string1 = hex"00 12 01 04"
  val string2 = hex"00 13 01 04"
  val string3 = hex"00 14 01 04"

  "decode (0)" in {
    PacketCoding.decodePacket(string0).require match {
      case RelatedA(slot, subslot) =>
        slot mustEqual 0
        subslot mustEqual 260
      case _ =>
        ko
    }
  }

  "decode (1)" in {
    PacketCoding.decodePacket(string1).require match {
      case RelatedA(slot, subslot) =>
        slot mustEqual 1
        subslot mustEqual 260
      case _ =>
        ko
    }
  }

  "decode (2)" in {
    PacketCoding.decodePacket(string2).require match {
      case RelatedA(slot, subslot) =>
        slot mustEqual 2
        subslot mustEqual 260
      case _ =>
        ko
    }
  }

  "decode (3)" in {
    PacketCoding.decodePacket(string3).require match {
      case RelatedA(slot, subslot) =>
        slot mustEqual 3
        subslot mustEqual 260
      case _ =>
        ko
    }
  }

  "encode (0)" in {
    val pkt = RelatedA(0, 260)
    val msg = PacketCoding.encodePacket(pkt).require.toByteVector
    msg mustEqual string0
  }

  "encode (1)" in {
    val pkt = RelatedA(1, 260)
    val msg = PacketCoding.encodePacket(pkt).require.toByteVector
    msg mustEqual string1
  }

  "encode (2)" in {
    val pkt = RelatedA(2, 260)
    val msg = PacketCoding.encodePacket(pkt).require.toByteVector
    msg mustEqual string2
  }

  "encode (3)" in {
    val pkt = RelatedA(3, 260)
    val msg = PacketCoding.encodePacket(pkt).require.toByteVector
    msg mustEqual string3
  }

  "encode (n)" in {
    RelatedA(4, 260) must throwA[IllegalArgumentException]
  }
}

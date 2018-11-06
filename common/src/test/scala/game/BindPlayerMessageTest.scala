// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class BindPlayerMessageTest extends Specification {
  val string_standard = hex"16028004000000000000000000000000000000"
  val string_ams = hex"16 05 8440616D73 08 28000000 00000000 00000 00000 0000"
  val string_tech = hex"16 01 8b40746563685f706c616e74 d4 28000000 38000000 00064 012b1 a044"
  val string_akkan = hex"16048440616d7388100000001400000214e171a8e33024"

  "decode (standard)" in {
    PacketCoding.DecodePacket(string_standard).require match {
      case BindPlayerMessage(action, bindDesc, unk1, logging, unk2, unk3, unk4, pos) =>
        action mustEqual 2
        bindDesc mustEqual ""
        unk1 mustEqual false
        logging mustEqual false
        unk2 mustEqual 1
        unk3 mustEqual 0
        unk4 mustEqual 0
        pos mustEqual Vector3.Zero
      case _ =>
        ko
    }
  }

  "decode (ams)" in {
    PacketCoding.DecodePacket(string_ams).require match {
      case BindPlayerMessage(action, bindDesc, unk1, logging, unk2, unk3, unk4, pos) =>
        action mustEqual 5
        bindDesc mustEqual "@ams"
        unk1 mustEqual false
        logging mustEqual false
        unk2 mustEqual 2
        unk3 mustEqual 10
        unk4 mustEqual 0
        pos mustEqual Vector3.Zero
      case _ =>
        ko
    }
  }

  "decode (tech)" in {
    PacketCoding.DecodePacket(string_tech).require match {
      case BindPlayerMessage(action, bindDesc, unk1, logging, unk2, unk3, unk4, pos) =>
        action mustEqual 1
        bindDesc mustEqual "@tech_plant"
        unk1 mustEqual true
        logging mustEqual true
        unk2 mustEqual 5
        unk3 mustEqual 10
        unk4 mustEqual 14
        pos mustEqual Vector3(4610.0f, 6292, 69.625f)
      case _ =>
        ko
    }
  }

  "decode (akkan)" in {
    PacketCoding.DecodePacket(string_akkan).require match {
      case BindPlayerMessage(action, bindDesc, unk1, logging, unk2, unk3, unk4, pos) =>
        action mustEqual 4
        bindDesc mustEqual "@ams"
        unk1 mustEqual true
        logging mustEqual false
        unk2 mustEqual 2
        unk3 mustEqual 4
        unk4 mustEqual 5
        pos mustEqual Vector3(2673.039f, 4423.547f, 39.1875f)
      case _ =>
        ko
    }
  }

  "encode (standard)" in {
    val msg = BindPlayerMessage.Standard
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_standard
  }

  "encode (ams)" in {
    val msg = BindPlayerMessage(5, "@ams", false, false, 2, 10, 0, Vector3.Zero)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_ams
  }

  "encode (tech)" in {
    val msg = BindPlayerMessage(1, "@tech_plant", true, true, 5, 10, 14, Vector3(4610.0f, 6292, 69.625f))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_tech
  }

  "encode (akkan)" in {
    val msg = BindPlayerMessage(4, "@ams", true, false, 2, 4, 5, Vector3(2673.039f, 4423.547f, 39.1875f))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_akkan
  }
}

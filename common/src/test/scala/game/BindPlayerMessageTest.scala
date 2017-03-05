// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class BindPlayerMessageTest extends Specification {
  val string_ams = hex"16 05 8440616D73 08 28000000 00000000 00000 00000 0000"
  val string_tech = hex"16 01 8b40746563685f706c616e74 d4 28000000 38000000 00064 012b1 a044"

  "decode (ams)" in {
    PacketCoding.DecodePacket(string_ams).require match {
      case BindPlayerMessage(action, bindDesc, unk1, logging, unk2, unk3, unk4, pos) =>
        action mustEqual 5
        bindDesc.length mustEqual 4
        bindDesc mustEqual "@ams"
        unk1 mustEqual false
        logging mustEqual false
        unk2 mustEqual 4
        unk3 mustEqual 40
        unk4 mustEqual 0
        pos.x mustEqual 0f
        pos.y mustEqual 0f
        pos.z mustEqual 0f
      case _ =>
        ko
    }
  }

  "decode (tech)" in {
    PacketCoding.DecodePacket(string_tech).require match {
      case BindPlayerMessage(action, bindDesc, unk1, logging, unk2, unk3, unk4, pos) =>
        action mustEqual 1
        bindDesc.length mustEqual 11
        bindDesc mustEqual "@tech_plant"
        unk1 mustEqual true
        logging mustEqual true
        unk2 mustEqual 10
        unk3 mustEqual 40
        unk4 mustEqual 56
        pos.x mustEqual 2060.0f
        pos.y mustEqual 598.0078f
        pos.z mustEqual 274.5f
      case _ =>
        ko
    }
  }

  "encode (ams)" in {
    val msg = BindPlayerMessage(5, "@ams", false, false, 4, 40, 0, Vector3(0, 0, 0))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_ams
  }

  "encode (tech)" in {
    val msg = BindPlayerMessage(1, "@tech_plant", true, true, 10, 40, 56, Vector3(2060.0f, 598.0078f, 274.5f))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_tech
  }

  "standard" in {
    val msg = BindPlayerMessage.STANDARD
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual hex"16028004000000000000000000000000000000"
  }
}

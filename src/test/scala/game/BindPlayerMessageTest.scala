// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{SpawnGroup, Vector3}
import scodec.bits._

class BindPlayerMessageTest extends Specification {
  val string_standard = hex"16028004000000000000000000000000000000"
  val string_ams      = hex"16 05 8440616D73 08 28000000 00000000 00000 00000 0000"
  val string_tech     = hex"16 01 8b40746563685f706c616e74 d4 28000000 38000000 00064 012b1 a044"
  val string_akkan    = hex"16048440616d7388100000001400000214e171a8e33024"

  "decode (standard)" in {
    PacketCoding.decodePacket(string_standard).require match {
      case BindPlayerMessage(action, bindDesc, unk1, logging, unk2, unk3, unk4, pos) =>
        action mustEqual BindStatus.Unbind
        bindDesc mustEqual ""
        unk1 mustEqual false
        logging mustEqual false
        unk2 mustEqual SpawnGroup.BoundAMS
        unk3 mustEqual 0
        unk4 mustEqual 0
        pos mustEqual Vector3.Zero
      case _ =>
        ko
    }
  }

  "decode (ams)" in {
    PacketCoding.decodePacket(string_ams).require match {
      case BindPlayerMessage(action, bindDesc, unk1, logging, unk2, unk3, unk4, pos) =>
        action mustEqual BindStatus.Unavailable
        bindDesc mustEqual "@ams"
        unk1 mustEqual false
        logging mustEqual false
        unk2 mustEqual SpawnGroup.AMS
        unk3 mustEqual 10
        unk4 mustEqual 0
        pos mustEqual Vector3.Zero
      case _ =>
        ko
    }
  }

  "decode (tech)" in {
    PacketCoding.decodePacket(string_tech).require match {
      case BindPlayerMessage(action, bindDesc, unk1, logging, unk2, unk3, unk4, pos) =>
        action mustEqual BindStatus.Bind
        bindDesc mustEqual "@tech_plant"
        unk1 mustEqual true
        logging mustEqual true
        unk2 mustEqual SpawnGroup.BoundFacility
        unk3 mustEqual 10
        unk4 mustEqual 14
        pos mustEqual Vector3(4610.0f, 6292, 69.625f)
      case _ =>
        ko
    }
  }

  "decode (akkan)" in {
    PacketCoding.decodePacket(string_akkan).require match {
      case BindPlayerMessage(action, bindDesc, unk1, logging, unk2, unk3, unk4, pos) =>
        action mustEqual BindStatus.Available
        bindDesc mustEqual "@ams"
        unk1 mustEqual true
        logging mustEqual false
        unk2 mustEqual SpawnGroup.AMS
        unk3 mustEqual 4
        unk4 mustEqual 5
        pos mustEqual Vector3(2673.039f, 4423.547f, 39.1875f)
      case _ =>
        ko
    }
  }

  "encode (standard)" in {
    val msg = BindPlayerMessage.Standard
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_standard
  }

  "encode (ams)" in {
    val msg = BindPlayerMessage(BindStatus.Unavailable, "@ams", false, false, SpawnGroup.AMS, 10, 0, Vector3.Zero)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_ams
  }

  "encode (tech)" in {
    val msg = BindPlayerMessage(
      BindStatus.Bind,
      "@tech_plant",
      true,
      true,
      SpawnGroup.BoundFacility,
      10,
      14,
      Vector3(4610.0f, 6292, 69.625f)
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_tech
  }

  "encode (akkan)" in {
    val msg = BindPlayerMessage(
      BindStatus.Available,
      "@ams",
      true,
      false,
      SpawnGroup.AMS,
      4,
      5,
      Vector3(2673.039f, 4423.547f, 39.1875f)
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_akkan
  }
}

// Copyright (c) 2023 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class OutfitRequestTest extends Specification {
  val string0 = hex"8e02b54f40401780560061006e00750020006f0075007400660069007400200066006f0072002000740068006500200070006c0061006e00650074007300690064006500200066006f00720065007600650072002000700072006f006a006500630074002100200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002d00660069006e00640020006f007500740020006d006f00720065002000610062006f0075007400200074006800650020005000530045004d0055002000700072006f006a0065006300740020006100740020005000530066006f00720065007600650072002e006e0065007400"
  val string2 = hex"8e22b54f405800c000c000c000c000c000c000c000"
  val string4 = hex"8e42b54f404aa0" //faked by modifying the previous example
  val string6 = hex"8e649e822010"
  val string8 = hex"8e81b2cf4050"

  "decode 0" in {
    PacketCoding.decodePacket(string0).require match {
      case OutfitRequest(id, OutfitRequestForm.Unk0(str)) =>
        id mustEqual 41593365L
        str mustEqual "Vanu outfit for the planetside forever project!                                                                                      -find out more about the PSEMU project at PSforever.net"
      case _ =>
        ko
    }
  }

  "decode 1" in {
    PacketCoding.decodePacket(string2).require match {
      case OutfitRequest(id, OutfitRequestForm.Unk1(list)) =>
        id mustEqual 41593365L
        list mustEqual List(Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""))
      case _ =>
        ko
    }
  }

  "decode 2 (fake)" in {
    PacketCoding.decodePacket(string4).require match {
      case OutfitRequest(id, OutfitRequestForm.Unk2(value)) =>
        id mustEqual 41593365L
        value mustEqual 85
      case _ =>
        ko
    }
  }

  "decode 3" in {
    PacketCoding.decodePacket(string6).require match {
      case OutfitRequest(id, OutfitRequestForm.Unk3(value)) =>
        id mustEqual 1176612L
        value mustEqual true
      case _ =>
        ko
    }
  }

  "decode 4" in {
    PacketCoding.decodePacket(string8).require match {
      case OutfitRequest(id, OutfitRequestForm.Unk4(value)) =>
        id mustEqual 41588237L
        value mustEqual true
      case _ =>
        ko
    }
  }

  "encode 0" in {
    val msg = OutfitRequest(41593365L, OutfitRequestForm.Unk0(
      "Vanu outfit for the planetside forever project!                                                                                      -find out more about the PSEMU project at PSforever.net"
    ))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string0
  }

  "encode 1" in {
    val msg = OutfitRequest(41593365L, OutfitRequestForm.Unk1(List(Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""))))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string2
  }

  "encode 2 (fake)" in {
    val msg = OutfitRequest(41593365L, OutfitRequestForm.Unk2(85))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string4
  }

  "encode 3" in {
    val msg = OutfitRequest(1176612L, OutfitRequestForm.Unk3(true))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string6
  }

  "encode 4" in {
    val msg = OutfitRequest(41588237L, OutfitRequestForm.Unk4(true))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string8
  }
}

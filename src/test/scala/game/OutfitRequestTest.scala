// Copyright (c) 2023-2025 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{OutfitRequest, OutfitRequestAction}
import scodec.bits._

class OutfitRequestTest extends Specification {

  val setMotd = hex"8e 02b54f40401780560061006e00750020006f0075007400660069007400200066006f0072002000740068006500200070006c0061006e00650074007300690064006500200066006f00720065007600650072002000700072006f006a006500630074002100200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002000200020002d00660069006e00640020006f007500740020006d006f00720065002000610062006f0075007400200074006800650020005000530045004d0055002000700072006f006a0065006300740020006100740020005000530066006f00720065007600650072002e006e0065007400"
  val setRanks = hex"8e 22b54f405800c000c000c000c000c000c000c000"
  val string4 = hex"8e 42b54f404aa0" //faked by modifying the previous example
  val string6 = hex"8e 649e822010"
  val string8 = hex"8e 81b2cf4050"

  "decode Motd" in {
    PacketCoding.decodePacket(setMotd).require match {
      case OutfitRequest(id, OutfitRequestAction.Motd(str)) =>
        id mustEqual 41593365L
        str mustEqual "Vanu outfit for the planetside forever project!                                                                                      -find out more about the PSEMU project at PSforever.net"
      case _ =>
        ko
    }
  }

  "decode Ranks" in {
    PacketCoding.decodePacket(setRanks).require match {
      case OutfitRequest(id, OutfitRequestAction.Ranks(list)) =>
        id mustEqual 41593365L
        list mustEqual List(Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""))
      case _ =>
        ko
    }
  }

  "decode Unk2 (fake)" in {
    PacketCoding.decodePacket(string4).require match {
      case OutfitRequest(id, OutfitRequestAction.Unk2(value)) =>
        id mustEqual 41593365L
        value mustEqual 85
      case _ =>
        ko
    }
  }

  "decode Unk3" in {
    PacketCoding.decodePacket(string6).require match {
      case OutfitRequest(id, OutfitRequestAction.Unk3(value)) =>
        id mustEqual 1176612L
        value mustEqual true
      case _ =>
        ko
    }
  }

  "decode Unk4" in {
    PacketCoding.decodePacket(string8).require match {
      case OutfitRequest(id, OutfitRequestAction.Unk4(value)) =>
        id mustEqual 41588237L
        value mustEqual true
      case _ =>
        ko
    }
  }

  "encode Motd" in {
    val msg = OutfitRequest(41593365L, OutfitRequestAction.Motd(
      "Vanu outfit for the planetside forever project!                                                                                      -find out more about the PSEMU project at PSforever.net"
    ))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual setMotd
  }

  "encode Ranks" in {
    val msg = OutfitRequest(41593365L, OutfitRequestAction.Ranks(List(Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), Some(""))))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual setRanks
  }

  "encode Unk2 (fake)" in {
    val msg = OutfitRequest(41593365L, OutfitRequestAction.Unk2(85))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string4
  }

  "encode Unk3" in {
    val msg = OutfitRequest(1176612L, OutfitRequestAction.Unk3(true))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string6
  }

  "encode Unk4" in {
    val msg = OutfitRequest(41588237L, OutfitRequestAction.Unk4(true))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string8
  }
}

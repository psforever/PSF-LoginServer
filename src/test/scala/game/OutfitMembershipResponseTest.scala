// Copyright (c) 2023-2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.OutfitMembershipResponse
import net.psforever.packet.game.OutfitMembershipResponse.PacketType
import org.specs2.mutable._
import scodec.bits._

class OutfitMembershipResponseTest extends Specification {

  val createResponse  = hex"8d 0 002b54f404000000010008080"
  val unk1            = hex"8d 2 01bb399e03ddb4f4050a078004e00690063006b009550006c0061006e006500740053006900640065005f0046006f00720065007600650072005f005400520000"
  val unk2            = hex"8d 4 0049b0f4042b54f4051405a006500720067006c0069006e006700390032009750006c0061006e006500740053006900640065005f0046006f00720065007600650072005f00560061006e00750000"
  val unk3            = hex"8d 6 00e8c2f40510d3b6030008080"
  val unk4            = hex"8d 8 002b54f404000000010008080"
  val unk5            = hex"8d a 022b54f4051fb0f4051c05000530046006f0075007400660069007400740065007300740031008080"

  "decode CreateResponse" in {
    PacketCoding.decodePacket(createResponse).require match {
      case OutfitMembershipResponse(packet_type, unk0, unk1, outfit_id, target_id, str1, str2, flag) =>
        packet_type mustEqual PacketType.CreateResponse
        unk0 mustEqual 0
        unk1 mustEqual 0
        outfit_id mustEqual 41593365
        target_id mustEqual 0
        str1 mustEqual ""
        str2 mustEqual ""
        flag mustEqual true
      case _ =>
        ko
    }
  }

  "encode CreateResponse" in {
    val msg = OutfitMembershipResponse(PacketType.CreateResponse, 0, 0, 41593365, 0, "", "", flag = true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual createResponse
  }

  "decode unk1" in {
    PacketCoding.decodePacket(unk1).require match {
      case OutfitMembershipResponse(packet_type, unk0, unk1, outfit_id, target_id, str1, str2, flag) =>
        packet_type mustEqual PacketType.Unk1
        unk0 mustEqual 0
        unk1 mustEqual 0
        outfit_id mustEqual 30383325
        target_id mustEqual 41605870
        str1 mustEqual "xNick"
        str2 mustEqual "PlanetSide_Forever_TR"
        flag mustEqual false
      case _ =>
        ko
    }
  }

  "encode unk1" in {
    val msg = OutfitMembershipResponse(PacketType.Unk1, 0, 0, 30383325, 41605870, "xNick", "PlanetSide_Forever_TR", flag = false)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk1
  }

  "decode unk2" in {
    PacketCoding.decodePacket(unk2).require match {
      case OutfitMembershipResponse(packet_type, unk0, unk1, outfit_id, target_id, str1, str2, flag) =>
        packet_type mustEqual PacketType.Unk2
        unk0 mustEqual 0
        unk1 mustEqual 0
        outfit_id mustEqual 41605156
        target_id mustEqual 41593365
        str1 mustEqual "Zergling92"
        str2 mustEqual "PlanetSide_Forever_Vanu"
        flag mustEqual false
      case _ =>
        ko
    }
  }

  "encode unk2" in {
    val msg = OutfitMembershipResponse(PacketType.Unk2, 0, 0, 41605156, 41593365, "Zergling92", "PlanetSide_Forever_Vanu", flag = false)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk2
  }

  "decode unk3" in {
    PacketCoding.decodePacket(unk3).require match {
      case OutfitMembershipResponse(packet_type, unk0, unk1, outfit_id, target_id, str1, str2, flag) =>
        packet_type mustEqual PacketType.Unk3
        unk0 mustEqual 0
        unk1 mustEqual 0
        outfit_id mustEqual 41574772
        target_id mustEqual 31156616
        str1 mustEqual ""
        str2 mustEqual ""
        flag mustEqual true
      case _ =>
        ko
    }
  }

  "encode unk3" in {
    val msg = OutfitMembershipResponse(PacketType.Unk3, 0, 0, 41574772, 31156616, "", "", flag = true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk3
  }

  "decode unk4" in {
    PacketCoding.decodePacket(unk4).require match {
      case OutfitMembershipResponse(packet_type, unk0, unk1, outfit_id, target_id, str1, str2, flag) =>
        packet_type mustEqual PacketType.Unk4
        unk0 mustEqual 0
        unk1 mustEqual 0
        outfit_id mustEqual 41593365
        target_id mustEqual 0
        str1 mustEqual ""
        str2 mustEqual ""
        flag mustEqual true
      case _ =>
        ko
    }
  }

  "encode unk4" in {
    val msg = OutfitMembershipResponse(PacketType.Unk4, 0, 0, 41593365, 0, "", "", flag = true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk4
  }

  "decode unk5" in {
    PacketCoding.decodePacket(unk5).require match {
      case OutfitMembershipResponse(packet_type, unk0, unk1, outfit_id, target_id, str1, str2, flag) =>
        packet_type mustEqual PacketType.Unk5
        unk0 mustEqual 0
        unk1 mustEqual 1
        outfit_id mustEqual 41593365
        target_id mustEqual 41605263
        str1 mustEqual "PSFoutfittest1"
        str2 mustEqual ""
        flag mustEqual true
      case _ =>
        ko
    }
  }

  "encode unk5" in {
    val msg = OutfitMembershipResponse(PacketType.Unk5, 0, 1, 41593365, 41605263, "PSFoutfittest1", "", flag = true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk5
  }
}

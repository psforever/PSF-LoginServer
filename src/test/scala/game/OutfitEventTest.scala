// Copyright (c) 2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.OutfitEvent.RequestType
import net.psforever.packet.game.OutfitEventAction._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import org.specs2.mutable._
import scodec.bits._

class OutfitEventTest extends Specification {
  val unk0_ABC: ByteVector = ByteVector.fromValidHex(
    "8f 1 a8c2 0001" + // packet head
    "2a 0 42006c00610063006b002000410072006d006f0072006500640020005200650061007000650072007300" + // Black Armored Reapers
    "1d9c4d0d" +
    "1d9c4d0d" +
    "ab00 0000" +
    "88 44006f00670020004d00650061007400" + // Dog Meat
    "87 5200750073007300690061006e00" + // Russian
    "80" + //
    "80" + //
    "8d 5300710075006100640020004c00650061006400650072007300" + // Squad Leaders
    "91 41006300740069006e006700200043006f006d006d0061006e006400650072007300" + // Acting Commanders
    "87 5200650061007000650072007300" + // Reapers
    "80" + //
    "00" +
    "9c 5c0023003000300030003000660066004d0075006d0062006c00650020005c00230030003000330033006600660049006e0066006f0020005c0023003000300036003600660066006900730020005c0023003000300039003900660066007400680065006d006f006f00730065002e00740079007000650066007200610067002e0063006f006d0020005c00230030003000630063006600660070006f007200740020005c002300300030006600660066006600390033003500300020005c0023003000300063006300660066006a006f0069006e0020005c0023003000300039003900660066006900740020005c0023003000300036003600660066006f00720020005c0023003000300033003300660066006200650020005c0023003000300030003000660066006b00690063006b00650064002e00" +
    "0f80" +
    "0000 00737296 24000000 00000000 00000000 0000")
  val unk1_ABC: ByteVector = hex"8f 2 302a 10 00 0"
  val unk2_ABC: ByteVector = ByteVector.fromValidHex(
    "8f 4 0201 feff" +
      "2e 0 50006c0061006e006500740053006900640065005f0046006f00720065007600650072005f00560061006e007500" + // PlanetSide_Forever_Vanu
      "00000000" +
      "00000000" +
      "0100 0000" +
      "80" +
      "80" +
      "80" +
      "80" +
      "80" +
      "80" +
      "80" +
      "80" +
      "80" +
      "0070" +
      "4982 00000000 00000000 00000000 00000000 0000")
  val unk3_ABC: ByteVector = hex"8f 6 0201 fe fe 0"
  val unk4_ABC: ByteVector = hex"8f 8 0201 fefe a02a 1000 0"
  val unk5_ABC: ByteVector = hex"8f a 0201 fefe 0400 0000 0"

  "decode Unk0 ABC" in {
    PacketCoding.decodePacket(unk0_ABC).require match {
      case OutfitEvent(request_type, outfit_guid, action) =>
        request_type mustEqual RequestType.Unk0
        outfit_guid mustEqual PlanetSideGUID(25044)
        action mustEqual Unk0(
          OutfitInfo(
            unk1 = 0,
            unk2 = 0,
            outfit_name = "Black Armored Reapers",
            unk6 = 223190045,
            unk7 = 223190045,
            member_count = 171,
            unk9 = 0,
            OutfitRankNames("Dog Meat","Russian","","","Squad Leaders","Acting Commanders","Reapers",""),
            "\\#0000ffMumble \\#0033ffInfo \\#0066ffis \\#0099ffthemoose.typefrag.com \\#00ccffport \\#00ffff9350 \\#00ccffjoin \\#0099ffit \\#0066ffor \\#0033ffbe \\#0000ffkicked.",
            PlanetSideGUID(32783),
            0,
            0,
            0,
            1210901990,
            0,
            0,
            0,
            0,
          )
        )
      case _ =>
        ko
    }
  }

  "encode Unk0 ABC" in {
    val msg = OutfitEvent(
      RequestType.Unk0,
      PlanetSideGUID(25044),
      Unk0(
        OutfitInfo(
          unk1 = 0,
          unk2 = 0,
          outfit_name = "Black Armored Reapers",
          unk6 = 223190045,
          unk7 = 223190045,
          member_count = 171,
          unk9 = 0,
          OutfitRankNames("Dog Meat","Russian","","","Squad Leaders","Acting Commanders","Reapers",""),
          "\\#0000ffMumble \\#0033ffInfo \\#0066ffis \\#0099ffthemoose.typefrag.com \\#00ccffport \\#00ffff9350 \\#00ccffjoin \\#0099ffit \\#0066ffor \\#0033ffbe \\#0000ffkicked.",
          PlanetSideGUID(32783),
          0,
          0,
          0,
          1210901990,
          0,
          0,
          0,
          0,
        )
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk0_ABC
  }

  "decode Unk1 ABC" in {
    PacketCoding.decodePacket(unk1_ABC).require match {
      case OutfitEvent(request_type, outfit_guid, action) =>
        request_type mustEqual RequestType.Unk1
        outfit_guid mustEqual PlanetSideGUID(5400)
        action mustEqual Unk1(unk0 = 8, unk1 = 0, unk2 = 0, unk3 = false)
      case _ =>
        ko
    }
  }

  "encode Unk1 ABC" in {
    val msg = OutfitEvent(
      RequestType.Unk1,
      PlanetSideGUID(5400),
      Unk1(
        unk0 = 8,
        unk1 = 0,
        unk2 = 0,
        unk3 = false,
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk1_ABC
  }

  "decode Unk2 ABC" in {
    PacketCoding.decodePacket(unk2_ABC).require match {
      case OutfitEvent(request_type, outfit_guid, action) =>
        request_type mustEqual RequestType.Unk2
        outfit_guid mustEqual PlanetSideGUID(1)
        action mustEqual Unk2(OutfitInfo(unk1 = 255, unk2 = 127, outfit_name = "PlanetSide_Forever_Vanu",
          unk6 = 0, unk7 = 0, member_count = 1, unk9 = 0, OutfitRankNames("","","","","","","",""),
          "", PlanetSideGUID(28672), 33353, 0, 0, 0, 0, 0, 0, 0))
      case _ =>
        ko
    }
  }

  "encode Unk2 ABC" in {
    val msg = OutfitEvent(
      RequestType.Unk2,
      PlanetSideGUID(1),
      Unk2(
        OutfitInfo(
          unk1 = 255,
          unk2 = 127,
          outfit_name = "PlanetSide_Forever_Vanu",
          unk6 = 0,
          unk7 = 0,
          member_count = 1,
          unk9 = 0,
          OutfitRankNames("","","","","","","",""),
          "",
          PlanetSideGUID(28672),
          33353,
          0,
          0,
          0,
          0,
          0,
          0,
          0,
        )
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk2_ABC
  }

  "decode Unk3 ABC" in {
    PacketCoding.decodePacket(unk3_ABC).require match {
      case OutfitEvent(request_type, outfit_guid, action) =>
        request_type mustEqual RequestType.Unk3
        outfit_guid mustEqual PlanetSideGUID(1)
        action mustEqual Unk3(
          unk0 = 255,
          unk1 = 127,
          unk2 = 0,
          unk3 = false,
          BitVector.fromValidHex("")
        )
      case _ =>
        ko
    }
  }

  "encode Unk3 ABC" in {
    val msg = OutfitEvent(
      RequestType.Unk3,
      PlanetSideGUID(1),
      Unk3(
        unk0 = 255,
        unk1 = 127,
        unk2 = 0,
        unk3 = false,
        BitVector.fromValidHex("")
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk3_ABC
  }

  "decode Unk4 ABC" in {
    PacketCoding.decodePacket(unk4_ABC).require match {
      case OutfitEvent(request_type, outfit_guid, action) =>
        request_type mustEqual RequestType.Unk4
        outfit_guid mustEqual PlanetSideGUID(1)
        action mustEqual Unk4(
          unk0 = 32767,
          unk1 = 5456,
          unk2 = 8,
          0,
          unk4 = false,
          BitVector.fromValidHex("")
        )
      case _ =>
        ko
    }
  }

  "encode Unk4 ABC" in {
    val msg = OutfitEvent(
      RequestType.Unk4,
      PlanetSideGUID(1),
      Unk4(
        unk0 = 32767,
        unk1 = 5456,
        unk2 = 8,
        unk3 = 0,
        unk4 = false,
        BitVector.fromValidHex("")
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk4_ABC
  }

  "decode Unk5 ABC" in {
    PacketCoding.decodePacket(unk5_ABC).require match {
      case OutfitEvent(request_type, outfit_guid, action) =>
        request_type mustEqual RequestType.Unk5
        outfit_guid mustEqual PlanetSideGUID(1)
        action mustEqual Unk5(
          unk0 = 32767,
          unk1 = 2,
          unk2 = 0,
          unk3 = 0,
          unk4 = false,
          BitVector.fromValidHex("") // OR f88c2a0417c1a06101001f20f4b8c00000404090ac9c6745dea88cadf0f810e03e0200f92 with bool at the back
        )
      case _ =>
        ko
    }
  }

  "encode Unk5 ABC" in {
    val msg = OutfitEvent(
      RequestType.Unk5,
      PlanetSideGUID(1),
      Unk5(
        unk0 = 32767,
        unk1 = 2,
        unk2 = 0,
        unk3 = 0,
        unk4 = false,
        BitVector.fromValidHex("")
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk5_ABC
  }
}

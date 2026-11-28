// Copyright (c) 2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.packets.OutfitEventAction._
import net.psforever.packet.game.packets.OutfitEvent
import org.specs2.mutable._
import scodec.bits._

class OutfitEventTest extends Specification {

  val initial: ByteVector = ByteVector.fromValidHex(
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
  val listEnd: ByteVector = hex"8f 2 302a 10 00 0"
  val update: ByteVector = ByteVector.fromValidHex(
    "8f 4 0201feff" +
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
  val leaving: ByteVector = hex"8f 6 0201 fe fe 0"
  val updateOutfitId: ByteVector = hex"8f 8 0201 fefe a02a 1000 0"
  val updateMemberCount: ByteVector = hex"8f a 0201 fefe 0400 0000 0"
  val formationPersistedNotify: ByteVector =  hex"8f d 3ba80e000"
  val membershipPersistedNotify: ByteVector = hex"8f f 3ba80e000"

  "decode Initial" in {
    PacketCoding.decodePacket(initial).require match {
      case OutfitEvent(outfit_guid, action) =>
        outfit_guid mustEqual 25044
        action mustEqual Initial(
          OutfitInfo(
            outfit_name = "Black Armored Reapers",
            outfit_points1 = 223190045,
            outfit_points2 = 223190045,
            member_count = 171,
            OutfitRankNames("Dog Meat","Russian","","","Squad Leaders","Acting Commanders","Reapers",""),
            "\\#0000ffMumble \\#0033ffInfo \\#0066ffis \\#0099ffthemoose.typefrag.com \\#00ccffport \\#00ffff9350 \\#00ccffjoin \\#0099ffit \\#0066ffor \\#0033ffbe \\#0000ffkicked.",
            15,
            formation_completed = true,
            formation_deadline = 0,
            created_timestamp = 1210901990,
            0,
            0,
            0,
          )
        )
      case _ =>
        ko
    }
  }

  "encode Initial" in {
    val msg = OutfitEvent(
      25044,
      Initial(
        OutfitInfo(
          outfit_name = "Black Armored Reapers",
          outfit_points1 = 223190045,
          outfit_points2 = 223190045,
          member_count = 171,
          OutfitRankNames("Dog Meat","Russian","","","Squad Leaders","Acting Commanders","Reapers",""),
          "\\#0000ffMumble \\#0033ffInfo \\#0066ffis \\#0099ffthemoose.typefrag.com \\#00ccffport \\#00ffff9350 \\#00ccffjoin \\#0099ffit \\#0066ffor \\#0033ffbe \\#0000ffkicked.",
          15,
          formation_completed = true,
          formation_deadline = 0,
          created_timestamp = 1210901990,
          0,
          0,
          0,
        )
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual initial
  }

  "decode ListEnd" in {
    PacketCoding.decodePacket(listEnd).require match {
      case OutfitEvent(outfit_guid, action) =>
        outfit_guid mustEqual 529688L
        action mustEqual ListEnd()
      case _ =>
        ko
    }
  }

  "encode ListEnd" in {
    val msg = OutfitEvent(
      529688L,
      ListEnd()
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual listEnd
  }

  "decode Update" in {
    PacketCoding.decodePacket(update).require match {
      case OutfitEvent(outfit_guid, action) =>
        outfit_guid mustEqual 2147418113L
        action mustEqual Update(OutfitInfo(
          outfit_name = "PlanetSide_Forever_Vanu",
          outfit_points1 = 0,
          outfit_points2 = 0,
          member_count = 1,
          OutfitRankNames("","","","","","","",""),
          "",
          0,
          formation_completed = false,
          formation_deadline = 300000,
          created_timestamp = 0,
          0,
          0,
          0,
        ))
      case _ =>
        ko
    }
  }

  "encode Update" in {
    val msg = OutfitEvent(
      2147418113L,
      Update(
        OutfitInfo(
          outfit_name = "PlanetSide_Forever_Vanu",
          outfit_points1 = 0,
          outfit_points2 = 0,
          member_count = 1,
          OutfitRankNames("","","","","","","",""),
          "",
          0,
          formation_completed = false,
          formation_deadline = 300000,
          created_timestamp = 0,
          0,
          0,
          0,
        )
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual update
  }

  "decode Leaving" in {
    PacketCoding.decodePacket(leaving).require match {
      case OutfitEvent(outfit_guid, action) =>
        outfit_guid mustEqual 2147418113L
        action mustEqual Leaving()
      case _ =>
        ko
    }
  }

  "encode Leaving" in {
    val msg = OutfitEvent(
      2147418113L,
      Leaving()
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual leaving
  }

  "decode UpdateOutfitId" in {
    PacketCoding.decodePacket(updateOutfitId).require match {
      case OutfitEvent(outfit_guid, action) =>
        outfit_guid mustEqual 2147418113L
        action mustEqual UpdateOutfitId(
          new_outfit_id = 529744L,
        )
      case _ =>
        ko
    }
  }

  "encode UpdateOutfitId" in {
    val msg = OutfitEvent(
      2147418113L,
      UpdateOutfitId(
        new_outfit_id = 529744L,
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual updateOutfitId
  }

  "decode UpdateMemberCount" in {
    PacketCoding.decodePacket(updateMemberCount).require match {
      case OutfitEvent(outfit_guid, action) =>
        outfit_guid mustEqual 2147418113L
        action mustEqual UpdateMemberCount(
          member_count = 2,
        )
      case _ =>
        ko
    }
  }

  "encode UpdateMemberCount" in {
    val msg = OutfitEvent(
      2147418113L,
      UpdateMemberCount(
        member_count = 2,
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual updateMemberCount
  }

  "decode FormationPersistedNotify" in {
    PacketCoding.decodePacket(formationPersistedNotify).require match {
      case OutfitEvent(outfit_guid, action) =>
        outfit_guid mustEqual 513181L
        action mustEqual FormationPersistedNotify()
      case _ =>
        ko
    }
  }

  "encode FormationPersistedNotify" in {
    val msg = OutfitEvent(
      513181L,
      FormationPersistedNotify()
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual formationPersistedNotify
  }

  "decode MembershipPersistedNotify" in {
    PacketCoding.decodePacket(membershipPersistedNotify).require match {
      case OutfitEvent(outfit_guid, action) =>
        outfit_guid mustEqual 513181L
        action mustEqual MembershipPersistedNotify()
      case _ =>
        ko
    }
  }

  "encode MembershipPersistedNotify" in {
    val msg = OutfitEvent(
      513181L,
      MembershipPersistedNotify()
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual membershipPersistedNotify
  }
}

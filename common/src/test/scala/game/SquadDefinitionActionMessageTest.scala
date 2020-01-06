// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.SquadAction._
import net.psforever.packet.game._
import net.psforever.types.{CertificationType, PlanetSideGUID}
import scodec.bits._

class SquadDefinitionActionMessageTest extends Specification {
  //local test data; note that the second field - unk1 - is always blank for now, but that probably changes
  val string_00 = hex"e7 00 0c0000" //guid: 3
  val string_03 = hex"E7 0c 0000c0" //index: 3
  val string_04 = hex"E7 10 0000c0" //index: 3
  val string_07 = hex"e7 1c 0000e68043006f0070007300200061006e00640020004d0069006c006900740061007200790020004f006600660069006300650072007300"
  val string_08 = hex"E7 20 000000"
  val string_10 = hex"E7 28 000004" //index: 1
  val string_19 = hex"E7 4c 0000218041002d005400650061006d00" //"A-Team"
  val string_20 = hex"E7 50 0000004000"
  val string_21 = hex"E7 54 000008" //index: 2
  val string_22 = hex"E7 58 000008" //index: 2
  val string_23 = hex"E7 5c 0000061842004c00550046004f005200" //"BLUFOR", index: 1
  val string_24 = hex"E7 60 000006386b0069006c006c002000620061006400200064007500640065007300" //"kill bad dudes", index: 1
  val string_25 = hex"E7 64 000004400000800000" //"Anti-Vehicular" (former), "Infiltration Suit" (latter), index: 1
  val string_26 = hex"E7 68 000000"
  val string_28 = hex"E7 70 000020" //On
  val string_31 = hex"E7 7c 000020" //On
  val string_34a = hex"E7 88 00002180420061006400610073007300000000000000040000" //"Badass", Solsar, Any matching position
  val string_34b = hex"E7 88 00002180420061006400610073007300000000000000080000" //"Badass", Hossin, Any matching position
  val string_34c = hex"E7 88 00002180420061006400610073007300000000000000080080" //"Badass", Hossin, Any position
  val string_34d = hex"E7 88 00002180420061006400610073007300100000200000080100" //"Badass", Hossin, Some("Anti-Vehicular", "Infiltration Suit")
  val string_34e = hex"E7 88 00002180420061006400610073007300100000200000080180" //"Badass", Hossin, All("Anti-Vehicular", "Infiltration Suit")
  val string_35 = hex"E7 8c 000000"
  val string_40 = hex"E7 a0 000004" //index: 1
  val string_41 = hex"E7 a4 000000"

  val string_43 = hex"e7 ac 000000"
  val string_failure = hex"E7 ff"

  "decode (00)" in {
    PacketCoding.DecodePacket(string_00).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(3)
        unk2 mustEqual 0
        action mustEqual DisplaySquad()
      case _ =>
        ko
    }
  }

  "decode (03)" in {
    PacketCoding.DecodePacket(string_03).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 3
        action mustEqual SaveSquadFavorite()
      case _ =>
        ko
    }
  }

  "decode (03)" in {
    PacketCoding.DecodePacket(string_04).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 3
        action mustEqual LoadSquadFavorite()
      case _ =>
        ko
    }
  }

  "decode (07)" in {
    PacketCoding.DecodePacket(string_07).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 3
        action mustEqual ListSquadFavorite("Cops and Military Officers")
      case _ =>
        ko
    }
  }

  "decode (08)" in {
    PacketCoding.DecodePacket(string_08).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual RequestListSquad()
      case _ =>
        ko
    }
  }

  "decode (10)" in {
    PacketCoding.DecodePacket(string_10).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual SelectRoleForYourself(1)
      case _ =>
        ko
    }
  }

  "decode (19)" in {
    PacketCoding.DecodePacket(string_19).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual ChangeSquadPurpose("A-Team")
      case _ =>
        ko
    }
  }

  "decode (20)" in {
    PacketCoding.DecodePacket(string_20).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual ChangeSquadZone(PlanetSideZoneID(1))
      case _ =>
        ko
    }
  }

  "decode (21)" in {
    PacketCoding.DecodePacket(string_21).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual CloseSquadMemberPosition(2)
      case _ =>
        ko
    }
  }

  "decode (22)" in {
    PacketCoding.DecodePacket(string_22).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual AddSquadMemberPosition(2)
      case _ =>
        ko
    }
  }

  "decode (23)" in {
    PacketCoding.DecodePacket(string_23).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual ChangeSquadMemberRequirementsRole(1, "BLUFOR")
      case _ =>
        ko
    }
  }

  "decode (24)" in {
    PacketCoding.DecodePacket(string_24).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual ChangeSquadMemberRequirementsDetailedOrders(1, "kill bad dudes")
      case _ =>
        ko
    }
  }

  "decode (25)" in {
    PacketCoding.DecodePacket(string_25).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual ChangeSquadMemberRequirementsCertifications(
          1,
          Set(CertificationType.AntiVehicular, CertificationType.InfiltrationSuit)
        )
      case _ =>
        ko
    }
  }

  "decode (26)" in {
    PacketCoding.DecodePacket(string_26).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual ResetAll()
      case _ =>
        ko
    }
  }

  "decode (28)" in {
    PacketCoding.DecodePacket(string_28).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual AutoApproveInvitationRequests(true)
      case _ =>
        ko
    }
  }

  "decode (31)" in {
    PacketCoding.DecodePacket(string_31).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual LocationFollowsSquadLead(true)
      case _ =>
        ko
    }
  }

  "decode (34a)" in {
    PacketCoding.DecodePacket(string_34a).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual SearchForSquadsWithParticularRole("Badass", Set(), 1, SearchMode.AnyPositions)
      case _ =>
        ko
    }
  }

  "decode (34b)" in {
    PacketCoding.DecodePacket(string_34b).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual SearchForSquadsWithParticularRole("Badass", Set(), 2, SearchMode.AnyPositions)
      case _ =>
        ko
    }
  }

  "decode (34c)" in {
    PacketCoding.DecodePacket(string_34c).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual SearchForSquadsWithParticularRole("Badass", Set(), 2, SearchMode.AvailablePositions)
      case _ =>
        ko
    }
  }

  "decode (34d)" in {
    PacketCoding.DecodePacket(string_34d).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual SearchForSquadsWithParticularRole("Badass", Set(CertificationType.InfiltrationSuit, CertificationType.AntiVehicular), 2, SearchMode.SomeCertifications)
      case _ =>
        ko
    }
  }

  "decode (34e)" in {
    PacketCoding.DecodePacket(string_34e).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual SearchForSquadsWithParticularRole("Badass", Set(CertificationType.InfiltrationSuit, CertificationType.AntiVehicular), 2, SearchMode.AllCertifications)
      case _ =>
        ko
    }
  }

  "decode (35)" in {
    PacketCoding.DecodePacket(string_35).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual CancelSquadSearch()
      case _ =>
        ko
    }
  }

  "decode (40)" in {
    PacketCoding.DecodePacket(string_40).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual FindLfsSoldiersForRole(1)
      case _ =>
        ko
    }
  }

  "decode (41)" in {
    PacketCoding.DecodePacket(string_41).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual CancelFind()
      case _ =>
        ko
    }
  }

  "decode (43, unknown)" in {
    PacketCoding.DecodePacket(string_43).require match {
      case SquadDefinitionActionMessage(unk1, unk2, action) =>
        unk1 mustEqual PlanetSideGUID(0)
        unk2 mustEqual 0
        action mustEqual Unknown(43, hex"00".toBitVector.take(6))
      case _ =>
        ko
    }
  }

  "decode (failure)" in {
    PacketCoding.DecodePacket(string_failure).isFailure mustEqual true
  }

  "encode (00)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(3), 0, DisplaySquad())
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_00
  }

  "encode (03)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 3, SaveSquadFavorite())
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_03
  }

  "encode (03)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 3, LoadSquadFavorite())
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_04
  }

  "encode (07)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 3, ListSquadFavorite("Cops and Military Officers"))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_07
  }

  "encode (08)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, RequestListSquad())
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_08
  }

  "encode (10)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SelectRoleForYourself(1))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_10
  }

  "encode (19)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, ChangeSquadPurpose("A-Team"))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_19
  }

  "encode (20)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, ChangeSquadZone(PlanetSideZoneID(1)))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_20
  }

  "encode (21)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, CloseSquadMemberPosition(2))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_21
  }

  "encode (22)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, AddSquadMemberPosition(2))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_22
  }

  "encode (23)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, ChangeSquadMemberRequirementsRole(1, "BLUFOR"))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_23
  }

  "encode (24)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, ChangeSquadMemberRequirementsDetailedOrders(1, "kill bad dudes"))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_24
  }

  "encode (25)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, ChangeSquadMemberRequirementsCertifications(
      1,
      Set(CertificationType.AntiVehicular, CertificationType.InfiltrationSuit)
    ))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_25
  }

  "encode (26)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, ResetAll())
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_26
  }

  "encode (28)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, AutoApproveInvitationRequests(true))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_28
  }

  "encode (31)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, LocationFollowsSquadLead(true))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_31
  }

  "encode (34a)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SearchForSquadsWithParticularRole("Badass", Set(), 1, SearchMode.AnyPositions))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_34a
  }

  "encode (34b)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SearchForSquadsWithParticularRole("Badass", Set(), 2, SearchMode.AnyPositions))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_34b
  }

  "encode (34c)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SearchForSquadsWithParticularRole("Badass", Set(), 2, SearchMode.AvailablePositions))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_34c
  }

  "encode (34d)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SearchForSquadsWithParticularRole("Badass", Set(CertificationType.InfiltrationSuit, CertificationType.AntiVehicular), 2, SearchMode.SomeCertifications))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_34d
  }

  "encode (34e)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SearchForSquadsWithParticularRole("Badass", Set(CertificationType.InfiltrationSuit, CertificationType.AntiVehicular), 2, SearchMode.AllCertifications))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_34e
  }

  "encode (35)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, CancelSquadSearch())
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_35
  }

  "encode (40)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, FindLfsSoldiersForRole(1))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_40
  }

  "encode (41)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, CancelFind())
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_41
  }

  "encode (43, unknown)" in {
    val msg = SquadDefinitionActionMessage(PlanetSideGUID(0), 0, Unknown(43, BitVector.empty))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_43
  }
}

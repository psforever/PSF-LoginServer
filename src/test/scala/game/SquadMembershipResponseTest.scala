// Copyright (c) 2019 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.SquadResponseType
import org.specs2.mutable._
import scodec.bits._

class SquadMembershipResponseTest extends Specification {
  val string_01 = hex"6f0 00854518050db2260108048006f006600440000"
  val string_02 = hex"6f0 0049e8220112aa1e01100530050004f0049004c0045005200530080"
  val string_11 = hex"6f1 995364f2040000000100080"
  val string_12 = hex"6f1 90cadcf4040000000100080"
  val string_21 = hex"6f2 010db2260085451805140560069007200750073004700690076006500720080"
  val string_22 = hex"6f2 010db22601da03aa03140560069007200750073004700690076006500720080"
  val string_31 = hex"6f3 07631db202854518050a048004d0046004900430000"
  val string_32 = hex"6f3 04c34fb402854518050e0440041004e00310031003100310000"
  val string_41 = hex"6f4 04cadcf405bbbef405140530041007200610069007300560061006e00750000"
  val string_42 = hex"6f4 05c9c0f405d71aec0516041006900720049006e006a006500630074006f00720000"
  val string_51 = hex"6f5 0249e8220049e822010e0430043005200490044004500520080"
  val string_71 = hex"6f7 1049e822000000000100080"
  val string_72 = hex"6f7 00cadcf4041355ae03100570069007a006b00690064003400350080"
  val string_81 = hex"6f8 001355ae02cadcf405100570069007a006b00690064003400350000"
  val string_91 = hex"6f9 008310080115aef40500080"
  val string_92 = hex"6f9 001355ae02cadcf405100570069007a006b00690064003400350000"
  val string_b1 = hex"6fb 021355ae02cadcf405140530041007200610069007300560061006e00750000"

  "SquadMembershipResponse" should {
    "decode (0-1)" in {
      PacketCoding.decodePacket(string_01).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.Invite
          unk2 mustEqual 0
          unk3 mustEqual 0
          unk4 mustEqual 42771010L
          unk5.contains(1300870L) mustEqual true
          unk6 mustEqual "HofD"
          unk7 mustEqual false
          unk8.isEmpty mustEqual true
        case _ =>
          ko
      }
    }

    "decode (0-2)" in {
      PacketCoding.decodePacket(string_02).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.Invite
          unk2 mustEqual 0
          unk3 mustEqual 0
          unk4 mustEqual 1176612L
          unk5.contains(1004937L) mustEqual true
          unk6 mustEqual "SPOILERS"
          unk7 mustEqual true
          unk8.isEmpty mustEqual true
        case _ =>
          ko
      }
    }

    "decode (1-1)" in {
      PacketCoding.decodePacket(string_11).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.Unk01
          unk2 mustEqual 19
          unk3 mustEqual 0
          unk4 mustEqual 41530025L
          unk5.contains(0L) mustEqual true
          unk6 mustEqual ""
          unk7 mustEqual true
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (1-2)" in {
      PacketCoding.decodePacket(string_12).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.Unk01
          unk2 mustEqual 18
          unk3 mustEqual 0
          unk4 mustEqual 41578085L
          unk5.contains(0L) mustEqual true
          unk6 mustEqual ""
          unk7 mustEqual true
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (2-1)" in {
      PacketCoding.decodePacket(string_21).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.Accept
          unk2 mustEqual 0
          unk3 mustEqual 0
          unk4 mustEqual 1300870L
          unk5.contains(42771010L) mustEqual true
          unk6 mustEqual "VirusGiver"
          unk7 mustEqual true
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (2-2)" in {
      PacketCoding.decodePacket(string_22).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.Accept
          unk2 mustEqual 0
          unk3 mustEqual 0
          unk4 mustEqual 1300870L
          unk5.contains(30736877L) mustEqual true
          unk6 mustEqual "VirusGiver"
          unk7 mustEqual true
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (3-1)" in {
      PacketCoding.decodePacket(string_31).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.Reject
          unk2 mustEqual 0
          unk3 mustEqual 3
          unk4 mustEqual 31035057L
          unk5.contains(42771010L) mustEqual true
          unk6 mustEqual "HMFIC"
          unk7 mustEqual false
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (3-2)" in {
      PacketCoding.decodePacket(string_32).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.Reject
          unk2 mustEqual 0
          unk3 mustEqual 2
          unk4 mustEqual 31106913L
          unk5.contains(42771010L) mustEqual true
          unk6 mustEqual "DAN1111"
          unk7 mustEqual false
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (4-1)" in {
      PacketCoding.decodePacket(string_41).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.Cancel
          unk2 mustEqual 0
          unk3 mustEqual 2
          unk4 mustEqual 41578085L
          unk5.contains(41607133L) mustEqual true
          unk6 mustEqual "SAraisVanu"
          unk7 mustEqual false
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (4-2)" in {
      PacketCoding.decodePacket(string_42).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.Cancel
          unk2 mustEqual 0
          unk3 mustEqual 2
          unk4 mustEqual 41607396L
          unk5.contains(41324011L) mustEqual true
          unk6 mustEqual "AirInjector"
          unk7 mustEqual false
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (5-1)" in {
      PacketCoding.decodePacket(string_51).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.Leave
          unk2 mustEqual 0
          unk3 mustEqual 1
          unk4 mustEqual 1176612L
          unk5.contains(1176612L) mustEqual true
          unk6 mustEqual "CCRIDER"
          unk7 mustEqual true
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (7-1)" in {
      PacketCoding.decodePacket(string_71).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.PlatoonInvite
          unk2 mustEqual 2
          unk3 mustEqual 0
          unk4 mustEqual 1176612L
          unk5.contains(0L) mustEqual true
          unk6 mustEqual ""
          unk7 mustEqual true
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (7-2)" in {
      PacketCoding.decodePacket(string_72).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.PlatoonInvite
          unk2 mustEqual 0
          unk3 mustEqual 0
          unk4 mustEqual 41578085L
          unk5.contains(30910985L) mustEqual true
          unk6 mustEqual "Wizkid45"
          unk7 mustEqual true
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (8-1)" in {
      PacketCoding.decodePacket(string_81).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.PlatoonAccept
          unk2 mustEqual 0
          unk3 mustEqual 0
          unk4 mustEqual 30910985L
          unk5.contains(41578085L) mustEqual true
          unk6 mustEqual "Wizkid45"
          unk7 mustEqual false
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (9-1)" in {
      PacketCoding.decodePacket(string_91).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.PlatoonReject
          unk2 mustEqual 0
          unk3 mustEqual 0
          unk4 mustEqual 297025L
          unk5.contains(41605002L) mustEqual true
          unk6 mustEqual ""
          unk7 mustEqual true
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (9-2)" in {
      PacketCoding.decodePacket(string_92).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.PlatoonReject
          unk2 mustEqual 0
          unk3 mustEqual 0
          unk4 mustEqual 30910985L
          unk5.contains(41578085L) mustEqual true
          unk6 mustEqual "Wizkid45"
          unk7 mustEqual false
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "decode (b-1)" in {
      PacketCoding.decodePacket(string_b1).require match {
        case SquadMembershipResponse(unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8) =>
          unk1 mustEqual SquadResponseType.PlatoonLeave
          unk2 mustEqual 0
          unk3 mustEqual 1
          unk4 mustEqual 30910985L
          unk5.contains(41578085L) mustEqual true
          unk6 mustEqual "SAraisVanu"
          unk7 mustEqual false
          unk8.contains(None) mustEqual true
        case _ =>
          ko
      }
    }

    "encode (0-1)" in {
      val msg = SquadMembershipResponse(SquadResponseType.Invite, 0, 0, 42771010L, Some(1300870L), "HofD", false, None)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_01
    }

    "encode (0-2)" in {
      val msg =
        SquadMembershipResponse(SquadResponseType.Invite, 0, 0, 1176612L, Some(1004937L), "SPOILERS", true, None)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_02
    }

    "encode (1-1)" in {
      val msg = SquadMembershipResponse(SquadResponseType.Unk01, 19, 0, 41530025L, Some(0L), "", true, Some(None))
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_11
    }

    "encode (1-2)" in {
      val msg = SquadMembershipResponse(SquadResponseType.Unk01, 18, 0, 41578085L, Some(0L), "", true, Some(None))
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_12
    }

    "encode (2-1)" in {
      val msg = SquadMembershipResponse(
        SquadResponseType.Accept,
        0,
        0,
        1300870L,
        Some(42771010L),
        "VirusGiver",
        true,
        Some(None)
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_21
    }

    "encode (2-2)" in {
      val msg = SquadMembershipResponse(
        SquadResponseType.Accept,
        0,
        0,
        1300870L,
        Some(30736877L),
        "VirusGiver",
        true,
        Some(None)
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_22
    }

    "encode (3-1)" in {
      val msg =
        SquadMembershipResponse(SquadResponseType.Reject, 0, 3, 31035057L, Some(42771010L), "HMFIC", false, Some(None))
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_31
    }

    "encode (3-2)" in {
      val msg = SquadMembershipResponse(
        SquadResponseType.Reject,
        0,
        2,
        31106913L,
        Some(42771010L),
        "DAN1111",
        false,
        Some(None)
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_32
    }

    "encode (4-1)" in {
      val msg = SquadMembershipResponse(
        SquadResponseType.Cancel,
        0,
        2,
        41578085L,
        Some(41607133L),
        "SAraisVanu",
        false,
        Some(None)
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_41
    }

    "encode (4-2)" in {
      val msg = SquadMembershipResponse(
        SquadResponseType.Cancel,
        0,
        2,
        41607396L,
        Some(41324011L),
        "AirInjector",
        false,
        Some(None)
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_42
    }

    "encode (5-1)" in {
      val msg =
        SquadMembershipResponse(SquadResponseType.Leave, 0, 1, 1176612L, Some(1176612L), "CCRIDER", true, Some(None))
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_51
    }

    "encode (7-1)" in {
      val msg = SquadMembershipResponse(SquadResponseType.PlatoonInvite, 2, 0, 1176612L, Some(0L), "", true, Some(None))
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_71
    }

    "encode (7-2)" in {
      val msg = SquadMembershipResponse(
        SquadResponseType.PlatoonInvite,
        0,
        0,
        41578085L,
        Some(30910985L),
        "Wizkid45",
        true,
        Some(None)
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_72
    }

    "encode (8-1)" in {
      val msg = SquadMembershipResponse(
        SquadResponseType.PlatoonAccept,
        0,
        0,
        30910985L,
        Some(41578085L),
        "Wizkid45",
        false,
        Some(None)
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_81
    }

    "encode (9-1)" in {
      val msg =
        SquadMembershipResponse(SquadResponseType.PlatoonReject, 0, 0, 297025L, Some(41605002L), "", true, Some(None))
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_91
    }

    "encode (9-2)" in {
      val msg = SquadMembershipResponse(
        SquadResponseType.PlatoonReject,
        0,
        0,
        30910985L,
        Some(41578085L),
        "Wizkid45",
        false,
        Some(None)
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_92
    }

    "encode (b-1)" in {
      val msg = SquadMembershipResponse(
        SquadResponseType.PlatoonLeave,
        0,
        1,
        30910985L,
        Some(41578085L),
        "SAraisVanu",
        false,
        Some(None)
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_b1
    }
  }
}

// Copyright (c) 2019 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.packets._
import net.psforever.packet.game.packets.SquadMembershipResponse
import net.psforever.types.SquadResponseType
import org.specs2.mutable._
import scodec.bits._

class SquadMembershipResponseTest extends Specification {
  val string_01 = hex"6f 0 00854518050db2260108048006f006600440000"
  val string_02 = hex"6f 0 0049e8220112aa1e01100530050004f0049004c0045005200530080"
  val string_11 = hex"6f 1 995364f2040000000100080"
  val string_12 = hex"6f 1 90cadcf4040000000100080"
  val string_21 = hex"6f 2 010db2260085451805140560069007200750073004700690076006500720080"
  val string_22 = hex"6f 2 010db22601da03aa03140560069007200750073004700690076006500720080"
  val string_31 = hex"6f 3 07631db202854518050a048004d0046004900430000"
  val string_32 = hex"6f 3 04c34fb402854518050e0440041004e00310031003100310000"
  val string_41 = hex"6f 4 04cadcf405bbbef405140530041007200610069007300560061006e00750000"
  val string_42 = hex"6f 4 05c9c0f405d71aec0516041006900720049006e006a006500630074006f00720000"
  val string_51 = hex"6f 5 0249e8220049e822010e0430043005200490044004500520080"
  val string_71 = hex"6f 7 1049e822000000000100080"
  val string_72 = hex"6f 7 00cadcf4041355ae03100570069007a006b00690064003400350080"
  val string_81 = hex"6f 8 001355ae02cadcf405100570069007a006b00690064003400350000"
  val string_91 = hex"6f 9 008310080115aef40500080"
  val string_92 = hex"6f 9 001355ae02cadcf405100570069007a006b00690064003400350000"
  val string_b1 = hex"6f b 021355ae02cadcf405140530041007200610069007300560061006e00750000"
  val string_invite_with_role = hex"6f 0 010db22600854518051405600690072007500730047006900760065007200e180470075006e006e0065007200" // crafted example, no dump available

  "SquadMembershipResponse" should {
    "decode (0-1)" in {
      PacketCoding.decodePacket(string_01).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.Invite
          unk2 mustEqual 0
          unk3 mustEqual 0
          char_id mustEqual 42771010L
          other_id.contains(1300870L) mustEqual true
          player_name mustEqual "HofD"
          is_sender mustEqual false
          role_name mustEqual Some(None)
        case _ =>
          ko
      }
    }

    "decode (0-2)" in {
      PacketCoding.decodePacket(string_02).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.Invite
          unk2 mustEqual 0
          unk3 mustEqual 0
          char_id mustEqual 1176612L
          other_id.contains(1004937L) mustEqual true
          player_name mustEqual "SPOILERS"
          is_sender mustEqual true
          role_name mustEqual Some(None)
        case _ =>
          ko
      }
    }

    "decode (1-1)" in {
      PacketCoding.decodePacket(string_11).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.ProximityInvite
          unk2 mustEqual 19
          unk3 mustEqual 0
          char_id mustEqual 41530025L
          other_id.contains(0L) mustEqual true
          player_name mustEqual ""
          is_sender mustEqual true
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (1-2)" in {
      PacketCoding.decodePacket(string_12).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.ProximityInvite
          unk2 mustEqual 18
          unk3 mustEqual 0
          char_id mustEqual 41578085L
          other_id.contains(0L) mustEqual true
          player_name mustEqual ""
          is_sender mustEqual true
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (2-1)" in {
      PacketCoding.decodePacket(string_21).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.Accept
          unk2 mustEqual 0
          unk3 mustEqual 0
          char_id mustEqual 1300870L
          other_id.contains(42771010L) mustEqual true
          player_name mustEqual "VirusGiver"
          is_sender mustEqual true
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (2-2)" in {
      PacketCoding.decodePacket(string_22).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.Accept
          unk2 mustEqual 0
          unk3 mustEqual 0
          char_id mustEqual 1300870L
          other_id.contains(30736877L) mustEqual true
          player_name mustEqual "VirusGiver"
          is_sender mustEqual true
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (3-1)" in {
      PacketCoding.decodePacket(string_31).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.Reject
          unk2 mustEqual 0
          unk3 mustEqual 3
          char_id mustEqual 31035057L
          other_id.contains(42771010L) mustEqual true
          player_name mustEqual "HMFIC"
          is_sender mustEqual false
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (3-2)" in {
      PacketCoding.decodePacket(string_32).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.Reject
          unk2 mustEqual 0
          unk3 mustEqual 2
          char_id mustEqual 31106913L
          other_id.contains(42771010L) mustEqual true
          player_name mustEqual "DAN1111"
          is_sender mustEqual false
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (4-1)" in {
      PacketCoding.decodePacket(string_41).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.Cancel
          unk2 mustEqual 0
          unk3 mustEqual 2
          char_id mustEqual 41578085L
          other_id.contains(41607133L) mustEqual true
          player_name mustEqual "SAraisVanu"
          is_sender mustEqual false
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (4-2)" in {
      PacketCoding.decodePacket(string_42).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.Cancel
          unk2 mustEqual 0
          unk3 mustEqual 2
          char_id mustEqual 41607396L
          other_id.contains(41324011L) mustEqual true
          player_name mustEqual "AirInjector"
          is_sender mustEqual false
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (5-1)" in {
      PacketCoding.decodePacket(string_51).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.Leave
          unk2 mustEqual 0
          unk3 mustEqual 1
          char_id mustEqual 1176612L
          other_id.contains(1176612L) mustEqual true
          player_name mustEqual "CCRIDER"
          is_sender mustEqual true
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (7-1)" in {
      PacketCoding.decodePacket(string_71).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.PlatoonInvite
          unk2 mustEqual 2
          unk3 mustEqual 0
          char_id mustEqual 1176612L
          other_id.contains(0L) mustEqual true
          player_name mustEqual ""
          is_sender mustEqual true
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (7-2)" in {
      PacketCoding.decodePacket(string_72).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.PlatoonInvite
          unk2 mustEqual 0
          unk3 mustEqual 0
          char_id mustEqual 41578085L
          other_id.contains(30910985L) mustEqual true
          player_name mustEqual "Wizkid45"
          is_sender mustEqual true
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (8-1)" in {
      PacketCoding.decodePacket(string_81).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.PlatoonAccept
          unk2 mustEqual 0
          unk3 mustEqual 0
          char_id mustEqual 30910985L
          other_id.contains(41578085L) mustEqual true
          player_name mustEqual "Wizkid45"
          is_sender mustEqual false
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (9-1)" in {
      PacketCoding.decodePacket(string_91).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.PlatoonReject
          unk2 mustEqual 0
          unk3 mustEqual 0
          char_id mustEqual 297025L
          other_id.contains(41605002L) mustEqual true
          player_name mustEqual ""
          is_sender mustEqual true
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (9-2)" in {
      PacketCoding.decodePacket(string_92).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.PlatoonReject
          unk2 mustEqual 0
          unk3 mustEqual 0
          char_id mustEqual 30910985L
          other_id.contains(41578085L) mustEqual true
          player_name mustEqual "Wizkid45"
          is_sender mustEqual false
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "decode (b-1)" in {
      PacketCoding.decodePacket(string_b1).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id, player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.PlatoonLeave
          unk2 mustEqual 0
          unk3 mustEqual 1
          char_id mustEqual 30910985L
          other_id.contains(41578085L) mustEqual true
          player_name mustEqual "SAraisVanu"
          is_sender mustEqual false
          role_name mustEqual None
        case _ =>
          ko
      }
    }

    "encode (0-1)" in {
      val msg = SquadMembershipResponse(SquadResponseType.Invite, 0, 0, 42771010L, Some(1300870L), "HofD", false, Some(None))
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_01
    }

    "encode (0-2)" in {
      val msg =
        SquadMembershipResponse(SquadResponseType.Invite, 0, 0, 1176612L, Some(1004937L), "SPOILERS", true, Some(None))
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_02
    }

    "encode (1-1)" in {
      val msg = SquadMembershipResponse(SquadResponseType.ProximityInvite, 19, 0, 41530025L, Some(0L), "", true, Some(None))
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_11
    }

    "encode (1-2)" in {
      val msg = SquadMembershipResponse(SquadResponseType.ProximityInvite, 18, 0, 41578085L, Some(0L), "", true, Some(None))
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

    "decode invite_with_role" in {
      PacketCoding.decodePacket(string_invite_with_role).require match {
        case SquadMembershipResponse(request_type, unk2, unk3, char_id, other_id , player_name, is_sender, role_name) =>
          request_type mustEqual SquadResponseType.Invite
          unk2 mustEqual 0
          unk3 mustEqual 0
          char_id mustEqual 1300870L
          other_id.contains(42771010L) mustEqual true
          player_name mustEqual "VirusGiver"
          is_sender mustEqual true
          role_name mustEqual Some(Some("Gunner"))
        case _ =>
          ko
      }
    }

    "encode invite_with_role" in {
      val msg = SquadMembershipResponse(
        SquadResponseType.Invite,
        0,
        0,
        1300870L,
        Some(42771010L),
        "VirusGiver",
        true,
        Some(Some("Gunner"))
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_invite_with_role
    }
  }
}

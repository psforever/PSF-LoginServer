// Copyright (c) 2023-2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.packet.game.OutfitMembershipRequest.RequestType
import net.psforever.packet.game.OutfitMembershipRequestAction._
import org.specs2.mutable._
import scodec.bits._

class OutfitMembershipRequestTest extends Specification {
  val create_ABC   = hex"8c 0 0200 000 1000 83 410042004300"
  val create_2222  = hex"8c 0 1000 000 1000 84 3200320032003200"
  val form_abc     = hex"8c 2 0200 000 1000 83 610062006300"
  val form_1       = hex"8c 2 1000 000 1000 81 3100"
  val invite_old   = hex"8c 5 bb399e0 2000 0000 1140 7600690072007500730067006900760065007200" // -- virusgiver
  val unk3         = hex"8c 5 bb399e0 2000 0000 1080 750072006f006200" // -- "urob" -- could be false positive -- seems to gets an OMSResp -> 0x8d271bb399e025af8f405080550072006f0062008080
  val accept_1     = hex"8c 6 0200 000 1000"
  val accept_2     = hex"8c 6 0400 000 1000"
  val reject_1     = hex"8c 8 0200 000 1000"
  val reject_2     = hex"8c 8 0400 000 1000"
  val cancel_3     = hex"8c a 0600 000 0000 0000 1000"
  val cancel_1_abc = hex"8c a 0200 000 0000 0000 1060 610062006300"
  val cancel_3_def = hex"8c a 0600 000 0000 0000 1060 640065006600" // /outfitcancel 123 def -- first parameter is skipped

  // dumped from half implemented outfit
  val invite  = hex"8c4020000000000000116069006e00760069007400650054006500730074003100"
  val kick    = hex"8cc020000017ac8f405000"
  val setrank = hex"8ce020000017ac8f404600" // setting rank from 0 to 1

  "decode CreateOutfit ABC" in {
    PacketCoding.decodePacket(create_ABC).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Create
        outfit_id mustEqual 1
        action mustEqual Create("", "ABC")
      case _ =>
        ko
    }
  }

  "encode CreateOutfit ABC" in {
    val msg = OutfitMembershipRequest(RequestType.Create, 1, Create("", "ABC"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual create_ABC
  }

  "decode CreateOutfit 2222" in {
    PacketCoding.decodePacket(create_2222).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Create
        outfit_id mustEqual 8
        action mustEqual Create("", "2222")
      case _ =>
        ko
    }
  }

  "encode CreateOutfit 2222" in {
    val msg = OutfitMembershipRequest(RequestType.Create, 8, Create("", "2222"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual create_2222
  }

  "decode FormOutfit abc" in {
    PacketCoding.decodePacket(form_abc).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Form
        outfit_id mustEqual 1
        action mustEqual Form("", "abc")
      case _ =>
        ko
    }
  }

  "encode FormOutfit abc" in {
    val msg = OutfitMembershipRequest(RequestType.Form, 1, Form("", "abc"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual form_abc
  }

  "decode FormOutfit 1" in {
    PacketCoding.decodePacket(form_1).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Form
        outfit_id mustEqual 8
        action mustEqual Form("", "1")
      case _ =>
        ko
    }
  }

  "encode FormOutfit 1" in {
    val msg = OutfitMembershipRequest(RequestType.Form, 8, Form("", "1"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual form_1
  }

  "decode Invite" in {
    PacketCoding.decodePacket(invite_old).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Invite
        outfit_id mustEqual 30383325L
        action mustEqual Invite(0, "virusgiver")
      case _ =>
        ko
    }
  }

  "encode Invite" in {
    val msg = OutfitMembershipRequest(RequestType.Invite, 30383325L, Invite(0, "virusgiver"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual invite_old
  }

  "decode AcceptOutfitInvite 1" in {
    PacketCoding.decodePacket(accept_1).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Accept
        outfit_id mustEqual 1
        action mustEqual AcceptInvite("")
      case _ =>
        ko
    }
  }

  "encode AcceptOutfitInvite 1" in {
    val msg = OutfitMembershipRequest(RequestType.Accept, 1, AcceptInvite(""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual accept_1
  }

  "decode AcceptOutfitInvite 2" in {
    PacketCoding.decodePacket(accept_2).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Accept
        outfit_id mustEqual 2
        action mustEqual AcceptInvite("")
      case _ =>
        ko
    }
  }

  "encode AcceptOutfitInvite 2" in {
    val msg = OutfitMembershipRequest(RequestType.Accept, 2, AcceptInvite(""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual accept_2
  }

  "decode RejectOutfitInvite 1" in {
    PacketCoding.decodePacket(reject_1).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Reject
        outfit_id mustEqual 1
        action mustEqual RejectInvite("")
      case _ =>
        ko
    }
  }

  "encode RejectOutfitInvite 1" in {
    val msg = OutfitMembershipRequest(RequestType.Reject, 1, RejectInvite(""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual reject_1
  }

  "decode RejectOutfitInvite 2" in {
    PacketCoding.decodePacket(reject_2).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Reject
        outfit_id mustEqual 2
        action mustEqual RejectInvite("")
      case _ =>
        ko
    }
  }

  "encode RejectOutfitInvite 2" in {
    val msg = OutfitMembershipRequest(RequestType.Reject, 2, RejectInvite(""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual reject_2
  }

  "decode CancelOutfitInvite 3" in {
    PacketCoding.decodePacket(cancel_3).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Cancel
        outfit_id mustEqual 3
        action mustEqual CancelInvite(0, "")
      case _ =>
        ko
    }
  }

  "encode CancelOutfitInvite 3" in {
    val msg = OutfitMembershipRequest(RequestType.Cancel, 3, CancelInvite(0, ""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual cancel_3
  }

  "decode CancelOutfitInvite 1 abc" in {
    PacketCoding.decodePacket(cancel_1_abc).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Cancel
        outfit_id mustEqual 1
        action mustEqual CancelInvite(0, "abc")
      case _ =>
        ko
    }
  }

  "encode CancelOutfitInvite 1 abc" in {
    val msg = OutfitMembershipRequest(RequestType.Cancel, 1, CancelInvite(0, "abc"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual cancel_1_abc
  }

  "decode CancelOutfitInvite 3 def" in {
    PacketCoding.decodePacket(cancel_3_def).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Cancel
        outfit_id mustEqual 3
        action mustEqual CancelInvite(0, "def")
      case _ =>
        ko
    }
  }

  "encode CancelOutfitInvite 3 def" in {
    val msg = OutfitMembershipRequest(RequestType.Cancel, 3, CancelInvite(0, "def"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual cancel_3_def
  }

  //

  "decode invite" in {
    PacketCoding.decodePacket(invite).require match {
      case OutfitMembershipRequest(request_type, outfit_id, Invite(unk1, member_name)) =>
        request_type mustEqual RequestType.Invite
        outfit_id mustEqual 1
        unk1 mustEqual 0
        member_name mustEqual "inviteTest1"
      case _ =>
        ko
    }
  }

  "encode invite" in {
    val msg = OutfitMembershipRequest(RequestType.Invite, 1, Invite(0, "inviteTest1"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual invite
  }


  "decode kick" in {
    PacketCoding.decodePacket(kick).require match {
      case OutfitMembershipRequest(request_type, outfit_id, Kick(avatar_id, member_name)) =>
        request_type mustEqual RequestType.Kick
        outfit_id mustEqual 1
        avatar_id mustEqual 41575613
        member_name mustEqual ""
      case _ =>
        ko
    }
  }

  "encode kick" in {
    val msg = OutfitMembershipRequest(RequestType.Kick, 1, Kick(41575613, ""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual kick
  }

  "decode setrank" in {
    PacketCoding.decodePacket(setrank).require match {
      case OutfitMembershipRequest(request_type, outfit_id, SetRank(avatar_id, rank, member_name)) =>
        request_type mustEqual RequestType.SetRank
        outfit_id mustEqual 1
        avatar_id mustEqual 41575613
        rank mustEqual 1
        member_name mustEqual ""
      case _ =>
        ko
    }
  }

  "encode setrank" in {
    val msg = OutfitMembershipRequest(RequestType.SetRank, 1, SetRank(41575613, 1, ""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual setrank
  }

}

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
  val unk2         = hex"8c 5 bb399e0 2000 0000 1140 7600690072007500730067006900760065007200" // -- virusgiver
  val unk3         = hex"8c 5 bb399e0 2000 0000 1080 750072006f006200" // -- "urob" -- could be false positive -- seems to gets an OMSResp -> 0x8d271bb399e025af8f405080550072006f0062008080
  val accept_1     = hex"8c 6 0200 000 1000"
  val accept_2     = hex"8c 6 0400 000 1000"
  val reject_1     = hex"8c 8 0200 000 1000"
  val reject_2     = hex"8c 8 0400 000 1000"
  val cancel_3     = hex"8c a 0600 000 0000 0000 1000"
  val cancel_1_abc = hex"8c a 0200 000 0000 0000 1060 610062006300"
  val cancel_3_def = hex"8c a 0600 000 0000 0000 1060 640065006600" // /outfitcancel 123 def -- first parameter is skipped

  "decode CreateOutfit ABC" in {
    PacketCoding.decodePacket(create_ABC).require match {
      case OutfitMembershipRequest(request_type, avatar_id, action) =>
        request_type mustEqual RequestType.Create
        avatar_id mustEqual 1
        action mustEqual CreateOutfit("", 0, unk4 = false, "ABC")
      case _ =>
        ko
    }
  }

  "encode CreateOutfit ABC" in {
    val msg = OutfitMembershipRequest(RequestType.Create, 1, CreateOutfit("", 0, unk4 = false, "ABC"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual create_ABC
  }

  "decode CreateOutfit 2222" in {
    PacketCoding.decodePacket(create_2222).require match {
      case OutfitMembershipRequest(request_type, avatar_id, action) =>
        request_type mustEqual RequestType.Create
        avatar_id mustEqual 8
        action mustEqual CreateOutfit("", 0, unk4 = false, "2222")
      case _ =>
        ko
    }
  }

  "encode CreateOutfit 2222" in {
    val msg = OutfitMembershipRequest(RequestType.Create, 8, CreateOutfit("", 0, unk4 = false, "2222"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual create_2222
  }

  "decode FormOutfit abc" in {
    PacketCoding.decodePacket(form_abc).require match {
      case OutfitMembershipRequest(request_type, avatar_id, action) =>
        request_type mustEqual RequestType.Form
        avatar_id mustEqual 1
        action mustEqual FormOutfit("", 0, unk4 = false, "abc")
      case _ =>
        ko
    }
  }

  "encode FormOutfit abc" in {
    val msg = OutfitMembershipRequest(RequestType.Form, 1, FormOutfit("", 0, unk4 = false, "abc"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual form_abc
  }

  "decode FormOutfit 1" in {
    PacketCoding.decodePacket(form_1).require match {
      case OutfitMembershipRequest(request_type, avatar_id, action) =>
        request_type mustEqual RequestType.Form
        avatar_id mustEqual 8
        action mustEqual FormOutfit("", 0, unk4 = false, "1")
      case _ =>
        ko
    }
  }

  "encode FormOutfit 1" in {
    val msg = OutfitMembershipRequest(RequestType.Form, 8, FormOutfit("", 0, unk4 = false, "1"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual form_1
  }

  "decode Unk2" in {
    PacketCoding.decodePacket(unk2).require match {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type mustEqual RequestType.Unk2
        outfit_id mustEqual 30383325L
        action mustEqual Unk2(0, 0, "virusgiver")
      case _ =>
        ko
    }
  }

  "encode Unk2" in {
    val msg = OutfitMembershipRequest(RequestType.Unk2, 30383325L, Unk2(0, 0, "virusgiver"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk2
  }

  "decode AcceptOutfitInvite 1" in {
    PacketCoding.decodePacket(accept_1).require match {
      case OutfitMembershipRequest(request_type, avatar_id, action) =>
        request_type mustEqual RequestType.Accept
        avatar_id mustEqual 1
        action mustEqual AcceptOutfitInvite("")
      case _ =>
        ko
    }
  }

  "encode AcceptOutfitInvite 1" in {
    val msg = OutfitMembershipRequest(RequestType.Accept, 1, AcceptOutfitInvite(""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual accept_1
  }

  "decode AcceptOutfitInvite 2" in {
    PacketCoding.decodePacket(accept_2).require match {
      case OutfitMembershipRequest(request_type, avatar_id, action) =>
        request_type mustEqual RequestType.Accept
        avatar_id mustEqual 2
        action mustEqual AcceptOutfitInvite("")
      case _ =>
        ko
    }
  }

  "encode AcceptOutfitInvite 2" in {
    val msg = OutfitMembershipRequest(RequestType.Accept, 2, AcceptOutfitInvite(""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual accept_2
  }

  "decode RejectOutfitInvite 1" in {
    PacketCoding.decodePacket(reject_1).require match {
      case OutfitMembershipRequest(request_type, avatar_id, action) =>
        request_type mustEqual RequestType.Reject
        avatar_id mustEqual 1
        action mustEqual RejectOutfitInvite("")
      case _ =>
        ko
    }
  }

  "encode RejectOutfitInvite 1" in {
    val msg = OutfitMembershipRequest(RequestType.Reject, 1, RejectOutfitInvite(""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual reject_1
  }

  "decode RejectOutfitInvite 2" in {
    PacketCoding.decodePacket(reject_2).require match {
      case OutfitMembershipRequest(request_type, avatar_id, action) =>
        request_type mustEqual RequestType.Reject
        avatar_id mustEqual 2
        action mustEqual RejectOutfitInvite("")
      case _ =>
        ko
    }
  }

  "encode RejectOutfitInvite 2" in {
    val msg = OutfitMembershipRequest(RequestType.Reject, 2, RejectOutfitInvite(""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual reject_2
  }

  "decode CancelOutfitInvite 3" in {
    PacketCoding.decodePacket(cancel_3).require match {
      case OutfitMembershipRequest(request_type, avatar_id, action) =>
        request_type mustEqual RequestType.Cancel
        avatar_id mustEqual 3
        action mustEqual CancelOutfitInvite(0, 0, "")
      case _ =>
        ko
    }
  }

  "encode CancelOutfitInvite 3" in {
    val msg = OutfitMembershipRequest(RequestType.Cancel, 3, CancelOutfitInvite(0, 0, ""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual cancel_3
  }

  "decode CancelOutfitInvite 1 abc" in {
    PacketCoding.decodePacket(cancel_1_abc).require match {
      case OutfitMembershipRequest(request_type, avatar_id, action) =>
        request_type mustEqual RequestType.Cancel
        avatar_id mustEqual 1
        action mustEqual CancelOutfitInvite(0, 0, "abc")
      case _ =>
        ko
    }
  }

  "encode CancelOutfitInvite 1 abc" in {
    val msg = OutfitMembershipRequest(RequestType.Cancel, 1, CancelOutfitInvite(0, 0, "abc"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual cancel_1_abc
  }

  "decode CancelOutfitInvite 3 def" in {
    PacketCoding.decodePacket(cancel_3_def).require match {
      case OutfitMembershipRequest(request_type, avatar_id, action) =>
        request_type mustEqual RequestType.Cancel
        avatar_id mustEqual 3
        action mustEqual CancelOutfitInvite(0, 0, "def")
      case _ =>
        ko
    }
  }

  "encode CancelOutfitInvite 3 def" in {
    val msg = OutfitMembershipRequest(RequestType.Cancel, 3, CancelOutfitInvite(0, 0, "def"))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual cancel_3_def
  }
}

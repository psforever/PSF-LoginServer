// Copyright (c) 2017 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.OutfitMembershipRequest.RequestType
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import org.specs2.mutable._
import scodec.bits._

class OutfitMembershipRequestTest extends Specification {
  val create_ABC   = hex"8c 0 0200 000 1000 83 410042004300"
  val create_2222  = hex"8c 0 1000 000 1000 84 3200320032003200"
  val form_abc     = hex"8c 2 0200 000 1000 83 610062006300"
  val form_1       = hex"8c 2 1000 000 1000 81 3100"
  val accept_1     = hex"8c 6 0200 000 1000"
  val accept_2     = hex"8c 6 0400 000 1000"
  val reject_1     = hex"8c 8 0200 000 1000"
  val reject_2     = hex"8c 8 0400 000 1000"
  val cancel_5     = hex"8c a 0600 000 0000 0000 1000"
  val cancel_1_abc = hex"8c a 0200 000 0000 0000 1060 610064006200"

  "decode create ABC" in {
    PacketCoding.decodePacket(create_ABC).require match {
      case OutfitMembershipRequest(request_type, avatar_id, unk1, unk2, unk3, unk4, outfit_name) =>
        request_type mustEqual RequestType.Create
        avatar_id mustEqual 1
        unk1 mustEqual 0
        unk2 mustEqual ""
        unk3 mustEqual 0
        unk4 mustEqual false
        outfit_name mustEqual "ABC"
      case _ =>
        ko
    }
  }

  "encode create ABC" in {
    val msg = OutfitMembershipRequest(RequestType.Create, PlanetSideGUID(1), 0, "", 0, unk4 = false, "ABC")
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual create_ABC
  }

  "decode create 2222" in {
    PacketCoding.decodePacket(create_2222).require match {
      case OutfitMembershipRequest(request_type, avatar_id, unk1, unk2, unk3, unk4, outfit_name) =>
        request_type mustEqual RequestType.Create
        avatar_id mustEqual PlanetSideGUID(8)
        unk1 mustEqual 0
        unk2 mustEqual ""
        unk3 mustEqual 0
        unk4 mustEqual false
        outfit_name mustEqual "2222"
      case _ =>
        ko
    }
  }

  "encode create 2222" in {
    val msg = OutfitMembershipRequest(RequestType.Create, PlanetSideGUID(8), 0, "", 0, unk4 = false, "2222")
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual create_2222
  }

  "decode form abc" in {
    PacketCoding.decodePacket(form_abc).require match {
      case OutfitMembershipRequest(request_type, avatar_id, unk1, unk2, unk3, unk4, outfit_name) =>
        request_type mustEqual RequestType.Form
        avatar_id mustEqual PlanetSideGUID(1)
        unk1 mustEqual 0
        unk2 mustEqual ""
        unk3 mustEqual 0
        unk4 mustEqual false
        outfit_name mustEqual "abc"
      case _ =>
        ko
    }
  }

  "encode form abc" in {
    val msg = OutfitMembershipRequest(RequestType.Form, PlanetSideGUID(1), 0, "", 0, unk4 = false, "abc")
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual form_abc
  }

  "decode form 1" in {
    PacketCoding.decodePacket(form_1).require match {
      case OutfitMembershipRequest(request_type, avatar_id, unk1, unk2, unk3, unk4, outfit_name) =>
        request_type mustEqual RequestType.Form
        avatar_id mustEqual PlanetSideGUID(8)
        unk1 mustEqual 0
        unk2 mustEqual ""
        unk3 mustEqual 0
        unk4 mustEqual false
        outfit_name mustEqual "1"
      case _ =>
        ko
    }
  }

  "encode form 1" in {
    val msg = OutfitMembershipRequest(RequestType.Form, PlanetSideGUID(8), 0, "", 0, unk4 = false, "1")
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual form_1
  }

  "decode accept 1" in {
    PacketCoding.decodePacket(accept_1).require match {
      case OutfitMembershipRequest(request_type, avatar_id, unk1, unk2, unk3, unk4, outfit_name) =>
        request_type mustEqual RequestType.Accept
        avatar_id mustEqual PlanetSideGUID(1)
        unk1 mustEqual 0
        unk2 mustEqual ""
        unk3 mustEqual 0
        unk4 mustEqual false
        outfit_name mustEqual ""
      case _ =>
        ko
    }
  }

  "decode accept 2" in {
    PacketCoding.decodePacket(accept_2).require match {
      case OutfitMembershipRequest(request_type, avatar_id, unk1, unk2, unk3, unk4, outfit_name) =>
        request_type mustEqual RequestType.Accept
        avatar_id mustEqual 2
        unk1 mustEqual 0
        unk2 mustEqual ""
        unk3 mustEqual 0
        unk4 mustEqual false
        outfit_name mustEqual ""
      case _ =>
        ko
    }
  }

  "decode reject 1" in {
    PacketCoding.decodePacket(reject_1).require match {
      case OutfitMembershipRequest(request_type, avatar_id, unk1, unk2, unk3, unk4, outfit_name) =>
        request_type mustEqual RequestType.Reject
        avatar_id mustEqual 1
        unk1 mustEqual 0
        unk2 mustEqual ""
        unk3 mustEqual 0
        unk4 mustEqual false
        outfit_name mustEqual ""
      case _ =>
        ko
    }
  }

  "decode reject 2" in {
    PacketCoding.decodePacket(reject_2).require match {
      case OutfitMembershipRequest(request_type, avatar_id, unk1, unk2, unk3, unk4, outfit_name) =>
        request_type mustEqual RequestType.Reject
        avatar_id mustEqual 2
        unk1 mustEqual 0
        unk2 mustEqual ""
        unk3 mustEqual 0
        unk4 mustEqual false
        outfit_name mustEqual ""
      case _ =>
        ko
    }
  }

  "decode cancel 5" in {
    PacketCoding.decodePacket(cancel_5).require match {
      case OutfitMembershipRequest(request_type, avatar_id, unk1, unk2, unk3, unk4, outfit_name) =>
        request_type mustEqual RequestType.Cancel
        avatar_id mustEqual 5
        unk1 mustEqual 0
        unk2 mustEqual ""
        unk3 mustEqual 0
        unk4 mustEqual false
        outfit_name mustEqual ""
      case _ =>
        ko
    }
  }

  "decode reject 1 abc" in {
    PacketCoding.decodePacket(cancel_1_abc).require match {
      case OutfitMembershipRequest(request_type, avatar_id, unk1, unk2, unk3, unk4, outfit_name) =>
        request_type mustEqual RequestType.Cancel
        avatar_id mustEqual 1
        unk1 mustEqual 0
        unk2 mustEqual ""
        unk3 mustEqual 0
        unk4 mustEqual false
        outfit_name mustEqual "abc"
      case _ =>
        ko
    }
  }
}

// Copyright (c) 2023-2025 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package game

import net.psforever.packet._
import net.psforever.packet.game.packets.OutfitMembershipResponse.PacketType
import net.psforever.packet.game.packets.OutfitMembershipResponse
import org.specs2.mutable._
import scodec.bits._

class OutfitMembershipResponseTest extends Specification {

  val createResponse  = hex"8d 0 002b54f404000000010008080"
  val invite          = hex"8d 2 01bb399e03ddb4f4050a078004e00690063006b009550006c0061006e006500740053006900640065005f0046006f00720065007600650072005f005400520000"
  val inviteAccepted  = hex"8d 4 0049b0f4042b54f4051405a006500720067006c0069006e006700390032009750006c0061006e006500740053006900640065005f0046006f00720065007600650072005f00560061006e00750000"
  val inviteRejected  = hex"8d 6 00e8c2f40510d3b6030008080"
  val inviteCancelled = hex"8d 8 002b54f404000000010008080"
  val kicked          = hex"8d a 022b54f4051fb0f4051c05000530046006f0075007400660069007400740065007300740031008080"

  "decode CreateResponse" in {
    PacketCoding.decodePacket(createResponse).require match {
      case OutfitMembershipResponse(packet_type, error, variant, actor_id, target_id, actor_name, target_name, to_actor) =>
        packet_type mustEqual PacketType.CreateResponse
        error mustEqual OutfitMembershipResponse.Error.NoError
        variant mustEqual 0
        actor_id mustEqual 41593365
        target_id mustEqual 0
        actor_name mustEqual ""
        target_name mustEqual ""
        to_actor mustEqual true
      case _ =>
        ko
    }
  }

  "encode CreateResponse" in {
    val msg = OutfitMembershipResponse(PacketType.CreateResponse, OutfitMembershipResponse.Error.NoError, 0, 41593365, 0, "", "", to_actor = true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual createResponse
  }

  "decode Invite" in {
    PacketCoding.decodePacket(invite).require match {
      case OutfitMembershipResponse(packet_type, error, variant, actor_id, target_id, actor_name, target_name, to_actor) =>
        packet_type mustEqual PacketType.Invite
        error mustEqual OutfitMembershipResponse.Error.NoError
        variant mustEqual 0
        actor_id mustEqual 30383325
        target_id mustEqual 41605870
        actor_name mustEqual "xNick"
        target_name mustEqual "PlanetSide_Forever_TR"
        to_actor mustEqual false
      case _ =>
        ko
    }
  }

  "encode Invite" in {
    val msg = OutfitMembershipResponse(PacketType.Invite, OutfitMembershipResponse.Error.NoError, 0, 30383325, 41605870, "xNick", "PlanetSide_Forever_TR", to_actor = false)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual invite
  }

  "decode InviteAccepted" in {
    PacketCoding.decodePacket(inviteAccepted).require match {
      case OutfitMembershipResponse(packet_type, error, variant, actor_id, target_id, actor_name, target_name, to_actor) =>
        packet_type mustEqual PacketType.InviteAccepted
        error mustEqual OutfitMembershipResponse.Error.NoError
        variant mustEqual 0
        actor_id mustEqual 41605156
        target_id mustEqual 41593365
        actor_name mustEqual "Zergling92"
        target_name mustEqual "PlanetSide_Forever_Vanu"
        to_actor mustEqual false
      case _ =>
        ko
    }
  }

  "encode InviteAccepted" in {
    val msg = OutfitMembershipResponse(PacketType.InviteAccepted, OutfitMembershipResponse.Error.NoError, 0, 41605156, 41593365, "Zergling92", "PlanetSide_Forever_Vanu", to_actor = false)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual inviteAccepted
  }

  "decode InviteRejected" in {
    PacketCoding.decodePacket(inviteRejected).require match {
      case OutfitMembershipResponse(packet_type, error, variant, actor_id, target_id, actor_name, target_name, to_actor) =>
        packet_type mustEqual PacketType.InviteRejected
        error mustEqual OutfitMembershipResponse.Error.NoError
        variant mustEqual 0
        actor_id mustEqual 41574772
        target_id mustEqual 31156616
        actor_name mustEqual ""
        target_name mustEqual ""
        to_actor mustEqual true
      case _ =>
        ko
    }
  }

  "encode InviteRejected" in {
    val msg = OutfitMembershipResponse(PacketType.InviteRejected, OutfitMembershipResponse.Error.NoError, 0, 41574772, 31156616, "", "", to_actor = true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual inviteRejected
  }

  "decode InviteCancelled" in {
    PacketCoding.decodePacket(inviteCancelled).require match {
      case OutfitMembershipResponse(packet_type, error, variant, actor_id, target_id, actor_name, target_name, to_actor) =>
        packet_type mustEqual PacketType.InviteCancelled
        error mustEqual OutfitMembershipResponse.Error.NoError
        variant mustEqual 0
        actor_id mustEqual 41593365
        target_id mustEqual 0
        actor_name mustEqual ""
        target_name mustEqual ""
        to_actor mustEqual true
      case _ =>
        ko
    }
  }

  "encode InviteCancelled" in {
    val msg = OutfitMembershipResponse(PacketType.InviteCancelled, OutfitMembershipResponse.Error.NoError, 0, 41593365, 0, "", "", to_actor = true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual inviteCancelled
  }

  "decode Kicked" in {
    PacketCoding.decodePacket(kicked).require match {
      case OutfitMembershipResponse(packet_type, error, variant, actor_id, target_id, actor_name, target_name, to_actor) =>
        packet_type mustEqual PacketType.Kicked
        error mustEqual OutfitMembershipResponse.Error.NoError
        variant mustEqual 1
        actor_id mustEqual 41593365
        target_id mustEqual 41605263
        actor_name mustEqual "PSFoutfittest1"
        target_name mustEqual ""
        to_actor mustEqual true
      case _ =>
        ko
    }
  }

  "encode Kicked" in {
    val msg = OutfitMembershipResponse(PacketType.Kicked, OutfitMembershipResponse.Error.NoError, 1, 41593365, 41605263, "PSFoutfittest1", "", to_actor = true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual kicked
  }

  private def roundTrips(msg: OutfitMembershipResponse): Boolean =
    PacketCoding.decodePacket(PacketCoding.encodePacket(msg).require.toByteVector).require == msg

  "decode unmodelled packet types 6 and 7" in {
    Seq(PacketType.Unk6, PacketType.Unk7).forall { packet_type =>
      roundTrips(OutfitMembershipResponse(packet_type, OutfitMembershipResponse.Error.NoError, 0, 1, 2, "a", "b", to_actor = true))
    } must beTrue
  }

  "decode error codes beyond the client's display table" in {
    Seq(
      OutfitMembershipResponse.Error.Unk22,
      OutfitMembershipResponse.Error.Unk25,
      OutfitMembershipResponse.Error.Unk31
    ).forall { error =>
      roundTrips(OutfitMembershipResponse(PacketType.Kicked, error, 0, 1, 2, "", "", to_actor = false))
    } must beTrue
  }
}

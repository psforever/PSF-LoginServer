// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.SquadRequestType
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client as manipulation protocol for squad and platoon members.
  * Answerable by a `SquadMembershipResponse` packet.
  * @param request_type the purpose of the request
  * @param char_id      a squad member unique identifier;
  *                     usually, the player being addresses by thie packet
  * @param unk3         na
  * @param player_name  name of the player being affected, if applicable
  * @param unk5         na
  */
final case class SquadMembershipRequest(
    request_type: SquadRequestType.Value,
    char_id: Long,
    unk3: Option[Long],
    player_name: String,
    unk5: Option[Option[String]]
) extends PlanetSideGamePacket {
  request_type match {
    case SquadRequestType.Accept | SquadRequestType.Reject | SquadRequestType.Disband | SquadRequestType.PlatoonAccept |
        SquadRequestType.PlatoonReject | SquadRequestType.PlatoonDisband =>
      assert(unk3.isEmpty, s"a $request_type request requires the unk3 field be undefined")
    case _ =>
      assert(unk3.nonEmpty, s"a $request_type request requires the unk3 field be defined")
  }
  if (request_type == SquadRequestType.Invite) {
    assert(unk5.nonEmpty, "an Invite request requires the unk5 field be defined")
  }

  type Packet = SquadMembershipRequest
  def opcode = GamePacketOpcode.SquadMembershipRequest
  def encode = SquadMembershipRequest.encode(this)
}

object SquadMembershipRequest extends Marshallable[SquadMembershipRequest] {
  implicit val codec: Codec[SquadMembershipRequest] = (("request_type" | SquadRequestType.codec) >>:~ { request_type =>
    ("unk2" | uint32L) ::
      conditional(
        request_type != SquadRequestType.Accept &&
          request_type != SquadRequestType.Reject &&
          request_type != SquadRequestType.Disband &&
          request_type != SquadRequestType.PlatoonAccept &&
          request_type != SquadRequestType.PlatoonReject &&
          request_type != SquadRequestType.PlatoonDisband,
        "unk3" | uint32L
      ) ::
      (("player_name" | PacketHelpers.encodedWideStringAligned(4)) >>:~ { pname =>
      conditional(
        request_type == SquadRequestType.Invite,
        "unk5" | optional(bool, PacketHelpers.encodedWideStringAligned({ if (pname.length == 0) 3 else 7 }))
      ).hlist
    })
  }).as[SquadMembershipRequest]
}

// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.SquadRequestType
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the server as manipulation protocol for squad and platoon members.
  * Prompted by and answers for a `SquadMembershipRequest` packet.
  * @param request_type the purpose of the request
  * @param unk1 na
  * @param unk2 na
  * @param char_id a squad member unique identifier;
  *                usually, the player being addresses by thie packet
  * @param other_id another squad member's unique identifier;
  *                 may be the same as `char_id`
  * @param player_name name of the player being affected, if applicable
  * @param unk5 na
  * @param unk6 na;
  *             the internal field, the `Option[String]`, never seems to be set
  */
final case class SquadMembershipResponse(request_type : SquadRequestType.Value,
                                         unk1 : Int,
                                         unk2 : Int,
                                         char_id : Long,
                                         other_id : Option[Long],
                                         player_name : String,
                                         unk5 : Boolean,
                                         unk6 : Option[Option[String]])
  extends PlanetSideGamePacket {
  type Packet = SquadMembershipResponse
  def opcode = GamePacketOpcode.SquadMembershipResponse
  def encode = SquadMembershipResponse.encode(this)
}

object SquadMembershipResponse extends Marshallable[SquadMembershipResponse] {
  implicit val codec : Codec[SquadMembershipResponse] = (
    "request_type" | SquadRequestType.codec >>:~ { d =>
      ("unk1" | uint(5)) ::
        ("unk2" | uint2) ::
        ("char_id" | uint32L) ::
        ("other_id" | conditional(d != SquadRequestType.Promote && d != SquadRequestType.PlatoonLeave, uint32L)) ::
        ("player_name" | PacketHelpers.encodedWideStringAligned(5)) ::
        ("unk5" | bool) ::
        conditional(d != SquadRequestType.Invite, optional(bool, "unk6" | PacketHelpers.encodedWideStringAligned(6)))
    }
    ).as[SquadMembershipResponse]
}

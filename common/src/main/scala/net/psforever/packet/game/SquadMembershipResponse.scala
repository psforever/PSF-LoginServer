// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.SquadRequestType
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param request_type the type of request being answered
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  * @param player_name the player being affected, if applicable
  * @param unk5 na
  * @param unk6 na
  */
final case class SquadMembershipResponse(request_type : SquadRequestType.Value,
                                         unk1 : Int,
                                         unk2 : Int,
                                         unk3 : Long,
                                         unk4 : Option[Long],
                                         player_name : String,
                                         unk5 : Boolean,
                                         unk6 : Option[Option[String]])
  extends PlanetSideGamePacket {
  /*
    if(response_type != 6 && response_type != 12)
      assert(unk5.isDefined, "unk5 field required")
    else
      assert(!unk5.isDefined, "unk5 defined but unk1 invalid value")
  */
  type Packet = SquadMembershipResponse
  def opcode = GamePacketOpcode.SquadMembershipResponse
  def encode = SquadMembershipResponse.encode(this)
}

object SquadMembershipResponse extends Marshallable[SquadMembershipResponse] {
  implicit val codec : Codec[SquadMembershipResponse] = (
    "request_type" | SquadRequestType.codec >>:~ { d =>
      ("unk1" | uint(5)) ::
        ("unk2" | uint2) ::
        ("unk3" | uint32L) ::
        ("unk4" | conditional(d != SquadRequestType.Promote && d != SquadRequestType.PlatoonLeave, uint32L)) ::
        ("player_name" | PacketHelpers.encodedWideStringAligned(5)) ::
        ("unk5" | bool) ::
        conditional(d != SquadRequestType.Invite, optional(bool, "unk6" | PacketHelpers.encodedWideStringAligned(6)))
    }
    ).as[SquadMembershipResponse]
}

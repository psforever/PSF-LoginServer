// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.SquadResponseType
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the server as message generation protocol for squad and platoon members.
  * Prompted by and answers for a `SquadMembershipRequest` packet.
  * @param request_type the purpose of the request
  * @param unk1 na
  * @param unk2 na
  * @param char_id a squad member unique identifier;
  *                usually, the player being addresses by thie packet
  * @param other_id another squad member's unique identifier;
  *                 may be the same as `char_id`
  * @param player_name name of the player being affected, if applicable
  * @param unk5 adjusts the nature of the request-type response based on the message recipient
  * @param unk6 na;
  *             the internal field, the `Option[String]`, never seems to be set
  * <br>
  * `request_type` (enum value) / `unk5` state (`false`/`true`)<br>
  * ----------------------------------------<br>
  *  - `Invite` (0)<br>
  * false => [PROMPT] "`player_name` has invited you into a squad." [YES/NO]<br>
  * true => "You have invited `player_name` to join your squad."<br>
  *  - `Unk01` (1)<br>
  * false => n/a<br>
  * true => n/a<br>
  *  - `Accept` (2)<br>
  * false => "`player_name` has accepted your invitation to join into your squad.<br>
  *   "You have formed a squad and are now that squad's commander." (if first time)<br>
  * true => "You have accepted an invitation to join a squad."<br>
  *   "You have successfully joined a squad for the first time." (if first time)<br>
  *  - `Reject` (3)<br>
  * false => "`player_name` does not want to join your squad at this time."<br>
  * true => "You have declined an invitation to join a squad."<br>
  *  - `Cancel` (4)<br>
  * false => "`player_name` has withdrawn his invitation."<br>
  * true => "You have canceled your invitation to `player_name`."<br>
  *  - `Leave` (5)<br>
  * false => "The Squad Leader has kicked you out of the squad."<br>
  * true => "You have kicked `player_name` out of the squad."<br>
  *  - `Disband` (6)<br>
  * false => "The squad has been disbanded."<br>
  * true => "You have disbanded the squad."<br>
  *  - `PlatoonInvite` (7)<br>
  * false => [PROMPT] "`player_name` has invited you into a platoon." [YES/NO]<br>
  * true => "You have invited `player_name`'s squad to join your platoon."<br>
  *  - `PlatoonAccept` (8)
  * false => "`player_name` has accepted your invitation to join into your platoon.<br>
  *   "You have formed a platoon and are now that platoon commander." (if first time)<br>
  * true => "You have accepted an invitation to join a platoon."<br>
  *   "You have successfully joined a platoon for the first time." (if first time)<br>
  *  - `PlatoonReject` (9)<br>
  * false => "`player_name` does not want to join your platoon at this time."<br>
  * true => "You have declined an invitation to join a platoon."<br>
  *  - `PlatoonCancel` (10)<br>
  * false => "`player_name` has withdrawn his invitation."<br>
  * true => "You have declined your invitation to `player_name`." (nonsense?)<br>
  *  - `PlatoonLeave` (11)<br>
  * false => "The Platoon Leader has kicked you out of the platoon."<br>
  * true => "You have kicked `player_name`'s squad out of the platoon."<br>
  *  - `PlatoonDisband` (12)<br>
  * false => "The platoon has been disbanded."<br>
  * true => "You have disbanded the platoon."
  */
final case class SquadMembershipResponse(
    request_type: SquadResponseType.Value,
    unk1: Int,
    unk2: Int,
    char_id: Long,
    other_id: Option[Long],
    player_name: String,
    unk5: Boolean,
    unk6: Option[Option[String]]
) extends PlanetSideGamePacket {
  type Packet = SquadMembershipResponse
  def opcode = GamePacketOpcode.SquadMembershipResponse
  def encode = SquadMembershipResponse.encode(this)
}

object SquadMembershipResponse extends Marshallable[SquadMembershipResponse] {
  implicit val codec: Codec[SquadMembershipResponse] = (
    "request_type" | SquadResponseType.codec >>:~ { d =>
      ("unk1" | uint(5)) ::
        ("unk2" | uint2) ::
        ("char_id" | uint32L) ::
        ("other_id" | conditional(d != SquadResponseType.Disband && d != SquadResponseType.PlatoonDisband, uint32L)) ::
        ("player_name" | PacketHelpers.encodedWideStringAligned(5)) ::
        ("unk5" | bool) ::
        conditional(d != SquadResponseType.Invite, optional(bool, "unk6" | PacketHelpers.encodedWideStringAligned(6)))
    }
  ).as[SquadMembershipResponse]
}

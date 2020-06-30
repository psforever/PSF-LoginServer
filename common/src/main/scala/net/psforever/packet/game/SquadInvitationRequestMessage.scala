// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * A message for communicating squad invitation.
  * When received by a client, the event message "You have invited `name` to join your squad" is produced
  * and a `SquadMembershipRequest` packet of type `Invite`
  * using `char_id` as the optional unique character identifier field is dispatched to the server.
  * The message is equivalent to a dispatched packet of type `SquadMembershipResponse`
  * with an `Invite` event with the referral field set to `true`.
  * @see `SquadMembershipResponse`
  * @param squad_guid the squad's GUID
  * @param slot a potentially valid slot index;
  *             0-9; higher numbers produce no response
  * @param char_id the unique character identifier
  * @param name the character's name;
  *             frequently, though that does not produce a coherent message,
  *             the avatar's own name is supplied in the event message instead of the name of another player
  */
final case class SquadInvitationRequestMessage(squad_guid: PlanetSideGUID, slot: Int, char_id: Long, name: String)
    extends PlanetSideGamePacket {
  type Packet = SquadInvitationRequestMessage
  def opcode = GamePacketOpcode.SquadInvitationRequestMessage
  def encode = SquadInvitationRequestMessage.encode(this)
}

object SquadInvitationRequestMessage extends Marshallable[SquadInvitationRequestMessage] {
  implicit val codec: Codec[SquadInvitationRequestMessage] = (
    ("squad_guid" | PlanetSideGUID.codec) ::
      ("slot" | uint4) ::
      ("char_id" | uint32L) ::
      ("name" | PacketHelpers.encodedWideStringAligned(adjustment = 4))
  ).as[SquadInvitationRequestMessage]
}

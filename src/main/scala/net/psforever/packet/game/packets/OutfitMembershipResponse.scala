// Copyright (c) 2025 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package net.psforever.packet.game.packets

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec}
import shapeless.{::, HNil}

/**
  * The `OutfitMembershipResponse` packet (game opcode `0x8D`) is the world server's response
  * to outfit membership operations (creating an outfit; inviting, accepting, rejecting,
  * cancelling or kicking members).
  *
  * '''Client behavior''' (static analysis of the shipped 3.15.84 client, decode `0x8a62b0`,
  * dispatch case `0x44e544` calling `ClientOutfitManagerHandleOutfitMembershipResponse`
  * at `0x4c72c0`):
  *
  *   * `packet_type` selects a response family (the client only implements codes `0`-`5`;
  *     `6` and `7` are dropped);
  *   * `error` selects the outcome within that family: `0` is the normal path, and
  *     `1`-`21` display one of the localized `@OutfitError*` notifications instead
  *     (the client-side strings are compiled in at `0xc64f54`-`0xc6519c`);
  *   * `variant` picks between equally-valid wordings inside a family; the client
  *     maps the values to distinct localized notifications: the invitation families
  *     know a *TimedOut* wording (`variant == 2` — `@OutfitResponse*InvitationTimedOut`),
  *     a *Disconnected* wording (`variant == 4` — `@OutfitResponse*InvitationDisconnected`),
  *     the plain wording (`0`/`1`), and a silent variant (`3`, no notification at all);
  *   * `is_actor` states whom the message is for:
  *     `false` - the acted-upon player (they see the invitation dialog, and their own
  *     accept/kick announcements), `true` - the acting player (the outfit leader or
  *     inviter, who sees confirmations of what they did);
  *   * `actor_name`/`target_name` interpolate the actor's and the outfit's/player's names
  *     into those notifications, and the two ids name the affected avatar and outfit in each
  *     family (for `CreateResponse`, the outfit's id; for the invitation families, the
  *     inviter and the invited avatar ids).
  *
  * The captured stock-server traffic shows `CreateResponse`, invite, accepted and
  * rejected-via-dialog samples, all with `error = 0`.
  *
  * @param packet_type the response family
  * @param error       a nonzero value displays the corresponding `@OutfitError*`
  *                     notification instead of the family's normal wording;
  *                     `0` follows the normal path
  * @param variant     selects between wording variants within one family
  * @param actor_id    the first context id of the family (outfit id, or acting avatar id)
  * @param target_id   the second context id of the family (the acted-upon avatar id)
  * @param actor_name  the name displayed for the acting player of the notification
  * @param target_name the name displayed for the outfit or acted-upon player
  * @param to_actor    the intended recipient of the message:
  *                     `true` - the acting/inviting player; `false` - the acted-upon player
  */
final case class OutfitMembershipResponse(
    packet_type: OutfitMembershipResponse.PacketType.Type,
    error: OutfitMembershipResponse.Error.Type,
    variant: Int,
    actor_id: Long,
    target_id: Long,
    actor_name: String,
    target_name: String,
    to_actor: Boolean
  ) extends PlanetSideGamePacket {
  type Packet = OutfitMembershipResponse

  def opcode: Type = GamePacketOpcode.OutfitMembershipResponse

  def encode: Attempt[BitVector] = OutfitMembershipResponse.encode(this)
}

object OutfitMembershipResponse extends Marshallable[OutfitMembershipResponse] {

  object PacketType extends Enumeration {
    type Type = Value

    /**
      * A catch-all response family; the client refreshes its outfit-window state
      * without displaying a notification.
      */
    val CreateResponse: PacketType.Value = Value(0)
    val Invite: PacketType.Value = Value(1) // an invitation extended to the receiving player
    val InviteAccepted: PacketType.Value = Value(2) // the invitation was accepted
    val InviteRejected: PacketType.Value = Value(3) // the invitation was rejected (the inviter's side reads variant 2 = timed out, 4 = invitee disconnected; the invited player's side ignores the variant)
    val InviteCancelled: PacketType.Value = Value(4) // the invitation was cancelled (the invited player's side reads variant 2 = timed out, 4 = themselves disconnected; the inviter's side ignores the variant)
    val Kicked: PacketType.Value = Value(5) // a member was kicked from the outfit
    val Unk6: PacketType.Value = Value(6) // 6 and 7 observed in stock era captures, validity unknown
    val Unk7: PacketType.Value = Value(7)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  /**
    * The `@OutfitError*` notification codes the client displays wherever an outfit
    * operation failed. Nonzero codes follow the client's error table
    * (`ClientOutfitManagerHandleOutfitMembershipResponse`, outer switch on the 5-bit field).
    */
  object Error extends Enumeration {
    type Type = Value

    val NoError: Error.Value = Value(0) // normal path - the notification of the packet_type family applies
    val GenericError: Error.Value = Value(1) // format OutfitError
    val NotInitialized: Error.Value = Value(2) // format OutfitErrorNotInitialized
    val NoPlayer: Error.Value = Value(3) // format OutfitErrorNoPlayer
    val SamePlayer: Error.Value = Value(4) // format OutfitErrorSamePlayer
    val AlreadyInvited: Error.Value = Value(5) // format OutfitErrorAlreadyInvited
    val AlreadyCreatingOutfit: Error.Value = Value(6) // format OutfitErrorAlreadyCreatingOutfit
    val AlreadyInOutfit: Error.Value = Value(7) // format OutfitErrorAlreadyInOutfit
    val DifferentEmpires: Error.Value = Value(8) // format OutfitErrorDifferentEmpires
    val NotInSameOutfit: Error.Value = Value(9) // format OutfitErrorNotInSameOutfit
    val TooManyOutfits: Error.Value = Value(10) // format OutfitErrorTooManyOutfits
    val TooManyMembers: Error.Value = Value(11) // format OutfitErrorTooManyMembers
    val NotOutfitLeader: Error.Value = Value(12) // format OutfitErrorNotOutfitLeader
    val NotOutfitOfficer: Error.Value = Value(13) // format OutfitErrorNotOutfitOfficer
    val NotOutfitMember: Error.Value = Value(14) // format OutfitErrorNotOutfitMember
    val NotInvited: Error.Value = Value(15) // format OutfitErrorNotInvited
    val NameIsInvalid: Error.Value = Value(16) // format OutfitErrorNameIsInvalid
    val NameAlreadyTaken: Error.Value = Value(17) // format OutfitErrorNameAlreadyTaken
    val NotSquadLeader: Error.Value = Value(18) // format OutfitErrorNotSquadLeader
    val NotEnoughSquadMembers: Error.Value = Value(19) // format OutfitErrorNotEnoughSquadMember
    val SquadMembersInOutfit: Error.Value = Value(20) // format OutfitErrorSquadMembersInOutfit
    val DatabaseError: Error.Value = Value(21) // format OutfitErrorDatabaseError
    // 22-31 have no display of their own in the client's membership-response handler
    // (its error switch ends at 21); the entries carry no modelled meaning, but keep
    // stray codes decodable instead of failing the enumeration's bounds check
    val Unk22: Error.Value = Value(22)
    val Unk23: Error.Value = Value(23)
    val Unk24: Error.Value = Value(24)
    val Unk25: Error.Value = Value(25)
    val Unk26: Error.Value = Value(26)
    val Unk27: Error.Value = Value(27)
    val Unk28: Error.Value = Value(28)
    val Unk29: Error.Value = Value(29)
    val Unk30: Error.Value = Value(30)
    val Unk31: Error.Value = Value(31)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(5))
  }

  implicit val codec: Codec[OutfitMembershipResponse] = (
    ("packet_type" | PacketType.codec) ::
    ("error" | Error.codec) ::
    ("variant" | uintL(3)) ::
    ("actor_id" | uint32L) ::
    ("target_id" | uint32L) ::
    ("actor_name" | PacketHelpers.encodedWideStringAligned(5)) ::
    ("target_name" | PacketHelpers.encodedWideString) ::
    ("to_actor" | bool)
  ).xmap[OutfitMembershipResponse](
    {
      case packet_type :: error :: variant :: actor_id :: target_id :: actor_name :: target_name :: to_actor :: HNil =>
        OutfitMembershipResponse(packet_type, error, variant, actor_id, target_id, actor_name, target_name, to_actor)
    },
    {
      case OutfitMembershipResponse(packet_type, error, variant, actor_id, target_id, actor_name, target_name, to_actor) =>
        packet_type :: error :: variant :: actor_id :: target_id :: actor_name :: target_name :: to_actor :: HNil
    }
  )
}

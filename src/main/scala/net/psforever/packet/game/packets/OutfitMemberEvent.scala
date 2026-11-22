// Copyright (c) 2025 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package net.psforever.packet.game.packets

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * The `OutfitMemberEvent` packet (game opcode `0x90`) is sent by the world server to outfit
  * members to update the state of one member of their outfit.
  * '''Client behavior''' (static analysis of the shipped 3.15.84 client, decode `0x8a5de0`,
  * dispatch case `0x44e5a7` calling `ClientOutfitManagerHandleOutfitMemberEvent` at `0x4c79b0`):
  * the client stores each member of the outfit in a per-outfit map keyed by the packet's
  * `member_id`. The `Update` action creates or refreshes one member row in the outfit
  * window; the `Kicked` action removes it.
  *
  * The captured stock-server traffic shows `Update` records for individual members of a
  * fully populated outfit.
  *
  * @param outfit_id na; the outfit that both the receiver and the updated member belong to
  * @param member_id na; the member (avatar id) the record describes
  * @param action na; how the member entry should be applied
  */
final case class OutfitMemberEvent(
    outfit_id: Long,
    member_id: Long,
    action: OutfitMemberEventAction
  ) extends PlanetSideGamePacket {
  type Packet = OutfitMemberEvent

  def opcode: Type = GamePacketOpcode.OutfitMemberEvent

  def encode: Attempt[BitVector] = OutfitMemberEvent.encode(this)
}

abstract class OutfitMemberEventAction(val code: Int)
object OutfitMemberEventAction {

  /**
    *
    * Update
    *
    * Update is used to inform outfit members about a new member.
    * It is sent after an invite acceptance, or for a rank change; the client reuses the
    * same wire shape for every row of the outfit window's member list.
    *
    * ---- the invitation-state discovery (2026-11-22) ----
    *
    * A record is either a plain member row, or a *pending invitation* row:
    *
    *   * `joined = true`: the member belongs to the outfit; the row is displayed
    *     for every outfit member.
    *   * `joined = false`: an invitation that has not been accepted (yet). The
    *     trailing 32-bit value is the *inviting player's* char id; the client
    *     draws the row with the invitation-state columns of the outfit window
    *     (`HasNotReplied` / `HasDeclined` / `HasAccepted`, member-row builder
    *     `0x4c97b0`) and only for the inviter himself - a pending row whose
    *     inviter id does not match the local avatar id is removed from the
    *     member list (`0x4cb160`).
    *
    * The captured stock-server traffic shows only `joined = true` rows.
    *
    * @param member_name the name of the newly-added or updated member
    * @param rank        the outfit rank of the member (`0`-`7`)
    * @param points      the outfit points of the member (the client displays this divided by `100`)
    * @param last_online the number of seconds since the member was last online, `0` if they are online
    * @param joined      `true` for a member of the outfit, `false` for a pending invitation
    *                    (which carries the trailing `invited_by` value and ends the record otherwise)
    * @param invited_by  the *inviting player's* char id, present only for pending
    *                    invitation records (`joined = false`). The invitation row is only
    *                    displayed on the inviter's client.
    */
  final case class Update(
    member_name: String,
    rank: Int,
    points: Long,
    last_online: Long,
    joined: Boolean,
    invited_by: Option[Long],
  ) extends OutfitMemberEventAction(code = 0)

  /**
    * The member was removed from the outfit.
    */
  final case class Kicked(
                         ) extends OutfitMemberEventAction(code = 1)

  final case class Unknown(badCode: Int, data: BitVector) extends OutfitMemberEventAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val UpdateCodec: Codec[Update] = (
      ("member_name" | PacketHelpers.encodedWideStringAligned(6)) ::
      ("rank" | uint(3)) ::
      ("points" | uint32L) ::
      ("last_online" | uint32L) ::
      (("joined" | bool) >>:~ { joined =>
        ("invited_by" | conditional(!joined, uint32L)).hlist
      })
    ).xmap[Update](
      {
        case member_name :: rank :: points :: last_online :: joined :: invited_by :: HNil =>
          Update(member_name, rank, points, last_online, joined, invited_by)
      },
      {
        case Update(member_name, rank, points, last_online, joined, invited_by) =>
          member_name :: rank :: points :: last_online :: joined :: invited_by :: HNil
      }
    )

    val KickedCodec: Codec[Kicked] = PacketHelpers.emptyCodec(Kicked())

    /**
      * A common form for known action code indexes with an unknown purpose and transformation is an "Unknown" object.
      * @param action the action behavior code
      * @return a transformation between the action code and the unknown bit data
      */
    def unknownCodec(action: Int): Codec[Unknown] =
      bits.xmap[Unknown](
        data => Unknown(action, data),
        {
          case Unknown(_, data) => data
        }
      )

    /**
      * The action code was completely unanticipated!
      * @param action the action behavior code
      * @return nothing; always fail
      */
    def failureCodec(action: Int): Codec[OutfitMemberEventAction] =
      everFailCondition.exmap[OutfitMemberEventAction](
        _ => Attempt.failure(Err(s"can not decode $action-type info - what is this thing?")),
        _ => Attempt.failure(Err(s"can not encode $action-type info - no such thing"))
      )
  }
}

object OutfitMemberEvent extends Marshallable[OutfitMemberEvent] {

  object PacketType extends Enumeration {
    type Type = Value

    val Update: PacketType.Value = Value(0)
    val Kicked: PacketType.Value = Value(1)
    val Unk2: PacketType.Value = Value(2)
    val Unk3: PacketType.Value = Value(3)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(2))
  }

  private def selectFromType(code: Int): Codec[OutfitMemberEventAction] = {
    import OutfitMemberEventAction.Codecs._

    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => UpdateCodec
      case 1 => KickedCodec
      case 2 => unknownCodec(code)
      case 3 => unknownCodec(code)

      case _ => failureCodec(code)
    }).asInstanceOf[Codec[OutfitMemberEventAction]]
  }

  implicit val codec: Codec[OutfitMemberEvent] = (
    ("packet_type" | PacketType.codec) >>:~ { packet_type =>
      ("outfit_id" | uint32L) ::
      ("member_id" | uint32L) ::
      ("action" | selectFromType(packet_type.id)).hlist
    }
  ).xmap[OutfitMemberEvent](
    {
      case _ :: outfit_id :: member_id:: action :: HNil =>
        OutfitMemberEvent(outfit_id, member_id, action)
    },
    {
      case OutfitMemberEvent(outfit_id, member_id, action) =>
        OutfitMemberEvent.PacketType(action.code) :: outfit_id :: member_id :: action :: HNil
    }
  )
}

// Copyright (c) 2025 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package net.psforever.packet.game.packets

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

final case class OutfitEvent(
    outfit_id: Long,
    action: OutfitEventAction
  ) extends PlanetSideGamePacket {
  type Packet = OutfitEvent

  def opcode: Type = GamePacketOpcode.OutfitEvent

  def encode: Attempt[BitVector] = OutfitEvent.encode(this)
}

abstract class OutfitEventAction(val code: Int)

object OutfitEventAction {

  final case class OutfitRankNames(
    rank1: String,
    rank2: String,
    rank3: String,
    rank4: String,
    rank5: String,
    rank6: String,
    rank7: String,
    rank8: String,
  )

  final case class OutfitInfo(
    outfit_name: String,
    outfit_points1: Long,
    outfit_points2: Long, // same as outfit_points1
    member_count: Long,
    outfit_rank_names: OutfitRankNames,
    motd: String,
    /* decal_id (u8, stored at outfit entry +0x80): the outfit's decal (logo) id -
    *   the DB schema's outfit.decal column carries the same concept
    *   (CHECK BETWEEN 0 AND 26, the client's OutfitLogoSelection feature).
    */
    decal_id: Int,
    /* unk11/unk12: when the flag is clear, the client stores "now + unk12 seconds"
    *   on the outfit entry (+0xac) - a running deadline, matching the "
    *   @OutfitCreateRemainingTime" wording of the outfit-creation UI; when set, a
    *   counterpart flag field (+0xa8) becomes 1. Best available reading: the outfit's
    *   formation/creation countdown state. */
    formation_completed: Boolean,
    formation_deadline: Long, // only set if formation_completed is false
    created_timestamp: Long,
    /* owned_base_zone_id / owned_base_facility_id travel into the outfit entry (+0xb0/+0xb4)
    *   through the entry's two-argument vtable method (+0x10); +0xb0 is resolved to a
    *   localized name (zone-data manager lookup, see the `OutfitBenefitMessage` 0xCA path),
    *   so it names a zone/facility the outfit has a standing benefit with.
    *  owned_base_benefit_timeout: stored as "now + unk25 seconds" (+0xb8) when the benefit
    *   id is nonzero - a remaining-seconds value for the benefit. */
    owned_base_zone_id: Long,
    owned_base_facility_id: Long,
    owned_base_benefit_timeout: Long,
  )

  /**
    * Initial
    *
    * Send at the start of an OutfitWindow info dump.
    *
    * Not always complete, seen as an initialization after login, join or while outfit is in formation.
    * @param outfit_info
    */
  final case class Initial(
    outfit_info: OutfitInfo
  ) extends OutfitEventAction(code = 0)

  final case class ListEnd(
  ) extends OutfitEventAction(code = 1)

  /**
    * Update
    *
    * Send after changing outfit Ranks, MOTD and other situations.
    * @param outfit_info
    */
  final case class Update(
    outfit_info: OutfitInfo,
  ) extends OutfitEventAction(code = 2)

  /**
    * Send to players to tell them they left the outfit.
    *
    * Resets them to behave like they have no outfit.
    * Will have them open the OutfitListWindow instead of the OutfitWindow.
    */
  final case class Leaving(
  ) extends OutfitEventAction(code = 3)

  /**
    * Used to switch from the temporary "invalid" outfit ID used while formation to a valid ID used from that point on.
    * @param new_outfit_id the new ID that represents this specific outfit in the DB
    */
  final case class UpdateOutfitId(
    new_outfit_id: Long,
  ) extends OutfitEventAction(code = 4)

  /**
    * Used to tell outfit members that the member count changed.
    * Send after InviteAccept or Kick actions
    * @param member_count
    */
  final case class UpdateMemberCount(
    member_count: Long,
  ) extends OutfitEventAction(code = 5)

  /**
    * A trailing outfit event observed in stock-server packet logs: roughly 15
    * to 20 seconds after an outfit was created, an action-6 event naming that
    * outfit is sent back to the player who created it - a confirmation of that
    * client's own creation request, and to no other outfit member.
    * Most plausibly the stock server echoed the asynchronous database
    * persistence of the new outfit row.
    */
  final case class FormationPersistedNotify(
  ) extends OutfitEventAction(code = 6)

  /**
    * A trailing outfit event observed in stock-server packet logs: roughly 15
    * to 20 seconds after a member joined, left, or was kicked from an outfit,
    * an action-7 event naming that outfit is sent back to the player who
    * performed the change - the accepting joiner, the leaver, or the kicking
    * member respectively, as a confirmation of that client's own request, and
    * to no other outfit member.
    * Most plausibly the stock server echoed the asynchronous database
    * persistence of the new outfit row.
    */
  final case class MembershipPersistedNotify(
  ) extends OutfitEventAction(code = 7)

  final case class Unknown(badCode: Int, data: BitVector) extends OutfitEventAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    private val OutfitRankNamesCodec: Codec[OutfitRankNames] = (
      PacketHelpers.encodedWideString ::
      PacketHelpers.encodedWideString ::
      PacketHelpers.encodedWideString ::
      PacketHelpers.encodedWideString ::
      PacketHelpers.encodedWideString ::
      PacketHelpers.encodedWideString ::
      PacketHelpers.encodedWideString ::
      PacketHelpers.encodedWideString
    ).xmap[OutfitRankNames](
      {
        case u0 :: u1 :: u2 :: u3 :: u4 :: u5 :: u6 :: u7 :: HNil =>
          OutfitRankNames(u0, u1, u2, u3, u4, u5, u6, u7)
      },
      {
        case OutfitRankNames(u0, u1, u2, u3, u4, u5, u6, u7) =>
          u0 :: u1 :: u2 :: u3 :: u4 :: u5 :: u6 :: u7 :: HNil
      }
    )

    private val InfoCodec: Codec[OutfitInfo] = (
      ("outfit_name" | PacketHelpers.encodedWideStringAligned(5)) ::
      ("outfit_points1" | uint32L) ::
      ("outfit_points2" | uint32L) ::
      ("member_count" | uint32L) ::
      ("outfit_rank_names" | OutfitRankNamesCodec) ::
      ("motd" | PacketHelpers.encodedWideString) ::
      ("decal_id" | uint8L) ::
      ("formation_completed" | bool) ::
      ("formation_deadline" | uint32L) ::
      ("created_timestamp" | uint32L) ::
      ("owned_base_zone_id" | uint32L) ::
      ("owned_base_facility_id" | uint32L) ::
      ("owned_base_benefit_timeout" | uint32L)
    ).xmap[OutfitInfo](
        {
          case outfit_name :: outfit_points1 :: outfit_points2 :: member_count :: outfit_rank_names :: motd :: decal_id :: formation_completed :: formation_deadline :: created_timestamp :: owned_base_zone_id :: owned_base_facility_id :: owned_base_benefit_timeout :: HNil =>
            OutfitInfo(outfit_name, outfit_points1, outfit_points2, member_count, outfit_rank_names, motd, decal_id, formation_completed, formation_deadline, created_timestamp, owned_base_zone_id, owned_base_facility_id, owned_base_benefit_timeout)
        },
        {
          case OutfitInfo(outfit_name, outfit_points1, outfit_points2, member_count, outfit_rank_names, motd, decal_id, formation_completed, formation_deadline, created_timestamp, owned_base_zone_id, owned_base_facility_id, owned_base_benefit_timeout) =>
            outfit_name :: outfit_points1 :: outfit_points2 :: member_count :: outfit_rank_names :: motd :: decal_id :: formation_completed :: formation_deadline :: created_timestamp :: owned_base_zone_id :: owned_base_facility_id :: owned_base_benefit_timeout :: HNil
        }
      )

    val InitialCodec: Codec[Initial] = (
      ("outfit_info" | InfoCodec)
    ).xmap[Initial](
      {
        case info =>
          Initial(info)
      },
      {
        case Initial(info) =>
          info
      }
    )

    val Unk1Codec: Codec[ListEnd] = PacketHelpers.emptyCodec(ListEnd())

    val UpdateCodec: Codec[Update] = (
      ("outfit_info" | InfoCodec)
    ).xmap[Update](
      {
        case info =>
          Update(info)
      },
      {
        case Update(info) =>
          info
      }
    )

    val LeavingCodec: Codec[Leaving] = PacketHelpers.emptyCodec(Leaving())

    val UpdateOutfitIdCodec: Codec[UpdateOutfitId] = ( // update outfit_id? // 2016.03.18 #10640 // after this packet the referenced id changes to the new one, old is not used again
      ("new_outfit_id" | uint32L)
    ).xmap[UpdateOutfitId](
      {
        case new_outfit_id =>
          UpdateOutfitId(new_outfit_id)
      },
      {
        case UpdateOutfitId(new_outfit_id) =>
          new_outfit_id
      }
    )

    val UpdateMemberCountCodec: Codec[UpdateMemberCount] = (
      ("" | uint32L)
    ).xmap[UpdateMemberCount](
      {
        case u1 =>
          UpdateMemberCount(u1)
      },
      {
        case UpdateMemberCount(u1) =>
          u1
      }
    )

    val FormationPersistedNotifyCodec: Codec[FormationPersistedNotify] = PacketHelpers.emptyCodec(FormationPersistedNotify())

    val MembershipPersistedNotifyCodec: Codec[MembershipPersistedNotify] = PacketHelpers.emptyCodec(MembershipPersistedNotify())

    /**
      * A common form for known action code indexes with an unknown purpose and transformation is an "Unknown" object.
      *
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
      *
      * @param action the action behavior code
      * @return nothing; always fail
      */
    def failureCodec(action: Int): Codec[OutfitEventAction] =
      everFailCondition.exmap[OutfitEventAction](
        _ => Attempt.failure(Err(s"can not match a codec pattern for decoding $action")),
        _ => Attempt.failure(Err(s"can not match a codec pattern for encoding $action"))
      )
  }
}

object OutfitEvent extends Marshallable[OutfitEvent] {

  object PacketType extends Enumeration {
    type Type = Value

    val Initial: PacketType.Value = Value(0)
    val Unk1: PacketType.Value = Value(1) // end listing of members
    val Update: PacketType.Value = Value(2)
    val Leaving: PacketType.Value = Value(3)
    val UpdateOutfitId: PacketType.Value = Value(4)
    val UpdateMemberCount: PacketType.Value = Value(5)
    val FormationPersistedNotify: PacketType.Value = Value(6)
    val MemberUpdatePersistedNotify: PacketType.Value = Value(7)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  private def selectFromType(code: Int): Codec[OutfitEventAction] = {
    import OutfitEventAction.Codecs._

    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => InitialCodec
      case 1 => Unk1Codec
      case 2 => UpdateCodec
      case 3 => LeavingCodec
      case 4 => UpdateOutfitIdCodec
      case 5 => UpdateMemberCountCodec
      case 6 => FormationPersistedNotifyCodec // included for live server parity
      case 7 => MembershipPersistedNotifyCodec // included for live server parity

      case _ => failureCodec(code)
    }).asInstanceOf[Codec[OutfitEventAction]]
  }

  implicit val codec: Codec[OutfitEvent] = (
    ("packet_type" | PacketType.codec) >>:~ { packet_type =>
      ("outfit_guid" | uint32L) ::
      ("action" | selectFromType(packet_type.id))
    }
  ).xmap[OutfitEvent](
    {
      case _ :: outfit_guid :: action :: HNil =>
        OutfitEvent(outfit_guid, action)
    },
    {
      case OutfitEvent(outfit_guid, action) =>
        OutfitEvent.PacketType(action.code) :: outfit_guid :: action :: HNil
    }
  )
}

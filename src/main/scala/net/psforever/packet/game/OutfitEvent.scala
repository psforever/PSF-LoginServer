// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.bits.BitVector
import scodec.codecs._
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
    unk10: Int,
    unk11: Boolean,
    unk12: Long, // only set if unk11 is false
    created_timestamp: Long,
    unk23: Long,
    unk24: Long,
    unk25: Long,
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

  final case class Unk1(
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
      ("" | uint8L) ::
      ("" | bool) ::
      ("" | uint32L) ::
      ("created_timestamp" | uint32L) ::
      ("" | uint32L) ::
      ("" | uint32L) ::
      ("" | uint32L)
    ).xmap[OutfitInfo](
        {
          case outfit_name :: outfit_points1 :: outfit_points2 :: member_count :: outfit_rank_names :: motd :: u10 :: u11 :: u12 :: created_timestamp :: u23 :: u24 :: u25 :: HNil =>
            OutfitInfo(outfit_name, outfit_points1, outfit_points2, member_count, outfit_rank_names, motd, u10, u11, u12, created_timestamp, u23, u24, u25)
        },
        {
          case OutfitInfo(outfit_name, outfit_points1, outfit_points2, member_count, outfit_rank_names, motd, u10, u11, u12, created_timestamp, u23, u24, u25) =>
            outfit_name :: outfit_points1 :: outfit_points2 :: member_count :: outfit_rank_names :: motd :: u10 :: u11 :: u12 :: created_timestamp :: u23 :: u24 :: u25 :: HNil
        }
      )

    val Unk0Codec: Codec[Initial] = (
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

    val Unk1Codec: Codec[Unk1] = PacketHelpers.emptyCodec(Unk1())

    val Unk2Codec: Codec[Update] = (
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

    val Unk3Codec: Codec[Leaving] = PacketHelpers.emptyCodec(Leaving())

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

    val Unk0: PacketType.Value = Value(0) // start listing of members
    val Unk1: PacketType.Value = Value(1) // end listing of members
    val Unk2: PacketType.Value = Value(2) // send after creating an outfit // normal info, same as Unk0
    val Unk3: PacketType.Value = Value(3) // below
    val UpdateOutfitId: PacketType.Value = Value(4)
    val UpdateMemberCount: PacketType.Value = Value(5)
    val Unk6: PacketType.Value = Value(6)
    val Unk7: PacketType.Value = Value(7)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  private def selectFromType(code: Int): Codec[OutfitEventAction] = {
    import OutfitEventAction.Codecs._
    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => Unk0Codec // view outfit window and members
      case 1 => Unk1Codec
      case 2 => Unk2Codec // sent after /outfitcreate and on login if in an outfit
      case 3 => Unk3Codec
      case 4 => UpdateOutfitIdCodec
      case 5 => UpdateMemberCountCodec
      case 6 => unknownCodec(action = code)
      case 7 => unknownCodec(action = code)

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

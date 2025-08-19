// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitEvent(
    request_type: OutfitEvent.RequestType.Type,
    outfit_guid: Long,
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
    member_count: Int,
    unk9: Int,
    outfit_rank_names: OutfitRankNames,
    motd: String,
    owner_guid: PlanetSideGUID, // ?
    unk20: Int,
    unk21: Int,
    unk21_2: Int,
    created_timestamp: Long,
    unk23: Long,
    unk24: Long,
    unk25: Long,
    u123: Int,
  )

  final case class Unk0(
    outfitInfo: OutfitInfo
  ) extends OutfitEventAction(code = 0)

  final case class Unk1(
    unk2: Int,
    unk3: Boolean,
  ) extends OutfitEventAction(code = 1)

  final case class Unk2(
    outfitInfo: OutfitInfo,
  ) extends OutfitEventAction(code = 2)

  final case class Unk3(
    unk2: Int,
    unk3: Boolean,
    data: BitVector,
  ) extends OutfitEventAction(code = 3)

  final case class Unk4(
    new_outfit_id: Long,
    unk3: Int,
    unk4: Boolean,
    data: BitVector,
  ) extends OutfitEventAction(code = 4)

  final case class Unk5(
    unk1: Int,
    unk2: Int,
    unk3: Int,
    unk4: Boolean,
    data: BitVector,
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
        PacketHelpers.encodedWideStringAligned(5) ::
        uint32L ::
        uint32L ::
        uint16L ::
        uint16L ::
        OutfitRankNamesCodec ::
        PacketHelpers.encodedWideString ::
        PlanetSideGUID.codec ::
        uint16L ::  //
        uint8L ::   // bool somewhere here
        uintL(1) :: //
        ("created_timestamp" | uint32L) ::
        uint32L ::
        uint32L ::
        uint32L ::
        uintL(7)
      ).xmap[OutfitInfo](
        {
          case outfit_name :: u6 :: u7 :: member_count :: u9 :: outfit_rank_names :: motd :: u19 :: u20 :: u21 :: u21_2 :: created_timestamp :: u23 :: u24 :: u25 :: u123 :: HNil =>
            OutfitInfo(outfit_name, u6, u7, member_count, u9, outfit_rank_names, motd, u19, u20, u21, u21_2, created_timestamp, u23, u24, u25, u123)
        },
        {
          case OutfitInfo(outfit_name, u6, u7, member_count, u9, outfit_rank_names, motd, u19, u20, u21, u21_2, created_timestamp, u23, u24, u25, u123) =>
            outfit_name :: u6 :: u7 :: member_count :: u9 :: outfit_rank_names :: motd :: u19 :: u20 :: u21 :: u21_2 :: created_timestamp :: u23 :: u24 :: u25 :: u123 :: HNil
        }
      )

    val Unk0Codec: Codec[Unk0] = (
      InfoCodec
      ).xmap[Unk0](
      {
        case info =>
          Unk0(info)
      },
      {
        case Unk0(info) =>
          info
      }
    )

    val Unk1Codec: Codec[Unk1] = (
        uint4L ::
        bool
      ).xmap[Unk1](
      {
        case u2 :: u3 :: HNil =>
          Unk1(u2, u3)
      },
      {
        case Unk1(u2, u3) =>
          u2 :: u3 :: HNil
      }
    )

    val Unk2Codec: Codec[Unk2] = (
      InfoCodec
      ).xmap[Unk2](
      {
        case info =>
          Unk2(info)
      },
      {
        case Unk2(info) =>
          info
      }
    )

    val Unk3Codec: Codec[Unk3] = (
        uint4L ::
        bool ::
        bits
      ).xmap[Unk3](
      {
        case u2 :: u3 :: data :: HNil =>
          Unk3(u2, u3, data)
      },
      {
        case Unk3(u2, u3, data) =>
          u2 :: u3 :: data :: HNil
      }
    )

    val Unk4Codec: Codec[Unk4] = ( // update outfit_id? // 2016.03.18 #10640 // after this packet the referenced id changes to the new one, old is not used again
        uint32L :: // real / other outfit_id
        uint4L ::
        bool ::
        bits
      ).xmap[Unk4](
      {
        case new_outfit_id :: u3 :: u4 :: data :: HNil =>
          Unk4(new_outfit_id, u3, u4, data)
      },
      {
        case Unk4(new_outfit_id, u3, u4, data) =>
          new_outfit_id ::u3 :: u4 :: data :: HNil
      }
    )

    val Unk5Codec: Codec[Unk5] = (
        uint16L ::
        uint16L ::
        uint4L ::
        bool ::
        bits
      ).xmap[Unk5](
      {
        case u1 :: u2 :: u3 :: u4 :: data :: HNil =>
          Unk5(u1, u2, u3, u4, data)
      },
      {
        case Unk5(u1, u2, u3, u4, data) =>
          u1 :: u2 :: u3 :: u4 :: data :: HNil
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

  object RequestType extends Enumeration {
    type Type = Value

    val Unk0: RequestType.Value = Value(0) // start listing of members
    val Unk1: RequestType.Value = Value(1) // end listing of members
    val Unk2: RequestType.Value = Value(2) // send after creating an outfit // normal info, same as Unk0
    val Unk3: RequestType.Value = Value(3) // below
    val Unk4: RequestType.Value = Value(4)
    val Unk5: RequestType.Value = Value(5)
    val unk6: RequestType.Value = Value(6)
    val unk7: RequestType.Value = Value(7)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  private def selectFromType(code: Int): Codec[OutfitEventAction] = {
    import OutfitEventAction.Codecs._
    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => Unk0Codec
      case 1 => Unk1Codec
      case 2 => Unk2Codec // sent after /outfitcreate ?
      case 3 => Unk3Codec
      case 4 => Unk4Codec
      case 5 => Unk5Codec
      case 6 => unknownCodec(action = code)
      case 7 => unknownCodec(action = code)

      case _ => failureCodec(code)
    }).asInstanceOf[Codec[OutfitEventAction]]
  }

  implicit val codec: Codec[OutfitEvent] = (
    ("request_type" | RequestType.codec) >>:~ { request_type =>
      ("outfit_guid" | uint32L) ::
        ("action" | selectFromType(request_type.id))
    }
    ).xmap[OutfitEvent](
    {
      case request_type :: outfit_guid :: action :: HNil =>
        OutfitEvent(request_type, outfit_guid, action)
    },
    {
      case OutfitEvent(request_type, outfit_guid, action) =>
        request_type :: outfit_guid :: action :: HNil
    }
  )
}

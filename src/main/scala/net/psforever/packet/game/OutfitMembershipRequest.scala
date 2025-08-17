// Copyright (c) 2023-2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMembershipRequest(
    request_type: OutfitMembershipRequest.RequestType.Type,
    outfit_id: Long,
    action: OutfitMembershipRequestAction
  ) extends PlanetSideGamePacket {
  type Packet = OutfitMembershipRequest

  def opcode: Type = GamePacketOpcode.OutfitMembershipRequest

  def encode: Attempt[BitVector] = OutfitMembershipRequest.encode(this)
}

abstract class OutfitMembershipRequestAction(val code: Int)

object OutfitMembershipRequestAction {

  final case class CreateOutfit(
    unk2: String,
    unk3: Int,
    unk4: Boolean,
    outfit_name: String
  ) extends OutfitMembershipRequestAction(code = 0)

  final case class FormOutfit(
    unk2: String,
    unk3: Int,
    unk4: Boolean,
    outfit_name: String
  ) extends OutfitMembershipRequestAction(code = 1)

  final case class Unk2(
    unk2: Int,
    unk3: Int,
    member_name: String,
  ) extends OutfitMembershipRequestAction(code = 2)
  final case class AcceptOutfitInvite(
    unk2: String
  ) extends OutfitMembershipRequestAction(code = 3)

  final case class RejectOutfitInvite(
    unk2: String
  ) extends OutfitMembershipRequestAction(code = 4)

  final case class CancelOutfitInvite(
    unk5: Int,
    unk6: Int,
    outfit_name: String
  ) extends OutfitMembershipRequestAction(code = 5)

  final case class Unknown(badCode: Int, data: BitVector) extends OutfitMembershipRequestAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val CreateOutfitCodec: Codec[CreateOutfit] =
      (
        PacketHelpers.encodedWideString ::
          uint4L ::
          bool ::
          PacketHelpers.encodedWideString
        ).xmap[CreateOutfit](
        {
          case unk2 :: unk3 :: unk4 :: outfit_name :: HNil =>
            CreateOutfit(unk2, unk3, unk4, outfit_name)
        },
        {
          case CreateOutfit(unk2, unk3, unk4, outfit_name) =>
            unk2 :: unk3 :: unk4 :: outfit_name :: HNil
        }
      )

    val FormOutfitCodec: Codec[FormOutfit] =
      (
        PacketHelpers.encodedWideString ::
          uint4L ::
          bool ::
          PacketHelpers.encodedWideString
        ).xmap[FormOutfit](
        {
          case unk2 :: unk3 :: unk4 :: outfit_name :: HNil =>
            FormOutfit(unk2, unk3, unk4, outfit_name)
        },
        {
          case FormOutfit(unk2, unk3, unk4, outfit_name) =>
            unk2 :: unk3 :: unk4 :: outfit_name :: HNil
        }
      )

    val Unk2Codec: Codec[Unk2] =
      (
        uint16L ::
          uint16L ::
          PacketHelpers.encodedWideStringAligned(5)
        ).xmap[Unk2](
        {
          case unk2 :: unk3 :: member_name :: HNil =>
            Unk2(unk2, unk3, member_name)
        },
        {
          case Unk2(unk2, unk3, member_name) =>
            unk2 :: unk3 :: member_name :: HNil
        }
      )

    val AcceptOutfitCodec: Codec[AcceptOutfitInvite] =
      PacketHelpers.encodedWideString.xmap[AcceptOutfitInvite](
        {
          case unk2 =>
            AcceptOutfitInvite(unk2)
        },
        {
          case AcceptOutfitInvite(unk2) =>
            unk2
        }
      )

    val RejectOutfitCodec: Codec[RejectOutfitInvite] =
      PacketHelpers.encodedWideString.xmap[RejectOutfitInvite](
        {
          case unk2 =>
            RejectOutfitInvite(unk2)
        },
        {
          case RejectOutfitInvite(unk2) =>
            unk2
        }
      )

    val CancelOutfitCodec: Codec[CancelOutfitInvite] =
      (
        uint16L ::
          uint16L ::
          PacketHelpers.encodedWideStringAligned(5)
        ).xmap[CancelOutfitInvite](
        {
          case unk5 :: unk6 :: outfit_name :: HNil =>
            CancelOutfitInvite(unk5, unk6, outfit_name)
        },
        {
          case CancelOutfitInvite(unk5, unk6, outfit_name) =>
            unk5 :: unk6 :: outfit_name :: HNil
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
    def failureCodec(action: Int): Codec[OutfitMembershipRequestAction] =
      everFailCondition.exmap[OutfitMembershipRequestAction](
        _ => Attempt.failure(Err(s"can not match a codec pattern for decoding $action")),
        _ => Attempt.failure(Err(s"can not match a codec pattern for encoding $action"))
      )
  }
}

object OutfitMembershipRequest extends Marshallable[OutfitMembershipRequest] {

  object RequestType extends Enumeration {
    type Type = Value

    val Create: RequestType.Value = Value(0)
    val Form:   RequestType.Value = Value(1)
    val Unk2:   RequestType.Value = Value(2)
    val Accept: RequestType.Value = Value(3)
    val Reject: RequestType.Value = Value(4)
    val Cancel: RequestType.Value = Value(5)
    val Unk6:   RequestType.Value = Value(6) // 6 and 7 seen as failed decodes, validity unknown
    val Unk7:   RequestType.Value = Value(7)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  private def selectFromType(code: Int): Codec[OutfitMembershipRequestAction] = {
    import OutfitMembershipRequestAction.Codecs._
    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => CreateOutfitCodec
      case 1 => FormOutfitCodec // so far same as Create
      case 2 => Unk2Codec
      case 3 => AcceptOutfitCodec
      case 4 => RejectOutfitCodec // so far same as Accept
      case 5 => CancelOutfitCodec
      case 6 => unknownCodec(action = code)
      case 7 => unknownCodec(action = code)
      // 3 bit limit
      case _ => failureCodec(code)
    }).asInstanceOf[Codec[OutfitMembershipRequestAction]]
  }

  implicit val codec: Codec[OutfitMembershipRequest] = (
    ("request_type" | RequestType.codec) >>:~ { request_type =>
      ("outfit_id" | uint32L) ::
        ("action" | selectFromType(request_type.id))
    }
  ).xmap[OutfitMembershipRequest](
    {
      case request_type :: outfit_id :: action :: HNil =>
        OutfitMembershipRequest(request_type, outfit_id, action)
    },
    {
      case OutfitMembershipRequest(request_type, outfit_id, action) =>
        request_type :: outfit_id :: action :: HNil
    }
  )
}

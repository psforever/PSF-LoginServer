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

/*
  Codecs 2,5,6,7 can either work off of the avatar_id (if GUI was used) or member_name (if chat command was used)
 */
object OutfitMembershipRequestAction {

  final case class Create(
    unk1: String,
    outfit_name: String
  ) extends OutfitMembershipRequestAction(code = 0)

  final case class Form(
    unk1: String,
    outfit_name: String
  ) extends OutfitMembershipRequestAction(code = 1)

  final case class Invite(
    avatar_id: Long,
    member_name: String,
  ) extends OutfitMembershipRequestAction(code = 2)

  final case class AcceptInvite(
    member_name: String
  ) extends OutfitMembershipRequestAction(code = 3)

  final case class RejectInvite(
    member_name: String
  ) extends OutfitMembershipRequestAction(code = 4)

  final case class CancelInvite(
    avatar_id: Long,
    member_name: String,
  ) extends OutfitMembershipRequestAction(code = 5)

  final case class Kick(
    avatar_id: Long,
    member_name: String,
  ) extends OutfitMembershipRequestAction(code = 6)

  final case class SetRank(
    avatar_id: Long, // 32
    rank: Int, // 3
    member_name: String,
  ) extends OutfitMembershipRequestAction(code = 7)

  final case class Unknown(badCode: Int, data: BitVector) extends OutfitMembershipRequestAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val CreateCodec: Codec[Create] =
      (
        PacketHelpers.encodedWideStringAligned(5) ::
          PacketHelpers.encodedWideString
        ).xmap[Create](
        {
          case u1 :: outfit_name :: HNil =>
            Create(u1, outfit_name)
        },
        {
          case Create(u1, outfit_name) =>
            u1 :: outfit_name :: HNil
        }
      )

    val FormCodec: Codec[Form] =
      (
        PacketHelpers.encodedWideStringAligned(5) ::
          PacketHelpers.encodedWideString
        ).xmap[Form](
        {
          case u1 :: outfit_name :: HNil =>
            Form(u1, outfit_name)
        },
        {
          case Form(u1, outfit_name) =>
            u1 :: outfit_name :: HNil
        }
      )

    val InviteCodec: Codec[Invite] =
      (
        uint32L ::
          PacketHelpers.encodedWideStringAligned(5)
        ).xmap[Invite](
        {
          case u1 :: member_name :: HNil =>
            Invite(u1, member_name)
        },
        {
          case Invite(u1, member_name) =>
            u1 :: member_name :: HNil
        }
      )

    val AcceptInviteCodec: Codec[AcceptInvite] =
      PacketHelpers.encodedWideString.xmap[AcceptInvite](
        {
          case u1 =>
            AcceptInvite(u1)
        },
        {
          case AcceptInvite(u1) =>
            u1
        }
      )

    val RejectInviteCodec: Codec[RejectInvite] =
      PacketHelpers.encodedWideString.xmap[RejectInvite](
        {
          case u1 =>
            RejectInvite(u1)
        },
        {
          case RejectInvite(u1) =>
            u1
        }
      )

    val CancelInviteCodec: Codec[CancelInvite] =
      (
        uint32L ::
          PacketHelpers.encodedWideStringAligned(5)
        ).xmap[CancelInvite](
        {
          case u1 :: outfit_name :: HNil =>
            CancelInvite(u1, outfit_name)
        },
        {
          case CancelInvite(u1, outfit_name) =>
            u1 :: outfit_name :: HNil
        }
      )

    val KickCodec: Codec[Kick] =
      (
        uint32L ::
          PacketHelpers.encodedWideStringAligned(5)
        ).xmap[Kick](
        {
          case u1 :: member_name :: HNil =>
            Kick(u1, member_name)
        },
        {
          case Kick(u1, member_name) =>
            u1 :: member_name :: HNil
        }
      )

    val SetRankCodec: Codec[SetRank] =
      (
        uint32L ::
          uintL(3) ::
          PacketHelpers.encodedWideStringAligned(2)
        ).xmap[SetRank](
        {
          case u1 :: rank :: member_name :: HNil =>
            SetRank(u1, rank, member_name)
        },
        {
          case SetRank(u1, rank, member_name) =>
            u1 :: rank :: member_name :: HNil
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
    val Invite:   RequestType.Value = Value(2)
    val Accept: RequestType.Value = Value(3)
    val Reject: RequestType.Value = Value(4)
    val Cancel: RequestType.Value = Value(5)
    val Kick:   RequestType.Value = Value(6)
    val SetRank:   RequestType.Value = Value(7)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  private def selectFromType(code: Int): Codec[OutfitMembershipRequestAction] = {
    import OutfitMembershipRequestAction.Codecs._
    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => CreateCodec
      case 1 => FormCodec // so far same as Create
      case 2 => InviteCodec
      case 3 => AcceptInviteCodec
      case 4 => RejectInviteCodec
      case 5 => CancelInviteCodec
      case 6 => KickCodec
      case 7 => SetRankCodec

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

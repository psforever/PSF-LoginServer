// Copyright (c) 2023 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMembershipRequest(
    request_type: OutfitMembershipRequest.RequestType.Type,
    avatar_guid: PlanetSideGUID,
    unk1: Int,
    action: OutfitAction
  ) extends PlanetSideGamePacket {
  type Packet = OutfitMembershipRequest

  def opcode = GamePacketOpcode.OutfitMembershipRequest

  def encode = OutfitMembershipRequest.encode(this)
}

abstract class OutfitAction(val code: Int)
object OutfitAction {

  final case class CreateOutfit(unk2: String, unk3: Int, unk4: Boolean, outfit_name: String) extends OutfitAction(code = 0)

  final case class FormOutfit(unk2: String, unk3: Int, unk4: Boolean, outfit_name: String) extends OutfitAction(code = 1)

  final case class AcceptOutfitInvite(unk2: String) extends OutfitAction(code = 3)

  final case class RejectOutfitInvite(unk2: String) extends OutfitAction(code = 4)

  final case class CancelOutfitInvite(unk5: Int, unk6: Int, outfit_name: String) extends OutfitAction(code = 5)

  final case class Unknown(badCode: Int, data: BitVector) extends OutfitAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val CreateOutfitCodec =
      (PacketHelpers.encodedWideString :: uint4L :: bool :: PacketHelpers.encodedWideString).xmap[CreateOutfit](
        {
          case unk2 :: unk3 :: unk4 :: outfit_name :: HNil =>
            CreateOutfit(unk2, unk3, unk4, outfit_name)
        },
        {
          case CreateOutfit(unk2, unk3, unk4, outfit_name) =>
            unk2 :: unk3 :: unk4 :: outfit_name :: HNil
        }
      )

    val FormOutfitCodec =
      (PacketHelpers.encodedWideString :: uint4L :: bool :: PacketHelpers.encodedWideString).xmap[FormOutfit](
        {
          case unk2 :: unk3 :: unk4 :: outfit_name :: HNil =>
            FormOutfit(unk2, unk3, unk4, outfit_name)
        },
        {
          case FormOutfit(unk2, unk3, unk4, outfit_name) =>
            unk2 :: unk3 :: unk4 :: outfit_name :: HNil
        }
      )

    val AcceptOutfitCodec =
      (PacketHelpers.encodedWideString).xmap[AcceptOutfitInvite](
        {
          case unk2 =>
            AcceptOutfitInvite(unk2)
        },
        {
          case AcceptOutfitInvite(unk2) =>
            unk2
        }
      )

    val RejectOutfitCodec =
      (PacketHelpers.encodedWideString).xmap[RejectOutfitInvite](
        {
          case unk2 =>
            RejectOutfitInvite(unk2)
        },
        {
          case RejectOutfitInvite(unk2) =>
            unk2
        }
      )

    val CancelOutfitCodec =
      (uint16L :: uint16L :: PacketHelpers.encodedWideStringAligned(5)).xmap[CancelOutfitInvite](
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
      * @param action the action behavior code
      * @return a transformation between the action code and the unknown bit data
      */
    def unknownCodec(action: Int) =
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
    def failureCodec(action: Int) =
      everFailCondition.exmap[OutfitAction](
        _ => Attempt.failure(Err(s"can not match a codec pattern for decoding $action")),
        _ => Attempt.failure(Err(s"can not match a codec pattern for encoding $action"))
      )
  }
}

object OutfitMembershipRequest extends Marshallable[OutfitMembershipRequest] {

  object RequestType extends Enumeration {
    type Type = Value

    val Create = Value(0)
    val Form   = Value(1)
    val Unk2   = Value(2)
    val Accept = Value(3)
    val Reject = Value(4)
    val Cancel = Value(5)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  def selectFromType(code: Int): Codec[OutfitAction] = {
    import OutfitAction.Codecs._
    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => CreateOutfitCodec
      case 1 => FormOutfitCodec // so far same as Create
      case 2 => unknownCodec(action = code)
      case 3 => AcceptOutfitCodec
      case 4 => RejectOutfitCodec // so far same as Accept
      case 5 => CancelOutfitCodec
      case 6 => unknownCodec(action = code)
      case 7 => unknownCodec(action = code)
      // 3 bit limit
      case _ => failureCodec(code)
    }).asInstanceOf[Codec[OutfitAction]]
  }

  implicit val codec: Codec[OutfitMembershipRequest] = (
    ("request_type" | RequestType.codec) >>:~ { request_type =>
      ("avatar_guid" | PlanetSideGUID.codec) ::
        ("unk1" | uint16L) ::
        ("action" | selectFromType(request_type.id))
    }
  ).xmap[OutfitMembershipRequest](
    {
      case request_type :: avatar_guid :: u1 :: action :: HNil =>
        OutfitMembershipRequest(request_type, avatar_guid, u1, action)
    },
    {
      case OutfitMembershipRequest(request_type, avatar_guid, u1, action) =>
        request_type :: avatar_guid :: u1 :: action :: HNil
    }
  )
}

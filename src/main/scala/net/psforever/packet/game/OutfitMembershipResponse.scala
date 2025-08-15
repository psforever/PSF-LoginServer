// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMembershipResponse(
    response_type: OutfitMembershipResponse.ResponseType.Type,
    unk0: Int,
    outfit_id: Long,
    unk2: PlanetSideGUID,
    unk3: Int,
    //unk4: Boolean,
    action: OutfitMembershipResponseAction
  ) extends PlanetSideGamePacket {
  type Packet = OutfitMembershipResponse

  def opcode: Type = GamePacketOpcode.OutfitMembershipResponse

  def encode: Attempt[BitVector] = OutfitMembershipResponse.encode(this)
}

abstract class OutfitMembershipResponseAction(val code: Int)
object OutfitMembershipResponseAction {

  final case class CreateOutfitResponse(str1: String, str2: String, str3: String) extends OutfitMembershipResponseAction(code = 0)

  final case class Unk1OutfitResponse(player_name: String, outfit_name: String, unk7: Int) extends OutfitMembershipResponseAction(code = 1)

  final case class Unk2OutfitResponse(player_name: String, outfit_name: String, unk7: Int) extends OutfitMembershipResponseAction(code = 2) // unk7 = rank?

  final case class Unk3OutfitResponse(unk2: String) extends OutfitMembershipResponseAction(code = 3)

  final case class Unk4OutfitResponse(unk5: Int, unk6: Int, outfit_name: String) extends OutfitMembershipResponseAction(code = 4)

  final case class Unk5OutfitResponse() extends OutfitMembershipResponseAction(code = 5)

  final case class Unk6OutfitResponse() extends OutfitMembershipResponseAction(code = 6)

  final case class Unk7OutfitResponse() extends OutfitMembershipResponseAction(code = 7)

  final case class Unknown(badCode: Int, data: BitVector) extends OutfitMembershipResponseAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val Unk0OutfitCodec: Codec[CreateOutfitResponse] = (
      PacketHelpers.encodedWideStringAligned(5) ::
        PacketHelpers.encodedWideString ::
        PacketHelpers.encodedWideString
      ).xmap[CreateOutfitResponse](
      {
        case str1 :: str2 :: str3 :: HNil =>
          CreateOutfitResponse(str1, str2, str3)
      },
      {
        case CreateOutfitResponse(str1, str2, str3) =>
          str1 :: str2 :: str3 :: HNil
      }
    )

    val Unk1OutfitCodec: Codec[Unk1OutfitResponse] = (
      PacketHelpers.encodedWideStringAligned(5) ::
        PacketHelpers.encodedWideString ::
        uint8L
      ).xmap[Unk1OutfitResponse](
      {
        case player_name :: outfit_name :: u7 :: HNil =>
          Unk1OutfitResponse(player_name, outfit_name, u7)
      },
      {
        case Unk1OutfitResponse(player_name, outfit_name, u7) =>
          player_name :: outfit_name :: u7 :: HNil
      }
    )

    val Unk2OutfitCodec: Codec[Unk2OutfitResponse] = (
      PacketHelpers.encodedWideStringAligned(5) ::
        PacketHelpers.encodedWideString ::
        uint8L
      ).xmap[Unk2OutfitResponse](
      {
        case player_name :: outfit_name :: u7 :: HNil =>
          Unk2OutfitResponse(player_name, outfit_name, u7)
      },
      {
        case Unk2OutfitResponse(player_name, outfit_name, u7) =>
          player_name :: outfit_name :: u7 :: HNil
      }
    )

    val Unk3OutfitCodec: Codec[Unk3OutfitResponse] =
      PacketHelpers.encodedWideString.xmap[Unk3OutfitResponse](
        {
          case unk2 =>
            Unk3OutfitResponse(unk2)
        },
        {
          case Unk3OutfitResponse(unk2) =>
            unk2
        }
      )

    val Unk4OutfitCodec: Codec[Unk4OutfitResponse] =
      (uint16L :: uint16L :: PacketHelpers.encodedWideStringAligned(5)).xmap[Unk4OutfitResponse](
        {
          case unk5 :: unk6 :: outfit_name :: HNil =>
            Unk4OutfitResponse(unk5, unk6, outfit_name)
        },
        {
          case Unk4OutfitResponse(unk5, unk6, outfit_name) =>
            unk5 :: unk6 :: outfit_name :: HNil
        }
      )

//    val Unk5OutfitCodec: Codec[Unk5OutfitResponse] =
//      (uint16L :: uint16L :: PacketHelpers.encodedWideStringAligned(5)).xmap[Unk5OutfitResponse](
//        {
//          case unk5 :: unk6 :: outfit_name :: HNil =>
//            Unk5OutfitResponse(unk5, unk6, outfit_name)
//        },
//        {
//          case Unk5OutfitResponse(unk5, unk6, outfit_name) =>
//            unk5 :: unk6 :: outfit_name :: HNil
//        }
//      )
//
//    val Unk6OutfitCodec: Codec[Unk6OutfitResponse] =
//      (uint16L :: uint16L :: PacketHelpers.encodedWideStringAligned(5)).xmap[Unk6OutfitResponse](
//        {
//          case _ =>
//            Unk6OutfitResponse()
//        },
//        {
//          case Unk6OutfitResponse() =>
//            _
//        }
//      )
//
//    val Unk7OutfitCodec: Codec[Unk7OutfitResponse] =
//      (uint16L :: uint16L :: PacketHelpers.encodedWideStringAligned(5)).xmap[Unk7OutfitResponse](
//        {
//          case _ =>
//            Unk7OutfitResponse()
//        },
//        {
//          case Unk7OutfitResponse() =>
//            _
//        }
//      )

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
    def failureCodec(action: Int): Codec[OutfitMembershipResponseAction] =
      everFailCondition.exmap[OutfitMembershipResponseAction](
        _ => Attempt.failure(Err(s"can not match a codec pattern for decoding $action")),
        _ => Attempt.failure(Err(s"can not match a codec pattern for encoding $action"))
      )
  }
}

object OutfitMembershipResponse extends Marshallable[OutfitMembershipResponse] {

  object ResponseType extends Enumeration {
    type Type = Value

    val CreateResponse: ResponseType.Value = Value(0)
    val Unk1: ResponseType.Value = Value(1)
    val Unk2: ResponseType.Value = Value(2) // Invited / Accepted / Added
    val Unk3: ResponseType.Value = Value(3)
    val Unk4: ResponseType.Value = Value(4)
    val Unk5: ResponseType.Value = Value(5)
    val Unk6: ResponseType.Value = Value(6) // 6 and 7 seen as failed decodes, validity unknown
    val Unk7: ResponseType.Value = Value(7)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  private def selectFromType(code: Int): Codec[OutfitMembershipResponseAction] = {
    import OutfitMembershipResponseAction.Codecs._
    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => Unk0OutfitCodec // seem as OMReq Create response
      case 1 => Unk1OutfitCodec
      case 2 => Unk2OutfitCodec
      case 3 => Unk3OutfitCodec
      case 4 => Unk4OutfitCodec
      case 5 => unknownCodec(action = code)
      case 6 => unknownCodec(action = code)
      case 7 => unknownCodec(action = code)
      // 3 bit limit
      case _ => failureCodec(code)
    }).asInstanceOf[Codec[OutfitMembershipResponseAction]]
  }

  implicit val codec: Codec[OutfitMembershipResponse] = (
    ("response_type" | ResponseType.codec) >>:~ { response_type =>
      ("unk0" | uint8L) ::
      ("outfit_id" | uint32L) ::
        ("target_guid" | PlanetSideGUID.codec) ::
        ("unk3" | uint16L) ::
        //("unk4" | bool) ::
        ("action" | selectFromType(response_type.id))
    }
    ).xmap[OutfitMembershipResponse](
    {
      case response_type :: u0 :: outfit_id :: target_guid :: u3 :: action :: HNil =>
        OutfitMembershipResponse(response_type, u0, outfit_id, target_guid, u3, action)
    },
    {
      case OutfitMembershipResponse(response_type, u0, outfit_id, u2, u3, action) =>
        response_type :: u0 :: outfit_id :: u2 :: u3 :: action :: HNil
    }
  )
}

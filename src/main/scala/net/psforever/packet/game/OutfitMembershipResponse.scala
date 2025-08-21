// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMembershipResponse(
    response_type: OutfitMembershipResponse.ResponseType.Type,
    unk0: Int,
    unk1: Int,
    outfit_id: Long,
    target_id: Long,
    action: OutfitMembershipResponseAction
  ) extends PlanetSideGamePacket {
  type Packet = OutfitMembershipResponse

  def opcode: Type = GamePacketOpcode.OutfitMembershipResponse

  def encode: Attempt[BitVector] = OutfitMembershipResponse.encode(this)
}

abstract class OutfitMembershipResponseAction(val code: Int)
object OutfitMembershipResponseAction {

  final case class Universal(
   str1: String,
   str2: String,
   flag: Boolean
 ) extends OutfitMembershipResponseAction(-1)

  final case class CreateResponse(
    str1: String,
    str2: String,
    str3: String
  ) extends OutfitMembershipResponseAction(code = 0)

  final case class Unk1OutfitResponse(
    player_name: String,
    outfit_name: String,
    unk7: Int
  ) extends OutfitMembershipResponseAction(code = 1)

  final case class Unk2OutfitResponse(
    player_name: String,
    outfit_name: String,
    unk7: Int
  ) extends OutfitMembershipResponseAction(code = 2) // unk7 = rank?

  final case class Unk3OutfitResponse(
    unk2: String
  ) extends OutfitMembershipResponseAction(code = 3)

  final case class Unk4OutfitResponse(
    unk5: Int,
    unk6: Int,
    outfit_name: String
  ) extends OutfitMembershipResponseAction(code = 4)

  final case class Unknown(badCode: Int, data: BitVector) extends OutfitMembershipResponseAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val UniversalResponseCodec: Codec[OutfitMembershipResponseAction] = (
      PacketHelpers.encodedWideStringAligned(5) ::
        PacketHelpers.encodedWideString ::
        ("flag" | bool)
      ).xmap[OutfitMembershipResponseAction](
      {
        case str1 :: str2 :: flag :: HNil =>
          Universal(str1, str2, flag)
      },
      {
        case Universal(str1, str2, flag) =>
          str1 :: str2 :: flag :: HNil
      }
    )

    val CreateOutfitCodec: Codec[CreateResponse] = (
      PacketHelpers.encodedWideStringAligned(5) ::
        PacketHelpers.encodedWideString ::
        PacketHelpers.encodedWideString
      ).xmap[CreateResponse](
      {
        case str1 :: str2 :: str3 :: HNil =>
          CreateResponse(str1, str2, str3)
      },
      {
        case CreateResponse(str1, str2, str3) =>
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
      PacketHelpers.encodedWideStringAligned(5).xmap[Unk3OutfitResponse](
        {
          case unk2 =>
            Unk3OutfitResponse(unk2)
        },
        {
          case Unk3OutfitResponse(unk2) =>
            unk2
        }
      )

    val Unk4OutfitCodec: Codec[Unk4OutfitResponse] = (
      uint16L ::
        uint16L ::
        PacketHelpers.encodedWideStringAligned(5)
      ).xmap[Unk4OutfitResponse](
        {
          case unk5 :: unk6 :: outfit_name :: HNil =>
            Unk4OutfitResponse(unk5, unk6, outfit_name)
        },
        {
          case Unk4OutfitResponse(unk5, unk6, outfit_name) =>
            unk5 :: unk6 :: outfit_name :: HNil
        }
      )

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
    val Unk1: ResponseType.Value = Value(1) // Info: Player has been invited / response to OutfitMembershipRequest Unk2 for that player
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
      case 0 => UniversalResponseCodec
      case 1 => UniversalResponseCodec
      case 2 => UniversalResponseCodec
      case 3 => UniversalResponseCodec
      case 4 => UniversalResponseCodec
      case 5 => UniversalResponseCodec
      case 6 => UniversalResponseCodec
      case 7 => UniversalResponseCodec

//      case 0 => CreateOutfitCodec // seem as OMReq Create response
//      case 1 => Unk1OutfitCodec
//      case 2 => Unk2OutfitCodec
//      case 3 => Unk3OutfitCodec
//      case 4 => Unk4OutfitCodec
//      case 5 => unknownCodec(action = code)
//      case 6 => unknownCodec(action = code)
//      case 7 => unknownCodec(action = code)

      // 3 bit limit
      case _ => failureCodec(code)
    }).asInstanceOf[Codec[OutfitMembershipResponseAction]]
  }

  implicit val codec: Codec[OutfitMembershipResponse] = (
    ("response_type" | ResponseType.codec) >>:~ { response_type =>
      ("unk0" | uintL(5)) ::
      ("unk1" | uintL(3)) ::
      ("outfit_id" | uint32L) ::
      ("target_id" | uint32L) ::
      ("action" | selectFromType(response_type.id))
    }
    ).xmap[OutfitMembershipResponse](
    {
      case response_type :: u0 :: u1 :: outfit_id :: target_id :: action :: HNil =>
        OutfitMembershipResponse(response_type, u0, u1, outfit_id, target_id, action)
    },
    {
      case OutfitMembershipResponse(response_type, u0, u1, outfit_id, target_id, action) =>
        response_type :: u0 :: u1 :: outfit_id :: target_id :: action :: HNil
    }
  )
}

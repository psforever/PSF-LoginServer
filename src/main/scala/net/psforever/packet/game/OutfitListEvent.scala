// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitListEvent(
    request_type: OutfitListEvent.RequestType.Type,
    action: OutfitListEventAction
  ) extends PlanetSideGamePacket {
  type Packet = OutfitListEvent

  def opcode: Type = GamePacketOpcode.OutfitListEvent

  def encode: Attempt[BitVector] = OutfitListEvent.encode(this)
}

abstract class OutfitListEventAction(val code: Int)

object OutfitListEventAction {

  final case class ListElementOutfit(
    outfit_id: Long,
    points: Long,
    members: Long,
    outfit_name: String,
    outfit_leader: String,
  ) extends OutfitListEventAction(code = 2)

  /*
    TODO: Check packet when bundle packet has been implemented (packet containing OutfitListEvent packets back to back)
    For now it seems like there is no valid packet captured
   */
  final case class Unk3(
    unk1: Long,
    data: ByteVector
  ) extends OutfitListEventAction(code = 3)

  final case class Unknown(badCode: Int, data: BitVector) extends OutfitListEventAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val ListElementOutfitCodec: Codec[ListElementOutfit] = (
      ("unk1" | uint32L) ::
        ("points" | uint32L) ::
        ("members" | uint32L) ::
        ("outfit_name" | PacketHelpers.encodedWideStringAligned(5)) ::
        ("outfit_leader" | PacketHelpers.encodedWideString)
      ).xmap[ListElementOutfit](
      {
        case u1 :: points :: members :: outfit_name :: outfit_leader :: HNil =>
          ListElementOutfit(u1, points, members, outfit_name, outfit_leader)
      },
      {
        case ListElementOutfit(u1, points, members, outfit_name, outfit_leader) =>
          u1 :: points :: members :: outfit_name :: outfit_leader :: HNil
      }
    )

    val Unk3Codec: Codec[Unk3] = (
      ("unk1" | uint32L) ::
        ("data" | bytes)
      ).xmap[Unk3](
      {
        case u1 :: data :: HNil =>
          Unk3(u1, data)
      },
      {
        case Unk3(u1, data) =>
          u1 :: data :: HNil
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
    def failureCodec(action: Int): Codec[OutfitListEventAction] =
      everFailCondition.exmap[OutfitListEventAction](
        _ => Attempt.failure(Err(s"can not match a codec pattern for decoding $action")),
        _ => Attempt.failure(Err(s"can not match a codec pattern for encoding $action"))
      )
  }
}

object OutfitListEvent extends Marshallable[OutfitListEvent] {
  import shapeless.{::, HNil}

  object RequestType extends Enumeration {
    type Type = Value

    val Unk0: RequestType.Value = Value(0)
    val Unk1: RequestType.Value = Value(1)
    val ListElementOutfit: RequestType.Value = Value(2)
    val Unk3: RequestType.Value = Value(3)
    val Unk4: RequestType.Value = Value(4)
    val Unk5: RequestType.Value = Value(5)
    val unk6: RequestType.Value = Value(6)
    val unk7: RequestType.Value = Value(7)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  private def selectFromType(code: Int): Codec[OutfitListEventAction] = {
    import OutfitListEventAction.Codecs._
    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => unknownCodec(action = code)
      case 1 => unknownCodec(action = code)
      case 2 => ListElementOutfitCodec
      case 3 => Unk3Codec // indicated in code
      case 4 => unknownCodec(action = code)
      case 5 => unknownCodec(action = code)
      case 6 => unknownCodec(action = code)
      case 7 => unknownCodec(action = code)

      case _ => failureCodec(code)
    }).asInstanceOf[Codec[OutfitListEventAction]]
  }

  implicit val codec: Codec[OutfitListEvent] = (
    ("request_type" | RequestType.codec) >>:~ { request_type =>
      ("action" | selectFromType(request_type.id)).hlist
    }
    ).xmap[OutfitListEvent](
    {
      case request_type :: action :: HNil =>
        OutfitListEvent(request_type, action)
    },
    {
      case OutfitListEvent(request_type, action) =>
        request_type :: action :: HNil
    }
  )
}

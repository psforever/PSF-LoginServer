// Copyright (c) 2024 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitEvent(
    request_type: OutfitEvent.RequestType.Type,
    unk1: Int,
    action: OutfitEventAction
  ) extends PlanetSideGamePacket {
  type Packet = OutfitEvent

  def opcode: Type = GamePacketOpcode.OutfitEvent

  def encode: Attempt[BitVector] = OutfitEvent.encode(this)
}

abstract class OutfitEventAction(val code: Int)

object OutfitEventAction {

  final case class CreatedOutfit(
    unk2: Int,
    unk3: Int,
    unk4: Int,
    outfit_name: String,
    unk6: Long,
    unk7: Long,
    members: Int,
    unk9: Int,
    unk10: String,
    unk11: String,
    unk12: String,
    unk13: String,
    unk14: String,
    unk15: String,
    unk16: String,
    unk17: String,
    unk18: String,
    unk19: Int,
    unk20: Int,
    unk21: Long,
    unk22: Long,
    unk23: Long,
    unk24: Long,
    unk25: Int,
  ) extends OutfitEventAction(code = 4)

  final case class Unknown(badCode: Int, data: BitVector) extends OutfitEventAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val CreatedOutfitCodec: Codec[CreatedOutfit] =
      (uint8L ::
        uint4L ::
        uintL(3) ::
        PacketHelpers.encodedWideStringAligned(5) ::
        uint32L ::
        uint32L ::
        uint16L ::
        uint16L ::
        PacketHelpers.encodedString ::
        PacketHelpers.encodedString ::
        PacketHelpers.encodedString ::
        PacketHelpers.encodedString ::
        PacketHelpers.encodedString ::
        PacketHelpers.encodedString ::
        PacketHelpers.encodedString ::
        PacketHelpers.encodedString ::
        PacketHelpers.encodedString ::
        uint16L ::
        uint16L ::
        uint32L ::
        uint32L ::
        uint32L ::
        uint32L ::
        uint16L
        ).xmap[CreatedOutfit](
        {
          case u2 :: u3 :: u4 :: outfit_name :: u6 :: u7 :: members :: u9 :: u10 :: u11 :: u12 :: u13 :: u14 :: u15 :: u16 :: u17 :: u18 :: u19 :: u20 :: u21 :: u22 :: u23 :: u24 :: u25 :: HNil =>
            CreatedOutfit(u2, u3, u4, outfit_name, u6, u7, members, u9, u10, u11, u12, u13, u14, u15, u16, u17, u18, u19, u20, u21, u22, u23, u24, u25)
        },
        {
          case CreatedOutfit(u2, u3, u4, outfit_name, u6, u7, members, u9, u10, u11, u12, u13, u14, u15, u16, u17, u18, u19, u20, u21, u22, u23, u24, u25) =>
            u2 :: u3 :: u4 :: outfit_name :: u6 :: u7 :: members :: u9 :: u10 :: u11 :: u12 :: u13 :: u14 :: u15 :: u16 :: u17 :: u18 :: u19 :: u20 :: u21 :: u22 :: u23 :: u24 :: u25 :: HNil
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

    val unk0: RequestType.Value = Value(0)
    val unk1: RequestType.Value = Value(1)
    val unk2: RequestType.Value = Value(2)
    val unk3: RequestType.Value = Value(3)
    val CreatedOutfit: RequestType.Value = Value(4)
    val unk5: RequestType.Value = Value(5)
    val Unk6: RequestType.Value = Value(6)
    val Unk7: RequestType.Value = Value(7)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uint4L)
  }

  private def selectFromType(code: Int): Codec[OutfitEventAction] = {
    import OutfitEventAction.Codecs._
    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => unknownCodec(action = code)
      case 1 => unknownCodec(action = code)
      case 2 => unknownCodec(action = code)
      case 3 => unknownCodec(action = code)
      case 4 => CreatedOutfitCodec // sent after /outfitcreate
      case 5 => unknownCodec(action = code)
      case 6 => unknownCodec(action = code)
      case 7 => unknownCodec(action = code)

      case _ => failureCodec(code)
    }).asInstanceOf[Codec[OutfitEventAction]]
  }

  implicit val codec: Codec[OutfitEvent] = (
    ("request_type" | RequestType.codec) >>:~ { request_type =>
      ("avatar_guid" | uint16L) ::
        ("action" | selectFromType(request_type.id))
    }
    ).xmap[OutfitEvent](
    {
      case request_type :: avatar_guid :: action :: HNil =>
        OutfitEvent(request_type, avatar_guid, action)
    },
    {
      case OutfitEvent(request_type, avatar_guid, action) =>
        request_type :: avatar_guid :: action :: HNil
    }
  )
}

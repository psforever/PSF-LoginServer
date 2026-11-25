// Copyright (c) 2025 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package net.psforever.packet.game.packets

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

final case class OutfitListEvent(
    action: OutfitListEventAction
  ) extends PlanetSideGamePacket {
  type Packet = OutfitListEvent

  def opcode: Type = GamePacketOpcode.OutfitListEvent

  def encode: Attempt[BitVector] = OutfitListEvent.encode(this)
}

abstract class OutfitListEventAction(val code: Int)

object OutfitListEventAction {

  /**
    * The client's outfit-browser handler clears its row collection when it sees this action
    * before a list of `ListElementOutfit` rows begins.
    */
  final case class ListBegin(
  ) extends OutfitListEventAction(code = 0)

  /**
    * A payload-free action; the client routes it through the same handler path as
    * `ListElementOutfit`, but the wire carries no additional fields.
    */
  final case class ListEnd(
  ) extends OutfitListEventAction(code = 1)

  final case class ListElementOutfit(
    outfit_id: Long,
    points: Long,
    members: Long,
    outfit_name: String,
    outfit_leader: String,
  ) extends OutfitListEventAction(code = 2)

  /**
    * TODO: Check packet when bundle packet has been implemented (packet containing OutfitListEvent packets back to back)
    * For now it seems like there is no valid packet captured
    */
  final case class Unk3(
    unk1: Long,
  ) extends OutfitListEventAction(code = 3)

  final case class Unknown(badCode: Int, data: BitVector) extends OutfitListEventAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val ListBeginCodec: Codec[ListBegin] = PacketHelpers.emptyCodec(ListBegin())

    val ListEndCodec: Codec[ListEnd] = PacketHelpers.emptyCodec(ListEnd())

    val ListElementCodec: Codec[ListElementOutfit] = (
      ("outfit_id" | uint32L) ::
      ("points" | uint32L) ::
      ("members" | uint32L) ::
      ("outfit_name" | PacketHelpers.encodedWideStringAligned(5)) ::
      ("outfit_leader" | PacketHelpers.encodedWideString)
    ).xmap[ListElementOutfit](
      {
        case outfit_id :: points :: members :: outfit_name :: outfit_leader :: HNil =>
          ListElementOutfit(outfit_id, points, members, outfit_name, outfit_leader)
      },
      {
        case ListElementOutfit(outfit_id, points, members, outfit_name, outfit_leader) =>
          outfit_id :: points :: members :: outfit_name :: outfit_leader :: HNil
      }
    )

    val Unk3Codec: Codec[Unk3] = (
      ("unk1" | uint32L)
    ).xmap[Unk3](
      {
        case u1 =>
          Unk3(u1)
      },
      {
        case Unk3(u1) =>
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
    def failureCodec(action: Int): Codec[OutfitListEventAction] =
      everFailCondition.exmap[OutfitListEventAction](
        _ => Attempt.failure(Err(s"can not match a codec pattern for decoding $action")),
        _ => Attempt.failure(Err(s"can not match a codec pattern for encoding $action"))
      )
  }
}

object OutfitListEvent extends Marshallable[OutfitListEvent] {
  import shapeless.{::, HNil}

  object PacketType extends Enumeration {
    type Type = Value

    val ListBegin: PacketType.Value = Value(0)
    val ListEnd: PacketType.Value = Value(1)
    val ListElement: PacketType.Value = Value(2)
    val Unk3: PacketType.Value = Value(3)
    // 4-7: the client decodes these actions without any payload and leaves them unhandled;
    // the entries have no modelled meaning here either, but keep the codes decodable
    val Unk4: PacketType.Value = Value(4)
    val Unk5: PacketType.Value = Value(5)
    val Unk6: PacketType.Value = Value(6)
    val Unk7: PacketType.Value = Value(7)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  /**
    * The outfit lists in the outfit browser are bracketed:
    * `0` clears the list rows collected so far (the client wipes its row collection),
    * `2` adds one outfit row, and `3` closes a list (carrying one 32-bit value).
    * Action `1` re-enters the client's row-handling path of `2`, but carries no payload.
    * Actions `4`-`7` are decoded by the client without any payload and are not handled;
    * they decode into `Unknown` (as they always did before the modelled actions were
    * split out) rather than failing the decode.
    */
  private def selectFromType(code: Int): Codec[OutfitListEventAction] = {
    import OutfitListEventAction.Codecs._

    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => ListBeginCodec
      case 1 => ListEndCodec
      case 2 => ListElementCodec
      case 3 => Unk3Codec

      case 4 | 5 | 6 | 7 => unknownCodec(action = code)

      case _ => failureCodec(code)
    }).asInstanceOf[Codec[OutfitListEventAction]]
  }

  implicit val codec: Codec[OutfitListEvent] = (
    ("packet_type" | PacketType.codec) >>:~ { packet_type =>
      ("action" | selectFromType(packet_type.id)).hlist
    }
  ).xmap[OutfitListEvent](
    {
      case _ :: action :: HNil =>
        OutfitListEvent(action)
    },
    {
      case OutfitListEvent(action) =>
        OutfitListEvent.PacketType(action.code) :: action :: HNil
    }
  )
}

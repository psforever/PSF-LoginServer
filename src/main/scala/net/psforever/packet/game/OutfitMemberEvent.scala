// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

/*
  packet_type is unimplemented! if packet_type == 0 only outfit_id and member_id are sent
  action is unimplemented! if action == 0 unk2 will contain one additional uint32L
  unk0_padding contains one byte of padding. may contain 4byte of unknown data depending on action
 */
final case class OutfitMemberEvent(
    packet_type: Int, // only 0 is known // TODO: needs implementation
    outfit_id: Long,
    member_id: Long,
    member_name: String, // from here is packet_type == 0 only
    rank: Int, // 0-7
    points: Long, // client divides this by 100
    last_login: Long, // seconds ago from current time, 0 if online
    action: OutfitMemberEvent.PacketType.Type, // this should always be 1, otherwise there will be actual data in unk0_padding!
    unk0_padding: OutfitMemberEventAction // only contains information if action is 0, 1 byte of padding otherwise
  ) extends PlanetSideGamePacket {
  type Packet = OutfitMemberEvent

  def opcode: Type = GamePacketOpcode.OutfitMemberEvent

  def encode: Attempt[BitVector] = OutfitMemberEvent.encode(this)
}

abstract class OutfitMemberEventAction(val code: Int)
object OutfitMemberEventAction {

  final case class Unk0(
    unk0: Long
  ) extends OutfitMemberEventAction(code = 0)

  final case class Padding(
    padding: Int
  ) extends OutfitMemberEventAction(code = 1)

  final case class Unknown(badCode: Int, data: BitVector) extends OutfitMemberEventAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val UnkNonPaddingCodec: Codec[Unk0] = (
      ("unk0" | uint32L)
      ).xmap[Unk0](
      {
        case u0 =>
          Unk0(u0)
      },
      {
        case Unk0(u0) =>
          u0
      }
    )

    val PaddingCodec: Codec[Padding] = (
      ("padding" | uint4L)
      ).xmap[Padding](
      {
        case padding =>
          Padding(padding)
      },
      {
        case Padding(padding) =>
          padding
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
    def failureCodec(action: Int): Codec[OutfitMemberEventAction] =
      everFailCondition.exmap[OutfitMemberEventAction](
        _ => Attempt.failure(Err(s"can not match a codec pattern for decoding $action")),
        _ => Attempt.failure(Err(s"can not match a codec pattern for encoding $action"))
      )
  }
}

object OutfitMemberEvent extends Marshallable[OutfitMemberEvent] {

  object PacketType extends Enumeration {
    type Type = Value

    val Unk0: PacketType.Value = Value(0)
    val Padding: PacketType.Value = Value(1) // Info: Player has been invited / response to OutfitMembershipRequest Unk2 for that player

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(1))
  }

  private def selectFromType(code: Int): Codec[OutfitMemberEventAction] = {
    import OutfitMemberEventAction.Codecs._
    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => UnkNonPaddingCodec
      case 1 => PaddingCodec

      case _ => failureCodec(code)
    }).asInstanceOf[Codec[OutfitMemberEventAction]]
  }

  implicit val codec: Codec[OutfitMemberEvent] = (
    ("packet_type" | uintL(2)) :: // this should selectFromType
      ("outfit_id" | uint32L) ::
      ("member_id" | uint32L) ::
      ("member_name" | PacketHelpers.encodedWideStringAligned(6)) :: // from here is packet_type == 0 only
      ("rank" | uint(3)) ::
      ("points" | uint32L) ::
      ("last_login" | uint32L) ::
      (("action" | PacketType.codec) >>:~ { action =>
        ("action_part" | selectFromType(action.id)).hlist
      })
    ).xmap[OutfitMemberEvent](
    {
      case packet_type :: outfit_id :: member_id :: member_name :: rank :: points :: last_login :: action :: unk0_padding :: HNil =>
        OutfitMemberEvent(packet_type, outfit_id, member_id, member_name, rank, points, last_login, action, unk0_padding)
    },
    {
      // TODO: remove once implemented
      // ensure we send packet_type 0 only
      case OutfitMemberEvent(_, outfit_id, member_id, member_name, rank, points, last_login, action, unk0_padding) =>
        0 :: outfit_id :: member_id :: member_name :: rank :: points :: last_login :: action :: unk0_padding :: HNil
    }
  )
}

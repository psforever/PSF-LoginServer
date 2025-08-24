// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMemberEvent(
    outfit_id: Long,
    member_id: Long,
    action: OutfitMemberEventAction
  ) extends PlanetSideGamePacket {
  type Packet = OutfitMemberEvent

  def opcode: Type = GamePacketOpcode.OutfitMemberEvent

  def encode: Attempt[BitVector] = OutfitMemberEvent.encode(this)
}

abstract class OutfitMemberEventAction(val code: Int)
object OutfitMemberEventAction {

  object PacketType extends Enumeration {
    type Type = Value

    val Unk0: PacketType.Value = Value(0)
    val Padding: PacketType.Value = Value(1)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(1))

  }

  /*
    action is unimplemented! if action == 0 unk2 will contain one additional uint32L
    padding contains one uint4L of padding. may contain uint32L of unknown data depending on action
 */
  final case class Unk0(
    member_name: String,
    rank: Int,
    points: Long, // client divides this by 100
    last_online: Long, // seconds ago from current time, 0 if online
    action: PacketType.Type, // should always be 1, otherwise there will be actual data in padding. not implemented!
    padding: Int // should always be 0, 4 bits of padding // only contains data if action is 0
  ) extends OutfitMemberEventAction(code = 0)

  final case class Unk1(
  ) extends OutfitMemberEventAction(code = 1)

  final case class Unknown(badCode: Int, data: BitVector) extends OutfitMemberEventAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val Unk0Codec: Codec[Unk0] = (
      ("member_name" | PacketHelpers.encodedWideStringAligned(6)) :: // from here is packet_type == 0 only
      ("rank" | uint(3)) ::
      ("points" | uint32L) ::
      ("last_login" | uint32L) ::
      ("action" | OutfitMemberEventAction.PacketType.codec) ::
      ("padding" | uint4L)
    ).xmap[Unk0](
      {
        case member_name :: rank :: points :: last_login :: action :: padding :: HNil =>
          Unk0(member_name, rank, points, last_login, action, padding)
      },
      {
        case Unk0(member_name, rank, points, last_login, action, padding) =>
          member_name :: rank :: points :: last_login :: action :: padding :: HNil
      }
    )

    val Unk1Codec: Codec[Unk1] = PacketHelpers.emptyCodec(Unk1())

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
    val Unk1: PacketType.Value = Value(1) // Info: Player has been invited / response to OutfitMembershipRequest Unk2 for that player
    val Unk2: PacketType.Value = Value(2)
    val Unk3: PacketType.Value = Value(3)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(2))
  }

  private def selectFromType(code: Int): Codec[OutfitMemberEventAction] = {
    import OutfitMemberEventAction.Codecs._
    import scala.annotation.switch

    ((code: @switch) match {
      case 0 => Unk0Codec
      case 1 => Unk1Codec
      case 2 => unknownCodec(code)
      case 3 => unknownCodec(code)

      case _ => failureCodec(code)
    }).asInstanceOf[Codec[OutfitMemberEventAction]]
  }

  implicit val codec: Codec[OutfitMemberEvent] = (
    ("packet_type" | PacketType.codec) >>:~ { packet_type =>
      ("outfit_id" | uint32L) ::
      ("member_id" | uint32L) ::
      ("action" | selectFromType(packet_type.id)).hlist
    }
  ).xmap[OutfitMemberEvent](
    {
      case _ :: outfit_id :: member_id:: action :: HNil =>
        OutfitMemberEvent(outfit_id, member_id, action)
    },
    {
      case OutfitMemberEvent(outfit_id, member_id, action) =>
        OutfitMemberEvent.PacketType(action.code) :: outfit_id :: member_id :: action :: HNil
    }
  )
}

// Copyright (c) 2023-2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.bits.ByteVector
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitRequest(
    id: Long,
    action: OutfitRequestAction
  ) extends PlanetSideGamePacket {
  type Packet = OutfitRequest
  def opcode = GamePacketOpcode.OutfitRequest
  def encode = OutfitRequest.encode(this)
}

/**
 * na
 */
abstract class OutfitRequestAction(val code: Int)

object OutfitRequestAction {
  /**
   * na
   * @param str na
   */
  final case class Motd(str: String) extends OutfitRequestAction(code = 0)
  /**
   * na
   * @param list na
   */
  final case class Ranks(list: List[Option[String]]) extends OutfitRequestAction(code = 1)
  /**
   * na
   * @param unk na
   */
  final case class Unk2(unk: Int) extends OutfitRequestAction(code = 2)

  /**
   * na
   * @param unk na
   */
  final case class Unk3(menuOpen: Boolean) extends OutfitRequestAction(code = 3)

  /**
   * na
   * @param unk na
   */
  final case class Unk4(menuOpen: Boolean) extends OutfitRequestAction(code = 4)

  /**
   * na
   * @param unk na
   */
  final case class Fail(unk: ByteVector) extends OutfitRequestAction(code = -1)
}

object OutfitRequest extends Marshallable[OutfitRequest] {
  /**
   * na
   */
  private val MotdCodec: Codec[OutfitRequestAction] = PacketHelpers.encodedWideStringAligned(adjustment = 5).hlist
    .xmap[OutfitRequestAction] (
      {
        case value :: HNil => OutfitRequestAction.Motd(value)
      },
      {
        case OutfitRequestAction.Motd(value) => value :: HNil
      }
    )

  /**
   * na
   */
  private val RankCodec: Codec[OutfitRequestAction] = unk1PaddedEntryCodec(len = 8, pad = 5).xmap[OutfitRequestAction] (
    list => OutfitRequestAction.Ranks(list),
    {
      case OutfitRequestAction.Ranks(list) => list
    }
  )

  /**
   * na
   */
  private def unk1PaddedEntryCodec(len: Int, pad: Int): Codec[List[Option[String]]] =
    {
      val nextPad = if (pad == 0) 7 else pad - 1
      optional(bool, PacketHelpers.encodedWideStringAligned(nextPad)) >>:~ { strOpt =>
        (strOpt match {
          case None if len > 1    => unk1PaddedEntryCodec(len - 1, nextPad)
          case Some(_) if len > 1 => unk1PaddedEntryCodec(len - 1, pad = 8)
          case _                  => PacketHelpers.listOfNSized(size = 0L, optional(bool, PacketHelpers.encodedWideString))
        }).hlist
      }
    }.xmap[List[Option[String]]](
      {
        case head :: tail :: HNil => head +: tail
      },
      list => list.head :: list.tail :: HNil
    )

  /**
   * na
   */
  private val unk2Codec: Codec[OutfitRequestAction] = uint8.hlist.xmap[OutfitRequestAction] (
    {
      case value :: HNil => OutfitRequestAction.Unk2(value)
    },
    {
      case OutfitRequestAction.Unk2(value) => value :: HNil
    }
  )

  /**
   * na
   */
  private val unk3Codec: Codec[OutfitRequestAction] = bool.hlist.xmap[OutfitRequestAction] (
    {
      case value :: HNil => OutfitRequestAction.Unk3(value)
    },
    {
      case OutfitRequestAction.Unk3(value) => value :: HNil
    }
  )

  /**
   * na
   */
  private val unk4Codec: Codec[OutfitRequestAction] = bool.hlist.xmap[OutfitRequestAction] (
    {
      case value :: HNil => OutfitRequestAction.Unk4(value)
    },
    {
      case OutfitRequestAction.Unk4(value) => value :: HNil
    }
  )

  /**
   * na
   */
  private def failCodec(action: Int): Codec[OutfitRequestAction] = conditional(included = false, bool).exmap[OutfitRequestAction](
    _ => Attempt.Failure(Err(s"can not decode $action-type info - what is this thing?")),
    _ => Attempt.Failure(Err(s"can not encode $action-type info - no such thing"))
  )

  object PacketType extends Enumeration {
    type Type = Value

    val Motd: PacketType.Value = Value(0)
    val Rank:   PacketType.Value = Value(1)
    val Unk2:   PacketType.Value = Value(2)
    val Detail: PacketType.Value = Value(3)
    val List: PacketType.Value = Value(4) // sent by client if menu is either open (true) or closed (false)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  /**
   * na
   */
  private def selectFromType(code: Int): Codec[OutfitRequestAction] = {
    code match {
      case 0 => MotdCodec
      case 1 => RankCodec
      case 2 => unk2Codec
      case 3 => unk3Codec
      case 4 => unk4Codec
      case _ => failCodec(code)
    }
  }

  implicit val codec: Codec[OutfitRequest] = (
    ("packet_type" | PacketType.codec) >>:~ { packet_type =>
      ("id" | uint32L) ::
      ("action" | selectFromType(packet_type.id)).hlist
    }
  ).xmap[OutfitRequest](
    {
      case _ :: id:: action :: HNil =>
        OutfitRequest(id, action)
    },
    {
      case OutfitRequest(id, action) =>
        OutfitRequest.PacketType(action.code) :: id :: action :: HNil
    }
  )
}

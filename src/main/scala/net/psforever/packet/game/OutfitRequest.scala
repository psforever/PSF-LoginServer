// Copyright (c) 2023 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.bits.ByteVector
import scodec.codecs._
import shapeless.{::, HNil}

/**
 * na
 */
abstract class OutfitRequestForm(val code: Int)

object OutfitRequestForm {
  /**
   * na
   * @param str na
   */
  final case class Unk0(str: String) extends OutfitRequestForm(code = 0)
  /**
   * na
   * @param list na
   */
  final case class Unk1(list: List[Option[String]]) extends OutfitRequestForm(code = 1)
  /**
   * na
   * @param unk na
   */
  final case class Unk2(unk: Int) extends OutfitRequestForm(code = 2)
  /**
   * na
   * @param unk na
   */
  final case class Unk3(unk: Boolean) extends OutfitRequestForm(code = 3)
  /**
   * na
   * @param unk na
   */
  final case class Unk4(unk: Boolean) extends OutfitRequestForm(code = 4)
  /**
   * na
   * @param unk na
   */
  final case class Fail(unk: ByteVector) extends OutfitRequestForm(code = -1)
}

/**
 * na
 * @param id na
 * @param info na
 */
final case class OutfitRequest(id: Long, info: OutfitRequestForm)
  extends PlanetSideGamePacket {
  type Packet = OrbitalStrikeWaypointMessage
  def opcode = GamePacketOpcode.OutfitRequest
  def encode = OutfitRequest.encode(this)
}

object OutfitRequest extends Marshallable[OutfitRequest] {
  /**
   * na
   */
  private val unk0Codec: Codec[OutfitRequestForm] = PacketHelpers.encodedWideStringAligned(adjustment = 5).hlist
    .xmap[OutfitRequestForm] (
      {
        case value :: HNil => OutfitRequestForm.Unk0(value)
      },
      {
        case OutfitRequestForm.Unk0(value) => value :: HNil
      }
    )

  /**
   * na
   */
  private val unk1Codec: Codec[OutfitRequestForm] = unk1PaddedEntryCodec(len = 8, pad = 5).xmap[OutfitRequestForm] (
    list => OutfitRequestForm.Unk1(list),
    {
      case OutfitRequestForm.Unk1(list) => list
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
  private val unk2Codec: Codec[OutfitRequestForm] = uint8.hlist.xmap[OutfitRequestForm] (
    {
      case value :: HNil => OutfitRequestForm.Unk2(value)
    },
    {
      case OutfitRequestForm.Unk2(value) => value :: HNil
    }
  )

  /**
   * na
   */
  private val unk3Codec: Codec[OutfitRequestForm] = bool.hlist.xmap[OutfitRequestForm] (
    {
      case value :: HNil => OutfitRequestForm.Unk3(value)
    },
    {
      case OutfitRequestForm.Unk3(value) => value :: HNil
    }
  )

  /**
   * na
   */
  private val unk4Codec: Codec[OutfitRequestForm] = bool.hlist.xmap[OutfitRequestForm] (
    {
      case value :: HNil => OutfitRequestForm.Unk4(value)
    },
    {
      case OutfitRequestForm.Unk4(value) => value :: HNil
    }
  )

  /**
   * na
   */
  private def failCodec(code: Int): Codec[OutfitRequestForm] = conditional(included = false, bool).exmap[OutfitRequestForm](
    _ => Attempt.Failure(Err(s"can not decode $code-type info - what is this thing?")),
    _ => Attempt.Failure(Err(s"can not encode $code-type info - no such thing"))
  )

  /**
   * na
   */
  private def infoCodec(code: Int): Codec[OutfitRequestForm] = {
    code match {
      case 0 => unk0Codec
      case 1 => unk1Codec
      case 2 => unk2Codec
      case 3 => unk3Codec
      case 4 => unk4Codec
      case _ => failCodec(code)
    }
  }

  implicit val codec: Codec[OutfitRequest] = (
    uint(bits = 3) >>:~ { code =>
      ("id" | uint32L) ::
        ("info" | infoCodec(code))
    }
    ).xmap[OutfitRequest](
    {
      case _:: id:: info :: HNil =>
        OutfitRequest(id, info)
    },
    {
      case OutfitRequest(id, info) =>
        info.code :: id :: info :: HNil
    }
  )
}

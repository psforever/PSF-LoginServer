// Copyright (c) 2024 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import shapeless.{::, HNil}
import net.psforever.types.Vector3
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._

import scala.annotation.switch

trait UplinkEvent {
  def code: Int
}

final case class Event0(code: Int) extends UplinkEvent

final case class Event1(code: Int, unk1: Int) extends UplinkEvent

final case class Event2(
                         code: Int,
                         unk1: Vector3,
                         unk2: Int,
                         unk3: Int,
                         unk4: Long,
                         unk5: Long,
                         unk6: Long,
                         unk7: Long,
                         unk8: Option[Boolean]
                       ) extends UplinkEvent

final case class UplinkPositionEvent(
                                      code: Int,
                                      event: UplinkEvent
                                    ) extends PlanetSideGamePacket {
  type Packet = UplinkPositionEvent
  def opcode: Type = GamePacketOpcode.UplinkPositionEvent
  def encode: Attempt[BitVector] = UplinkPositionEvent.encode(this)
}

object UplinkPositionEvent extends Marshallable[UplinkPositionEvent] {
  private def event0Codec(code: Int): Codec[Event0] = conditional(included = false, bool).xmap[Event0](
    _ => Event0(code),
    {
      case Event0(_) => None
    }
  )

  private def event1Codec(code: Int): Codec[Event1] =
    ("unk1" | uint8L).hlist.xmap[Event1](
      {
        case unk1 :: HNil => Event1(code, unk1)
      },
      {
        case Event1(_, unk1) => unk1 :: HNil
      }
    )

  private def event2NoBoolCodec(code: Int): Codec[Event2] = (
    ("unk1" | Vector3.codec_pos) ::
      ("unk2" | uint8) ::
      ("unk3" | uint16L) ::
      ("unk4" | uint32L) ::
      ("unk5" | uint32L) ::
      ("unk6" | uint32L) ::
      ("unk7" | uint32L)
  ).xmap[Event2](
    {
      case u1 :: u2 :: u3 :: u4 :: u5 :: u6 :: u7 :: HNil =>
        Event2(code, u1, u2, u3, u4, u5, u6, u7, None)
    },
    {
      case Event2(_, u1, u2, u3, u4, u5, u6, u7, _) =>
        u1 :: u2 :: u3 :: u4 :: u5 :: u6 :: u7 :: HNil
    }
  )

  private def event2WithBoolCodec(code: Int): Codec[Event2] = (
    ("unk1" | Vector3.codec_pos) ::
      ("unk2" | uint8) ::
      ("unk3" | uint16L) ::
      ("unk4" | uint32L) ::
      ("unk5" | uint32L) ::
      ("unk6" | uint32L) ::
      ("unk7" | uint32L) ::
      ("unk8" | bool)
    ).xmap[Event2](
    {
      case u1 :: u2 :: u3 :: u4 :: u5 :: u6 :: u7 :: u8 :: HNil =>
        Event2(code, u1, u2, u3, u4, u5, u6, u7, Some(u8))
    },
    {
      case Event2(_, u1, u2, u3, u4, u5, u6, u7, Some(u8)) =>
        u1 :: u2 :: u3 :: u4 :: u5 :: u6 :: u7 :: u8 :: HNil
    }
  )

  private def switchUplinkEvent(code: Int): Codec[UplinkEvent] = {
    ((code: @switch) match {
      case 0     => event2NoBoolCodec(code)
      case 1 | 2 => event2WithBoolCodec(code)
      case 3 | 4 => event1Codec(code)
      case _     => event0Codec(code)
    }).asInstanceOf[Codec[UplinkEvent]]
  }

  implicit val codec: Codec[UplinkPositionEvent] = (
    ("code" | uint(bits = 3)) >>:~ { code =>
      ("event" | switchUplinkEvent(code)).hlist
    }
  ).as[UplinkPositionEvent]
}

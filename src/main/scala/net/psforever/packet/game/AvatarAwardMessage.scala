// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

import scala.annotation.switch

abstract class AwardOption(val code: Int) {
  def unk1: Long
  def unk2: Long
}

final case class AwardOptionZero(unk1: Long, unk2: Long) extends AwardOption(code = 0)

final case class AwardOptionOne(unk1: Long) extends AwardOption(code = 1) {
  def unk2: Long = 0L
}

final case class AwardOptionTwo(unk1: Long) extends AwardOption(code = 3) {
  def unk2: Long = 0L
}

/**
  * na
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  */
final case class AvatarAwardMessage(
                                     unk1: Long,
                                     unk2: AwardOption,
                                     unk3: Int
                                   )
  extends PlanetSideGamePacket {
  type Packet = AvatarAwardMessage
  def opcode = GamePacketOpcode.AvatarAwardMessage
  def encode = AvatarAwardMessage.encode(this)
}

object AvatarAwardMessage extends Marshallable[AvatarAwardMessage] {
  private val codec_one: Codec[AwardOptionOne] = {
    uint32L.hlist
  }.xmap[AwardOptionOne](
    {
      case a :: HNil => AwardOptionOne(a)
    },
    {
      case AwardOptionOne(a) => a :: HNil
    }
  )

  private val codec_two: Codec[AwardOptionTwo] = {
    uint32L.hlist
  }.xmap[AwardOptionTwo](
    {
      case a :: HNil => AwardOptionTwo(a)
    },
    {
      case AwardOptionTwo(a) => a :: HNil
    }
  )

  private val codec_zero: Codec[AwardOptionZero] = {
    uint32L :: uint32L
  }.xmap[AwardOptionZero](
    {
      case a :: b :: HNil => AwardOptionZero(a, b)
    },
    {
      case AwardOptionZero(a, b) => a :: b :: HNil
    }
  )

  private def selectAwardOption(code: Int): Codec[AwardOption] = {
    ((code: @switch) match {
      case 2 | 3 => codec_two
      case 1     => codec_one
      case 0     => codec_zero
    }).asInstanceOf[Codec[AwardOption]]
  }

  implicit val codec: Codec[AvatarAwardMessage] = (
    ("unk1" | uint32L) ::
    (uint2 >>:~ { code =>
      ("unk2" | selectAwardOption(code)) ::
      ("unk3" | uint8L)
    })
    ).xmap[AvatarAwardMessage](
    {
      case unk1 :: _ :: unk2 :: unk3 :: HNil =>
        AvatarAwardMessage(unk1, unk2, unk3)
    },
    {
      case AvatarAwardMessage(unk1, unk2, unk3) =>
        unk1 :: unk2.code :: unk2 :: unk3 :: HNil
    }
  )
}

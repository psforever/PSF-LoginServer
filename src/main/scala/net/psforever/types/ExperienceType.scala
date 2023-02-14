// Copyright (c) 2023 PSForever
package net.psforever.types

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.packet.PacketHelpers
import scodec.Codec
import scodec.codecs.uint

sealed abstract class ExperienceType(val value: Int) extends IntEnumEntry

object ExperienceType extends IntEnum[ExperienceType] {
  val values: IndexedSeq[ExperienceType] = findValues

  case object Normal extends ExperienceType(value = 0)
  case object Support extends ExperienceType(value = 2)
  case object RabbitBall extends ExperienceType(value = 4)

  implicit val codec: Codec[ExperienceType] = PacketHelpers.createIntEnumCodec(enum = this, uint(bits = 3))
}

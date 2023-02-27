// Copyright (c) 2017 PSForever
package net.psforever.types

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.packet.PacketHelpers
import scodec.codecs._
import scodec.Codec

sealed abstract class UniformStyle(val value: Int) extends IntEnumEntry

/**
 * Values for the four different color designs that impact a player's uniform.
 * Exo-suits get minor graphical updates at the following battle rank levels: seven (1), fourteen (2), and twenty-five (4).
 * At battle rank twenty-four (3), the style does not update visually but switches to one suitable for display of cosmetics.
 * The design for value 5 is visually descriptive of the third upgrade.
 */
object UniformStyle extends IntEnum[UniformStyle] {
  val values: IndexedSeq[UniformStyle] = findValues

  case object Normal extends UniformStyle(value = 0)
  case object FirstUpgrade extends UniformStyle(value = 1)
  case object SecondUpgrade extends UniformStyle(value = 2)
  case object SecondUpgradeBR24 extends UniformStyle(value = 3)
  case object ThirdUpgrade extends UniformStyle(value = 4)
  case object ThirdUpgradeEx extends UniformStyle(value = 5)

  implicit val codec: Codec[UniformStyle] = PacketHelpers.createIntEnumCodec(this, uint(bits = 3))
}

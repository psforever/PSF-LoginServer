// Copyright (c) 2021 PSForever
package net.psforever.objects.avatar

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class SpecialCarry(override val value: String) extends StringEnumEntry

object SpecialCarry extends StringEnum[SpecialCarry] {
  val values = findValues

  case object CaptureFlag extends SpecialCarry(value = "CaptureFlag")
  case object VanuModule extends SpecialCarry(value = "VanuModule")
  case object MonolithUnit extends SpecialCarry(value = "MonolithUnit")
  case object RabbitBall extends SpecialCarry(value = "RabbitBall")
}

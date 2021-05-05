// Copyright (c) 2021 PSForever
package net.psforever.objects.avatar

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class SpecialCarry(override val value: String) extends StringEnumEntry

/**
  * Things that the player can carry that are not stored in the inventory or in holsters.
  */
object SpecialCarry extends StringEnum[SpecialCarry] {
  val values = findValues

  /** The lattice logic unit (LLU).  Not actually a flag. */
  case object CaptureFlag extends SpecialCarry(value = "CaptureFlag")
  /** Special enhancement modules generated in cavern facilities to be installed into above ground facilities. */
  case object VanuModule extends SpecialCarry(value = "VanuModule")
  /** Mysterious MacGuffins tied to the Bending. */
  case object MonolithUnit extends SpecialCarry(value = "MonolithUnit")
  /** Pyon~~ */
  case object RabbitBall extends SpecialCarry(value = "RabbitBall")
}

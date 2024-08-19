package net.psforever.types

import enumeratum.{Enum, EnumEntry}

/**
  * The progress state of being a drowning victim.
  */
sealed abstract class OxygenState extends EnumEntry {}

/**
  * The progress state of being a drowning victim.
  * `Suffocation` means being too far under water.
  * In terms of percentage, progress proceeds towards 0.
  * `Recovery` means emerging from being too far under water.
  * In terms of percentage, progress proceeds towards 100.
  */
object OxygenState extends Enum[OxygenState] {
  val values: IndexedSeq[OxygenState] = findValues

  case object Recovery    extends OxygenState
  case object Suffocation extends OxygenState
}

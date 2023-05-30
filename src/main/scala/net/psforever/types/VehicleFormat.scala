// Copyright (c) 2023 PSForever
package net.psforever.types

import enumeratum.values.{IntEnum, IntEnumEntry}

/**
 * An enumeration of the various formats that known structures that the stream of bits for `VehicleData` can assume.
 */
sealed abstract class VehicleFormat(val value: Int) extends IntEnumEntry

object VehicleFormat extends IntEnum[VehicleFormat] {
  val values: IndexedSeq[VehicleFormat] = findValues

  case object Normal extends VehicleFormat(value = 0)
  case object Battleframe extends VehicleFormat(value = 1)
  case object BattleframeFlight extends VehicleFormat(value = 2)
  case object Utility extends VehicleFormat(value = 6)
  case object Variant extends VehicleFormat(value = 8)
}

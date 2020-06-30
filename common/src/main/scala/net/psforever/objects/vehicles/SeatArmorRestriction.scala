// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

/**
  * An `Enumeration` of exo-suit-based seat access restrictions.<br>
  * <br>
  * The default value is `NoMax` as that is the most common seat type.
  * `NoReinforcedOrMax` is next most common.
  * `MaxOnly` is a rare seat restriction found in pairs on Galaxies and on the large "Ground Transport" vehicles.
  */
object SeatArmorRestriction extends Enumeration {
  type Type = Value

  val MaxOnly, NoMax, NoReinforcedOrMax = Value
}

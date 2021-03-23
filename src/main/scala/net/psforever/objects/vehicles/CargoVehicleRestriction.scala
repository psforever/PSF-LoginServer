// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

/**
  * An `Enumeration` of exo-suit-based mount access restrictions.<br>
  * <br>
  * The default value is `NoMax` as that is the most common mount.
  * `NoReinforcedOrMax` is next most common.
  * `MaxOnly` is a rare mount restriction found in pairs on Galaxies and on the large "Ground Transport" vehicles.
  */
object CargoVehicleRestriction extends Enumeration {
  type Type = Value

  val Small, Large = Value
}

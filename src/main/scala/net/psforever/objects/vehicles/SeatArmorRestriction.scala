// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

/**
  * An `Enumeration` of exo-suit-based mount access restrictions.<br>
  * <br>
  * The default value is `NoMax` as that is the most common mount type.
  * `NoReinforcedOrMax` is next most common.
  * `MaxOnly` is a rare mount restriction found in pairs on Galaxies and on the large "Ground Transport" vehicles.
  * `Unrestricted` is for "seats" that do not limit by exo-suit type, such the orbital shuttle.
  */
object SeatArmorRestriction extends Enumeration {
  type Type = Value

  val MaxOnly, NoMax, NoReinforcedOrMax, Unrestricted = Value
}

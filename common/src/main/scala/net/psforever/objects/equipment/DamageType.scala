// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

/**
  * An `Enumeration` of the damage type.
  */
object DamageType extends Enumeration(1) {
  type Type = Value

  final val Direct,
  Splash,
  Radiation,
  Aggravated,
  Plasma,
  Comet,
  None = Value
}

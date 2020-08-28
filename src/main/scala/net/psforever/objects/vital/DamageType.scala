// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

/**
  * An `Enumeration` of the damage type.
  * These types are not necessarily representative of the kind of projectile being used;
  * for example, the bolt driver's `bolt` is considered "splash."
  */
object DamageType extends Enumeration(1) {
  type Type = Value

  final val Direct, Splash, Lash, Radiation, Aggravated, Plasma, Comet, None = Value
}

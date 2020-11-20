package net.psforever.objects.vital.base

/**
  * An `Enumeration` of the damage type.
  * These types are not necessarily representative of the kind of delivery method being employed;
  * for example, the bolt driver's `bolt` is considered a "splash"ing projectile.
  */
object DamageType extends Enumeration(1) {
  type Type = Value

  final val Direct, Splash, Lash, Radiation, Aggravated, Plasma, Comet, None = Value
}

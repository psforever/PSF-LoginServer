package net.psforever.objects.vital.base

/**
  * An `Enumeration` of the damage types
  * not only distinguishing damage being inflicted
  * but, more importantly, what kind of resistance is brought to bare against that damage.
  * For additional types exclusive to aggravation, refer to `Aura`.
  */
object DamageType extends Enumeration(1) {
  type Type = Value

  //"one" (numerical 1 in the ADB) corresponds to objects that explode and kill fields
  final val Direct, Splash, Lash, Radiation, Aggravated, One, Siphon, None = Value
}

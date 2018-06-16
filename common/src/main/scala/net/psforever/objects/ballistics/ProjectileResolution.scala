// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

/**
  * An `Enumeration` of outcomes regarding what actually happened to the projectile.
  */
object ProjectileResolution extends Enumeration {
  type Type = Value

  val
  Unresolved, //original basic non-resolution
  MissedShot, //projectile did not encounter any collision object and was despawned
  Resolved, //a general "projectile encountered something" status with a more specific resolution
  Hit, //direct hit, one target
  Splash, //area of effect damage, potentially multiple targets
  Lash //lashing damage, potentially multiple targets
  = Value
}

// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

/**
  * An `Enumeration` of outcomes regarding what actually happened to the projectile,
  * complementing normal damage type distinction in directing damage calculations.<br>
  * <br>
  * Although the latter states reflect what sort of damage the projectile might perform - `Hit`, `Splash`, etc. -
  * the state is more a communication about how that damage is interpreted by the server.
  * For example, some projectiles:
  * perform `Direct` damage, are reported by `HitMessage` packets, and resolve as `Hit`;
  * or, perform `Direct` damage, are reported by `LashDamage` packets, and resolve as `Lash`.
  * Furthermore, some projectiles:
  * perform `Splash` damage, are reported by `SplashHitMessage` packets, and resolve as `Splash`;
  * or, perform `Aggravated` damage, are reported by `SplashHitMessage` packets
  * and resolve either as `AggravatedDirect` or as `AggravatedSplash`.
  */
object ProjectileResolution extends Enumeration {
  type Type = Value

  val
  Unresolved,           //original basic non-resolution
  MissedShot,           //projectile did not encounter any collision object and was despawned
  Resolved,             //a general "projectile encountered something" status with a more specific resolution
  Hit,                  //direct hit, one target
  Splash,               //area of effect damage, potentially multiple targets
  Lash,                 //lashing damage, potentially multiple targets
  AggravatedDirect,     //direct hit aggravated damage
  AggravatedDirectBurn, //continuous direct hit aggravated damage
  AggravatedSplash,     //splashed aggravated damage
  AggravatedSplashBurn  //continuous splashed aggravated damage
  = Value
}

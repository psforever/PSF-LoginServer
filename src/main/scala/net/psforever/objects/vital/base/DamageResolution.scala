package net.psforever.objects.vital.base

/**
  * An `Enumeration` of outcomes regarding what actually happened to the damage,
  * complementing normal damage type distinction in directing damage calculations.<br>
  * <br>
  * Although some of the earlier states reflect what sort of damage might perform - `Hit`, `Splash`, etc. -
  * this state is more a communication about how that damage is interpreted by the server.
  * For example, some projectiles:
  * perform `Direct` damage, are reported by `HitMessage` packets, and resolve as `Hit`;
  * or, perform `Direct` damage, are reported by `LashDamage` packets, and resolve as `Lash`.
  * Furthermore, some projectiles:
  * perform `Splash` damage, are reported by `SplashHitMessage` packets, and resolve as `Splash`;
  * or, perform `Aggravated` damage, are reported by `SplashHitMessage` packets
  * and resolve either as `AggravatedDirect` or as `AggravatedSplash`.
  */
object DamageResolution extends Enumeration {
  type Type = Value

  val
  Unresolved,           //original basic non-resolution
  Missed,               //did not interact with anything and was neutralized
  Resolved,             //a general "interacted with something" status, begging for a more specific resolution
  Hit,                  //direct hit, one target
  Splash,               //area of effect damage, potentially multiple targets
  Lash,                 //lashing damage, potentially multiple targets
  AggravatedDirect,     //direct hit aggravated damage
  AggravatedDirectBurn, //continuous direct hit aggravated damage
  AggravatedSplash,     //splashed aggravated damage
  AggravatedSplashBurn, //continuous splashed aggravated damage
  Explosion,            //area of effect damage caused by an internal mechanism; unrelated to Splash
  Environmental,        //died to environmental causes
  Suicide,              //i don't want to be the one the battles always choose
  Collision,            //went splat
  Radiation             //it hurts to stand too close
  = Value
}

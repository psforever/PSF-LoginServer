//Copyright (c) 2020 PSForever
package net.psforever.objects.ballistics

/**
  * Projectile quality is an external aspect of projectiles
  * that is not dependent on hard-coded definitions of the entities
  * used to compose the projectile such as the knowlegde of the emitting `Tool` (weapon).
  * A flag or a damage modifier, depending on use.
  * To the extent that it can be used as a numeric modifier,
  * insists on defining a numeric modifier component rather to what it is trying to express.
  * That numeric modifier does not have to be used for anything.
  */
sealed trait ProjectileQuality {
  def mod: Float
}

/**
  * Implement the numeric modifier with the value as one.
  */
sealed trait SameAsQuality extends ProjectileQuality {
  def mod: Float = 1f
}

object ProjectileQuality {
  /** Standard projectile quality.  More of a flag than a modifier. */
  case object Normal extends SameAsQuality

  /** Quality that flags the first stage of aggravation (initial damage). */
  case object AggravatesTarget extends SameAsQuality

  /** The complete lack of quality.  Even the numeric modifier is zeroed. */
  case object Zeroed extends ProjectileQuality { def mod = 0f }

  /** Assign a custom numeric qualifier value, usually to be applied to damage calculations. */
  case class Modified(mod: Float) extends ProjectileQuality
}

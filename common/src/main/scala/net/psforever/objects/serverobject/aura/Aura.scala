// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.aura

/**
  * An effect that can be emitted by a target game object entity.
  */
sealed class Aura()

/**
  * Visual effects emitted by a target, usually a `Player` entity.
  * Most often paired with aggravated damage.
  * Unrelated to the effects emitted by obtaining and transporting
  * the lattice logic unit, or a facility module, or the rabbit ball.
  */
object Aura {
  /** Since `None` is an actual effect, the "no effect" default is repurposed as "Nothing". */
  final case object Nothing extends Aura

  /** Conferred by the `aphelion_starfire_projectile`. */
  final case object None extends Aura

  /** A green emission. */
  final case object Plasma extends Aura

  /** A purple emission. */
  final case object Comet extends Aura

  /** A white and yellow starburst emission. */
  final case object Napalm extends Aura

  /** A red and orange emission. */
  final case object Fire extends Aura
}

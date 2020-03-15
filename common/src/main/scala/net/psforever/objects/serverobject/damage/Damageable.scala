//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.vital.Vitality

/**
  * The base "control" `Actor` mixin for damage-handling code.
  * A valid entity requires health points and
  * may have additional obstructions to adjusting those health points such as armor and shields.
  * All of these should be affected by the damage where applicable.
  */
trait Damageable {
  /**
    * Contextual access to the object being the target of this damage.
    * Needs declaration in lowest implementing code.
    * @return the entity controlled by this actor
    */
  def DamageableObject : Damageable.Target

  /** the official mixin hook; `orElse` onto the "control" `Actor` `receive` */
  final val takesDamage : Receive = TakesDamage

  /**
    * Implementation of the mixin hook will be provided by a child class.
    * Override this method only when directly implementing.
    * @see `takesDamage`
    * @see `DamageableAmenity.PerformDamage`
    */
  protected def TakesDamage : Receive
}

object Damageable {
  /* the type of all entities governed by this mixin; see Repairable.Target */
  final type Target = PlanetSideServerObject with Vitality

  /**
    * The entity has ben destroyed.
    * @param target the entity being damaged
    * @param cause historical information about the damage
    */
  def DestructionAwareness(target : Damageable.Target, cause : ResolvedProjectile) : Unit = {
    target.Destroyed = true
  }
}

//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.resolution.ResolutionCalculations
import net.psforever.objects.vital.base.{DamageResult, ProjectileDamageInteraction}

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
  def DamageableObject: Damageable.Target

  /** the official mixin hook;
    * `orElse` onto the "control" `Actor` `receive`; or,
    * cite the `originalTakesDamage` protocol during inheritance overrides */
  val takesDamage: Receive = {
    case Vitality.Damage(damage_func) =>
      val obj = DamageableObject
      if (obj.CanDamage) {
        PerformDamage(obj, damage_func)
      }
  }

  /** a duplicate of the core implementation for the default mixin hook, for use in overriding */
  final val originalTakesDamage: Receive = {
    case Vitality.Damage(damage_func) =>
      val obj = DamageableObject
      if (obj.CanDamage) {
        PerformDamage(obj, damage_func)
      }
  }

  /**
    * Assess the vital statistics of the target, apply the damage, and determine if any of those statistics changed.
    * By default, only take an interest in the change of "health".
    * If implementing custom damage with no new message handling, override this method.
    * @see `ResolutionCalculations.Output`
    * @param target the entity to be damaged
    * @param applyDamageTo the function that applies the damage to the target in a target-tailored fashion
    */
  protected def PerformDamage(target: Damageable.Target, applyDamageTo: ResolutionCalculations.Output): Unit
}

object Damageable {
  /* the type of all entities governed by this mixin; see Repairable.Target */
  final type Target = PlanetSideServerObject with Vitality
  /* the master channel for logging damage resolution information
   * the format of the channel is expected to follow:
   * "[identifier]: BEFORE=[before1/before2/etc.] AFTER=[after1/after2/etc.] CHANGE=[change1/change2/etc.]"
   * ... where before1 - change1 = after1, and so forth, for each field that matters
   * the fields do not have to be labeled but the first (if not only) should always be Health
   */
  final val LogChannel: String = "DamageResolution"

  /**
    * Does the possibility exist that the designated target can be affected by this projectile's damage?
    * @see `Hackable`
    * @see `ObjectDefinition.DamageableByFriendlyFire`
    * @param obj the entity being damaged
    * @param damage the amount of damage
    * @param data historical information about the damage
    * @return `true`, if the target can be affected;
    *        `false`, otherwise
    */
  def CanDamage(obj: Vitality with FactionAffinity, damage: Int, data: DamageResult): Boolean = {
    val definition = obj.Definition
    (damage > 0 || data.causesAggravation) &&
    definition.Damageable &&
    (definition.DamageableByFriendlyFire || adversarialOrHackableChecks(obj, data))
  }

  /**
    * Does the possibility exist that the designated target can be affected by this projectile's jammer effect?
    * @see `Hackable`
    * @see `ProjectileDefinition..JammerProjectile`
    * @param obj the entity being damaged
    * @param data historical information about the damage
    * @return `true`, if the target can be affected;
    *        `false`, otherwise
    */
  def CanJammer(obj: Vitality with FactionAffinity, data: DamageResult): Boolean = {
    data.causesJammering &&
    obj.isInstanceOf[JammableUnit] &&
    adversarialOrHackableChecks(obj, data)
  }

  private def adversarialOrHackableChecks(obj: Vitality with FactionAffinity, data: DamageResult): Boolean = {
    (data.adversarial match {
      case Some(adversarial) => adversarial.attacker.Faction != adversarial.defender.Faction
      case None                                  => true
    }) ||
     (obj match {
       case hobj: Hackable => hobj.HackedBy.nonEmpty
       case _              => false
     })
  }

  /**
    * Does the possibility exist that the designated target can be affected by this projectile?
    * @param obj the entity being damaged
    * @param damage the amount of damage
    * @param data historical information about the damage
    * @return `true`, if the target can be affected;
    *        `false`, otherwise
    */
  def CanDamageOrJammer(obj: Vitality with FactionAffinity, damage: Int, data: DamageResult): Boolean = {
    data match {
      case o: ProjectileDamageInteraction => CanDamage(obj, damage, o) || CanJammer(obj, o)
      case _ => false
    }
  }
  def CanDamageOrJammer(obj: Vitality with FactionAffinity, damage: Int, data: ProjectileDamageInteraction): Boolean = {
    CanDamage(obj, damage, data) || CanJammer(obj, data)
  }

  /**
    * The entity has ben destroyed.
    * @param target the entity being damaged
    * @param cause historical information about the damage
    */
  def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    target.Destroyed = true
  }
}

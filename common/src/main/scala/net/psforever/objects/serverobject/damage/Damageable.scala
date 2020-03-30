//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.hackable.Hackable
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
  /* the master channel for logging damage resolution information
   * the format of the channel is expected to follow:
   * "[identifier]: BEFORE=[before1/before2/etc.] AFTER=[after1/after2/etc.] CHANGE=[change1/change2/etc.]"
   * ... where before1 - change1 = after1, and so forth, for each field that matters
   * the fields do not have to be labeled but the first (if not only) should always be Health
   */
  final val LogChannel : String = "DamageResolution"

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
  def CanDamage(obj : Vitality with FactionAffinity, damage : Int, data : ResolvedProjectile) : Boolean = {
    val definition = obj.Definition
    damage > 0 &&
      definition.Damageable &&
      (definition.DamageableByFriendlyFire ||
        (data.projectile.owner.Faction != obj.Faction ||
          (obj match {
            case hobj : Hackable => hobj.HackedBy.nonEmpty
            case _ => false
          })
        )
      )
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
  def CanJammer(obj : Vitality with FactionAffinity, data : ResolvedProjectile) : Boolean = {
    val projectile = data.projectile
    projectile.profile.JammerProjectile &&
      obj.isInstanceOf[JammableUnit] &&
      (projectile.owner.Faction != obj.Faction ||
        (obj match {
          case hobj : Hackable => hobj.HackedBy.nonEmpty
          case _ => false
        })
      )
  }

  /**
    * Does the possibility exist that the designated target can be affected by this projectile?
    * @param obj the entity being damaged
    * @param damage the amount of damage
    * @param data historical information about the damage
    * @return `true`, if the target can be affected;
    *        `false`, otherwise
    */
  def CanDamageOrJammer(obj : Vitality with FactionAffinity, damage : Int, data : ResolvedProjectile) : Boolean = {
    CanDamage(obj, damage, data) || CanJammer(obj, data)
  }

  /**
    * The entity has ben destroyed.
    * @param target the entity being damaged
    * @param cause historical information about the damage
    */
  def DestructionAwareness(target : Damageable.Target, cause : ResolvedProjectile) : Unit = {
    target.Destroyed = true
  }
}

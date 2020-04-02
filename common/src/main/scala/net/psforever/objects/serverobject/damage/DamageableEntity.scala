//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.resolution.ResolutionCalculations
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideGUID
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

/**
  * The "control" `Actor` mixin for damage-handling code,
  * for both expansion into other mixins and specific application on its own.
  */
trait DamageableEntity extends Damageable {
  /** log specifically for damage events */
  private[this] val damageLog = org.log4s.getLogger(Damageable.LogChannel)

  /**
    * Log a damage message.
    * @param msg the message for the damage log
    */
  protected def DamageLog(msg : String) : Unit = {
    damageLog.info(msg)
  }
  /**
    * Log a damage message with a decorator for this target.
    * The decorator is constructed by the `Actor` name of the entity, sliced after the last forward/slash.
    * For example, for "foo/bar/name", the decorator is just "name".
    * @see `PlanetSideServerObject`
    * @param target the entity to be used for the decorator
    * @param msg the message for the damage log
    */
  protected def DamageLog(target : Damageable.Target, msg : String) : Unit = {
    val name = target.Actor.toString
    val slashPoint = name.lastIndexOf("/")
    DamageLog(s"${name.substring(slashPoint + 1, name.length - 1)}: $msg")
  }

  /**
    * Catch the expected damage message and apply checks to the target.
    * If adding custom message handling in an future child implementation,
    * override this method and call `super.TakesDamage.orElse { ... }`.
    * @see `Damageable.TakesDamage`
    * @see `ResolutionCalcultions.Output`
    * @see `Vitality.CanDamage`
    * @see `Vitality.Damage`
    */
  protected def TakesDamage : Receive = {
    case Vitality.Damage(damage_func) =>
      val obj = DamageableObject
      if(obj.CanDamage) {
        PerformDamage(obj, damage_func)
      }
  }

  /**
    * Assess the vital statistics of the target, apply the damage, and determine if any of those statistics changed.
    * By default, only take an interest in the change of "health".
    * If implementing custom `DamageableAmenity` with no new message handling, choose to override this method.
    * @see `DamageableAmenity.TakesDamage`
    * @see `ResolutionCalculations.Output`
    * @see `Vitality.Health`
    * @param target the entity to be damaged
    * @param applyDamageTo the function that applies the damage to the target in a target-tailored fashion
    */
  protected def PerformDamage(target : Damageable.Target, applyDamageTo : ResolutionCalculations.Output) : Unit = {
    val originalHealth = target.Health
    val cause = applyDamageTo(target)
    val health = target.Health
    val damage = originalHealth - health
    if(WillAffectTarget(target, damage, cause)) {
      target.History(cause)
      DamageLog(target, s"BEFORE=$originalHealth, AFTER=$health, CHANGE=$damage")
      HandleDamage(target, cause, damage)
    }
    else {
      target.Health = originalHealth
    }
  }

  /**
    * Does the damage or the projectile that caused the damage offer any reason
    * to execute the reminder of damage resolution considerations?
    * The projectile causing additional affects, e.g., jamming, should be tested here, when applicable.
    * Contrast with `Vitality.CanDamage`.
    * The damage value tested against should be the total value of all meaningful vital statistics affected.
    * @see `Damageable.CanDamageOrJammer`
    * @see `PerformDamage`
    * @param target the entity to be damaged
    * @param damage the amount of damage
    * @param cause historical information about the damage
    * @return `true`, if damage resolution is to be evaluated;
    *        `false`, otherwise
    */
  protected def WillAffectTarget(target : Damageable.Target, damage : Int, cause : ResolvedProjectile) : Boolean = {
    Damageable.CanDamageOrJammer(target, damage, cause)
  }

  /**
    * Select between mere damage reception or target destruction.
    * @see `VitalDefinition.DamageDestroysAt`
    * @param target the entity being damaged
    * @param cause historical information about the damage
    * @param damage the amount of damage
    */
  protected def HandleDamage(target : Damageable.Target, cause : ResolvedProjectile, damage : Int) : Unit = {
    if(!target.Destroyed && target.Health <= target.Definition.DamageDestroysAt) {
      DestructionAwareness(target, cause)
    }
    else {
      DamageAwareness(target, cause, damage)
    }
  }

  /**
    * What happens when damage is sustained but the target does not get destroyed.
    * @param target the entity being damaged
    * @param cause historical information about the damage
    * @param amount the amount of damage
    */
  protected def DamageAwareness(target : Damageable.Target, cause : ResolvedProjectile, amount : Int) : Unit = {
    DamageableEntity.DamageAwareness(target, cause, amount)
  }

  /**
    * What happens when the target sustains too much damage and is destroyed.
    * @see `Damageable.DestructionAwareness`
    * @param target the entity being destroyed
    * @param cause historical information about the damage
    */
  protected def DestructionAwareness(target : Damageable.Target, cause : ResolvedProjectile) : Unit = {
    Damageable.DestructionAwareness(target, cause)
    DamageableEntity.DestructionAwareness(target, cause)
  }
}

object DamageableEntity {
  /**
    * A damaged target dispatches messages to:
    * - reports its adjusted its health;
    * - alert the activity monitor for that `Zone` about the damage; and,
    * - provide a feedback message regarding the damage.
    * @see `AvatarAction.PlanetsideAttributeToAll`
    * @see `AvatarAction.SendResponse`
    * @see `AvatarServiceMessage`
    * @see `DamageFeedbackMessage`
    * @see `JammableUnit.Jammered`
    * @see `Service.defaultPlayerGUID`
    * @see `Zone.Activity`
    * @see `Zone.AvatarEvents`
    * @see `Zone.HotSpot.Activity`
    * @see `Zone.LivePlayers`
    * @param target the entity being damaged
    * @param cause historical information about the damage
    */
  def DamageAwareness(target : Damageable.Target, cause : ResolvedProjectile, amount : Int) : Unit = {
    if(Damageable.CanJammer(target, cause)) {
      target.Actor ! JammableUnit.Jammered(cause)
    }
    if(amount > 0) {
      val zone = target.Zone
      if(!target.Destroyed) {
        val tguid = target.GUID
        zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttributeToAll(tguid, 0, target.Health))
      }
      zone.Activity ! Zone.HotSpot.Activity(cause.target, cause.projectile.owner, cause.hit_pos)
    }
  }

  /**
    * A destroyed target dispatches messages to:
    * - reports its adjusted its health; and,
    * - report about its destruction.
    * @see `AvatarAction.Destroy`
    * @see `AvatarAction.PlanetsideAttribute`
    * @see `AvatarServiceMessage`
    * @see `DamageFeedbackMessage`
    * @see `JammableUnit.ClearJammeredSound`
    * @see `JammableUnit.ClearJammeredStatus`
    * @see `Zone.AvatarEvents`
    * @param target the entity being destroyed
    * @param cause historical information about the damage
    */
  def DestructionAwareness(target : Damageable.Target, cause : ResolvedProjectile) : Unit = {
    //un-jam
    target.Actor ! JammableUnit.ClearJammeredSound()
    target.Actor ! JammableUnit.ClearJammeredStatus()
    //
    val zone = target.Zone
    val zoneId = zone.Id
    val tguid = target.GUID
    val attribution = target.Zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => PlanetSideGUID(0)
    }
    zone.AvatarEvents ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 0, target.Health))
    zone.AvatarEvents ! AvatarServiceMessage(zoneId, AvatarAction.Destroy(tguid, attribution, Service.defaultPlayerGUID, target.Position))
  }
}

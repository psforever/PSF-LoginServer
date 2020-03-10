//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.resolution.ResolutionCalculations
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.DamageFeedbackMessage
import net.psforever.types.PlanetSideGUID
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

trait DamageableEntity extends Damageable {
  private[this] val damageLog = org.log4s.getLogger("DamageResolution")

  /**
    * na
    * @param msg na
    */
  final protected def DamageLog(msg : String) : Unit = {
    damageLog.info(msg)
  }

  /**
    * na
    * @return na
    */
  protected def TakesDamage : Receive = {
    case Vitality.Damage(damage_func) =>
      val obj = DamageableObject
      if(obj.CanDamage) {
        PerformDamage(obj, damage_func)
      }
  }

  val takesDamage : Receive = TakesDamage

  /**
    * na
    * @param target na
    * @param applyDamageTo na
    */
  protected def PerformDamage(target : Damageable.Target, applyDamageTo : ResolutionCalculations.Output) : Unit = {
    val originalHealth = target.Health
    val cause = applyDamageTo(target)
    val health = target.Health
    val damage = originalHealth - health
    if(damage > 0) {
      val name = target.Actor.toString
      val slashPoint = name.lastIndexOf("/")
      DamageLog(s"${name.substring(slashPoint + 1, name.length - 1)}: BEFORE=$originalHealth, AFTER=$health, CHANGE=$damage")
      HandleDamage(target, cause, damage)
    }
  }

  /**
    * na
    * @param target na
    * @param cause na
    * @param damage na
    */
  protected def HandleDamage(target : Damageable.Target, cause : ResolvedProjectile, damage : Int) : Unit = {
    if(damage > 0) {
      val zone = target.Zone
      val health = target.Health
      zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttribute(target.GUID, 0, health))
      if(health <= target.Definition.DamageDestroysAt) {
        DestructionAwareness(target, cause)
      }
      else {
        DamageAwareness(target, cause, damage)
      }
    }
  }

  /**
    * na
    * @param target na
    * @param cause na
    * @param amount na
    */
  protected def DamageAwareness(target : Damageable.Target, cause : ResolvedProjectile, amount : Int) : Unit = {
    val playerGUID = target.Zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => PlanetSideGUID(0)
    }
    DamageableEntity.DamageAwareness(target, playerGUID, cause)
  }

  /**
    * na
    * @param target na
    * @param cause na
    */
  protected def DestructionAwareness(target : Damageable.Target, cause : ResolvedProjectile) : Unit = {
    Destruction(target)
    val playerGUID = target.Zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => PlanetSideGUID(0)
    }
    DamageableEntity.DestructionAwareness(target, playerGUID)
  }
}

object DamageableEntity {
  /**
    * na
    * @param target na
    * @param attribution na
    * @param cause na
    */
  def DamageAwareness(target : Damageable.Target, attribution : PlanetSideGUID, cause : ResolvedProjectile) : Unit = {
    val zone = target.Zone
    val targetGUID = target.GUID
    zone.Activity ! Zone.HotSpot.Activity(cause.target, cause.projectile.owner, cause.hit_pos)
    zone.AvatarEvents ! AvatarServiceMessage(
      cause.projectile.owner.Name,
      AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageFeedbackMessage(5, true, Some(attribution), None, None, false, Some(targetGUID), None, None, None, 0, 0L, 0))
    )
  }

  /**
    * na
    * @param target na
    * @param attribution na
    */
  def DestructionAwareness(target : Damageable.Target, attribution : PlanetSideGUID) : Unit = {
    val zone = target.Zone
    val zoneId = zone.Id
    zone.AvatarEvents ! AvatarServiceMessage(zoneId, AvatarAction.Destroy(target.GUID, attribution, Service.defaultPlayerGUID, target.Position))
  }
}

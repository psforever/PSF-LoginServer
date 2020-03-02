//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.vital.Vitality
import net.psforever.packet.game.DamageFeedbackMessage
import net.psforever.types.PlanetSideGUID
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

trait DamageableAmenity extends Damageable {
  def DamageableObject : Amenity

  val takesDamage : Receive = {
    case Vitality.Damage(damage_func) =>
      val obj = DamageableObject
      val definition = obj.Definition
      if(obj.CanDamage) {
        val originalHealth = obj.Health
        val cause = damage_func(obj)
        val health = obj.Health
        val damageToHealth = originalHealth - health
        DamageableAmenity.HandleDamageResolution(obj, cause, damageToHealth)
        if(damageToHealth > 0) {
          val name = obj.Actor.toString
          val slashPoint = name.lastIndexOf("/")
          org.log4s.getLogger("DamageResolution").info(s"${name.substring(slashPoint + 1, name.length - 1)}: BEFORE=$originalHealth, AFTER=$health, CHANGE=$damageToHealth")
        }
      }
  }
}

object DamageableAmenity {
  def HandleDamageResolution(target : Amenity, cause : ResolvedProjectile, damage : Int) : Unit = {
    val zone = target.Zone
    val targetGUID = target.GUID
    val playerGUID = zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => PlanetSideGUID(0)
    }
    if(target.Health > target.Definition.DamageDestroysAt) {
      if(damage > 0) {
        HandleDamageAwareness(target, playerGUID, cause)
      }
    }
    else {
      HandleDestructionAwareness(target, playerGUID, cause)
    }
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.PlanetsideAttribute(targetGUID, 0, target.Health))
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDamageAwareness(target : Amenity, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    val targetGUID = target.GUID
    target.Zone.AvatarEvents ! AvatarServiceMessage(
      lastShot.projectile.owner.Name,
      AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageFeedbackMessage(5, true, Some(attribution), None, None, false, Some(targetGUID), None, None, None, 0, 0L, 0))
    )
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDestructionAwareness(target : Amenity, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    val zone = target.Zone
    val zoneId = zone.Id
    val events = zone.AvatarEvents
    val targetGUID = target.GUID
    target.Destroyed = true
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 50, 1))
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(targetGUID, 51, 1))
    events ! AvatarServiceMessage(zoneId, AvatarAction.Destroy(targetGUID, attribution, Service.defaultPlayerGUID, target.Position))
  }
}

//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.turret.{TurretUpgrade, WeaponTurret}
import net.psforever.objects.vehicles.MountedWeapons
import net.psforever.objects.vital.DamageType
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.DamageWithPositionMessage
import net.psforever.types.Vector3
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.vehicle.support.TurretUpgrader
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

/**
  * The "control" `Actor` mixin for damage-handling code for `WeaponTurret` objects.
  */
trait DamageableWeaponTurret
  extends DamageableEntity
  with AggravatedBehavior {
  _: Actor =>

  def damageableWeaponTurretPostStop(): Unit = {
    EndAllAggravation()
  }

  def DamageableObject: Damageable.Target with WeaponTurret
  def AggravatedObject: Damageable.Target with WeaponTurret = DamageableObject

  override val takesDamage: Receive = originalTakesDamage.orElse(aggravatedBehavior)

  override protected def DamageAwareness(target: Damageable.Target, cause: ResolvedProjectile, amount: Any): Unit = {
    val obj            = DamageableObject
    val zone           = target.Zone
    val events         = zone.VehicleEvents
    val targetGUID     = target.GUID
    val zoneId         = zone.id
    val damageToHealth = amount match {
      case a: Int => a
      case _ => 0
    }
    var announceConfrontation: Boolean = damageToHealth > 0
    val aggravated = TryAggravationEffectActivate(cause) match {
      case Some(_) =>
        announceConfrontation = true
        false
      case _ =>
        cause.projectile.profile.ProjectileDamageTypes.contains(DamageType.Aggravated)
    }

    //log historical event
    target.History(cause)
    //damage
    if (Damageable.CanDamageOrJammer(target, damageToHealth, cause)) {
      //jammering
      if (Damageable.CanJammer(target, cause)) {
        target.Actor ! JammableUnit.Jammered(cause)
      }
      //stat changes
      //TODO some turrets have shields
      if (damageToHealth > 0) {
        DamageableMountable.DamageAwareness(DamageableObject, cause, damageToHealth)
        events ! VehicleServiceMessage(
          zoneId,
          VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 0, obj.Health)
        )
        announceConfrontation = true
      }
    }
    if (announceConfrontation) {
      if (aggravated) {
        val msg = VehicleAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(damageToHealth, Vector3.Zero))
        obj.Seats.values
          .collect { case seat if seat.Occupant.nonEmpty => seat.Occupant.get.Name }
          .foreach { channel =>
            events ! VehicleServiceMessage(channel, msg)
          }
      }
      else {
        //activity on map
        zone.Activity ! Zone.HotSpot.Activity(cause.target, cause.projectile.owner, cause.hit_pos)
        //alert to damage source
        DamageableMountable.DamageAwareness(obj, cause, damageToHealth)
      }
    }
  }

  override protected def DestructionAwareness(target: Damageable.Target, cause: ResolvedProjectile): Unit = {
    super.DestructionAwareness(target, cause)
    val obj = DamageableObject
    EndAllAggravation()
    DamageableWeaponTurret.DestructionAwareness(obj, cause)
    DamageableMountable.DestructionAwareness(obj, cause)
  }
}

object DamageableWeaponTurret {

  /**
    * A destroyed target dispatches a message to conceal (delete) its weapons from users.
    * If affected by a jammer property, the jammer propoerty will be removed.
    * If the type of entity is a `WeaponTurret`, the weapons are converted to their "normal" upgrade state.
    * @see `AvatarAction.DeleteObject`
    * @see `AvatarServiceMessage`
    * @see `MountedWeapons`
    * @see `MountedWeapons.Weapons`
    * @see `Service.defaultPlayerGUID`
    * @see `TurretUpgrade.None`
    * @see `TurretUpgrader.AddTask`
    * @see `TurretUpgrader.ClearSpecific`
    * @see `WeaponTurret`
    * @see `VehicleServiceMessage.TurretUpgrade`
    * @see `Zone.AvatarEvents`
    * @see `Zone.VehicleEvents`
    * @param target the entity being destroyed;
    *               note: `MountedWeapons` is a parent of `WeaponTurret`
    *               but the handling code closely associates with the former
    * @param cause historical information about the damage
    */
  def DestructionAwareness(target: Damageable.Target with MountedWeapons, cause: ResolvedProjectile): Unit = {
    //wreckage has no (visible) mounted weapons
    val zone         = target.Zone
    val zoneId       = zone.id
    val avatarEvents = zone.AvatarEvents
    target.Weapons.values
      .filter {
        _.Equipment.nonEmpty
      }
      .foreach(slot => {
        val wep = slot.Equipment.get
        avatarEvents ! AvatarServiceMessage(zoneId, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, wep.GUID))
      })
    target match {
      case turret: WeaponTurret =>
        if (turret.Upgrade != TurretUpgrade.None) {
          val vehicleEvents = zone.VehicleEvents
          vehicleEvents ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.ClearSpecific(List(turret), zone))
          vehicleEvents ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.AddTask(turret, zone, TurretUpgrade.None))
        }
      case _ =>
    }
  }
}

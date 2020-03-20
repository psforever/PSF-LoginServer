//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.turret.{TurretUpgrade, WeaponTurret}
import net.psforever.objects.vehicles.MountedWeapons
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.vehicle.support.TurretUpgrader
import services.vehicle.VehicleServiceMessage

/**
  * The "control" `Actor` mixin for damage-handling code for `WeaponTurret` objects.
  */
trait DamageableWeaponTurret extends DamageableEntity {
  def DamageableObject : Damageable.Target with WeaponTurret

  override def WillAffectTarget(damage : Int, cause : ResolvedProjectile) : Boolean = {
    //jammable
    super.WillAffectTarget(damage, cause) || cause.projectile.profile.JammerProjectile
  }

  override protected def DamageAwareness(target : Damageable.Target, cause : ResolvedProjectile, amount : Int) : Unit = {
    DamageableWeaponTurret.DamageAwareness(target, cause) //jammer, even if no damage amount
    if(amount > 0) {
      super.DamageAwareness(target, cause, amount)
      DamageableMountable.DamageAwareness(DamageableObject, cause)
    }
  }

  override protected def DestructionAwareness(target : Damageable.Target, cause : ResolvedProjectile) : Unit = {
    super.DestructionAwareness(target, cause)
    val obj = DamageableObject
    DamageableWeaponTurret.DestructionAwareness(obj, cause)
    DamageableMountable.DestructionAwareness(obj, cause)
  }
}

object DamageableWeaponTurret {
  /**
    * The weapons attached to the turret jams if the projectile has jammering properties.
    * @see `JammableUnit.Jammered`
    * @param target the entity being damaged
    * @param cause historical information about the damage
    */
  def DamageAwareness(target : Damageable.Target, cause : ResolvedProjectile) : Unit = {
    if(cause.projectile.profile.JammerProjectile) {
      target.Actor ! JammableUnit.Jammered(cause)
    }
  }

  /**
    * A destroyed target dispatches a message to conceal (delete) its weapons from users.
    * If affected by a jammer property, the jammer propoerty will be removed.
    * If the type of entity is a `WeaponTurret`, the weapons are converted to their "normal" upgrade state.
    * @see `AvatarAction.DeleteObject`
    * @see `AvatarServiceMessage`
    * @see `JammableUnit.ClearJammeredSound`
    * @see `JammableUnit.ClearJammeredStatus`
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
  def DestructionAwareness(target : Damageable.Target with MountedWeapons, cause : ResolvedProjectile) : Unit = {
    //un-jam
    target.Actor ! JammableUnit.ClearJammeredSound()
    target.Actor ! JammableUnit.ClearJammeredStatus()
    //wreckage has no (visible) mounted weapons
    val zone = target.Zone
    val zoneId = zone.Id
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
      case turret : WeaponTurret =>
        if(turret.Upgrade != TurretUpgrade.None) {
          val vehicleEvents = zone.VehicleEvents
          vehicleEvents ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.ClearSpecific(List(turret), zone))
          vehicleEvents ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.AddTask(turret, zone, TurretUpgrade.None))
        }
      case _ =>
    }
  }
}

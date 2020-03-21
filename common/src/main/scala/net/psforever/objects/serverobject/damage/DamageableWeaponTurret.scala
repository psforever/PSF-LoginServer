//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import net.psforever.objects.ballistics.ResolvedProjectile
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

  override protected def DamageAwareness(target : Damageable.Target, cause : ResolvedProjectile, amount : Int) : Unit = {
    super.DamageAwareness(target, cause, amount)
    if(amount > 0) {
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
  def DestructionAwareness(target : Damageable.Target with MountedWeapons, cause : ResolvedProjectile) : Unit = {
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

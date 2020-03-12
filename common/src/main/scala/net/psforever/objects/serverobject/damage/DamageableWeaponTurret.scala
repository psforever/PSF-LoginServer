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

trait DamageableWeaponTurret extends DamageableEntity {
  def DamageableObject : Damageable.Target with WeaponTurret

  override def WillAffectTarget(damage : Int, cause : ResolvedProjectile) : Boolean = {
    super.WillAffectTarget(damage, cause) || cause.projectile.profile.JammerProjectile
  }

  override protected def DamageAwareness(target : Damageable.Target, cause : ResolvedProjectile, amount : Int) : Unit = {
    super.DamageAwareness(target, cause, amount)
    val obj = DamageableObject
    DamageableWeaponTurret.DamageAwareness(obj, cause, amount)
    DamageableMountable.DamageAwareness(obj, cause, amount)
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
    * na
    * @param obj na
    * @param cause na
    * @param amount na
    */
  def DamageAwareness(obj : Damageable.Target, cause : ResolvedProjectile, amount : Int) : Unit = {
    //if entity gets jammered ...
    if(cause.projectile.profile.JammerProjectile) {
      obj.Actor ! JammableUnit.Jammered(cause)
    }
  }

  /**
    * na
    * @param obj na
    * @param lastShot na
    */
  def DestructionAwareness(obj : Damageable.Target with MountedWeapons, lastShot : ResolvedProjectile) : Unit = {
    //un-jam
    obj.Actor ! JammableUnit.ClearJammeredSound()
    obj.Actor ! JammableUnit.ClearJammeredStatus()
    //wreckage has no (visible) mounted weapons
    val zone = obj.Zone
    val zoneId = zone.Id
    val avatarEvents = zone.AvatarEvents
    obj.Weapons.values
      .filter {
        _.Equipment.nonEmpty
      }
      .foreach(slot => {
        val wep = slot.Equipment.get
        avatarEvents ! AvatarServiceMessage(zoneId, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, wep.GUID))
      })
    obj match {
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

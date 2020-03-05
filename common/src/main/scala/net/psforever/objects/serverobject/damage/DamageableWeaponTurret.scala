//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.Player
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.turret.{TurretUpgrade, WeaponTurret}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideGUID
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.vehicle.support.TurretUpgrader
import services.vehicle.{VehicleAction, VehicleServiceMessage}

trait DamageableWeaponTurret extends Damageable {
  def DamageableObject : PlanetSideServerObject with Vitality with WeaponTurret

  val takesDamage : Receive = {
    case Vitality.Damage(damage_func) =>
      val obj = DamageableObject
      if(obj.CanDamage) {
        val originalHealth = obj.Health
        val cause = damage_func(obj)
        val health = obj.Health
        val damageToHealth = originalHealth - health
        DamageableWeaponTurret.HandleDamageResolution(obj, cause, damageToHealth)
        if(damageToHealth > 0) {
          val name = obj.Actor.toString
          val slashPoint = name.lastIndexOf("/")
          org.log4s.getLogger("DamageResolution").info(s"${name.substring(slashPoint + 1, name.length - 1)}: BEFORE=$originalHealth, AFTER=$health, CHANGE=$damageToHealth")
        }
      }
  }
}

object DamageableWeaponTurret {
  def HandleDamageResolution(target : PlanetSideServerObject with Vitality with WeaponTurret, cause : ResolvedProjectile, damage : Int) : Unit = {
    val zone = target.Zone
    val targetGUID = target.GUID
    val playerGUID = target.Zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => targetGUID
    }
    val continentId = zone.Id
    if(target.Health > 0) {
      //alert occupants to damage source
      if(damage > 0) {
        zone.Activity ! Zone.HotSpot.Activity(cause.target, cause.projectile.owner, cause.hit_pos)
        //alert occupants to damage source
        HandleDamageAwareness(target, playerGUID, cause)
      }
      if(cause.projectile.profile.JammerProjectile) {
        target.Actor ! JammableUnit.Jammered(cause)
      }
    }
    else {
      //alert to vehicle death (hence, occupants' deaths)
      HandleDestructionAwareness(target, playerGUID, cause)
    }
    zone.VehicleEvents ! VehicleServiceMessage(continentId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 0, target.Health))
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDamageAwareness(target : PlanetSideServerObject with WeaponTurret, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    val zone = target.Zone
    val zoneId = zone.Id
    //alert occupants to damage source
    target.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    }).foreach(seat => {
      val tplayer = seat.Occupant.get
      zone.AvatarEvents ! AvatarServiceMessage(zoneId, AvatarAction.HitHint(attribution, tplayer.GUID))
    })
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDestructionAwareness(target : PlanetSideServerObject with Vitality with WeaponTurret, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    target.Actor ! JammableUnit.ClearJammeredSound()
    target.Actor ! JammableUnit.ClearJammeredStatus()
    val zone = target.Zone
    val zoneId = zone.Id
    val avatarEvents = zone.AvatarEvents
    val tguid = target.GUID
    target.Destroyed = true
    target.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    }).foreach(seat => {
      val tplayer = seat.Occupant.get
      tplayer.History(lastShot)
      tplayer.Actor ! Player.Die()
    })
    //turret wreckage has no weapons
    target.Weapons.values
      .filter {
        _.Equipment.nonEmpty
      }
      .foreach(slot => {
        val wep = slot.Equipment.get
        avatarEvents ! AvatarServiceMessage(zoneId, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, wep.GUID))
      })
    target.Health = 0
    avatarEvents ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 0, target.Health))
    avatarEvents ! AvatarServiceMessage(zoneId, AvatarAction.Destroy(tguid, attribution, PlanetSideGUID(0), target.Position))
    if(target.Upgrade != TurretUpgrade.None) {
      val vehicleEvents = zone.VehicleEvents
      vehicleEvents ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.ClearSpecific(List(target), zone))
      vehicleEvents ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.AddTask(target, zone, TurretUpgrade.None))
    }
  }
}

// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.{DefaultCancellable, GlobalDefinitions, Player}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.equipment.{JammableMountedWeapons, JammableUnit}
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideGUID
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}
import services.vehicle.{VehicleAction, VehicleServiceMessage}
import services.vehicle.support.TurretUpgrader

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


/**
  * An `Actor` that handles messages being dispatched to a specific `MannedTurret`.<br>
  * <br>
  * Mounted turrets have only slightly different entry requirements than a normal vehicle
  * because they encompass both faction-specific facility turrets
  * and faction-blind cavern sentry turrets.
  * @param turret the `MannedTurret` object being governed
  */
class FacilityTurretControl(turret : FacilityTurret) extends Actor
  with FactionAffinityBehavior.Check
  with MountableBehavior.Dismount
  with JammableMountedWeapons {

  if(turret.Definition == GlobalDefinitions.vanu_sentry_turret) {
    // todo: Schedule this to start when weapon is discharged, not all the time
    context.system.scheduler.schedule(3 seconds, 200 milliseconds, self, FacilityTurret.RechargeAmmo())
  }

  def MountableObject = turret

  def JammableObject = turret

  def FactionObject : FactionAffinity = turret

  def receive : Receive = checkBehavior
    .orElse(jammableBehavior)
    .orElse(dismountBehavior)
    .orElse {
      case FacilityTurret.RechargeAmmo() =>
        val weapon = turret.ControlledWeapon(1).get.asInstanceOf[net.psforever.objects.Tool]

        // recharge when last shot fired 3s delay, +1, 200ms interval
        if(weapon.Magazine < weapon.MaxMagazine && System.nanoTime() - weapon.LastDischarge > 3000000000L) {
          weapon.Magazine += 1
          val seat = turret.Seat(0).get
          seat.Occupant match {
            case Some(player: Player) => turret.Zone.LocalEvents ! LocalServiceMessage(turret.Zone.Id, LocalAction.RechargeVehicleWeapon(player.GUID, turret.GUID, weapon.GUID))
            case _ => ;
          }
        }
      case Mountable.TryMount(user, seat_num) =>
        turret.Seat(seat_num) match {
          case Some(seat) =>
            if((!turret.Definition.FactionLocked || user.Faction == turret.Faction) &&
              (seat.Occupant = user).contains(user)) {
              user.VehicleSeated = turret.GUID
              sender ! Mountable.MountMessages(user, Mountable.CanMount(turret, seat_num))
            }
            else {
              sender ! Mountable.MountMessages(user, Mountable.CanNotMount(turret, seat_num))
            }
          case None =>
            sender ! Mountable.MountMessages(user, Mountable.CanNotMount(turret, seat_num))
        }

      case Vitality.Damage(damage_func) =>
        if(turret.Health > 0) {
          val originalHealth = turret.Health
          val cause = damage_func(turret)
          val health = turret.Health
          val damageToHealth = originalHealth - health
          FacilityTurretControl.HandleDamageResolution(turret, cause, damageToHealth)
          if(damageToHealth > 0) {
            val name = turret.Actor.toString
            val slashPoint = name.lastIndexOf("/")
            org.log4s.getLogger("DamageResolution").info(s"${name.substring(slashPoint + 1, name.length - 1)}: BEFORE=$originalHealth, AFTER=$health, CHANGE=$damageToHealth")
          }
        }

      case _ => ;
    }
}

object FacilityTurretControl {
  def HandleDamageResolution(target : FacilityTurret, cause : ResolvedProjectile, damage : Int) : Unit = {
    val zone = target.Zone
    val targetGUID = target.GUID
    val playerGUID = target.Zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => targetGUID
    }
    val continentId = zone.Id
    if(target.Health > 1) {
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
  def HandleDamageAwareness(target : FacilityTurret, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
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
  def HandleDestructionAwareness(target : FacilityTurret, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    target.Actor ! JammableUnit.ClearJammeredSound()
    target.Actor ! JammableUnit.ClearJammeredStatus()
    val zone = target.Zone
    val zoneId = zone.Id
    target.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    }).foreach(seat => {
      val tplayer = seat.Occupant.get
      tplayer.History(lastShot)
      tplayer.Actor ! Player.Die()
    })
    //turret wreckage has no weapons
    //      target.Weapons.values
    //        .filter {
    //          _.Equipment.nonEmpty
    //        }
    //        .foreach(slot => {
    //          val wep = slot.Equipment.get
    //          zone.AvatarEvents ! AvatarServiceMessage(continentId, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, wep.GUID))
    //        })
    //      zone.AvatarEvents ! AvatarServiceMessage(continentId, AvatarAction.Destroy(targetGUID, playerGUID, playerGUID, player.Position))
    target.Health = 1 //TODO turret "death" at 0, as is proper
    zone.VehicleEvents ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, target.GUID, 0, target.Health)) //TODO not necessary
    if(target.Upgrade != TurretUpgrade.None) {
      zone.VehicleEvents ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.ClearSpecific(List(target), zone))
      zone.VehicleEvents ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.AddTask(target, zone, TurretUpgrade.None))
    }
  }
}

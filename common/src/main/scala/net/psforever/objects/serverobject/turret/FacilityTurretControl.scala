// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.{DefaultCancellable, Tool}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.equipment.{JammableUnit, JammingUnit}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.vehicles.MountedWeapons
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.vehicle.{VehicleAction, VehicleServiceMessage}
import services.vehicle.support.TurretUpgrader

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
  with MountableBehavior.Dismount {
  var jammeredSoundTimer : Cancellable = DefaultCancellable.obj
  var jammeredStatusTimer : Cancellable = DefaultCancellable.obj

  def MountableObject = turret //do not add type!

  def FactionObject : FactionAffinity = turret

  def receive : Receive = checkBehavior
    .orElse(dismountBehavior)
    .orElse {
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

      case JammableUnit.Jammered(cause) =>
        TryJammerWithProjectile(turret, cause)

      case JammableUnit.ClearJammeredSound() =>
        CancelJammeredSound(turret)

      case JammableUnit.ClearJammeredStatus() =>
        StopJammeredStatus(turret)

      case _ => ;
    }

  def TryJammerWithProjectile(target : FacilityTurret, cause : ResolvedProjectile) : Unit = {
    val radius = cause.projectile.profile.DamageRadius
    JammingUnit.FindJammerDuration(cause.projectile.profile, target) match {
      case Some(dur) if Vector3.DistanceSquared(cause.hit_pos, cause.target.Position) < radius * radius =>
        //jammered sound
        target.Zone.VehicleEvents ! VehicleServiceMessage(target.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, target.GUID, 54, 1))
        import scala.concurrent.ExecutionContext.Implicits.global
        jammeredSoundTimer = context.system.scheduler.scheduleOnce(30 seconds, self, JammableUnit.ClearJammeredSound())
        //jammered status
        StartJammeredStatus(target, dur)
      case _ => ;
    }
  }

  def StartJammeredStatus(target : PlanetSideServerObject with MountedWeapons, dur : Int) : Unit = {
    jammeredStatusTimer.cancel
    FacilityTurretControl.JammeredStatus(target, 1)
    import scala.concurrent.ExecutionContext.Implicits.global
    jammeredStatusTimer = context.system.scheduler.scheduleOnce(dur milliseconds, self, JammableUnit.ClearJammeredStatus())
  }

  def StopJammeredStatus(target : PlanetSideServerObject with MountedWeapons) : Boolean = {
    FacilityTurretControl.JammeredStatus(target, 0)
    jammeredStatusTimer.cancel
  }

  def CancelJammeredSound(target : PlanetSideServerObject) : Unit = {
    jammeredSoundTimer.cancel
    target.Zone.VehicleEvents ! VehicleServiceMessage(target.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, target.GUID, 54, 0))
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
      val tplayerGUID = tplayer.GUID
      zone.AvatarEvents ! AvatarServiceMessage(tplayer.Name, AvatarAction.KilledWhileInVehicle(tplayerGUID))
      zone.AvatarEvents ! AvatarServiceMessage(zoneId, AvatarAction.ObjectDelete(tplayerGUID, tplayerGUID)) //dead player still sees self
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

  def JammeredStatus(target : PlanetSideServerObject with MountedWeapons, statusCode : Int) : Unit = {
    val zone = target.Zone
    val zoneId = zone.Id
    zone.VehicleEvents ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, target.GUID, 27, statusCode))
    target.Weapons.values
      .map { _.Equipment }
      .collect {
        case Some(item : Tool) =>
          zone.VehicleEvents ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, item.GUID, 27, statusCode))
      }
  }
}

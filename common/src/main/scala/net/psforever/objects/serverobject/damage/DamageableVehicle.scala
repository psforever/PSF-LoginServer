//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.Zone
import net.psforever.types.{DriveState, PlanetSideGUID, Vector3}
import services.{RemoverActor, Service}
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}
import services.vehicle.{VehicleAction, VehicleService, VehicleServiceMessage}

import scala.concurrent.duration._

trait DamageableVehicle extends Damageable {
  def DamageableObject : Vehicle

  val takesDamage : Receive = {
    case Vitality.Damage(damage_func) =>
      val obj =  DamageableObject
      if(obj.CanDamage) {
        val originalHealth = obj.Health
        val originalShields = obj.Shields
        val cause = damage_func(obj)
        val health = obj.Health
        val shields = obj.Shields
        val damageToHealth = originalHealth - health
        val damageToShields = originalShields - shields
        DamageableVehicle.HandleDamageResolution(obj, cause, damageToHealth + damageToShields)
        if(damageToHealth > 0 || damageToShields > 0) {
          val name = obj.Actor.toString
          val slashPoint = name.lastIndexOf("/")
          org.log4s.getLogger("DamageResolution").info(s"${name.substring(slashPoint + 1, name.length - 1)}: BEFORE=$originalHealth/$originalShields, AFTER=$health/$shields, CHANGE=$damageToHealth/$damageToShields")
        }
      }
  }
}

object DamageableVehicle {
  /**
    * na
    * @param target na
    */
  def HandleDamageResolution(target : Vehicle, cause : ResolvedProjectile, damage : Int) : Unit = {
    val zone = target.Zone
    val targetGUID = target.GUID
    val playerGUID = zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => PlanetSideGUID(0)
    }
    if(target.Health > 0) {
      //activity on map
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
    zone.VehicleEvents ! VehicleServiceMessage(zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 0, target.Health))
    zone.VehicleEvents ! VehicleServiceMessage(s"${target.Actor}", VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 68, target.Shields))
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDamageAwareness(target : Vehicle, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    val zone = target.Zone
    //alert occupants to damage source
    target.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    }).foreach(seat => {
      val tplayer = seat.Occupant.get
      zone.AvatarEvents ! AvatarServiceMessage(tplayer.Name, AvatarAction.HitHint(attribution, tplayer.GUID))
    })
    //alert cargo occupants to damage source
    target.CargoHolds.values.foreach(hold => {
      hold.Occupant match {
        case Some(cargo) =>
          cargo.Health = 0
          cargo.Shields = 0
          cargo.History(lastShot)
          HandleDamageAwareness(cargo, attribution, lastShot)
        case None => ;
      }
    })
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDestructionAwareness(target : Vehicle, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    target.Actor ! JammableUnit.ClearJammeredSound()
    target.Actor ! JammableUnit.ClearJammeredStatus()
    val zone = target.Zone
    val continentId = zone.Id
    target.Destroyed = true
    //alert to vehicle death (hence, occupants' deaths)
    target.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    }).foreach(seat => {
      val tplayer = seat.Occupant.get
      tplayer.History(lastShot)
      tplayer.Actor ! Player.Die()
    })
    //vehicle wreckage has no weapons
    target.Weapons.values
      .filter {
        _.Equipment.nonEmpty
      }
      .foreach(slot => {
        val wep = slot.Equipment.get
        zone.AvatarEvents ! AvatarServiceMessage(continentId, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, wep.GUID))
      })
    target.CargoHolds.values.foreach(hold => {
      hold.Occupant match {
        case Some(cargo) =>
          cargo.Health = 0
          cargo.Shields = 0
          cargo.Position += Vector3.z(1)
          cargo.History(lastShot) //necessary to kill cargo vehicle occupants //TODO: collision damage
          HandleDestructionAwareness(cargo, attribution, lastShot) //might cause redundant packets
        case None => ;
      }
    })
    target.Definition match {
      case GlobalDefinitions.ams =>
        target.Actor ! Deployment.TryDeploymentChange(DriveState.Undeploying)
      case GlobalDefinitions.router =>
        target.Actor ! Deployment.TryDeploymentChange(DriveState.Undeploying)
        VehicleService.BeforeUnloadVehicle(target, zone)
        zone.LocalEvents ! LocalServiceMessage(zone.Id, LocalAction.ToggleTeleportSystem(PlanetSideGUID(0), target, None))
      case _ => ;
    }
    zone.AvatarEvents ! AvatarServiceMessage(continentId, AvatarAction.Destroy(target.GUID, attribution, attribution, target.Position))
    zone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(target), zone))
    zone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.AddTask(target, zone, Some(1 minute)))
  }
}

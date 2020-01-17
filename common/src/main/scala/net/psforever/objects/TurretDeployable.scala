// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.ce.{ComplexDeployable, Deployable, DeployedItem}
import net.psforever.objects.definition.{ComplexDeployableDefinition, SimpleDeployableDefinition}
import net.psforever.objects.definition.converter.SmallTurretConverter
import net.psforever.objects.equipment.{JammableMountedWeapons, JammableUnit}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.mount.MountableBehavior
import net.psforever.objects.serverobject.turret.{TurretDefinition, WeaponTurret}
import net.psforever.objects.vital.{StandardResolutions, StandardVehicleDamage, StandardVehicleResistance, Vitality}
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideGUID
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.vehicle.{VehicleAction, VehicleServiceMessage}

class TurretDeployable(tdef : TurretDeployableDefinition) extends ComplexDeployable(tdef)
  with WeaponTurret
  with JammableUnit
  with Hackable {
  WeaponTurret.LoadDefinition(this) //calls the equivalent of Health = Definition.MaxHealth

  def MountPoints : Map[Int, Int] = Definition.MountPoints.toMap

  //override to clarify inheritance conflict
  override def Health : Int = super[ComplexDeployable].Health
  //override to clarify inheritance conflict
  override def Health_=(toHealth : Int) : Int = super[ComplexDeployable].Health_=(toHealth)

  override def Definition = tdef
}

class TurretDeployableDefinition(private val objectId : Int) extends ComplexDeployableDefinition(objectId)
  with TurretDefinition {
  Name = "turret_deployable"
  Packet = new SmallTurretConverter
  Damage = StandardVehicleDamage
  Resistance = StandardVehicleResistance
  Model = StandardResolutions.FacilityTurrets

  //override to clarify inheritance conflict
  override def MaxHealth : Int = super[ComplexDeployableDefinition].MaxHealth
  //override to clarify inheritance conflict
  override def MaxHealth_=(max : Int) : Int = super[ComplexDeployableDefinition].MaxHealth_=(max)

  override def Initialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    obj.Actor = context.actorOf(Props(classOf[TurretControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }

  override def Uninitialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    SimpleDeployableDefinition.SimpleUninitialize(obj, context)
  }
}

object TurretDeployableDefinition {
  def apply(dtype : DeployedItem.Value) : TurretDeployableDefinition = {
    new TurretDeployableDefinition(dtype.id)
  }
}

/** control actors */

class TurretControl(turret : TurretDeployable) extends Actor
  with FactionAffinityBehavior.Check
  with JammableMountedWeapons //note: jammable status is reported as vehicle events, not local events
  with MountableBehavior.Mount
  with MountableBehavior.Dismount {
  def MountableObject = turret //do not add type!

  def JammableObject = turret

  def FactionObject : FactionAffinity = turret

  def receive : Receive = checkBehavior
    .orElse(jammableBehavior)
    .orElse(dismountBehavior)
    .orElse(turretMountBehavior)
    .orElse {
      case Vitality.Damage(damage_func) =>  //note: damage status is reported as vehicle events, not local events
        if(turret.Health > 0) {
          val originalHealth = turret.Health
          val cause = damage_func(turret)
          val health = turret.Health
          val damageToHealth = originalHealth - health
          TurretControl.HandleDamageResolution(turret, cause, damageToHealth)
          if(damageToHealth > 0) {
            val name = turret.Actor.toString
            val slashPoint = name.lastIndexOf("/")
            org.log4s.getLogger("DamageResolution").info(s"${name.substring(slashPoint + 1, name.length - 1)}: BEFORE=$originalHealth, AFTER=$health, CHANGE=$damageToHealth")
          }
        }

      case _ => ;
    }
}

object TurretControl {
  /**
    * na
    * @param target na
    */
  def HandleDamageResolution(target : TurretDeployable, cause : ResolvedProjectile, damage : Int) : Unit = {
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
      //alert to turret death (hence, occupants' deaths)
      HandleDestructionAwareness(target, playerGUID, cause)
    }
    zone.VehicleEvents ! VehicleServiceMessage(zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 0, target.Health))
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDamageAwareness(target : TurretDeployable, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    val zone = target.Zone
    //alert occupants to damage source
    target.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    }).foreach(seat => {
      val tplayer = seat.Occupant.get
      zone.AvatarEvents ! AvatarServiceMessage(tplayer.Name, AvatarAction.HitHint(attribution, tplayer.GUID))
    })
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDestructionAwareness(target : TurretDeployable, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    target.Actor ! JammableUnit.ClearJammeredSound()
    target.Actor ! JammableUnit.ClearJammeredStatus()
    val zone = target.Zone
    val continentId = zone.Id
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
    Deployables.AnnounceDestroyDeployable(target, None)
    zone.AvatarEvents ! AvatarServiceMessage(continentId, AvatarAction.Destroy(target.GUID, attribution, attribution, target.Position))
  }
}

// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.ce.{ComplexDeployable, Deployable, DeployableCategory}
import net.psforever.objects.definition.{ComplexDeployableDefinition, SimpleDeployableDefinition}
import net.psforever.objects.definition.converter.ShieldGeneratorConverter
import net.psforever.objects.equipment.{JammableBehavior, JammableUnit}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.Service
import services.vehicle.{VehicleAction, VehicleServiceMessage}

class ShieldGeneratorDeployable(cdef : ShieldGeneratorDefinition) extends ComplexDeployable(cdef)
  with Hackable
  with JammableUnit

class ShieldGeneratorDefinition extends ComplexDeployableDefinition(240) {
  Packet = new ShieldGeneratorConverter
  DeployCategory = DeployableCategory.ShieldGenerators

  override def Initialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    obj.Actor = context.actorOf(Props(classOf[ShieldGeneratorControl], obj), s"${obj.Definition.Name}_${obj.GUID.guid}")
  }

  override def Uninitialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    SimpleDeployableDefinition.SimpleUninitialize(obj, context)
  }
}

class ShieldGeneratorControl(gen : ShieldGeneratorDeployable) extends Actor
  with JammableBehavior {

  def JammableObject = gen

  def receive : Receive = jammableBehavior
    .orElse {
      case Vitality.Damage(damage_func) =>  //note: damage status is reported as vehicle events, not local events
        if(gen.Health > 0) {
          val originalHealth = gen.Health
          val cause = damage_func(gen)
          val health = gen.Health
          val damageToHealth = originalHealth - health
          ShieldGeneratorControl.HandleDamageResolution(gen, cause, damageToHealth)
          if(damageToHealth > 0) {
            val name = gen.Actor.toString
            val slashPoint = name.lastIndexOf("/")
            org.log4s.getLogger("DamageResolution").info(s"${name.substring(slashPoint + 1, name.length - 1)}: BEFORE=$originalHealth, AFTER=$health, CHANGE=$damageToHealth")
          }
        }

      case _ => ;
    }
  /*
  while the shield generator is technically a supported jammable target, how that works is currently unknown
  electing to use a "status only, no sound" approach by overriding one with an empty function is not entirely arbitrary
  the superclass of "status" calls also sets the jammed object property
   */
  override def StartJammeredSound(target : Any, dur : Int) : Unit =  { }

  override def StartJammeredStatus(target : Any, dur : Int) : Unit = target match {
    case obj : PlanetSideServerObject with JammableUnit if !obj.Jammed =>
      obj.Zone.VehicleEvents ! VehicleServiceMessage(obj.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 27, 1))
      super.StartJammeredStatus(obj, dur)
    case _ => ;
  }

  override def CancelJammeredSound(target : Any) : Unit =  { }

  override def CancelJammeredStatus(target : Any) : Unit = {
    target match {
      case obj : PlanetSideServerObject with JammableUnit  if obj.Jammed =>
        obj.Zone.VehicleEvents ! VehicleServiceMessage(obj.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 27, 0))
      case _ => ;
    }
    super.CancelJammeredStatus(target)
  }
}

object ShieldGeneratorControl {
  /**
    * na
    * @param target na
    */
  def HandleDamageResolution(target : ShieldGeneratorDeployable, cause : ResolvedProjectile, damage : Int) : Unit = {
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
      }
      if(cause.projectile.profile.JammerProjectile) {
        target.Actor ! JammableUnit.Jammered(cause)
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
  def HandleDestructionAwareness(target : ShieldGeneratorDeployable, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    target.Actor ! JammableUnit.ClearJammeredSound()
    target.Actor ! JammableUnit.ClearJammeredStatus()
    val zone = target.Zone
    Deployables.AnnounceDestroyDeployable(target, None)
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.Destroy(target.GUID, attribution, attribution, target.Position))
  }
}

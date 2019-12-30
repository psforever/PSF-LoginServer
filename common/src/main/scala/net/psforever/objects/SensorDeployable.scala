// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.ce._
import net.psforever.objects.definition.converter.SmallDeployableConverter
import net.psforever.objects.definition.{ComplexDeployableDefinition, SimpleDeployableDefinition}
import net.psforever.objects.equipment.{JammableBehavior, JammableUnit}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.vital.{StandardResolutions, Vitality}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.duration._

class SensorDeployable(cdef : SensorDeployableDefinition) extends ComplexDeployable(cdef)
  with Hackable
  with JammableUnit

class SensorDeployableDefinition(private val objectId : Int) extends ComplexDeployableDefinition(objectId) {
  Name = "sensor_deployable"
  DeployCategory = DeployableCategory.Sensors
  Model = StandardResolutions.SimpleDeployables
  Packet = new SmallDeployableConverter

  override def Initialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    obj.Actor = context.actorOf(Props(classOf[SensorDeployableControl], obj), s"${obj.Definition.Name}_${obj.GUID.guid}")
  }

  override def Uninitialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    SimpleDeployableDefinition.SimpleUninitialize(obj, context)
  }
}

object SensorDeployableDefinition {
  def apply(dtype : DeployedItem.Value) : SensorDeployableDefinition = {
    new SensorDeployableDefinition(dtype.id)
  }
}

class SensorDeployableControl(sensor : SensorDeployable) extends Actor
  with JammableBehavior {

  def JammableObject = sensor

  def receive : Receive = jammableBehavior.orElse {
    case Vitality.Damage(damage_func) =>
      val originalHealth = sensor.Health
      if(originalHealth > 0) {
        val cause = damage_func(sensor)
        SensorDeployableControl.HandleDamageResolution(sensor, cause, originalHealth - sensor.Health)
      }

    case _ => ;
  }

  override def StartJammeredSound(target : Any, dur : Int) : Unit = target match {
    case obj : PlanetSideServerObject if !jammedSound =>
      obj.Zone.VehicleEvents ! VehicleServiceMessage(obj.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 54, 1))
      super.StartJammeredSound(obj, dur)
    case _ => ;
  }

  override def StartJammeredStatus(target : Any, dur : Int) : Unit = target match {
    case obj : PlanetSideServerObject with JammableUnit if !obj.Jammed =>
      sensor.Zone.LocalEvents ! LocalServiceMessage(sensor.Zone.Id, LocalAction.TriggerEffectInfo(Service.defaultPlayerGUID, "on", obj.GUID, false, 1000))
      super.StartJammeredStatus(obj, dur)
    case _ => ;
  }

  override def CancelJammeredSound(target : Any) : Unit = {
    target match {
      case obj : PlanetSideServerObject if jammedSound =>
        obj.Zone.VehicleEvents ! VehicleServiceMessage(obj.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 54, 0))
      case _ => ;
    }
    super.CancelJammeredSound(target)
  }

  override def CancelJammeredStatus(target : Any) : Unit = {
    target match {
      case obj : PlanetSideServerObject with JammableUnit if obj.Jammed =>
        sensor.Zone.LocalEvents ! LocalServiceMessage(sensor.Zone.Id, LocalAction.TriggerEffectInfo(Service.defaultPlayerGUID, "on", obj.GUID, true, 1000))
      case _ => ;
    }
    super.CancelJammeredStatus(target)
  }
}

object SensorDeployableControl {
  def HandleDamageResolution(target : SensorDeployable, cause : ResolvedProjectile, damage : Int) : Unit = {
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
  def HandleDestructionAwareness(target : SensorDeployable, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    target.Actor ! JammableUnit.ClearJammeredSound()
    target.Actor ! JammableUnit.ClearJammeredStatus()
    val zone = target.Zone
    Deployables.AnnounceDestroyDeployable(target, Some(0 seconds))
    zone.LocalEvents ! LocalServiceMessage(zone.Id, LocalAction.TriggerEffectInfo(Service.defaultPlayerGUID, "on", target.GUID, false, 1000))
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.Destroy(target.GUID, attribution, attribution, target.Position))
  }
}

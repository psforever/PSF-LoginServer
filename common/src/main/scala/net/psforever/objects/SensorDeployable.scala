// Copyright (c) 2019 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.ce._
import net.psforever.objects.definition.converter.SmallDeployableConverter
import net.psforever.objects.definition.{ComplexDeployableDefinition, SimpleDeployableDefinition}
import net.psforever.objects.equipment.{JammableBehavior, JammableUnit}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.repair.RepairableEntity
import net.psforever.objects.vital.StandardResolutions
import net.psforever.types.{PlanetSideGUID, Vector3}
import services.Service
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
    obj.Actor = context.actorOf(Props(classOf[SensorDeployableControl], obj), PlanetSideServerObject.UniqueActorName(obj))
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
  with JammableBehavior
  with DamageableEntity
  with RepairableEntity {
  def JammableObject = sensor
  def DamageableObject = sensor
  def RepairableObject = sensor

  def receive : Receive = jammableBehavior
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse {
      case _ => ;
  }

  override protected def DamageLog(msg : String) : Unit = { }

  override protected def DestructionAwareness(target : Damageable.Target, cause : ResolvedProjectile) : Unit = {
    super.DestructionAwareness(target, cause)
    SensorDeployableControl.DestructionAwareness(sensor, PlanetSideGUID(0))
  }

  override def StartJammeredSound(target : Any, dur : Int) : Unit = target match {
    case obj : PlanetSideServerObject if !jammedSound =>
      obj.Zone.VehicleEvents ! VehicleServiceMessage(obj.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 54, 1))
      super.StartJammeredSound(obj, dur)
    case _ => ;
  }

  override def StartJammeredStatus(target : Any, dur : Int) : Unit = target match {
    case obj : PlanetSideServerObject with JammableUnit if !obj.Jammed =>
      val zone = obj.Zone
      zone.LocalEvents ! LocalServiceMessage(zone.Id, LocalAction.TriggerEffectInfo(Service.defaultPlayerGUID, "on", obj.GUID, false, 1000))
      super.StartJammeredStatus(obj, dur)
    case _ => ;
  }

  override def CancelJammeredSound(target : Any) : Unit = {
    target match {
      case obj : PlanetSideServerObject if jammedSound =>
        val zone = obj.Zone
        zone.VehicleEvents ! VehicleServiceMessage(zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 54, 0))
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
  /**
    * na
    * @param target na
    * @param attribution na
    */
  def DestructionAwareness(target : Damageable.Target with Deployable, attribution : PlanetSideGUID) : Unit = {
    Deployables.AnnounceDestroyDeployable(target, Some(1 seconds))
    val zone = target.Zone
    zone.LocalEvents ! LocalServiceMessage(zone.Id,
      LocalAction.TriggerEffectInfo(Service.defaultPlayerGUID, "on", target.GUID, false, 1000)
    )
    //position the explosion effect near the bulky area of the sensor stalk
    val ang = target.Orientation
    val explosionPos =  {
      val pos = target.Position
      val yRadians = ang.y.toRadians
      val d = Vector3.Rz(Vector3(0, 0.875f, 0), ang.z) * math.sin(yRadians).toFloat
      Vector3(
        pos.x + d.x,
        pos.y + d.y,
        pos.z + math.cos(yRadians).toFloat * 0.875f
      )
    }
    zone.LocalEvents ! LocalServiceMessage(zone.Id,
      LocalAction.TriggerEffectLocation(Service.defaultPlayerGUID, "motion_sensor_destroyed", explosionPos, ang)
    )
    //TODO replaced by an alternate model (charred stub)?
  }
}

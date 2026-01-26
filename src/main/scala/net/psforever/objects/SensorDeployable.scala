// Copyright (c) 2019 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import net.psforever.objects.ce._
import net.psforever.objects.definition.converter.SmallDeployableConverter
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.equipment.{JammableBehavior, JammableUnit}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.repair.RepairableEntity
import net.psforever.objects.vital.SimpleResolutions
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.types.{PlanetSideGUID, Vector3}
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.annotation.unused
import scala.concurrent.duration._

class SensorDeployable(cdef: SensorDeployableDefinition) extends Deployable(cdef) with Hackable with JammableUnit

class SensorDeployableDefinition(private val objectId: Int) extends DeployableDefinition(objectId) {
  Name = "sensor_deployable"
  DeployCategory = DeployableCategory.Sensors
  Model = SimpleResolutions.calculate
  Packet = new SmallDeployableConverter

  override def Initialize(obj: Deployable, context: ActorContext): Unit = {
    obj.Actor =
      context.actorOf(Props(classOf[SensorDeployableControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }
}

object SensorDeployableDefinition {
  def apply(dtype: DeployedItem.Value): SensorDeployableDefinition = {
    new SensorDeployableDefinition(dtype.id)
  }
}

class SensorDeployableControl(sensor: SensorDeployable)
    extends Actor
    with DeployableBehavior
    with JammableBehavior
    with DamageableEntity
    with RepairableEntity {
  def DeployableObject: SensorDeployable = sensor
  def JammableObject: SensorDeployable   = sensor
  def DamageableObject: SensorDeployable = sensor
  def RepairableObject: SensorDeployable = sensor

  override def postStop(): Unit = {
    super.postStop()
    deployableBehaviorPostStop()
  }

  def receive: Receive =
    deployableBehavior
      .orElse(jammableBehavior)
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse {
        case _ => ;
      }

  override protected def DamageLog(@unused msg: String): Unit = {}

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    SensorDeployableControl.DestructionAwareness(sensor, PlanetSideGUID(0))
  }

  override def StartJammeredSound(target: Any, dur: Int): Unit =
    target match {
      case obj: PlanetSideServerObject if !jammedSound =>
        obj.Zone.VehicleEvents ! VehicleServiceMessage(
          obj.Zone.id,
          VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 54, 1)
        )
        super.StartJammeredSound(obj, dur)
      case _ => ;
    }

  override def StartJammeredStatus(target: Any, dur: Int): Unit =
    target match {
      case obj: PlanetSideServerObject with JammableUnit =>
        val zone = obj.Zone
        zone.LocalEvents ! LocalServiceMessage(
          zone.id,
          LocalAction.TriggerEffectInfo(obj.GUID, "on", unk1=false, 1000)
        )
        super.StartJammeredStatus(obj, dur)
      case _ => ;
    }

  override def CancelJammeredSound(target: Any): Unit = {
    target match {
      case obj: PlanetSideServerObject if jammedSound =>
        val zone = obj.Zone
        zone.VehicleEvents ! VehicleServiceMessage(
          zone.id,
          VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 54, 0)
        )
      case _ => ;
    }
    super.CancelJammeredSound(target)
  }

  override def CancelJammeredStatus(target: Any): Unit = {
    target match {
      case obj: PlanetSideServerObject with JammableUnit if obj.Jammed =>
        val zone = sensor.Zone
        zone.LocalEvents ! LocalServiceMessage(
          zone.id,
          LocalAction.TriggerEffectInfo(obj.GUID, "on", unk1=true, 1000)
        )
      case _ => ;
    }
    super.CancelJammeredStatus(target)
  }

  override def finalizeDeployable(callback: ActorRef) : Unit = {
    super.finalizeDeployable(callback)
    val zone = sensor.Zone
    zone.LocalEvents ! LocalServiceMessage(
      zone.id,
      LocalAction.TriggerEffectInfo(sensor.GUID, "on", unk1=true, 1000)
    )
  }
}

object SensorDeployableControl {

  /**
    * na
    * @param target na
    * @param attribution na
    */
  def DestructionAwareness(target: Deployable, attribution: PlanetSideGUID): Unit = {
    Deployables.AnnounceDestroyDeployable(target, Some(1 seconds))
    val zone = target.Zone
    zone.LocalEvents ! LocalServiceMessage(
      zone.id,
      LocalAction.TriggerEffectInfo(target.GUID, "on", unk1=false, 1000)
    )
    //position the explosion effect near the bulky area of the sensor stalk
    val ang = target.Orientation
    val explosionPos = {
      val pos      = target.Position
      val yRadians = ang.y.toRadians
      val d        = Vector3.Rz(Vector3(0, 0.875f, 0), ang.z) * math.sin(yRadians).toFloat
      Vector3(
        pos.x + d.x,
        pos.y + d.y,
        pos.z + math.cos(yRadians).toFloat * 0.875f
      )
    }
    zone.LocalEvents ! LocalServiceMessage(
      zone.id,
      LocalAction.TriggerEffectLocation("motion_sensor_destroyed", explosionPos, ang)
    )
    //TODO replaced by an alternate model (charred stub)?
  }
}

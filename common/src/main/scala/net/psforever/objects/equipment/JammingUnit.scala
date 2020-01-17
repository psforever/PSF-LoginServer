// Copyright (c) 2019 PSForever
package net.psforever.objects.equipment

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.{DefaultCancellable, PlanetSideGameObject, Tool}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.vehicles.MountedWeapons
import net.psforever.objects.zones.ZoneAware
import net.psforever.types.Vector3
import services.Service
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * A property conferred to game objects that can be affected by an electromagnetic pulse.
  * Being "jammered" is a status that causes weakness due to temporary equipment disabling or the elimination of certain objects.
  */
trait JammableUnit {
  /** being jammed (jammered) is an on/off state */
  private var jammed : Boolean = false

  def Jammed : Boolean = jammed

  def Jammed_=(state : Boolean) : Boolean = {
    jammed = state
    Jammed
  }
}

object JammableUnit {
  /**
    * A message for generic jammering.
    * Currently, unused.
    */
  final case class Jammer()
  /**
    * A message for jammering due to a projectile.
    * @param cause information pertaining to the projectile
    */
  final case class Jammered(cause : ResolvedProjectile)
  /**
    * Stop the auditory aspect of being jammered.
    */
  final case class ClearJammeredSound()
  /**
    * Stop the status effects of being jammered.
    */
  final case class ClearJammeredStatus()
}

/**
  * A property conferred onto game objects that can induce the effects of an electromagnetic pulse.
  * @see `TargetValidation`
  * @see `EffectTarget`
  */
trait JammingUnit {
  /** a list of qualifying conditional tests for determining if an object is to be affected by the jammered status;
    * if qualifying, that object will be inflicted with a number of milliseconds of the jammered status */
  private val jammedEffectDuration : mutable.ListBuffer[(TargetValidation, Int)] = new mutable.ListBuffer()

  def HasJammedEffectDuration : Boolean = jammedEffectDuration.isEmpty

  def JammedEffectDuration : mutable.ListBuffer[(TargetValidation, Int)] = jammedEffectDuration
}

object JammingUnit {
  /**
    * Determine whether an object that can be jammered is to be jammered by this source,
    * and for how long.
    * If the object succeeds for multiple qualification tests,
    * prioritize the lengthiest duration.
    * @param jammer the source of the "jammered" status
    * @param target the object to be determined if affected by the source's jammering
    * @return the duration to be jammered, if any, in milliseconds
    */
  def FindJammerDuration(jammer : JammingUnit, target : PlanetSideGameObject) : Option[Int] = {
    jammer.JammedEffectDuration
      .collect { case (TargetValidation(_, test), duration) if test(target) => duration }
      .toList
      .sortWith(_ > _)
      .headOption
  }

  /**
    * Determine whether a group of objects that can be jammered is to be jammered by this source,
    * and for how long.
    * If the object succeeds for multiple qualification tests,
    * prioritize the lengthiest duration.
    * @param jammer the source of the "jammered" status
    * @param targets the objects to be determined if affected by the source's jammering
    * @return the indexed durations to be jammered, if any, in milliseconds
    */
  def FindJammerDuration(jammer : JammingUnit, targets : Seq[PlanetSideGameObject]) : Seq[Option[Int]] = {
    targets.map { target => FindJammerDuration(jammer, target) }
  }
}

/**
  * An `Actor` control object mix-in that manages common responses to the "jammerable" status.
  * Two aspects to jammering are supported -
  * a telling buzzing sound that follows the affected target
  * and actual effects upon the target's actions -
  * and are controlled independently.
  * The primary purpose of this behavior is to control timers that toggle the states of these two aspects.
  */
trait JammableBehavior {
  this : Actor =>
  /** flag for jammed sound */
  protected var jammedSound : Boolean = false
  /** the sound timer */
  protected var jammeredSoundTimer : Cancellable = DefaultCancellable.obj
  /** the effect timer */
  protected var jammeredStatusTimer : Cancellable = DefaultCancellable.obj

  /** `ZoneAware` is used for callback to the event systems */
  def JammableObject : PlanetSideServerObject with JammableUnit with ZoneAware

  /**
    * If the target can be validated against, affect it with the jammered status.
    * @param target the objects to be determined if affected by the source's jammering
    * @param cause the source of the "jammered" status
    */
  def TryJammerEffectActivate(target : Any, cause : ResolvedProjectile) : Unit = target match {
    case obj : PlanetSideServerObject =>
      val radius = cause.projectile.profile.DamageRadius
      JammingUnit.FindJammerDuration(cause.projectile.profile, obj) match {
        case Some(dur) if Vector3.DistanceSquared(cause.hit_pos, cause.target.Position) < radius * radius =>
          StartJammeredSound(obj, dur)
          StartJammeredStatus(obj, dur)
        case _ => ;
      }
    case _ => ;
  }

  /**
    * Activate a distinctive buzzing sound effect.
    * Due to considerations of the object that is the target, this is left to be implemented by a subclass.
    * We merely start the timer.
    * @param target an object that can be affected by the jammered status
    * @param dur the duration of the timer, in milliseconds;
    *            by default, 30000
    */
  def StartJammeredSound(target : Any, dur : Int = 30000) : Unit = {
    if(!jammedSound) {
      jammedSound = true
      import scala.concurrent.ExecutionContext.Implicits.global
      jammeredSoundTimer.cancel
      jammeredSoundTimer = context.system.scheduler.scheduleOnce(dur milliseconds, self, JammableUnit.ClearJammeredSound())
    }
  }

  /**
    * Deactivate the effects of the jammered status.
    * Due to considerations of the object that is the target, this is left to be implemented by a subclass.
    * We merely stop the timer.
    * @param target an object that can be affected by the jammered status
    * @param dur the duration of the timer, in milliseconds
    */
  def StartJammeredStatus(target : Any, dur : Int) : Unit = {
    JammableObject.Jammed = true
    jammeredStatusTimer.cancel
    import scala.concurrent.ExecutionContext.Implicits.global
    jammeredStatusTimer = context.system.scheduler.scheduleOnce(dur milliseconds, self, JammableUnit.ClearJammeredStatus())
  }

  /**
    * Deactivate a distinctive buzzing sound effect.
    * Due to considerations of the object that is the target, this is left to be implemented by a subclass.
    * We merely stop the timer.
    * @param target an object that can be affected by the jammered status
    */
  def CancelJammeredSound(target : Any) : Unit = {
    jammedSound = false
    jammeredSoundTimer.cancel
  }

  /**
    * Deactivate the effects of the jammered status.
    * Due to considerations of the object that is the target, this is left to be implemented by a subclass.
    * We merely stop the timer.
    * @param target an object that can be affected by the jammered status
    */
  def CancelJammeredStatus(target : Any) : Unit = {
    JammableObject.Jammed = false
    jammeredStatusTimer.cancel
  }

  val jammableBehavior : Receive = {
    case JammableUnit.Jammered(cause) =>
      TryJammerEffectActivate(JammableObject, cause)

    case JammableUnit.ClearJammeredSound() =>
      CancelJammeredSound(JammableObject)

    case JammableUnit.ClearJammeredStatus() =>
      CancelJammeredStatus(JammableObject)
  }
}

/**
  * A common mix-in variation to manage common responses to the "jammerable" status for game objects with mounted weapons.
  * @see `MountedWeapons`
  * @see `Service`
  * @see `VehicleAction`
  * @see `VehicleService`
  * @see `VehicleServiceMessage`
  * @see `Zone.VehicleEvents`
  */
trait JammableMountedWeapons extends JammableBehavior {
  _ : Actor =>

  override def StartJammeredSound(target : Any, dur : Int) : Unit = {
    target match {
      case obj : PlanetSideServerObject with MountedWeapons with JammableUnit if !jammedSound =>
        obj.Zone.VehicleEvents ! VehicleServiceMessage(obj.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 27, 1))
        super.StartJammeredSound(target, dur)
      case _ => ;
    }
  }

  override def StartJammeredStatus(target : Any, dur : Int) : Unit = {
    target match {
      case obj : PlanetSideServerObject with MountedWeapons with JammableUnit if !obj.Jammed =>
        JammableMountedWeapons.JammeredStatus(obj, 1)
        super.StartJammeredStatus(target, dur)
      case _ => ;
    }
  }

  override def CancelJammeredSound(target : Any) : Unit = {
    target match {
      case obj : PlanetSideServerObject if jammedSound =>
        obj.Zone.VehicleEvents ! VehicleServiceMessage(obj.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 27, 0))
      case _ => ;
    }
    super.CancelJammeredSound(target)
  }

  override def CancelJammeredStatus(target : Any) : Unit = {
    target match {
      case obj : PlanetSideServerObject with MountedWeapons with JammableUnit if obj.Jammed =>
        JammableMountedWeapons.JammeredStatus(obj, 0)
      case _ => ;
    }
    super.CancelJammeredStatus(target)
  }
}

object JammableMountedWeapons {
  /**
    * Retrieve all of the weapons on a `MountedWeapons` target object and apply a jammered status effect to each.
    * @param target an object that can be affected by the jammered status
    * @param statusCode the jammered status condition;
    *                   0 for deactivation;
    *                   1 for activation
    */
  def JammeredStatus(target : PlanetSideServerObject with MountedWeapons, statusCode : Int) : Unit = {
    val zone = target.Zone
    val zoneId = zone.Id
    target.Weapons.values
      .map { _.Equipment }
      .collect {
        case Some(item : Tool) =>
          item.Jammed = statusCode==1
          zone.VehicleEvents ! VehicleServiceMessage(zoneId, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, item.GUID, 27, statusCode))
      }
  }
}

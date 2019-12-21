// Copyright (c) 2019 PSForever
package net.psforever.objects.equipment

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.{DefaultCancellable, PlanetSideGameObject, Tool}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.terminals.TargetValidation
import net.psforever.objects.vehicles.MountedWeapons
import net.psforever.objects.zones.ZoneAware
import net.psforever.types.Vector3
import services.Service
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.collection.mutable
import scala.concurrent.duration._

trait JammableUnit {
  private var jammed : Boolean = false

  def Jammed : Boolean = jammed

  def Jammed_=(state : Boolean) : Boolean = {
    jammed = state
    Jammed
  }
}

object JammableUnit {
  final case class Jammer()

  final case class Jammered(cause : ResolvedProjectile)

  final case class ClearJammeredSound()

  final case class ClearJammeredStatus()
}

trait JammingUnit {
  private val jammedEffectDuration : mutable.ListBuffer[(TargetValidation, Int)] = new mutable.ListBuffer()

  def HasJammedEffectDuration : Boolean = jammedEffectDuration.isEmpty

  def JammedEffectDuration : mutable.ListBuffer[(TargetValidation, Int)] = jammedEffectDuration
}

object JammingUnit {
  def FindJammerDuration(jammer : JammingUnit, target : PlanetSideGameObject) : Option[Int] = {
    jammer.JammedEffectDuration
      .collect { case (TargetValidation(_, test), duration) if test(target) => duration }
      .toList
      .sortWith(_ > _)
      .headOption
  }

  def FindJammerDuration(jammer : JammingUnit, targets : Seq[PlanetSideGameObject]) : Seq[Option[Int]] = {
    targets.map { target => FindJammerDuration(jammer, target) }
  }
}

trait JammableBehavior {
  _ : Actor =>
  protected var jammeredSoundTimer : Cancellable = DefaultCancellable.obj
  protected var jammeredStatusTimer : Cancellable = DefaultCancellable.obj

  def JammableObject : PlanetSideServerObject with JammableUnit with ZoneAware

  def TryJammerEffectActivate(target : Any, cause : ResolvedProjectile) : Unit = target match {
    case obj : PlanetSideServerObject =>
      val radius = cause.projectile.profile.DamageRadius
      JammingUnit.FindJammerDuration(cause.projectile.profile, obj) match {
        case Some(dur) if Vector3.DistanceSquared(cause.hit_pos, cause.target.Position) < radius * radius =>
          StartJammeredSound(obj)
          StartJammeredStatus(obj, dur)
        case _ => ;
      }
    case _ => ;
  }

  def StartJammeredSound(target : Any, dur : Int = 30000) : Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    jammeredSoundTimer.cancel
    jammeredSoundTimer = context.system.scheduler.scheduleOnce(30 seconds, self, JammableUnit.ClearJammeredSound())
  }

  def StartJammeredStatus(target : Any, dur : Int) : Unit = {
    JammableObject.Jammed = true
    jammeredStatusTimer.cancel
    import scala.concurrent.ExecutionContext.Implicits.global
    jammeredStatusTimer = context.system.scheduler.scheduleOnce(dur milliseconds, self, JammableUnit.ClearJammeredStatus())
  }

  def CancelJammeredSound(target : Any) : Unit = {
    jammeredSoundTimer.cancel
  }

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

trait JammableMountedWeapons extends JammableBehavior {
  _ : Actor =>

  override def StartJammeredSound(target : Any, dur : Int) : Unit = target match {
    case obj : PlanetSideServerObject with MountedWeapons =>
      obj.Zone.VehicleEvents ! VehicleServiceMessage(obj.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 54, 1))
      super.StartJammeredSound(obj, dur)
    case _ => ;
  }

  override def StartJammeredStatus(target : Any, dur : Int) : Unit = target match {
    case obj : PlanetSideServerObject with MountedWeapons =>
      JammableMountedWeapons.JammeredStatus(obj, 1)
      super.StartJammeredStatus(obj, dur)
    case _ => ;
  }

  override def CancelJammeredSound(target : Any) : Unit = target match {
    case obj : PlanetSideServerObject =>
      obj.Zone.VehicleEvents ! VehicleServiceMessage(obj.Zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 54, 0))
      super.CancelJammeredSound(obj)
    case _ => ;
  }

  override def CancelJammeredStatus(target : Any) : Unit = target match {
    case obj : PlanetSideServerObject with MountedWeapons =>
      JammableMountedWeapons.JammeredStatus(obj, 0)
      super.CancelJammeredStatus(obj)
    case _ => ;
  }
}

object JammableMountedWeapons {
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

// Copyright (c) 2024 PSForever
package net.psforever.objects

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.turret.auto.AutomatedTurret.Target
import net.psforever.objects.serverobject.turret.auto.{AutomatedTurret, AutomatedTurretBehavior}
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.types.PlanetSideGUID

import scala.concurrent.duration.FiniteDuration

/** definition */

class SmallTurretDeployableDefinition(private val objectId: Int)
  extends TurretDeployableDefinition(objectId) {
  override def Initialize(obj: Deployable, context: ActorContext): Unit = {
    obj.Actor = context.actorOf(Props(classOf[SmallTurretControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }
}

object SmallTurretDeployableDefinition {
  def apply(dtype: DeployedItem.Value): SmallTurretDeployableDefinition = {
    new SmallTurretDeployableDefinition(dtype.id)
  }
}

/** control actor */

class SmallTurretControl(turret: TurretDeployable)
  extends TurretDeployableControl
    with AutomatedTurretBehavior {
  def TurretObject: TurretDeployable     = turret
  def DeployableObject: TurretDeployable = turret
  def JammableObject: TurretDeployable   = turret
  def FactionObject: TurretDeployable    = turret
  def DamageableObject: TurretDeployable = turret
  def RepairableObject: TurretDeployable = turret
  def AffectedObject: TurretDeployable   = turret
  def AutomatedTurretObject: TurretDeployable = turret

  override def postStop(): Unit = {
    super.postStop()
    selfReportingDatabaseUpdate()
    automaticTurretPostStop()
  }

  def receive: Receive =
    commonBehavior
      .orElse(automatedTurretBehavior)
      .orElse {
        case _ => ()
      }

  protected def engageNewDetectedTarget(
                                         target: AutomatedTurret.Target,
                                         channel: String,
                                         turretGuid: PlanetSideGUID,
                                         weaponGuid: PlanetSideGUID
                                       ): Unit = {
    val zone = target.Zone
    AutomatedTurretBehavior.startTracking(zone, channel, turretGuid, List(target.GUID))
    AutomatedTurretBehavior.startShooting(zone, channel, weaponGuid)
  }

  protected def noLongerEngageTarget(
                                      target: AutomatedTurret.Target,
                                      channel: String,
                                      turretGuid: PlanetSideGUID,
                                      weaponGuid: PlanetSideGUID
                                    ): Option[AutomatedTurret.Target] = {
    val zone = target.Zone
    AutomatedTurretBehavior.stopTracking(zone, channel, turretGuid)
    AutomatedTurretBehavior.stopShooting(zone, channel, weaponGuid)
    None
  }

  protected def testNewDetected(
                                 target: AutomatedTurret.Target,
                                 channel: String,
                                 turretGuid: PlanetSideGUID,
                                 weaponGuid: PlanetSideGUID
                               ): Unit = {
    val zone = target.Zone
    AutomatedTurretBehavior.startTracking(zone, channel, turretGuid, List(target.GUID))
    AutomatedTurretBehavior.startShooting(zone, channel, weaponGuid)
    AutomatedTurretBehavior.stopShooting(zone, channel, weaponGuid)
  }

  protected def testKnownDetected(
                                   target: AutomatedTurret.Target,
                                   channel: String,
                                   turretGuid: PlanetSideGUID,
                                   weaponGuid: PlanetSideGUID
                                 ): Unit = {
    val zone = target.Zone
    AutomatedTurretBehavior.startShooting(zone, channel, weaponGuid)
    AutomatedTurretBehavior.stopShooting(zone, channel, weaponGuid)
  }

  override protected def suspendTargetTesting(
                                               target: Target,
                                               channel: String,
                                               turretGuid: PlanetSideGUID,
                                               weaponGuid: PlanetSideGUID
                                             ): Unit = {
    AutomatedTurretBehavior.stopTracking(target.Zone, channel, turretGuid)
  }

  override def TryJammerEffectActivate(target: Any, cause: DamageResult): Unit = {
    val startsUnjammed = !JammableObject.Jammed
    super.TryJammerEffectActivate(target, cause)
    if (JammableObject.Jammed && AutomatedTurretObject.Definition.AutoFire.exists(_.retaliatoryDelay > 0)) {
      if (startsUnjammed) {
        AutomaticOperation = false
      }
      //look in direction of cause of jamming
      val zone = JammableObject.Zone
      AutomatedTurretBehavior.getAttackVectorFromCause(zone, cause).foreach { attacker =>
        AutomatedTurretBehavior.startTracking(zone, zone.id, AutomatedTurretObject.GUID, List(attacker.GUID))
      }
    }
  }

  override def CancelJammeredStatus(target: Any): Unit = {
    val startsJammed = JammableObject.Jammed
    super.CancelJammeredStatus(target)
    if (startsJammed && AutomaticOperation_=(state = true)) {
      val zone = TurretObject.Zone
      AutomatedTurretBehavior.stopTracking(zone, zone.id, TurretObject.GUID)
    }
  }

  override protected def DamageAwareness(target: Damageable.Target, cause: DamageResult, amount: Any): Unit = {
    amount match {
      case 0 => ()
      case _ => attemptRetaliation(target, cause)
    }
    super.DamageAwareness(target, cause, amount)
  }

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    AutomaticOperation = false
    super.DestructionAwareness(target, cause)
  }

  override def deconstructDeployable(time: Option[FiniteDuration]) : Unit = {
    AutomaticOperation = false
    super.deconstructDeployable(time)
  }

  override def finalizeDeployable(callback: ActorRef): Unit = {
    super.finalizeDeployable(callback)
    AutomaticOperation = true
  }
}

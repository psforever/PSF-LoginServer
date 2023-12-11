// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.{Default, Player}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.DamageableEntity
import net.psforever.objects.sourcing.{SourceEntry, SourceUniqueness}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, ObjectDetectedMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{PlanetSideGUID, Vector3}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait AutomatedTurret
  extends PlanetSideServerObject
  with WeaponTurret {
  import AutomatedTurret.Target
  private var currentTarget: Option[Target] = None

  private var targets: List[Target] = List[Target]()

  def Target: Option[Target] = currentTarget

  def Target_=(newTarget: Target): Option[Target] = {
    Target_=(Some(newTarget))
  }

  def Target_=(newTarget: Option[Target]): Option[Target] = {
    if (newTarget.isDefined != currentTarget.isDefined) {
      currentTarget = newTarget
    }
    currentTarget
  }

  def Targets: List[Target] = targets

  def Detected(target: Target): Option[Target] = {
    val unique = SourceEntry(target).unique
    targets.find(SourceEntry(_).unique == unique)
  }

  def Detected(target: SourceUniqueness): Option[Target] = {
    targets.find(SourceEntry(_).unique == target)
  }

  def AddTarget(target: Target): Unit = {
    targets = targets :+ target
  }

  def RemoveTarget(target: Target): Unit = {
    val unique = SourceEntry(target).unique
    targets = targets.filterNot(SourceEntry(_).unique == unique)
  }

  def Clear(): List[Target] = {
    val oldTargets = targets
    targets = Nil
    oldTargets
  }

  def Definition: ObjectDefinition with TurretDefinition
}

object AutomatedTurret {
  type Target = PlanetSideServerObject with Vitality
}

trait AutomatedTurretBehavior {
  _: Actor with DamageableEntity =>
  import AutomatedTurret.Target

  private var automaticOperation: Boolean = false

  private var currentTargetToken: Option[SourceUniqueness] = None

  private var currentTarget: Option[Target] = None

  private var currentTargetLastShotReported: Long = 0L

  private var periodicValidationTest: Cancellable = Default.Cancellable

  def AutomatedTurretObject: AutomatedTurret

  val automatedTurretBehavior: Actor.Receive = {
    case AutomatedTurretBehavior.Alert(target) =>
      bringAttentionToTarget(target)

    case AutomatedTurretBehavior.ConfirmShot(target) =>
      confirmShot(target)

    case AutomatedTurretBehavior.Unalert(target) =>
      disregardTarget(target)

    case AutomatedTurretBehavior.Reset =>
      resetAlerts()

    case AutomatedTurretBehavior.PeriodicCheck =>
      performPeriodicTargetValidation()
  }

  def AutomaticOperation: Boolean = automaticOperation

  def AutomaticOperation_=(state: Boolean): Boolean = {
    val previousState = automaticOperation
    automaticOperation = state
    if (!previousState && state) {
      trySelectNewTarget()
    } else if (previousState && !state) {
      currentTarget.foreach { noLongerEngageDetectedTarget }
    }
    state
  }

  private def bringAttentionToTarget(target: Target): Unit = {
    val targets = AutomatedTurretObject.Targets
    val size = targets.size
    AutomatedTurretObject.Detected(target)
      .orElse {
        AutomatedTurretObject.AddTarget(target)
        retimePeriodicTargetChecks(size)
        Some(target)
      }
  }

  private def confirmShot(target: Target): Unit = {
    val now = System.currentTimeMillis()
    if (currentTargetToken.isEmpty || now - currentTargetLastShotReported > 1500L) {
      currentTargetLastShotReported = now
      engageNewDetectedTarget(target)
    } else if (currentTargetToken.contains(SourceEntry(target).unique) && now - currentTargetLastShotReported < 3000L) {
      currentTargetLastShotReported = now
    }
  }

  private def disregardTarget(target: Target): Unit = {
    val targets = AutomatedTurretObject.Targets
    val size = targets.size
    AutomatedTurretObject.Detected(target)
      .collect { out =>
        AutomatedTurretObject.RemoveTarget(target)
        testTargetListQualifications(size)
        out
      }
      .flatMap {
        noLongerDetectTargetIfCurrent
      }
  }

  private def resetAlerts(): Unit = {
    currentTarget.foreach { noLongerEngageDetectedTarget }
    AutomatedTurretObject.Clear()
    testTargetListQualifications(beforeSize = 1)
  }

  private def testNewDetectedTarget(target: Target, channel: String): Unit = {
    val zone = target.Zone
    AutomatedTurretBehavior.startTrackingTargets(zone, channel, AutomatedTurretObject.GUID, List(target.GUID))
    AutomatedTurretBehavior.startShooting(zone, channel, AutomatedTurretObject.Weapons.values.head.Equipment.get.GUID)
    AutomatedTurretBehavior.stopShooting(zone, channel, AutomatedTurretObject.Weapons.values.head.Equipment.get.GUID)
  }

  protected def engageNewDetectedTarget(target: Target): Unit = {
    val zone = target.Zone
    val zoneid = zone.id
    AutomatedTurretObject.Target = target
    currentTarget = Some(target)
    currentTargetToken = Some(SourceEntry(target).unique)
    AutomatedTurretBehavior.startTrackingTargets(zone, zoneid, AutomatedTurretObject.GUID, List(target.GUID))
    AutomatedTurretBehavior.startShooting(zone, zoneid, AutomatedTurretObject.Weapons.values.head.Equipment.get.GUID)
  }

  protected def noLongerDetectTargetIfCurrent(target: Target): Option[Target] = {
    if (currentTargetToken.contains(SourceEntry(target).unique)) {
      noLongerEngageDetectedTarget(target)
    } else {
      currentTarget
    }
  }

  protected def noLongerEngageDetectedTarget(target: Target): Option[Target] = {
    val zone = target.Zone
    val zoneid = zone.id
    AutomatedTurretObject.Target = None
    currentTarget = None
    currentTargetToken = None
    AutomatedTurretBehavior.stopTrackingTargets(zone, zoneid, AutomatedTurretObject.GUID)
    AutomatedTurretBehavior.stopShooting(zone, zoneid, AutomatedTurretObject.Weapons.values.head.Equipment.get.GUID)
    None
  }

  private def trySelectNewTarget(): Option[Target] = {
    currentTarget.orElse {
      val turretPosition = AutomatedTurretObject.Position
      AutomatedTurretObject.Targets
        .filter { target =>
          !target.Destroyed && (target match {
            case p: Player => validTargetCheck(p)
            case _ => false
          })
        }
        .sortBy {
          target => Vector3.DistanceSquared(target.Position, turretPosition)
        }
        .flatMap { case target: Player =>
          testNewDetectedTarget(target, target.Name)
          Some(target)
        }
        .headOption
    }
  }

  private def validTargetCheck(target: Target): Boolean = {
    !target.Destroyed && (target match {
      case p: Player =>
        if (p.Cloaked) false
        else if (p.Crouching) false
        else p.isMoving(test = 3f)
      case _ => false
    })
  }

  private def performPeriodicTargetValidation(): List[Target] = {
    val size = AutomatedTurretObject.Targets.size
    val list = performDistanceCheck()
    performCurrentTargetDecayCheck()
    testTargetListQualifications(size)
    list
  }

  private def performDistanceCheck(): List[Target] = {
    //cull targets
    val pos = AutomatedTurretObject.Position
    val removedTargets = AutomatedTurretObject.Targets
      .collect {
        case t if t.Destroyed || Vector3.DistanceSquared(t.Position, pos) > 625 =>
          AutomatedTurretObject.RemoveTarget(t)
          t
      }
    removedTargets
  }

  private def testTargetListQualifications(beforeSize: Int): Boolean = {
    beforeSize > 0 && AutomatedTurretObject.Targets.isEmpty && periodicValidationTest.cancel()
  }

  private def retimePeriodicTargetChecks(beforeSize: Int): Boolean = {
    if (beforeSize == 0 && AutomatedTurretObject.Targets.nonEmpty) {
      periodicValidationTest = context.system.scheduler.scheduleWithFixedDelay(
        Duration.Zero,
        1.second,
        self,
        AutomatedTurretBehavior.PeriodicCheck
      )
      true
    } else {
      false
    }
  }

  private def performCurrentTargetDecayCheck(): Unit = {
    //complete culling and/or check the current selected target
    if (System.currentTimeMillis() - currentTargetLastShotReported > 3000L) {
      currentTarget.foreach { noLongerEngageDetectedTarget }
      if (automaticOperation) {
        //trySelectNewTarget()
      }
    }
  }

  def automaticTurretPostStop(): Unit = {
    periodicValidationTest.cancel()
    currentTarget.foreach { noLongerEngageDetectedTarget }
    AutomatedTurretObject.Targets.foreach { AutomatedTurretObject.RemoveTarget }
  }
}

object AutomatedTurretBehavior {
  import AutomatedTurret.Target
  final case class Alert(target: Target)

  final case class Unalert(target: Target)

  final case class ConfirmShot(target: Target)

  final case object Reset

  private case object PeriodicCheck

  def startTrackingTargets(zone: Zone, channel: String, guid: PlanetSideGUID, list: List[PlanetSideGUID]): Unit = {
    zone.LocalEvents ! LocalServiceMessage(
      channel,
      LocalAction.SendResponse(ObjectDetectedMessage(guid, guid, 0, list))
    )
  }

  def stopTrackingTargets(zone: Zone, channel: String, guid: PlanetSideGUID): Unit = {
    zone.LocalEvents ! LocalServiceMessage(
      channel,
      LocalAction.SendResponse(ObjectDetectedMessage(guid, guid, 0, List(PlanetSideGUID(0))))
    )
  }

  private def startShooting(zone: Zone, channel: String, weaponGuid: PlanetSideGUID): Unit = {
    zone.LocalEvents ! LocalServiceMessage(
      channel,
      LocalAction.SendResponse(ChangeFireStateMessage_Start(weaponGuid))
    )
  }

  private def stopShooting(zone: Zone, channel: String, weaponGuid: PlanetSideGUID): Unit = {
    zone.LocalEvents ! LocalServiceMessage(
      channel,
      LocalAction.SendResponse(ChangeFireStateMessage_Stop(weaponGuid))
    )
  }
}

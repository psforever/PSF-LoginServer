// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.{Default, Player}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.sourcing.{SourceEntry, SourceUniqueness}
import net.psforever.objects.vital.Vitality
import net.psforever.packet.game.{ChangeFireStateMessage_Start, ObjectDetectedMessage}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.{PlanetSideGUID, Vector3}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait AutomatedTurret
  extends PlanetSideServerObject
  with WeaponTurret {
  import AutomatedTurret.Target
  private var targets: List[Target] = List[Target]()

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
  _: Actor =>
  import AutomatedTurret.Target
  import AutomatedTurretBehavior.TurretTargetEntry

  private val targets: mutable.HashMap[SourceUniqueness, TurretTargetEntry] =
    mutable.HashMap[SourceUniqueness, TurretTargetEntry]()

  private var targetDistanceCheck: Cancellable = Default.Cancellable

  def AutomatedTurretObject: AutomatedTurret

  val automatedTurretBehavior: Actor.Receive = {
    case AutomatedTurretBehavior.Alert(target) =>
      val targets = AutomatedTurretObject.Targets
      val size = targets.size
      AutomatedTurretObject.Detected(target)
        .orElse {
          AutomatedTurretObject.AddTarget(target)
          retimeDistanceCheck(size)
          Some(target)
        }
        .foreach { newDetectedTarget }

    case AutomatedTurretBehavior.Unalert(target) =>
      val targets = AutomatedTurretObject.Targets
      val size = targets.size
      AutomatedTurretObject.Detected(target)
        .collect { out =>
          AutomatedTurretObject.RemoveTarget(target)
          testDistanceCheckQualifications(size)
          out
        }
        .foreach { noLongerDetectedTarget }

    case AutomatedTurretBehavior.ResetAlerts =>
      AutomatedTurretObject.Clear().foreach { noLongerDetectedTarget }
      testDistanceCheckQualifications(beforeSize = 1)

    case AutomatedTurretBehavior.PeriodicDistanceCheck =>
      performPeriodicDistanceCheck()
  }

  private def newDetectedTarget(target: Target): Unit = {
    target match {
      case target: Player =>
        target.Zone.AvatarEvents ! AvatarServiceMessage(
          target.Name,
          AvatarAction.SendResponse(PlanetSideGUID(0), ObjectDetectedMessage(AutomatedTurretObject.GUID, AutomatedTurretObject.GUID, 0, List(target.GUID)))
        )
        target.Zone.AvatarEvents ! AvatarServiceMessage(
          target.Name,
          AvatarAction.SendResponse(PlanetSideGUID(0), ChangeFireStateMessage_Start(AutomatedTurretObject.Weapons.values.head.Equipment.get.GUID))
        )
      case _ => ()
    }
  }

  private def noLongerDetectedTarget(target: Target): Unit = {
    target match {
      case target: Player =>
        target.Zone.AvatarEvents ! AvatarServiceMessage(
          target.Name,
          AvatarAction.SendResponse(PlanetSideGUID(0), ObjectDetectedMessage(AutomatedTurretObject.GUID, AutomatedTurretObject.GUID, 0, List(PlanetSideGUID(0))))
        )
      case _ => ()
    }
  }

  private def testDistanceCheckQualifications(beforeSize: Int): Unit = {
    if (beforeSize > 0 && AutomatedTurretObject.Targets.isEmpty) {
      targetDistanceCheck.cancel()
    }
  }

  private def retimeDistanceCheck(beforeSize: Int): Unit = {
    if (beforeSize == 0 && AutomatedTurretObject.Targets.nonEmpty) {
      targetDistanceCheck = context.system.scheduler.scheduleAtFixedRate(
        1.second,
        1.second,
        self,
        AutomatedTurretBehavior.PeriodicDistanceCheck
      )
    }
  }

  private def performPeriodicDistanceCheck(): List[Target] = {
    val pos = AutomatedTurretObject.Position
    val earlyTargets = AutomatedTurretObject.Targets
    val earlySize = earlyTargets.size
    val removedTargets = earlyTargets
      .collect {
        case t if Vector3.DistanceSquared(t.Position, pos) > 625 =>
          AutomatedTurretObject.RemoveTarget(t)
          t
      }
    removedTargets.foreach { noLongerDetectedTarget }
    testDistanceCheckQualifications(earlySize)
    removedTargets
  }
}

object AutomatedTurretBehavior {
  import AutomatedTurret.Target
  final case class Alert(target: Target)

  final case class Unalert(target: Target)

  final case object ResetAlerts

  private case object PeriodicDistanceCheck

  final case class TurretTargetEntry(target: Target, regard: RegardTargetAs.TurretOpinion)

  object RegardTargetAs {
    trait TurretOpinion

    final case object Friendly extends TurretOpinion
    final case object Testing extends TurretOpinion
    final case object Unreachable extends TurretOpinion
    final case object Blocked extends TurretOpinion
    final case object Invalid extends TurretOpinion
    final case object Attack extends TurretOpinion
  }
}

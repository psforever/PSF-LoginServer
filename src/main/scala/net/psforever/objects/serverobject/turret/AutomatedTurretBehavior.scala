// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.{Default, Player, Vehicle}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.DamageableEntity
import net.psforever.objects.sourcing.{SourceEntry, SourceUniqueness}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.{InteractsWithZone, Zone}
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, ObjectDetectedMessage}
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{PlanetSideGUID, Vector3}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

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

  private var currentTargetLastShotReported: Long = 0L

  private var periodicValidationTest: Cancellable = Default.Cancellable

  private lazy val autoStats: Option[Automation] = AutomatedTurretObject.Definition.AutoFire

  private var previousTestedTargets: Set[Target] = Set()

  def AutomatedTurretObject: AutomatedTurret

  val automatedTurretBehavior: Actor.Receive = if (autoStats.isDefined) {
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
  } else {
    Actor.emptyBehavior
  }

  def AutomaticOperation: Boolean = automaticOperation

  def AutomaticOperation_=(state: Boolean): Boolean = {
    val previousState = automaticOperation
    if (autoStats.isDefined) {
      automaticOperation = state
      if (!previousState && state) {
        trySelectNewTarget()
      } else if (previousState && !state) {
        AutomatedTurretObject.Target.foreach {
          noLongerEngageDetectedTarget
        }
      }
      state
    } else {
      false
    }
  }

  protected def AutomaticOperationFunctionalityChecks: Boolean

  protected def CurrentTargetLastShotReported: Long = currentTargetLastShotReported

  protected def CurrentTargetLastShotReported_=(value: Long): Long = {
    currentTargetLastShotReported = value
    CurrentTargetLastShotReported
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
    if (currentTargetToken.isEmpty) {
      currentTargetLastShotReported = now
      engageNewDetectedTarget(target)
    } else if (
        currentTargetToken.contains(SourceEntry(target).unique) &&
        now - currentTargetLastShotReported < autoStats.map(_.missedShotCooldown).getOrElse(0L)) {
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
    AutomatedTurretObject.Target.foreach(noLongerEngageDetectedTarget)
    AutomatedTurretObject.Target = None
    AutomatedTurretObject.Clear()
    testTargetListQualifications(beforeSize = 1)
    previousTestedTargets.foreach(noLongerEngageTestedTarget)
    previousTestedTargets = Set()
    currentTargetToken = None
  }

  protected def engageNewDetectedTarget(target: Target): Unit = {
    val zone = target.Zone
    val zoneid = zone.id
    previousTestedTargets.filterNot(_ == target).foreach(noLongerEngageTestedTarget)
    previousTestedTargets = Set(target)
    currentTargetToken = Some(SourceEntry(target).unique)
    AutomatedTurretObject.Target = target
    AutomatedTurretBehavior.startTracking(target, zoneid, AutomatedTurretObject.GUID, List(target.GUID))
    AutomatedTurretBehavior.startShooting(target, zoneid, AutomatedTurretObject.Weapons.values.head.Equipment.get.GUID)
  }

  protected def noLongerDetectTargetIfCurrent(target: Target): Option[Target] = {
    if (currentTargetToken.contains(SourceEntry(target).unique)) {
      noLongerEngageDetectedTarget(target)
    } else {
      AutomatedTurretObject.Target
    }
  }

  protected def noLongerEngageDetectedTarget(target: Target): Option[Target] = {
    AutomatedTurretObject.Target = None
    currentTargetToken = None
    noLongerEngageTestedTarget(target)
    None
  }

  private def noLongerEngageTestedTarget(target: Target): Option[Target] = {
    val zone = target.Zone
    val zoneid = zone.id
    AutomatedTurretBehavior.stopTracking(target, zoneid, AutomatedTurretObject.GUID)
    AutomatedTurretBehavior.stopShooting(target, zoneid, AutomatedTurretObject.Weapons.values.head.Equipment.get.GUID)
    None
  }

  protected def trySelectNewTarget(): Option[Target] = {
    AutomatedTurretObject.Target.orElse {
      val turretPosition = AutomatedTurretObject.Position
      val turretGuid = AutomatedTurretObject.GUID
      val weaponGuid = AutomatedTurretObject.Weapons.values.head.Equipment.get.GUID
      val radius = autoStats.get.targetTriggerRange
      val validation = autoStats.get.targetValidation
      val faction = AutomatedTurretObject.Faction
      //noinspection CollectHeadOption
      val (targets, forValidation) = AutomatedTurretObject
        .Targets
        .collect { case target
          if /*target.Faction != faction &&*/
            AutomatedTurretBehavior.shapedDistanceCheckAgainstValue(autoStats, target.Position, turretPosition, radius, result = -1) &&
            validation.exists(func => func(target)) =>
          target
        }
        .sortBy(target => Vector3.DistanceSquared(target.Position, turretPosition))
        .collect {
          case target: Player =>
            AutomatedTurretBehavior.Generic.testNewDetected(target, target.Name, turretGuid, weaponGuid)
            Seq((target, target))
          case target: Vehicle =>
            target.Seats.values
              .flatMap(_.occupants)
              .collectFirst { passenger =>
                AutomatedTurretBehavior.Generic.testNewDetected(passenger, passenger.Name, turretGuid, weaponGuid)
                (target, passenger)
              }
        }
        .flatten
        .unzip
      //call an explicit stop for these targets
      (for {
        a <- forValidation
        b <- previousTestedTargets
        if SourceEntry(a).unique != SourceEntry(b).unique
      } yield b)
        .foreach(noLongerEngageTestedTarget)
      previousTestedTargets = forValidation.toSet
      targets.headOption
    }
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
    val range = autoStats.map(_.targetDetectionRange).getOrElse(0f)
    val removedTargets = AutomatedTurretObject.Targets
      .collect {
        case t: InteractsWithZone
          if t.Destroyed || AutomatedTurretBehavior.shapedDistanceCheckAgainstValue(autoStats, t.Position, pos, range) =>
          AutomatedTurretObject.RemoveTarget(t)
          t
      }
    removedTargets
  }

  private def performCurrentTargetDecayCheck(): Unit = {
    val now = System.currentTimeMillis()
    val selectDelay = autoStats.map(_.targetSelectCooldown).getOrElse(3000L)
    AutomatedTurretObject.Target
      .collect { target =>
        //test target
        generalDecayCheck(
          now,
          autoStats.map(_.targetEscapeRange).getOrElse(400f),
          selectDelay,
          autoStats.map(_.missedShotCooldown).getOrElse(3000L),
          autoStats.map(_.targetEliminationCooldown).getOrElse(0L)
        )(target)
      }
      .orElse {
        //no target; unless we are deactivated or have any unfinished delays, search for new target
        if (automaticOperation && now - currentTargetLastShotReported >= 0) {
          trySelectNewTarget()
        }
        None
      }
  }

  private def generalDecayCheck(
                                 now: Long,
                                 escapeRange: Float,
                                 selectDelay: Long,
                                 cooldownDelay: Long,
                                 eliminationDelay: Long
                               )(target: Target): Option[Target] = {
    if (target.Destroyed) {
      //if the target died while we were shooting at it
      noLongerEngageDetectedTarget(target)
      currentTargetLastShotReported = now + eliminationDelay
      None
    } else if (AutomatedTurretBehavior.shapedDistanceCheckAgainstValue(autoStats, target.Position, AutomatedTurretObject.Position, escapeRange)) {
      //if the target made sufficient distance from the turret
      noLongerEngageDetectedTarget(target)
      currentTargetLastShotReported = now + cooldownDelay
      None
    } else if (now - currentTargetLastShotReported >= cooldownDelay) {
      //if the target goes mia through lack of response
      noLongerEngageDetectedTarget(target)
      currentTargetLastShotReported = now + selectDelay
      None
    } else {
      //continue shooting
      Some(target)
    }
  }

  private def testTargetListQualifications(beforeSize: Int): Boolean = {
    beforeSize > 0 && AutomatedTurretObject.Targets.isEmpty && periodicValidationTest.cancel()
  }

  private def retimePeriodicTargetChecks(beforeSize: Int): Boolean = {
    if (beforeSize == 0 && AutomatedTurretObject.Targets.nonEmpty && autoStats.isDefined) {
      val repeated = autoStats.map(_.detectionSpeed).getOrElse(0.seconds)
      retimePeriodicTargetChecks(repeated)
      true
    } else {
      false
    }
  }

  private def retimePeriodicTargetChecks(repeated: FiniteDuration): Unit = {
    periodicValidationTest.cancel()
    periodicValidationTest = context.system.scheduler.scheduleWithFixedDelay(
      0.seconds,
      repeated,
      self,
      AutomatedTurretBehavior.PeriodicCheck
    )
  }

  def automaticTurretPostStop(): Unit = {
    periodicValidationTest.cancel()
    AutomatedTurretObject.Target.foreach { noLongerEngageDetectedTarget }
    AutomatedTurretObject.Targets.foreach { AutomatedTurretObject.RemoveTarget }
  }

  protected def attemptRetaliation(target: Target, cause: DamageResult): Option[Target] = {
    if (automaticOperation && autoStats.exists(_.retaliatoryDuration > 0)) {
      AutomatedTurretBehavior.getAttackerFromCause(target.Zone, cause).collect {
        case attacker if attacker.Faction != target.Faction =>
          performRetaliation(attacker)
          attacker
      }
    } else {
      None
    }
  }

  private def performRetaliation(target: Target): Option[Target] = {
    AutomatedTurretObject.Target
      .collect {
        case _ if autoStats.exists(_.retaliationOverridesTarget) =>
          engageNewDetectedTarget(target)
          target
      }
      .orElse {
        engageNewDetectedTarget(target)
        Some(target)
      }
  }
}

object AutomatedTurretBehavior {
  import AutomatedTurret.Target
  final case class Alert(target: Target)

  final case class Unalert(target: Target)

  final case class ConfirmShot(target: Target)

  final case object Reset

  private case object PeriodicCheck

  trait AutomatedTurretDispatch {
    private val noTargets :List[PlanetSideGUID] = List(Service.defaultPlayerGUID)

    def getEventBus(target: Target): ActorRef

    def composeMessageEnvelope(channel: String, msg: PlanetSideGamePacket): Any

    def startTracking(target: Target, channel: String, turretGuid: PlanetSideGUID, list: List[PlanetSideGUID]): Unit = {
      getEventBus(target) ! composeMessageEnvelope(channel, startTrackingMsg(turretGuid, list))
    }

    def stopTracking(target: Target, channel: String, turretGuid: PlanetSideGUID): Unit = {
      getEventBus(target) ! composeMessageEnvelope(channel, stopTrackingMsg(turretGuid))
    }

    def startShooting(target: Target, channel: String, weaponGuid: PlanetSideGUID): Unit = {
      getEventBus(target) ! composeMessageEnvelope(channel, startShootingMsg(weaponGuid))
    }

    def stopShooting(target: Target, channel: String, weaponGuid: PlanetSideGUID): Unit = {
      getEventBus(target) ! composeMessageEnvelope(channel, stopShootingMsg(weaponGuid))
    }

    def testNewDetected(target: Target, channel: String, turretGuid: PlanetSideGUID, weaponGuid: PlanetSideGUID): Unit = {
      startTracking(target, channel, turretGuid, List(target.GUID))
      startShooting(target, channel, weaponGuid)
      stopShooting(target, channel, weaponGuid)
    }

    private def startTrackingMsg(guid: PlanetSideGUID, list: List[PlanetSideGUID]): PlanetSideGamePacket = {
      ObjectDetectedMessage(guid, guid, 0, list)
    }

    private def stopTrackingMsg(turretGuid: PlanetSideGUID): PlanetSideGamePacket = {
      ObjectDetectedMessage(turretGuid, turretGuid, 0, noTargets)
    }

    private def startShootingMsg(weaponGuid: PlanetSideGUID): PlanetSideGamePacket = {
      ChangeFireStateMessage_Start(weaponGuid)
    }

    private def stopShootingMsg(weaponGuid: PlanetSideGUID): PlanetSideGamePacket = {
      ChangeFireStateMessage_Stop(weaponGuid)
    }
  }

  object Generic extends AutomatedTurretDispatch {
    def getEventBus(target: Target): ActorRef = {
      target.Zone.LocalEvents
    }

    def composeMessageEnvelope(channel: String, msg: PlanetSideGamePacket): Any = {
      LocalServiceMessage(channel, LocalAction.SendResponse(msg))
    }
  }

  object Vehicle extends AutomatedTurretDispatch {
    def getEventBus(target: Target): ActorRef = {
      target.Zone.VehicleEvents
    }

    def composeMessageEnvelope(channel: String, msg: PlanetSideGamePacket): Any = {
      VehicleServiceMessage(channel, VehicleAction.SendResponse(Service.defaultPlayerGUID, msg))
    }
  }

  def startTracking(target: Target, channel: String, turretGuid: PlanetSideGUID, list: List[PlanetSideGUID]): Unit = {
    target match {
      case v: Vehicle => Vehicle.startTracking(v, channel, turretGuid, list)
      case _          => Generic.startTracking(target, channel, turretGuid, list)
    }
  }

  def stopTracking(target: Target, channel: String, turretGuid: PlanetSideGUID): Unit = {
    target match {
      case v: Vehicle => Vehicle.stopTracking(v, channel, turretGuid)
      case _          => Generic.stopTracking(target, channel, turretGuid)
    }
  }

  def startShooting(target: Target, channel: String, weaponGuid: PlanetSideGUID): Unit = {
    target match {
      case v: Vehicle => Vehicle.startShooting(v, channel, weaponGuid)
      case _          => Generic.startShooting(target, channel, weaponGuid)
    }
  }

  def stopShooting(target: Target, channel: String, weaponGuid: PlanetSideGUID): Unit = {
    target match {
      case v: Vehicle => Vehicle.stopShooting(v, channel, weaponGuid)
      case _          => Generic.stopShooting(target, channel, weaponGuid)
    }
  }

  def testNewDetected(target: Target, channel: String, turretGuid: PlanetSideGUID, weaponGuid: PlanetSideGUID): Unit = {
    target match {
      case v: Vehicle => Vehicle.testNewDetected(v, channel, turretGuid, weaponGuid)
      case _          => Generic.testNewDetected(target, channel, turretGuid, weaponGuid)
    }
  }

  def getAttackerFromCause(zone: Zone, cause: DamageResult): Option[PlanetSideServerObject with Vitality] = {
    import net.psforever.objects.sourcing._
    cause
      .interaction
      .adversarial
      .collect { adversarial =>
        adversarial.attacker match {
          case p: PlayerSource =>
            p.seatedIn
              .map { _._1.unique }
              .collect {
                case v: UniqueVehicle => zone.Vehicles.find(SourceEntry(_).unique == v)
                case a: UniqueAmenity => zone.GUID(a.guid)
                case d: UniqueDeployable => zone.DeployableList.find(SourceEntry(_).unique == d)
              }
              .flatten
              .orElse {
                val name = p.Name
                zone.LivePlayers.find(_.Name.equals(name))
              }
          case o =>
            o.unique match {
              case v: UniqueVehicle => zone.Vehicles.find(SourceEntry(_).unique == v)
              case a: UniqueAmenity => zone.GUID(a.guid)
              case d: UniqueDeployable => zone.DeployableList.find(SourceEntry(_).unique == d)
              case _ => None
            }
        }
      }
      .flatten
      .collect {
        case out: PlanetSideServerObject with Vitality => out
      }
  }

  def shapedDistanceCheckAgainstValue(
                                               stats: Option[Automation],
                                               position: Vector3,
                                               testPosition: Vector3,
                                               testRange: Float,
                                               result: Int = 1 //by default, calculation > input
                                             ): Boolean = {
    val testRangeSq = testRange * testRange
    if (stats.exists(_.cylindricalCheck)) {
      val height = testRange + stats.map(_.cylindricalHeight).getOrElse(0f)
      math.abs(position.z - testPosition.z).compareTo(height) == result &&
        Vector3.DistanceSquared(position.xy, testPosition.xy).compareTo(testRangeSq) == result
    } else {
      Vector3.DistanceSquared(position, testPosition).compareTo(testRangeSq) == result
    }
  }
}

// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.turret.auto

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.avatar.scoring.EquipmentStat
import net.psforever.objects.equipment.EffectTarget
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.DamageableEntity
import net.psforever.objects.serverobject.interior.Sidedness
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.turret.Automation
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry, SourceUniqueness}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.exp.ToDatabase
import net.psforever.objects.zones.{InteractsWithZone, Zone}
import net.psforever.objects.{Default, PlanetSideGameObject, Player}
import net.psforever.packet.game.{ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, ObjectDetectedMessage}
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{PlanetSideGUID, Vector3}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait AutomatedTurretBehavior {
  _: Actor with DamageableEntity =>
  import AutomatedTurret.Target
  /** a local reference to the automated turret data on the entity's definition */
  private lazy val autoStats: Option[Automation] = AutomatedTurretObject.Definition.AutoFire
  /** whether the automated turret is functional or if anything is blocking its operation */
  private var automaticOperation: Boolean = false
  /** quick reference of the current target, if any */
  private var currentTargetToken: Option[SourceUniqueness] = None
  /** time of the current target's selection or the last target's selection */
  private var currentTargetSwitchTime: Long = 0L
  /** time of the last confirmed shot hitting the target */
  private var currentTargetLastShotTime: Long = 0L
  /** game world position when the last shot's confirmation was recorded */
  private var currentTargetLocation: Option[Vector3] = None
  /** timer managing the available target qualifications test
   * whether or not a previously valid target is still a valid target */
  private var periodicValidationTest: Cancellable = Default.Cancellable
  /** targets that have been the subject of test shots just recently;
   * emptied when switching from the test shot cycle to actually selecting a target */
  private var ongoingTestedTargets: Seq[Target] = Seq[Target]()

  /** timer managing the trailing target qualifications self test
   * where the source will shoot directly at some target
   * expecting a response in return */
  private var selfReportedRefire: Cancellable = Default.Cancellable
  /** self-reported weapon fire produces projectiles that were shot;
   * due to the call and response nature of this mode, they also count as shots that were landed */
  private var shotsFired: Int = 0
  /** self-reported weapon fire produces targets that were eliminated;
   * this may duplicate information processed during some other database update call */
  private var targetsDestroyed: Int = 0

  def AutomatedTurretObject: AutomatedTurret

  val automatedTurretBehavior: Actor.Receive = if (autoStats.isDefined) {
    case AutomatedTurretBehavior.Alert(target) =>
      bringAttentionToTarget(target)

    case AutomatedTurretBehavior.ConfirmShot(target, _) =>
      normalConfirmShot(target)

    case SelfReportedConfirmShot(target) =>
      movementCancelSelfReportingFireConfirmShot(target)

    case AutomatedTurretBehavior.Unalert(target) =>
      disregardTarget(target)

    case AutomatedTurretBehavior.Reset =>
      resetAlerts()

    case AutomatedTurretBehavior.PeriodicCheck =>
      performPeriodicTargetValidation()
  } else {
    Actor.emptyBehavior
  }

  def AutomaticOperationPossible: Boolean = autoStats.isDefined

  def AutomaticOperation: Boolean = automaticOperation

  /**
   * In relation to whether the automated turret is operational,
   * set the value of a flag to record this condition.
   * Additionally, perform actions relevant to the state changes:
   * turning on when previously inactive;
   * and, turning off when previously active.
   * @param state new state
   * @return state that results from this action
   */
  def AutomaticOperation_=(state: Boolean): Boolean = {
    val previousState = automaticOperation
    val newState = state && AutomaticOperationFunctionalityChecks
    automaticOperation = newState
    if (!previousState && newState) {
      trySelectNewTarget()
    } else if (previousState && !newState) {
      ongoingTestedTargets = Seq()
      cancelSelfReportedAutoFire()
      AutomatedTurretObject.Target.foreach(noLongerEngageDetectedTarget)
    }
    newState
  }

  /**
   * A checklist of conditions that must be met before automatic operation of the turret should be possible.
   * Should not actually change the current activation state of the turret.
   * @return `true`, if it would be possible for automated behavior to become operational;
   *         `false`, otherwise
   */
  protected def AutomaticOperationFunctionalityChecks: Boolean = AutomaticOperationPossible

  /**
   * The last time weapons fire from the turret was confirmed by this control agency.
   * Exists for subclass access.
   * @return the time
   */
  protected def CurrentTargetLastShotReported: Long = currentTargetLastShotTime

  /**
   * Set a new last time weapons fire from the turret was confirmed by this control agency.
   * Exists for subclass access.
   * @param value the new time
   * @return the time
   */
  protected def CurrentTargetLastShotReported_=(value: Long): Long = {
    currentTargetLastShotTime = value
    CurrentTargetLastShotReported
  }

  /* Actor level functions */

  /**
   * Add a new potential target to the turret's list of known targets
   * only if this is a new potential target.
   * If the provided target is the first potential target known to the turret,
   * begin the timer that determines when or if that target is no longer considered qualified.
   * @param target something the turret can potentially shoot at
   */
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

  /**
   * Remove a target from the turret's list of known targets.
   * If the provided target is the last potential target known to the turret,
   * cancel the timer that determines when or if targets are to be considered qualified.
   * If we are shooting at the target, stop shooting at it.
   * @param target something the turret can potentially shoot at
   */
  private def disregardTarget(target: Target): Unit = {
    val targets = AutomatedTurretObject.Targets
    val size = targets.size
    AutomatedTurretObject.Detected(target)
      .collect { out =>
        AutomatedTurretObject.RemoveTarget(target)
        testTargetQualificationsForOngoingChecks(size)
        out
      }
      .flatMap {
        noLongerDetectTargetIfCurrent
      }
  }

  /**
   * Undo all the things.
   * It's like nothing ever happened.
   */
  private def resetAlerts(): Unit = {
    cancelPeriodicTargetChecks()
    cancelSelfReportedAutoFire()
    AutomatedTurretObject.Target.foreach(noLongerEngageDetectedTarget)
    AutomatedTurretObject.Target = None
    AutomatedTurretObject.Clear()
    currentTargetToken = None
    currentTargetLocation = None
    ongoingTestedTargets = Seq()
  }

  /* Normal automated turret behavior */

  /**
   * Process feedback from automatic turret weapon fire.
   * The most common situation in which this is encountered is when the turret is instructed to shoot at something
   * and that something reports being hit with the resulting projectile
   * and, as a result, a message is sent to the turret to encourage it to continue to shoot.
   * If there is no primary target yet, this target becomes primary.
   * @param target something the turret can potentially shoot at
   * @return `true`, if the target submitted was recognized by the turret;
   *         `false`, if the target can not be the current target
   */
  private def normalConfirmShot(target: Target): Boolean = {
    val now = System.currentTimeMillis()
    if (
      currentTargetToken.isEmpty &&
        target.Faction != AutomatedTurretObject.Faction &&
        now >= currentTargetLastShotTime
    ) {
      currentTargetLastShotTime = now
      currentTargetLocation = Some(target.Position)
      ongoingTestedTargets = Seq()
      cancelSelfReportedAutoFire()
      engageNewDetectedTarget(target)
      true
    } else if (
      currentTargetToken.contains(SourceUniqueness(target)) &&
        now - currentTargetLastShotTime < autoStats.map(_.cooldowns.missedShot).getOrElse(0L)
    ) {
      val escapeRange = autoStats.map(_.ranges.escape).getOrElse(400f)
      val distSq = Vector3.DistanceSquared(target.Position, AutomatedTurretObject.Position)
      val escapeSq = escapeRange * escapeRange
      if (distSq <= escapeSq) {
        currentTargetLastShotTime = now
        currentTargetLocation = Some(target.Position)
        cancelSelfReportedAutoFire()
        true
      } else {
        false
      }
    } else {
      false
    }
  }

  /**
   * Point the business end of the turret's weapon at a provided target
   * and begin shooting at that target.
   * The turret will rotate to follow the target's movements in the game world.
   * Perform some cleanup of potential targets and
   * perform setup of variables useful to maintain firepower against the target.
   * @param target something the turret can potentially shoot at
   */
  private def engageNewDetectedTarget(target: Target): Unit = {
    val zone = target.Zone
    val zoneid = zone.id
    currentTargetToken = Some(SourceUniqueness(target))
    currentTargetLocation = Some(target.Position)
    currentTargetSwitchTime = System.currentTimeMillis()
    AutomatedTurretObject.Target = target
    engageNewDetectedTarget(
      target,
      zoneid,
      AutomatedTurretObject.GUID,
      AutomatedTurretObject.Weapons.values.head.Equipment.get.GUID
    )
  }

  /**
   * Point the business end of the turret's weapon at a provided target
   * and begin shooting at that target.
   * The turret will rotate to follow the target's movements in the game world.<br>
   * For implementing behavior.
   * Must be implemented.
   * @param target something the turret can potentially shoot at
   * @param channel scope of the message
   * @param turretGuid turret
   * @param weaponGuid turret's weapon
   */
  protected def engageNewDetectedTarget(target: Target, channel: String, turretGuid: PlanetSideGUID, weaponGuid: PlanetSideGUID): Unit

  /**
   * If the provided target is the current target:
   * Stop pointing the business end of the turret's weapon at a provided target.
   * Stop shooting at the target.
   * @param target something the turret can potentially shoot at
   * @return something the turret was potentially shoot at
   */
  protected def noLongerDetectTargetIfCurrent(target: Target): Option[Target] = {
    if (currentTargetToken.contains(SourceUniqueness(target))) {
      cancelSelfReportedAutoFire()
      noLongerEngageDetectedTarget(target)
    } else {
      AutomatedTurretObject.Target
    }
  }

  /**
   * Stop pointing the business end of the turret's weapon at a provided target.
   * Stop shooting at the target.
   * Adjust some local values to disengage from the target.
   * @param target something the turret can potentially shoot at
   * @return something the turret was potentially shoot at
   */
  private def noLongerEngageDetectedTarget(target: Target): Option[Target] = {
    AutomatedTurretObject.Target = None
    currentTargetToken = None
    currentTargetLocation = None
    noLongerEngageTarget(
      target,
      target.Zone.id,
      AutomatedTurretObject.GUID,
      AutomatedTurretObject.Weapons.values.head.Equipment.get.GUID
    )
    None
  }

  /**
   * Stop pointing the business end of the turret's weapon at a provided target.
   * Stop shooting at the target.<br>
   * For implementing behavior.
   * Must be implemented.
   * @param target something the turret can potentially shoot at
   * @param channel scope of the message
   * @param turretGuid turret
   * @param weaponGuid turret's weapon
   * @return something the turret was potentially shooting at
   */
  protected def noLongerEngageTarget(target: Target, channel: String, turretGuid: PlanetSideGUID, weaponGuid: PlanetSideGUID): Option[Target]

  /**
   * While the automated turret is operational and active,
   * and while the turret does not have a current target to point towards and shoot at,
   * collect all of the potential targets known to the turret
   * and perform test shots that would only be visible to certain client perspectives.
   * If those perspectives report back about those test shots being confirmed hits,
   * the first reported confirmed test shot will be the chosen target.
   * We will potentially have an old list of targets that were tested the previous pass
   * and can be compared against a fresher list of targets.
   * Explicitly order certain unrepresented targets to stop being tested
   * in case the packets between the server and the client do not get transmitted properly
   * or the turret is not assembled correctly in its automatic fire definition.
   * @return something the turret can potentially shoot at;
   *         it doesn't really matter which something is returned but, rather, if anything is returned
   */
  protected def trySelectNewTarget(): Option[Target] = {
    AutomatedTurretObject.Target.orElse {
      val turretPosition = AutomatedTurretObject.Position
      val turretGuid = AutomatedTurretObject.GUID
      val side = AutomatedTurretObject.WhichSide
      val weaponGuid = AutomatedTurretObject.Weapons.values.head.Equipment.get.GUID
      val radius = autoStats.get.ranges.trigger
      val validation = autoStats.get.checks.validation
      val disqualifiers = autoStats.get.checks.blanking
      val faction = AutomatedTurretObject.Faction
      //current targets
      val selectedTargets = AutomatedTurretObject
        .Targets
        .collect { case target
          if !target.Destroyed &&
            target.Faction != faction &&
            Sidedness.equals(target.WhichSide, side) &&
            AutomatedTurretBehavior.shapedDistanceCheckAgainstValue(autoStats, target.Position, turretPosition, radius, result = -1) &&
            validation.exists(func => func(target)) &&
            disqualifiers.takeWhile(func => func(target)).isEmpty =>
          target
        }
      //sort targets into categories
      val (previousTargets, newTargets, staleTargets) = {
        val previouslyTestedTokens = ongoingTestedTargets.map(target => SourceUniqueness(target))
        val (previous_targets, new_targets) = selectedTargets.partition(target => previouslyTestedTokens.contains(SourceUniqueness(target)))
        val previousTargetTokens = previous_targets.map(target => (SourceUniqueness(target), target))
        val stale_targets = {
          for {
            (token, target) <- previousTargetTokens
            if !previouslyTestedTokens.contains(token)
          } yield target
        }
        (previous_targets, new_targets, stale_targets)
      }
      //associate with proper functionality and perform callbacks
      val newTargetsFunc: Iterable[(Target, (Target, String, PlanetSideGUID, PlanetSideGUID) => Unit)] =
        newTargets.map(target => (target, testNewDetected))
      val previousTargetsFunc: Iterable[(Target, (Target, String, PlanetSideGUID, PlanetSideGUID) => Unit)] =
        previousTargets.map(target => (target, testKnownDetected))
      ongoingTestedTargets = (newTargetsFunc ++ previousTargetsFunc)
        .toSeq
        .sortBy { case (target, _) => Vector3.DistanceSquared(target.Position, turretPosition) }
        .flatMap { case (target, func) => processForTestingTarget(target, turretGuid, weaponGuid, func) }
        .map { case (target, _) => target }
      staleTargets.foreach(target => processForTestingTarget(target, turretGuid, weaponGuid, suspendTargetTesting))
      selectedTargets.headOption
    }
  }

  /**
   * Dispatch packets in the direction of a client perspective
   * to determine if this target can be reliably struck with a projectile from the turret's weapon.
   * This resolves to a player avatar entity usually and is communicated on that player's personal name channel.
   * @param target something the turret can potentially shoot at
   * @param turretGuid turret
   * @param weaponGuid turret's weapon
   * @param processFunc na
   * @return a tuple composed of:
   *         something the turret can potentially shoot at
   *         something that will report whether the test shot struck the target
   */
  private def processForTestingTarget(
                                       target: Target,
                                       turretGuid: PlanetSideGUID,
                                       weaponGuid: PlanetSideGUID,
                                       processFunc: (Target, String, PlanetSideGUID, PlanetSideGUID)=>Unit
                                     ): Option[(Target, Target)] = {
    target match {
      case target: Player =>
        processFunc(target, target.Name, turretGuid, weaponGuid)
        Some((target, target))
      case target: Mountable =>
        target.Seats.values
          .flatMap(_.occupants)
          .collectFirst { passenger =>
            processFunc(target, passenger.Name, turretGuid, weaponGuid)
            (target, passenger)
          }
      case _ =>
        None
    }
  }

  /**
   * Dispatch packets in the direction of a client perspective
   * to determine if this target can be reliably struck with a projectile from the turret's weapon.<br>
   * For implementing behavior.
   * Must be implemented.
   * @param target something the turret can potentially shoot at
   * @param channel scope of the message
   * @param turretGuid turret
   * @param weaponGuid turret's weapon
   */
  protected def testNewDetected(target: Target, channel: String, turretGuid: PlanetSideGUID, weaponGuid: PlanetSideGUID): Unit

  /**
   * Dispatch packets in the direction of a client perspective
   * to determine if this target can be reliably struck with a projectile from the turret's weapon.<br>
   * For implementing behavior.
   * Must be implemented.
   * @param target something the turret can potentially shoot at
   * @param channel scope of the message
   * @param turretGuid not used
   * @param weaponGuid turret's weapon
   */
  protected def testKnownDetected(target: Target, channel: String, turretGuid: PlanetSideGUID, weaponGuid: PlanetSideGUID): Unit

  /**
   * na<br>
   * For overriding behavior.
   * @param target something the turret can potentially shoot at
   * @param channel scope of the message
   * @param turretGuid not used
   * @param weaponGuid turret's weapon
   */
  protected def suspendTargetTesting(
                                      target: Target,
                                      channel: String,
                                      turretGuid: PlanetSideGUID,
                                      weaponGuid: PlanetSideGUID
                                    ): Unit = { /*do nothing*/ }

  /**
   * Cull all targets that have been detected by this turret at some point
   * by determining which targets are either destroyed
   * or by determining which targets are too far away to be detected anymore.
   * If there are no more available targets, cancel the timer that governs this evaluation.
   * @return a list of somethings the turret can potentially shoot at that were removed
   */
  private def performPeriodicTargetValidation(): List[Target] = {
    val size = AutomatedTurretObject.Targets.size
    val list = performDistanceCheck()
    performCurrentTargetDecayCheck()
    testTargetQualificationsForOngoingChecks(size)
    list
  }

  /**
   * Cull all targets that have been detected by this turret at some point
   * by determining which targets are either destroyed
   * or by determining which targets are too far away to be detected anymore.
   * @return a list of somethings the turret can potentially shoot at that were removed
   */
  private def performDistanceCheck(): List[Target] = {
    //cull targets
    val pos = AutomatedTurretObject.Position
    val range = autoStats.map(_.ranges.detection).getOrElse(0f)
    val removedTargets = AutomatedTurretObject.Targets
      .collect {
        case t: InteractsWithZone
          if t.Destroyed || AutomatedTurretBehavior.shapedDistanceCheckAgainstValue(autoStats, t.Position, pos, range) =>
          AutomatedTurretObject.RemoveTarget(t)
          t
      }
    removedTargets
  }

  /**
   * An important process loop in the target engagement and target management of an automated turret.
   * If a target has been selected, perform a test to determine whether it remains the selected ("current") target.
   * If there is no target selected, or the previous selected target was demoted from being selected,
   * determine if enough time has passed before testing all available targets to find a new selected target.
   */
  private def performCurrentTargetDecayCheck(): Unit = {
    val now = System.currentTimeMillis()
    AutomatedTurretObject.Target
      .collect { target =>
        //test target
        generalDecayCheck(
          target,
          now,
          autoStats.map(_.ranges.escape).getOrElse(400f),
          autoStats.map(_.cooldowns.targetSelect).getOrElse(3000L),
          autoStats.map(_.cooldowns.missedShot).getOrElse(3000L),
          autoStats.map(_.cooldowns.targetElimination).getOrElse(0L)
        )
      }
      .orElse {
        //no target; unless we are deactivated or have any unfinished delays, search for new target
        //cancelSelfReportedAutoFire()
        //currentTargetLocation = None
        if (automaticOperation && now - currentTargetLastShotTime >= 0) {
          trySelectNewTarget()
        }
        None
      }
  }

  /**
   * An important process loop in the target engagement and target management of an automated turret.
   * If a target has been selected, perform a test to determine whether it remains the selected ("current") target.
   * If the target has been destroyed,
   * no longer qualifies as a target due to an internal or external change,
   * has moved beyond the turret's maximum engagement range,
   * or has been missing for a certain amount of time,
   * declare the the turret should no longer be shooting at (whatever) it (was).
   * Apply appropriate cooldown to instruct the turret to wait before attempting to select a new current target.
   * @param target something the turret can potentially shoot at
   * @return something the turret can potentially shoot at
   */
  private def generalDecayCheck(
                                 target: Target,
                                 now: Long,
                                 escapeRange: Float,
                                 selectDelay: Long,
                                 cooldownDelay: Long,
                                 eliminationDelay: Long
                               ): Option[Target] = {
    if (target.Destroyed) {
      //if the target died or is no longer considered a valid target while we were shooting at it
      cancelSelfReportedAutoFire()
      noLongerEngageDetectedTarget(target)
      currentTargetLastShotTime = now + eliminationDelay
      None
    } else if ((AutomatedTurretBehavior.commonBlanking ++ autoStats.map(_.checks.blanking).getOrElse(Nil)).exists(func => func(target))) {
      //if the target, while being engaged, stops counting as a valid target
      cancelSelfReportedAutoFire()
      noLongerEngageDetectedTarget(target)
      currentTargetLastShotTime = now + selectDelay
      None
    } else if (AutomatedTurretBehavior.shapedDistanceCheckAgainstValue(autoStats, target.Position, AutomatedTurretObject.Position, escapeRange)) {
      //if the target made sufficient distance from the turret
      cancelSelfReportedAutoFire()
      noLongerEngageDetectedTarget(target)
      currentTargetLastShotTime = now + cooldownDelay
      None
    }
    else if ({
      target match {
        case mount: Mountable => !mount.Seats.values.exists(_.isOccupied)
        case _                => false
      }
    }) {
      //certain targets can go "unresponsive" even though they should still be reachable, otherwise the target is mia
      trySelfReportedAutofireIfStationary()
      noLongerEngageDetectedTarget(target)
      currentTargetLastShotTime = now + selectDelay
      None
    } else if (now - currentTargetLastShotTime >= cooldownDelay) {
      //if the target goes mia through lack of response
      noLongerEngageDetectedTarget(target)
      currentTargetLastShotTime = now + selectDelay
      None
    } else {
      //continue shooting
      Some(target)
    }
  }

  /**
   * If there are no available targets,
   * and no current target,
   * stop the evaluation of available targets.
   * @param beforeListSize size of the list of available targets before some operation took place
   * @return `true`, if the evaluation of available targets was stopped;
   *         `false`, otherwise
   */
  private def testTargetQualificationsForOngoingChecks(beforeListSize: Int): Boolean = {
    beforeListSize > 0 &&
      AutomatedTurretObject.Targets.isEmpty &&
      AutomatedTurretObject.Target.isEmpty &&
      cancelPeriodicTargetChecks()
  }

  /**
   * If there is no current target,
   * start or restart the evaluation of available targets.
   * @param beforeSize size of the list of available targets before some operation took place
   * @return `true`, if the evaluation of available targets was stopped;
   *         `false`, otherwise
   */
  private def retimePeriodicTargetChecks(beforeSize: Int): Boolean = {
    if (beforeSize == 0 && AutomatedTurretObject.Targets.nonEmpty && autoStats.isDefined) {
      val repeated = autoStats.map(_.detectionSweepTime).getOrElse(1.seconds)
      retimePeriodicTargetChecks(repeated)
      true
    } else {
      false
    }
  }

  /**
   * Start or restart the evaluation of available targets immediately.
   * @param repeated delay in between evaluation periods
   */
  private def retimePeriodicTargetChecks(repeated: FiniteDuration): Unit = {
    periodicValidationTest.cancel()
    periodicValidationTest = context.system.scheduler.scheduleWithFixedDelay(
      0.seconds,
      repeated,
      self,
      AutomatedTurretBehavior.PeriodicCheck
    )
  }

  /**
   * Stop evaluation of available targets,
   * including tests for targets being removed from selection for the current target,
   * and tests whether the current target should remain a valid target.
   * @return `true`, because we can not fail
   * @see `Default.Cancellable`
   */
  private def cancelPeriodicTargetChecks(): Boolean = {
    ongoingTestedTargets = Seq()
    periodicValidationTest.cancel()
    periodicValidationTest = Default.Cancellable
    true
  }

  /**
   * Undo all the things, even the turret's knowledge of available targets.
   * It's like nothing ever happened.
   * @see `Actor.postStop`
   */
  protected def automaticTurretPostStop(): Unit = {
    resetAlerts()
    AutomatedTurretObject.Targets.foreach { AutomatedTurretObject.RemoveTarget }
    selfReportingCleanUp()
  }

  /* Retaliation behavior */

  /**
   * Retaliation is when a turret returns fire on a potential target that had just previously dealt damage to it.
   * Occasionally, the turret will drop its current target for the retaliatory target.
   * @param target something the turret can potentially shoot at
   * @param cause information about the damaging incident that caused the turret to consider retaliation
   * @return something the turret can potentially shoot at
   */
  protected def attemptRetaliation(target: PlanetSideServerObject with Vitality, cause: DamageResult): Option[PlanetSideServerObject with Vitality] = {
    val unique = SourceUniqueness(target)
    if (
      automaticOperation &&
        !currentTargetToken.contains(unique) &&
        autoStats.exists(_.retaliatoryDelay > 0)
    ) {
      AutomatedTurretBehavior.getAttackVectorFromCause(target.Zone, cause).collect {
        case attacker
          if attacker.Faction != target.Faction &&
            performRetaliation(attacker).nonEmpty &&
            currentTargetToken.contains(unique) =>
          if (periodicValidationTest.isCancelled) {
            //timer may need to be started, for example if damaged by things outside of detection perimeter
            retimePeriodicTargetChecks(autoStats.map(_.detectionSweepTime).getOrElse(1.seconds))
          }
          attacker
      }
    } else {
      None
    }
  }

  /**
   * Retaliation is when a turret returns fire on a potential target that had just previously dealt damage to it.
   * Occasionally, the turret will drop its current target for the retaliatory target.
   * @param target something the turret can potentially shoot at
   * @return something the turret can potentially shoot at
   */
  private def performRetaliation(target: Target): Option[Target] = {
    AutomatedTurretObject.Target
      .collect {
        case existingTarget
          if autoStats.exists { auto =>
            auto.retaliationOverridesTarget &&
              currentTargetSwitchTime + auto.retaliatoryDelay > System.currentTimeMillis() &&
              auto.checks.blanking.takeWhile(func => func(target)).isEmpty
          } =>
          //conditions necessary for overriding the current target
          cancelSelfReportedAutoFire()
          noLongerEngageDetectedTarget(existingTarget)
          engageNewDetectedTarget(target)
          target

        case existingTarget =>
          //stay with the current target
          existingTarget
      }
      .orElse {
        //no current target
        if (autoStats.exists(_.checks.blanking.takeWhile(func => func(target)).isEmpty)) {
          engageNewDetectedTarget(target)
          Some(target)
        } else {
          None
        }
      }
  }

  /* Self-reporting automatic turret behavior */

  /**
   * Process confirmation shot feedback from self-reported automatic turret weapon fire.
   * If the target has moved from the last time reported, cancel self-reported fire and revert to standard turret operation.
   * Fire a normal test shot specifically at that target to determine if it is yet out of range.
   * @param target something the turret can potentially shoot at
   */
  private def movementCancelSelfReportingFireConfirmShot(target: Target): Unit = {
    currentTargetLastShotTime = System.currentTimeMillis()
    shotsFired += 1
    target match {
      case v: Mountable
        if v.Destroyed && !v.Seats.values.exists(_.isOccupied) =>
        targetsDestroyed += 1
      case _ => ()
    }
    AutomatedTurretObject.Target
      .collect { oldTarget =>
        if (currentTargetToken.contains(SourceUniqueness(oldTarget))) {
          //target already being handled
          if (oldTarget.Destroyed || currentTargetLocation.exists(loc => Vector3.DistanceSquared(loc, oldTarget.Position) > 1f)) {
            //stop (destroyed, or movement disqualification)
            cancelSelfReportedAutoFire()
            noLongerEngageDetectedTarget(oldTarget)
            processForTestingTarget(
              oldTarget,
              AutomatedTurretObject.GUID,
              AutomatedTurretObject.Weapons.values.head.Equipment.get.GUID,
              testNewDetected
            )
          }
        } else {
          //stop (wrong target)
          cancelSelfReportedAutoFire()
        }
      }
      .orElse {
        //start new target
        engageNewDetectedTarget(target)
        tryPerformSelfReportedAutofire(target)
        None
      }
  }

  /**
   * If the target still is known to the turret,
   * and if the target has not moved recently,
   * but if none of the turret's projectiles have been confirmed shoots,
   * it may still be reachable with weapons fire.
   * Directly engage the target to simulate client perspective weapons fire damage.
   * If you enter this mode, and the target can be damaged this way, the target needs to move in the game world to switch back.
   * @return `true`, if the self-reporting test shot was discharged;
   *         `false`, otherwise
   */
  private def trySelfReportedAutofireIfStationary(): Boolean = {
    AutomatedTurretObject.Target
      .collect {
        case target
          if currentTargetLocation.exists(loc => Vector3.DistanceSquared(loc, target.Position) <= 1f) &&
            autoStats.exists(_.refireTime > 0.seconds) =>
          trySelfReportedAutofireTest(target)
      }
      .getOrElse(false)
  }

  /**
   * Directly engage the target to simulate client perspective weapons fire damage.
   * If you enter this mode, and the target can be damaged this way, the target needs to move in the game world to switch back.
   * @return `true`, if the self-reporting test shot was discharged;
   *         `false`, otherwise
   */
  private def trySelfReportedAutofireTest(target: Target): Boolean = {
    if (selfReportedRefire.isCancelled) {
      target.Actor ! AiDamage(AutomatedTurretObject)
      true
    } else {
      false
    }
  }

  /**
   * Directly engage the target to simulate client perspective weapons fire damage.
   * If you enter this mode, and the target can be damaged this way, the target needs to move in the game world to switch out.
   * @param target something the turret can potentially shoot at
   * @return `true`, if the self-reporting operation was initiated;
   *         `false`, otherwise
   */
  private def tryPerformSelfReportedAutofire(target: Target): Boolean = {
    if (selfReportedRefire.isCancelled) {
      selfReportedRefire = context.system.scheduler.scheduleWithFixedDelay(
        0.seconds,
        autoStats.map(_.refireTime).getOrElse(1.seconds),
        target.Actor,
        AiDamage(AutomatedTurretObject)
      )
      true
    } else {
      false
    }
  }

  /**
   * Stop directly communicating with a target to simulate weapons fire damage.
   * Utilized as a p[art of the auto-fire reset process.
   * @return `true`, because we can not fail
   * @see `Default.Cancellable`
   */
  private def cancelSelfReportedAutoFire(): Boolean = {
    selfReportedRefire.cancel()
    selfReportedRefire = Default.Cancellable
    true
  }

  /**
   * Cleanup for the variables involved in self-reporting.
   * Set them to zero.
   */
  protected def selfReportingCleanUp(): Unit = {
    shotsFired = 0
    targetsDestroyed = 0
  }

  /**
   * The self-reporting mode for automatic turrets produces weapon fire data that should be sent to the database.
   * The targets destroyed from self-reported fire are also logged to the database.
   */
  protected def selfReportingDatabaseUpdate(): Unit = {
    AutomatedTurretObject.TurretOwner match {
      case p: PlayerSource =>
        val weaponId = AutomatedTurretObject.Weapons.values.head.Equipment.map(_.Definition.ObjectId).getOrElse(0)
        ToDatabase.reportToolDischarge(p.CharId, EquipmentStat(weaponId, shotsFired, shotsFired, targetsDestroyed, 0))
        selfReportingCleanUp()
      case _ => ()
    }
  }
}

object AutomatedTurretBehavior {
  import AutomatedTurret.Target
  final case class Alert(target: Target)

  final case class Unalert(target: Target)

  final case class ConfirmShot(target: Target, reporter: Option[SourceEntry] = None)

  final case object Reset

  private case object PeriodicCheck

  private val commonBlanking: List[PlanetSideGameObject => Boolean] = List(
    EffectTarget.Validation.AutoTurretBlankPlayerTarget,
    EffectTarget.Validation.AutoTurretBlankVehicleTarget
  )

  private val noTargets: List[PlanetSideGUID] = List(Service.defaultPlayerGUID)

  /**
   * Are we tracking a target entity?
   * @param zone the region in which the messages will be dispatched
   * @param channel scope of the message
   * @param turretGuid turret
   * @param list target's globally unique identifier, in list form
   */
  def startTracking(zone: Zone, channel: String, turretGuid: PlanetSideGUID, list: List[PlanetSideGUID]): Unit = {
    zone.LocalEvents ! LocalServiceMessage(
      channel,
      LocalAction.SendResponse(ObjectDetectedMessage(turretGuid, turretGuid, 0, list))
    )
  }

  /**
   * Are we no longer tracking a target entity?
   * @param zone the region in which the messages will be dispatched
   * @param channel scope of the message
   * @param turretGuid turret
   */
  def stopTracking(zone: Zone, channel: String, turretGuid: PlanetSideGUID): Unit = {
    zone.LocalEvents ! LocalServiceMessage(
      channel,
      LocalAction.SendResponse(ObjectDetectedMessage(turretGuid, turretGuid, 0, noTargets))
    )
  }

  /**
   * Are we shooting a weapon?
   * @param zone the region in which the messages will be dispatched
   * @param channel scope of the message
   * @param weaponGuid turret's weapon
   */
  def startShooting(zone: Zone, channel: String, weaponGuid: PlanetSideGUID): Unit = {
    zone.LocalEvents ! LocalServiceMessage(
      channel,
      LocalAction.SendResponse(ChangeFireStateMessage_Start(weaponGuid))
    )
  }

  /**
   * Are we no longer shooting a weapon?
   * @param zone the region in which the messages will be dispatched
   * @param channel scope of the message
   * @param weaponGuid turret's weapon
   */
  def stopShooting(zone: Zone, channel: String, weaponGuid: PlanetSideGUID): Unit = {
    zone.LocalEvents ! LocalServiceMessage(
      channel,
      LocalAction.SendResponse(ChangeFireStateMessage_Stop(weaponGuid))
    )
  }

  /**
   * Provided damage information and a zone in which the damage occurred,
   * find a reference to the entity that caused the damage.
   * The entity that caused the damage should also be damageable itself.<br>
   * Very important: do not return the owner of the entity that caused the damage;
   * return the cause of the damage.<br>
   * Very important: does not properly trace damage from automatic weapons fire.
   * @param zone where the damage occurred
   * @param cause damage information
   * @return entity that caused the damage
   * @see `Vitality`
   */
  def getAttackVectorFromCause(zone: Zone, cause: DamageResult): Option[AutomatedTurret.Target] = {
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
                case v: UniqueVehicle => zone.Vehicles.find(SourceUniqueness(_) == v)
                case a: UniqueAmenity => zone.GUID(a.guid)
                case d: UniqueDeployable => zone.DeployableList.find(SourceUniqueness(_) == d)
              }
              .flatten
              .orElse {
                val name = p.Name
                zone.LivePlayers.find(_.Name.equals(name))
              }
          case o =>
            o.unique match {
              case v: UniqueVehicle => zone.Vehicles.find(SourceUniqueness(_) == v)
              case a: UniqueAmenity => zone.GUID(a.guid)
              case d: UniqueDeployable => zone.DeployableList.find(SourceUniqueness(_) == d)
              case _ => None
            }
        }
      }
      .flatten
      .collect {
        case out: AutomatedTurret.Target => out
      }
  }

  /**
   * Perform special distance checks that are either spherical or cylindrical.
   * Spherical distance checks are the default.
   * @param stats check if doing cylindrical tests
   * @param positionA one position in the game world
   * @param positionB another position in the game world
   * @param range input distance to test against
   * @param result complies with standard `compareTo` operations;
   *               `foo.compareTo(bar)`,
   *               where "foo" is calculated using `Vector3.DistanceSquared` or the absolute value of the vertical distance,
   *               and "bar" is `range`-squared
   * @return if the actual result of the comparison matches its anticipation `result`
   */
  def shapedDistanceCheckAgainstValue(
                                       stats: Option[Automation],
                                       positionA: Vector3,
                                       positionB: Vector3,
                                       range: Float,
                                       result: Int = 1 //by default, calculation > input
                                     ): Boolean = {
    val testRangeSq = range * range
    if (stats.exists(_.cylindrical)) {
      val height = range + stats.map(_.cylindricalExtraHeight).getOrElse(0f)
      (if (positionA.z > positionB.z) positionA.z - positionB.z else positionB.z - positionA.z).compareTo(height) == result &&
        Vector3.DistanceSquared(positionA.xy, positionB.xy).compareTo(testRangeSq) == result
    } else {
      Vector3.DistanceSquared(positionA, positionB).compareTo(testRangeSq) == result
    }
  }
}

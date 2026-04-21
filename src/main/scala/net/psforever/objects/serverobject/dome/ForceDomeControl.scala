// Copyright (c) 2025 PSForever
package net.psforever.objects.serverobject.dome

import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.structures.{Amenity, Building, PoweredAmenityControl}
import net.psforever.objects.serverobject.terminals.capture.{CaptureTerminal, CaptureTerminalAware, CaptureTerminalAwareBehavior}
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.etc.ForceDomeExposure
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.prop.DamageWithPosition
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.ChatMsg
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{ChatMessageType, PlanetSideEmpire, PlanetSideGeneratorState, Vector3}

import scala.annotation.unused

object ForceDomeControl {
  trait Command

  final case object CustomExpand extends Command

  final case object CustomCollapse extends Command

  final case object NormalBehavior extends Command

  final case object ApplyProtection extends Command

  final case object RemoveProtection extends Command

  final case object Purge extends Command

  /**
   * Dispatch a message to update the state of the clients with the server state of the capitol force dome.
   * @param dome            force dome
   * @param activationState new force dome status
   */
  def ChangeDomeEnergizedState(dome: ForceDomePhysics, activationState: Boolean): Unit = {
    dome.Energized = activationState
    val owner = dome.Owner
    val zone = owner.Zone
    owner.Actor ! BuildingActor.AmenityStateChange(dome)
    zone.LocalEvents ! LocalServiceMessage(
      zone.id,
      LocalAction.UpdateForceDomeStatus(Service.defaultPlayerGUID, owner.GUID, activationState)
    )
  }

  /**
   * If this building is a capitol major facility,
   * use the faction affinity, the generator status, and the resource silo's capacitance level
   * to determine if the capitol force dome should be active.
   * @param building building being evaluated
   * @param dome     force dome
   * @return the condition of the capitol force dome;
   *         `None`, if the facility is not a capitol building;
   *         `Some(true|false)` to indicate the state of the force dome
   */
  def CheckForceDomeStatus(building: Building, dome: ForceDomePhysics): Option[Boolean] = {
    if (building.IsCapitol) {
      Some(
        if (InvalidBuildingCapitolForceDomeConditions(building)) {
          false
        } else {
          building
            .Neighbours(building.Faction)
            .map(_.count(b => !InvalidBuildingCapitolForceDomeConditions(b)))
            .exists(_ > 1)
        }
      )
    } else {
      None
    }
  }

  /**
   * The natural conditions of a facility that is not eligible for its capitol force dome to be expanded.
   * The only test not employed is whether or not the target building is a capitol.
   * Omission of this condition makes this test capable of evaluating subcapitol eligibility
   * for capitol force dome expansion.
   * @param building target building
   * @return `true`, if the conditions for capitol force dome are not met;
   *         `false`, otherwise
   */
  def InvalidBuildingCapitolForceDomeConditions(building: Building): Boolean = {
    building.Faction == PlanetSideEmpire.NEUTRAL ||
      building.NtuLevel == 0 ||
      building.Generator.exists(_.Condition == PlanetSideGeneratorState.Destroyed)
  }

  /**
   * Apply a fixed point and a rotation value to a series of vertex offsets,
   * then daisy-chain the resulting vertices in such a way that
   * it creates a perimeter around the (building) owner of the capitol force dome.
   * The resulting capitol force dome barrier is a blocky pyramoid shape.
   * @param dome force dome
   * @return perimeter of the force dome barrier
   */
  def SetupForceDomePerimeter(dome: ForceDomePhysics): List[(Vector3, Vector3)] = {
    val center = dome.Position.xy
    val rotation = math.toRadians(dome.Owner.Orientation.z).toFloat
    val perimeterOffsets = dome.Definition.PerimeterOffsets
    val perimeterPoints = perimeterOffsets.map {
      center + Vector3.PlanarRotateAroundPoint(_, Vector3(0, 0, 1), rotation)
    }
    ((0 until perimeterPoints.size - 1).map { index =>
      (perimeterPoints(index), perimeterPoints(index + 1))
    } :+ (perimeterPoints.last, perimeterPoints.head)).toList
  }

  /**
   * The capitol force dome should have changed states but it will not!
   * Make certain everyone knows!
   * @param building target building
   * @param state whether the force dome is energized or not
   */
  def CustomDomeStateEnforcedMessage(
                                      building: Building,
                                      state: Boolean
                                    ): Unit = {
    val zone = building.Zone
    val message = LocalAction.SendResponse(ChatMsg(
      ChatMessageType.UNK_229,
      s"The Capitol force dome at ${building.Name} will remain ${if (state) "activated" else "deactivated"}."
    ))
    zone.LocalEvents ! LocalServiceMessage(zone.id, message)
  }

  /**
   * The capitol force dome will start changing states normally.
   * Make certain everyone knows.
   * @param building facility
   */
  def NormalDomeStateMessage(building: Building): Unit = {
    val events = building.Zone.LocalEvents
    val message = LocalAction.SendResponse(ChatMsg(
      ChatMessageType.UNK_227,
      "Expected capitol force dome state change will resume."
    ))
    building.PlayersInSOI.foreach { player =>
      events ! LocalServiceMessage(player.Name, message)
    }
  }

  /**
   * Evaluate the conditions of the building
   * and determine if its capitol force dome state should be updated
   * to reflect the actual conditions of the base or its surrounding bases.
   * If this building is considered a subcapitol facility to the zone's actual capitol facility,
   * and has the capitol force dome has a dependency upon it,
   * pass a message onto that facility that it should check its own state alignment.
   * @param building facility with `dome`
   * @param dome force dome
   * @return current state of the capitol force dome
   */
  def AlignForceDomeStatusAndUpdate(building: Building, dome: ForceDomePhysics): Boolean = {
    val energizedState = dome.Energized
    CheckForceDomeStatus(building, dome).exists {
      case true if !energizedState =>
        ChangeDomeEnergizedState(dome, activationState = true)
        dome.Owner.Actor ! BuildingActor.MapUpdate()
        true
      case false if energizedState =>
        ChangeDomeEnergizedState(dome, activationState = false)
        dome.Owner.Actor ! BuildingActor.MapUpdate()
        false
      case _ =>
        energizedState
    }
  }

  /**
   * Evaluate the conditions of the building
   * and determine if its capitol force dome state should be updated
   * to reflect the actual conditions of the base or its surrounding bases.
   * If this building is considered a subcapitol facility to the zone's actual capitol facility,
   * and has the capitol force dome has a dependency upon it,
   * pass a message onto that facility that it should check its own state alignment.
   * @param building facility with `dome`
   * @param dome force dome
   * @return current state of the capitol force dome
   */
  private def AlignForceDomeStatus(building: Building, dome: ForceDomePhysics): Boolean = {
    val energizedState = dome.Energized
    CheckForceDomeStatus(building, dome).exists {
      case true if !energizedState =>
        ChangeDomeEnergizedState(dome, activationState = true)
        true
      case false if energizedState =>
        ChangeDomeEnergizedState(dome, activationState = false)
        false
      case _ =>
        energizedState
    }
  }

  /**
   * Being too close to the force dome can destroy targets if they do not match the faction alignment of the dome.
   * This is the usual fate of opponents upon it being expanded (energized).
   * @see `Zone.serverSideDamage`
   * @param dome force dome
   * @param perimeter ground-level perimeter of the force dome is defined by these segments (as vertex pairs)
   * @return list of affected entities
   */
  def ForceDomeKills(dome: ForceDomePhysics, perimeter: List[(Vector3, Vector3)]): List[PlanetSideServerObject] = {
    Zone.serverSideDamage(
      dome.Zone,
      dome,
      ForceDomeExposure.damageProperties,
      makesContactWithForceDome,
      TargetUnderForceDome(perimeter),
      forceDomeTargets(dome.Definition.UseRadius, dome.Faction)
    )
  }

  /**
   * Prepare damage information related to being caugt underneath the capitol force dome when it expands.
   * @param source a game object that represents the source of the explosion
   * @param target a game object that is affected by the explosion
   * @return a `DamageInteraction` object
   */
  private def makesContactWithForceDome(
                                         source: PlanetSideGameObject with FactionAffinity with Vitality,
                                         target: PlanetSideGameObject with FactionAffinity with Vitality
                                       ): DamageInteraction = {
    DamageInteraction(
      SourceEntry(target),
      ForceDomeExposure(SourceEntry(source)),
      target.Position
    )
  }

  /**
   * To be considered within a force dome, a target entity must satisfy two orientations
   * where the second condition is one of two qualifications:
   * 1. within an angular perimeter boundary, and
   * 2a. below the base coordinate of the force dome or
   * 2b. within a region above the facility encapsulated by the force dome
   * @param segments ground-level perimeter of the force dome is defined by these segments (as vertex pairs)
   * @param obj1 a game entity, should be the force dome
   * @param obj2 a game entity, should be a damageable target of the force dome's wrath
   * @param maxDistance not applicable
   * @return `true`, if target is detected within the force dome kill region
   *        `false`, otherwise
   */
  def TargetUnderForceDome(
                            segments: List[(Vector3, Vector3)]
                          )
                          (
                            obj1: PlanetSideGameObject,
                            obj2: PlanetSideGameObject,
                            @unused maxDistance: Float
                          ): Boolean = {
    val Vector3(centerX, centerY, centerZ) = obj1.Position
    val Vector3(targetX, targetY, targetZ) = obj2.Position
    lazy val insideOfThePerimeter = !segments.exists { case (point1, point2) =>
      //want targets within the perimeter; if there's any intersection, target is outside of the perimeter
      segmentIntersectionTestPerSegment(centerX, centerY, targetX, targetY, point1.x, point1.y, point2.x, point2.y)
    }
    segments.nonEmpty && insideOfThePerimeter && (targetZ <= centerZ || underForceDomeCeiling(obj1, obj2, targetZ))
  }

  /**
   * The force dome is the top portion of an irregular prism.
   * It's walls are similarly angled inwards, usually.
   * There's no way to properly represent this without too much work.
   * Just ensure the target is either below the dome or within a circular region
   * defined by the base of the generator to the top of the extended stalk as the measurement of a hemisphere.
   * @see `Zone.distanceCheck`
   * @param obj1 a game entity, should be the force dome
   * @param obj2 a game entity, should be a damageable target of the force dome's wrath
   * @param height target height that must be under the ceiling
   * @return `true`, if target is detected within the force dome kill region
   *        `false`, otherwise
   */
  private def underForceDomeCeiling(obj1: PlanetSideGameObject, obj2: PlanetSideGameObject, height: Float): Boolean = {
    obj1 match {
      case dome: ForceDomePhysics =>
        val generatorHeightOffsetZ = dome.Definition.GeneratorOffset.z
        height < dome.Owner.Position.z + generatorHeightOffsetZ * 1.5f || Zone.distanceCheck(obj1, obj2, generatorHeightOffsetZ + 42f)
      case _ =>
        false
    }
  }

  /**
   * A function to assist line segment intersection tests.
   * The important frame of reference is checking whether a hypothetical segment between a point and a target
   * intersects with an established line segment between two other points.
   * For our purposes, the resulting line segments will never be collinear, so there is no reason to test that.
   * @param pointX x-coordinate used to create a test segment
   * @param pointY y-coordinate used to create a test segment
   * @param targetX x-coordinate of an important point for a test segment
   * @param targetY y-coordinate of an important point for a test segment
   * @param segmentPoint1x x-coordinate of one point from a segment
   * @param segmentPoint1y y-coordinate of one point from a segment
   * @param segmentPoint2x x-coordinate of a different point from a segment
   * @param segmentPoint2y y-coordinate of a different point from a segment
   * @return `true`, if the points form into two segments that intersect;
   *         `false`, otherwise
   */
  private def segmentIntersectionTestPerSegment(
                                                 pointX: Float,
                                                 pointY: Float,
                                                 targetX: Float,
                                                 targetY: Float,
                                                 segmentPoint1x: Float,
                                                 segmentPoint1y: Float,
                                                 segmentPoint2x: Float,
                                                 segmentPoint2y: Float
                                               ): Boolean = {
    //based on Franklin Antonio's "Faster Line Segment Intersection" topic "in Graphics Gems III" book (http://www.graphicsgems.org/)
    //compare, java.awt.geom.Line2D.linesIntersect
    val ax = targetX - pointX //delta-x of segment from center to target
    val ay = targetY - pointY //delta-y of segment from center to target
    val bx = segmentPoint1x - segmentPoint2x //delta-x of test segment
    val by = segmentPoint1y - segmentPoint2y //delta-y of test segment
    val cx = pointX - segmentPoint1x //delta-x of hypotenuse of triangle formed by center, test segment endpoint, and intersection point
    val cy = pointY - segmentPoint1y //delta-y of hypotenuse of triangle formed by center, test segment endpoint, and intersection point
    val alphaNumerator = by * cx - bx * cy //cross product of matrix [(by, bx)(cy, cx)]
    val denominator = ay * bx - ax * by //cross product of matrix [(ay, ax)(cy, cx)]
    val betaNumerator = ax * cy - ay * cx //cross product of matrix [(ax, ay)(cx, cy)]
    if (
      denominator > 0 &&
        (alphaNumerator < 0 || alphaNumerator > denominator || betaNumerator < 0 || betaNumerator > denominator)
    ) {
      false
    } else if (
      denominator < 0 &&
        (alphaNumerator > 0 || alphaNumerator < denominator || betaNumerator > 0 || betaNumerator < denominator)
    ) {
      false
    } else {
      //a collinear line test could go here, but we don't need it
      true
    }
  }

  /**
   * Collect all enemy players, vehicles, and combat engineering deployables in a sector.
   * @see `DamageWithPosition`
   * @see `Zone.blockMap.sector`
   * @param zone   the zone in which the explosion should occur
   * @param source a game entity that is treated as the origin and is excluded from results
   * @param damagePropertiesBySource information about the effect/damage
   * @return a list of affected entities
   */
  private def forceDomeTargets(
                                radius: Float,
                                targetFaction: PlanetSideEmpire.Value
                              )
                              (
                                zone: Zone,
                                source: PlanetSideGameObject with Vitality,
                                damagePropertiesBySource: DamageWithPosition
                              ): List[PlanetSideServerObject with Vitality] = {
    val sector = zone.blockMap.sector(source.Position.xy, radius)
    val playerTargets = sector.livePlayerList.filterNot { _.VehicleSeated.nonEmpty }
    //vehicles
    val vehicleTargets = sector.vehicleList.filterNot { v => v.Destroyed || v.MountedIn.nonEmpty }
    //deployables
    val deployableTargets = sector.deployableList.filterNot { _.Destroyed }
    //altogether ...
    (playerTargets ++ vehicleTargets ++ deployableTargets).filterNot(_.Faction == targetFaction)
  }
}

/**
 * An `Actor` that handles messages being dispatched to a specific capitol facility's force dome.
 * @param dome the `ForceDomePhysics` object being governed
 */
class ForceDomeControl(dome: ForceDomePhysics)
  extends PoweredAmenityControl
    with CaptureTerminalAwareBehavior
    with FactionAffinityBehavior.Check {
  def CaptureTerminalAwareObject: Amenity with CaptureTerminalAware = dome
  def FactionObject: FactionAffinity = dome

  /** a capitol force dome's owner should always be a facility;
   * to save time, cast this entity and cache it for repeated use once;
   * force dome is not immediately owned by its correct facility so delay determination */
  private lazy val domeOwnerAsABuilding = dome.Owner.asInstanceOf[Building]
  /** ground-level perimeter of the force dome is defined by these segments (as vertex pairs) */
  private lazy val perimeterSegments: List[(Vector3, Vector3)] = ForceDomeControl.SetupForceDomePerimeter(dome)
  /** force the dome into a certain state regardless of what conditions would normally transition it into that state */
  private var customState: Option[Boolean] = None

  def commonBehavior: Receive = checkBehavior
    .orElse {
      case ForceDomeControl.CustomExpand
        if !dome.Energized && (customState.isEmpty || customState.contains(false)) =>
        customState = Some(true)
        ForceDomeControl.CustomDomeStateEnforcedMessage(domeOwnerAsABuilding, state = true)
        ForceDomeControl.ChangeDomeEnergizedState(dome, activationState = true)

      case ForceDomeControl.CustomExpand
        if customState.isEmpty =>
        customState = Some(true)
        ForceDomeControl.CustomDomeStateEnforcedMessage(domeOwnerAsABuilding, state = true)

      case ForceDomeControl.CustomCollapse
        if dome.Energized && (customState.isEmpty || customState.contains(true)) =>
        customState = Some(false)
        ForceDomeControl.CustomDomeStateEnforcedMessage(domeOwnerAsABuilding, state = false)
        ForceDomeControl.ChangeDomeEnergizedState(dome, activationState = false)

      case ForceDomeControl.CustomCollapse
        if customState.isEmpty =>
        customState = Some(false)
        ForceDomeControl.CustomDomeStateEnforcedMessage(domeOwnerAsABuilding, state = false)

      case ForceDomeControl.NormalBehavior
        if customState.nonEmpty =>
        customState = None
        ForceDomeControl.NormalDomeStateMessage(domeOwnerAsABuilding)
        if (!blockedByCustomStateOr(ForceDomeControl.AlignForceDomeStatusAndUpdate)) {
          ForceDomeControl.ForceDomeKills(dome, perimeterSegments)
        }

      case ForceDomeControl.ApplyProtection
        if dome.Energized =>
        dome.Perimeter = perimeterSegments
        dome.Owner.Actor ! BuildingActor.AmenityStateChange(dome)

      case ForceDomeControl.RemoveProtection =>
        dome.Perimeter = List.empty
        dome.Owner.Actor ! BuildingActor.AmenityStateChange(dome)

      case ForceDomeControl.Purge =>
        ForceDomeControl.ForceDomeKills(dome, perimeterSegments)
    }

  def poweredStateLogic: Receive = {
    commonBehavior
      .orElse(captureTerminalAwareBehaviour)
      .orElse {
        case BuildingActor.AlertToFactionChange(_) =>
          blockedByCustomStateOr(ForceDomeControl.AlignForceDomeStatusAndUpdate)

        case _ => ()
      }
  }

  def unpoweredStateLogic: Receive = {
    commonBehavior
      .orElse {
        case _ => ()
      }
  }

  def powerTurnOffCallback() : Unit = {
    deenergizeUnlessSuppressedDueToCustomState()
  }

  def powerTurnOnCallback() : Unit = {
    blockedByCustomStateOr(ForceDomeControl.AlignForceDomeStatus)
  }

  override protected def captureTerminalIsResecured(terminal: CaptureTerminal): Unit = {
    super.captureTerminalIsResecured(terminal)
    blockedByCustomStateOr(ForceDomeControl.AlignForceDomeStatus)
  }

  override protected def captureTerminalIsHacked(terminal: CaptureTerminal): Unit = {
    super.captureTerminalIsHacked(terminal)
    deenergizeUnlessSuppressedDueToCustomState()
  }

  /**
   * Power down the force dome if it was previously being powered and
   * as long as a custom state of being energized is not being enforced.
   */
  private def deenergizeUnlessSuppressedDueToCustomState(): Unit = {
    if (dome.Energized) {
      if (customState.isEmpty) {
        ForceDomeControl.ChangeDomeEnergizedState(dome, activationState = false)
      } else {
        ForceDomeControl.CustomDomeStateEnforcedMessage(domeOwnerAsABuilding, state = true)
      }
    }
  }

  /**
   * Yield to a custom value enforcing a certain force dome state - energized or powered down.
   * If the custom state is not declared, run the function and analyze any change in the force dome's natural state.
   * Apply changes to region represented as "bound" by the perimeter as indicated by a state change.
   * @param func function to run if not blocked
   * @return current energized state of the dome
   */
  private def blockedByCustomStateOr(func: (Building, ForceDomePhysics) => Boolean): Boolean = {
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
    customState match {
      case None =>
        val oldState = dome.Energized
        val newState = func(domeOwnerAsABuilding, dome)
        if (!oldState && newState) {
          //dome activating
          context.system.scheduler.scheduleOnce(delay = 1500 milliseconds, self, ForceDomeControl.Purge)
          context.system.scheduler.scheduleOnce(delay = 4000 milliseconds, self, ForceDomeControl.ApplyProtection)
        } else if (oldState && !newState) {
          context.system.scheduler.scheduleOnce(delay = 1500 milliseconds, self, ForceDomeControl.RemoveProtection)
        }
        newState
      case Some(state)
        if !ForceDomeControl.CheckForceDomeStatus(domeOwnerAsABuilding, dome).contains(state) =>
        ForceDomeControl.CustomDomeStateEnforcedMessage(domeOwnerAsABuilding, state)
        state
      case Some(state) =>
        state
    }
  }
}

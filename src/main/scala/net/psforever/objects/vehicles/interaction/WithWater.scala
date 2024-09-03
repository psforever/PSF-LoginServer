// Copyright (c) 2024 PSForever
package net.psforever.objects.vehicles.interaction

import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Vehicle}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.environment.interaction.{InteractionWith, RespondsToZoneEnvironment}
import net.psforever.objects.serverobject.environment.interaction.common.Watery
import net.psforever.objects.serverobject.environment.interaction.common.Watery.OxygenStateTarget
import net.psforever.objects.serverobject.environment.{EnvironmentTrait, PieceOfEnvironment, interaction}
import net.psforever.objects.vehicles.control.VehicleControl
import net.psforever.objects.zones.InteractsWithZone
import net.psforever.types.OxygenState

import scala.annotation.unused
import scala.concurrent.duration._

class WithWater()
  extends InteractionWith
    with Watery {
  /** do this every time we're in sufficient contact with water */
  private var doInteractingWithBehavior: (InteractsWithZone, PieceOfEnvironment, Option[Any]) => Unit = wadingBeforeDrowning

  /**
   * Water is wet.
   * @param obj the target
   * @param body the environment
   */
  def doInteractingWith(
                         obj: InteractsWithZone,
                         body: PieceOfEnvironment,
                         data: Option[Any]
                       ): Unit = {
    depth = math.max(0f, body.collision.altitude - obj.Position.z)
    doInteractingWithBehavior(obj, body, data)
    obj.Actor ! RespondsToZoneEnvironment.Timer(attribute, delay = 500 milliseconds, obj.Actor, interaction.InteractingWithEnvironment(body, Some("wading")))
  }

  /**
   * Wading only happens while the vehicle's wheels are mostly above the water.
   * @param obj the target
   * @param body the environment
   */
  private def wadingBeforeDrowning(
                                    obj: InteractsWithZone,
                                    body: PieceOfEnvironment,
                                    data: Option[Any]
                                  ): Unit = {
    //we're already "wading", let's see if we're drowning
    if (depth >= GlobalDefinitions.MaxDepth(obj)) {
      //drowning
      beginDrowning(obj, body, data)
    }
  }

  /**
   * Too much water causes players to slowly suffocate.
   * When they (finally) drown, they will die.
   * @param obj the target
   * @param body the environment
   */
  private def beginDrowning(
                             obj: InteractsWithZone,
                             body: PieceOfEnvironment,
                             @unused data: Option[Any]
                           ): Unit = {
    obj match {
      case vehicle: Vehicle =>
        val (effect, time, percentage): (Boolean, Long, Float) = {
          val (a, b, c) = Watery.drowningInWateryConditions(obj, condition.map(_.state), waterInteractionTime)
          if (a && GlobalDefinitions.isFlightVehicle(vehicle.Definition)) {
            (true, 0L, 0f) //no progress bar
          } else {
            (a, b, c)
          }
        }
        if (effect) {
          condition = Some(OxygenStateTarget(obj.GUID, body, OxygenState.Suffocation, percentage))
          waterInteractionTime = System.currentTimeMillis() + time
          obj.Actor ! RespondsToZoneEnvironment.StopTimer(WithWater.WaterAction)
          obj.Actor ! RespondsToZoneEnvironment.Timer(WithWater.WaterAction, delay = time milliseconds, obj.Actor, VehicleControl.Disable(true))
          WithWater.doInteractingWithTargets(
            obj,
            percentage,
            body,
            vehicle.Seats.values.flatMap(_.occupants).filter(p => p.isAlive && (p.Zone eq obj.Zone))
          )
          doInteractingWithBehavior = drowning
        }
      case _ => ()
    }
  }

  /**
   * Too much water causes vehicles to slowly disable.
   * When fully waterlogged, the vehicle is completely immobile.
   * @param obj the target
   * @param body the environment
   */
  private def drowning(
                        obj: InteractsWithZone,
                        body: PieceOfEnvironment,
                        data: Option[Any]
                      ): Unit = {
    //test if player ever gets head above the water level
    if (depth < GlobalDefinitions.MaxDepth(obj)) {
      val (effect, time, percentage) = Watery.recoveringFromWateryConditions(obj, condition.map(_.state), waterInteractionTime)
      //switch to recovery
      if (percentage > 0) {
        recoverFromDrowning(obj, body, data, effect, time, percentage)
        doInteractingWithBehavior = recovering
      }
    }
  }

  /**
   * When out of water, the vehicle is no longer being waterlogged.
   * It does have to endure a recovery period to get back to normal, though.
   * @param obj the target
   * @param body the environment
   */
  private def recoverFromDrowning(
                                   obj: InteractsWithZone,
                                   body: PieceOfEnvironment,
                                   data: Option[Any]
                                 ): Unit = {
    val state = condition.map(_.state)
    if (state.contains(OxygenState.Suffocation)) {
      //set up for recovery
      val (effect, time, percentage) = Watery.recoveringFromWateryConditions(obj, state, waterInteractionTime)
      if (percentage < 99f) {
        //we're not too far gone
        recoverFromDrowning(obj, body, data, effect, time, percentage)
      }
      doInteractingWithBehavior = recovering
    } else {
      doInteractingWithBehavior = wadingBeforeDrowning
    }
  }

  /**
   * When out of water, the vehicle is no longer being waterlogged.
   * It does have to endure a recovery period to get back to normal, though.
   * @param obj the target
   * @param body the environment
   * @param effect na
   * @param time current time until completion of the next effect
   * @param percentage value to display in the drowning UI progress bar
   */
  private def recoverFromDrowning(
                                   obj: InteractsWithZone,
                                   body: PieceOfEnvironment,
                                   @unused data: Option[Any],
                                   effect: Boolean,
                                   time: Long,
                                   percentage: Float
                                 ): Unit = {
    obj match {
      case vehicle: Vehicle =>
        val cond = OxygenStateTarget(obj.GUID, body, OxygenState.Recovery, percentage)
        if (effect) {
          condition = Some(cond)
          waterInteractionTime = System.currentTimeMillis() + time
          obj.Actor ! RespondsToZoneEnvironment.StopTimer(WithWater.WaterAction)
          obj.Actor ! RespondsToZoneEnvironment.Timer(WithWater.WaterAction, delay = time milliseconds, obj.Actor, interaction.RecoveredFromEnvironmentInteraction(attribute))
          //inform the players
          WithWater.stopInteractingWithTargets(
            obj,
            percentage,
            body,
            vehicle.Seats.values.flatMap(_.occupants).filter(p => p.isAlive && (p.Zone eq obj.Zone))
          )
        }
      case _ => ()
    }
  }

  /**
   * The recovery period is much faster than the waterlogging process.
   * Check for when the vehicle fully recovers,
   * and that the vehicle does not regress back to waterlogging.
   * @param obj the target
   * @param body the environment
   */
  def recovering(
                  obj: InteractsWithZone,
                  body: PieceOfEnvironment,
                  data: Option[Any]
                ): Unit = {
    lazy val state = condition.map(_.state)
    if (depth >= GlobalDefinitions.MaxDepth(obj)) {
      //go back to drowning
      beginDrowning(obj, body, data)
    } else if (state.contains(OxygenState.Recovery)) {
      //check recovery conditions
      val (_, _, percentage) = Watery.recoveringFromWateryConditions(obj, state, waterInteractionTime)
      if (percentage < 1f) {
        doInteractingWithBehavior = wadingBeforeDrowning
      }
    }
  }/**
   * When out of water, the vehicle no longer risks becoming disabled.
   * It does have to endure a recovery period to get back to full dehydration
   * Flying vehicles are exempt from this process due to the abrupt disability they experience.
   * @param obj the target
   * @param body the environment
   */
  override def stopInteractingWith(
                                    obj: InteractsWithZone,
                                    body: PieceOfEnvironment,
                                    data: Option[Any]
                                  ): Unit = {
    val cond = condition.map(_.state)
    if (cond.contains(OxygenState.Suffocation)) {
      //go from suffocating to recovery
      recoverFromDrowning(obj, body, data)
    } else if (cond.isEmpty) {
      //neither suffocating nor recovering, so just reset everything
      recoverFromInteracting(obj)
      obj.Actor ! RespondsToZoneEnvironment.StopTimer(attribute)
      waterInteractionTime = 0L
      depth = 0f
      condition = None
      doInteractingWithBehavior = wadingBeforeDrowning
    }
  }

  override def recoverFromInteracting(obj: InteractsWithZone): Unit = {
    super.recoverFromInteracting(obj)
    if (condition.exists(_.state == OxygenState.Suffocation)) {
      stopInteractingWith(obj, condition.map(_.body).get, None)
    }
    condition = None
  }
}

object WithWater {
  /** special environmental trait to queue actions independent from the primary wading test */
  case object WaterAction extends EnvironmentTrait {
    override def canInteractWith(obj: PlanetSideGameObject): Boolean = false
    override def testingDepth(obj: PlanetSideGameObject): Float = Float.PositiveInfinity
  }

  /**
   * Tell the given targets that water causes vehicles to become disabled if they dive off too far, too deep.
   * @see `InteractingWithEnvironment`
   * @see `OxygenState`
   * @see `OxygenStateTarget`
   * @param obj the target
   * @param percentage the progress bar completion state
   * @param body the environment
   * @param targets recipients of the information
   */
  def doInteractingWithTargets(
                                obj: PlanetSideServerObject,
                                percentage: Float,
                                body: PieceOfEnvironment,
                                targets: Iterable[PlanetSideServerObject]
                              ): Unit = {
    val state = Some(OxygenStateTarget(obj.GUID, body, OxygenState.Suffocation, percentage))
    targets.foreach(_.Actor ! interaction.InteractingWithEnvironment(body, state))
  }

  /**
   * Tell the given targets that, when out of water, the vehicle no longer risks becoming disabled.
   * @see `EscapeFromEnvironment`
   * @see `OxygenState`
   * @see `OxygenStateTarget`
   * @param obj the target
   * @param percentage the progress bar completion state
   * @param body the environment
   * @param targets recipients of the information
   */
  def stopInteractingWithTargets(
                                  obj: PlanetSideServerObject,
                                  percentage: Float,
                                  body: PieceOfEnvironment,
                                  targets: Iterable[PlanetSideServerObject]
                                ): Unit = {
    val state = Some(OxygenStateTarget(obj.GUID, body, OxygenState.Recovery, percentage))
    targets.foreach(_.Actor ! interaction.EscapeFromEnvironment(body, state))
  }
}

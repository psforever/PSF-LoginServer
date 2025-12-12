// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.environment.interaction.common

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.environment.interaction.InteractWithEnvironment
import net.psforever.objects.serverobject.environment.interaction.common.Watery.OxygenStateTarget
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentTrait, PieceOfEnvironment}
import net.psforever.objects.zones.interaction.InteractsWithZone
import net.psforever.types.{OxygenState, PlanetSideGUID}

trait Watery {
  val attribute: EnvironmentTrait = EnvironmentAttribute.Water
  /** how long the current interaction has been progressing in the current way */
  protected var waterInteractionTime: Long = 0
  /** information regarding the drowning state */
  protected var condition: Option[OxygenStateTarget] = None
  /** information regarding the drowning state */
  def Condition: Option[OxygenStateTarget] = condition
  /** how far the player's feet are below the surface of the water */
  protected var depth: Float = 0f
  /** how far the player's feet are below the surface of the water */
  def Depth: Float = depth
}

object Watery {
  /**
   * Related to the progress of interacting with a body of water deeper than you are tall or
   * deeper than your vehicle is off the ground.
   * @param guid target
   * @param body environment being interacted with
   * @param state whether recovering or suffocating
   * @param progress the percentage of completion towards the state
   */
  final case class OxygenStateTarget(
                                      guid: PlanetSideGUID,
                                      body: PieceOfEnvironment,
                                      state: OxygenState,
                                      progress: Float
                                    )

  /**
   * na
   * @param target evaluate this to determine if to continue with this loss
   * @return whether or not we are sufficiently submerged in water
   */
  def wading(target: PlanetSideGameObject with InteractsWithZone): Boolean = {
    target
      .interaction()
      .collectFirst {
        case env: InteractWithEnvironment =>
          env
            .Interactions
            .get(EnvironmentAttribute.Water)
            .collectFirst {
              case water: Watery => water.Depth > 0f
            }
      }
      .flatten
      .contains(true)
  }

  /**
   * Calculate the effect of being exposed to a watery environment beyond an entity's critical region.
   * @param obj the target
   * @param interaction na
   * @return three values:
   *         whether any change in effect will occur,
   *         for how long this new change if effect will occur after starting,
   *         and what the starting progress value of this new effect looks like
   */
  def drowningInWater(
                       obj: PlanetSideServerObject,
                       interaction: Watery
                     ): (Boolean, Long, Float) = {
    drowningInWateryConditions(obj, interaction.condition.map(_.state), interaction.waterInteractionTime)
  }
  /**
   * Calculate the effect of being exposed to a watery environment beyond an entity's critical region.
   * @param obj the target
   * @param condition the current environment progressive event of the target, e.g., already drowning
   * @param completionTime how long since the current environment progressive event started
   * @return three values:
   *         whether any change in effect will occur,
   *         for how long this new change if effect will occur after starting,
   *         and what the starting progress value of this new effect looks like
   */
  def drowningInWateryConditions(
                                  obj: PlanetSideServerObject,
                                  condition: Option[OxygenState],
                                  completionTime: Long
                                ): (Boolean, Long, Float) = {
    condition match {
      case None =>
        //start suffocation process
        (true, obj.Definition.UnderwaterLifespan(OxygenState.Suffocation), 100f)
      case Some(OxygenState.Recovery) =>
        //switching from recovery to suffocation
        val oldDuration: Long = obj.Definition.UnderwaterLifespan(OxygenState.Recovery)
        val newDuration: Long = obj.Definition.UnderwaterLifespan(OxygenState.Suffocation)
        val oldTimeRemaining: Long = completionTime - System.currentTimeMillis()
        val oldTimeRatio: Float = 1f - oldTimeRemaining / oldDuration.toFloat
        val percentage: Float = oldTimeRatio * 100
        val newDrownTime: Long = (newDuration * oldTimeRatio).toLong
        (true, newDrownTime, percentage)
      case Some(OxygenState.Suffocation) =>
        //interrupted while suffocating, calculate the progress and keep suffocating
        val oldDuration: Long = obj.Definition.UnderwaterLifespan(OxygenState.Suffocation)
        val oldTimeRemaining: Long = completionTime - System.currentTimeMillis()
        val percentage: Float = (oldTimeRemaining / oldDuration.toFloat) * 100f
        (false, oldTimeRemaining, percentage)
      case _ =>
        (false, 0L, 0f)
    }
  }

  /**
   * Calculate the effect of being removed from a watery environment over an entity's critical region.
   * @param obj the target
   * @param interaction na
   * @return three values:
   *         whether any change in effect will occur,
   *         for how long this new change if effect will occur after starting,
   *         and what the starting progress value of this new effect looks like
   */
  def recoveringFromWater(
                           obj: PlanetSideServerObject,
                           interaction: Watery
                         ): (Boolean, Long, Float) = {
    recoveringFromWateryConditions(obj, interaction.condition.map(_.state), interaction.waterInteractionTime)
  }
  /**
   * Calculate the effect of being removed from a watery environment over an entity's critical region.
   * @param obj the target
   * @param condition the current environment progressive event of the target, e.g., already drowning
   * @param completionTime how long since the current environment progressive event started
   * @return three values:
   *         whether any change in effect will occur,
   *         for how long this new change if effect will occur after starting,
   *         and what the starting progress value of this new effect looks like
   */
  def recoveringFromWateryConditions(
                                      obj: PlanetSideServerObject,
                                      condition: Option[OxygenState],
                                      completionTime: Long
                                    ): (Boolean, Long, Float) = {
    condition match {
      case Some(OxygenState.Suffocation) =>
        //switching from suffocation to recovery
        val oldDuration: Long = obj.Definition.UnderwaterLifespan(OxygenState.Suffocation)
        val newDuration: Long = obj.Definition.UnderwaterLifespan(OxygenState.Recovery)
        val oldTimeRemaining: Long = math.max(0, completionTime - System.currentTimeMillis())
        val oldTimeRatio: Float = oldTimeRemaining / oldDuration.toFloat
        val percentage: Float = oldTimeRatio * 100
        val recoveryTime: Long = newDuration * (1f - oldTimeRatio).toLong
        (true, recoveryTime, percentage)
      case Some(OxygenState.Recovery) =>
        //interrupted while recovering, calculate the progress and keep recovering
        val currTime = System.currentTimeMillis()
        val duration: Long = obj.Definition.UnderwaterLifespan(OxygenState.Recovery)
        val startTime: Long = completionTime - duration
        val timeRemaining: Long = completionTime - currTime
        val percentage: Float = ((currTime - startTime) / duration.toFloat) * 100f
        (false, timeRemaining, percentage)
      case _ =>
        (false, 0L, 100f)
    }
  }
}

// Copyright (c) 2024 PSForever
package net.psforever.objects.avatar.interaction

import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Player}
import net.psforever.objects.serverobject.environment.interaction.{InteractionWith, RespondsToZoneEnvironment}
import net.psforever.objects.serverobject.environment.interaction.common.Watery
import net.psforever.objects.serverobject.environment.interaction.common.Watery.OxygenStateTarget
import net.psforever.objects.serverobject.environment.{EnvironmentTrait, PieceOfEnvironment, interaction}
import net.psforever.objects.zones.InteractsWithZone
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.OxygenState

import scala.concurrent.duration._

class WithWater(val channel: String)
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
    if (getExtra(data).nonEmpty) {
      inheritAndPushExtraData(obj, body, data)
    } else {
      depth = math.max(0f, body.collision.altitude - obj.Position.z)
      doInteractingWithBehavior(obj, body, data)
      obj.Actor ! RespondsToZoneEnvironment.Timer(attribute, delay = 500 milliseconds, obj.Actor, interaction.InteractingWithEnvironment(body, Some("wading")))
    }
  }

  /**
   * Wading only happens while the player's head is above the water.
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
    } else {
      //inform the player that their mounted vehicle is in trouble (that they are in trouble (but not from drowning (yet)))
      val extra = getExtra(data)
      if (extra.nonEmpty) {
        displayOxygenState(
          obj,
          condition.getOrElse(OxygenStateTarget(obj.GUID, body, OxygenState.Recovery, 95f)),
          extra
        )
      }
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
                             data: Option[Any]
                           ): Unit = {
    val (effect, time, percentage) = Watery.drowningInWateryConditions(obj, condition.map(_.state), waterInteractionTime)
    if (effect) {
      val cond = OxygenStateTarget(obj.GUID, body, OxygenState.Suffocation, percentage)
      waterInteractionTime = System.currentTimeMillis() + time
      condition = Some(cond)
      obj.Actor ! RespondsToZoneEnvironment.StopTimer(WithWater.WaterAction)
      obj.Actor ! RespondsToZoneEnvironment.Timer(WithWater.WaterAction, delay = time milliseconds, obj.Actor, Player.Die())
      //inform the player that they are in trouble
      displayOxygenState(obj, cond, getExtra(data))
      doInteractingWithBehavior = drowning
    }
  }

  /**
   * Too much water causes players to slowly suffocate.
   * When they (finally) drown, they will die.
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
      val (_, _, percentage) = Watery.recoveringFromWateryConditions(obj, condition.map(_.state), waterInteractionTime)
      //switch to recovery
      if (percentage > 0) {
        recoverFromDrowning(obj, body, data)
        doInteractingWithBehavior = recoverFromDrowning
      }
    }
  }

  /**
   * When out of water, the player is no longer suffocating.
   * The player does have to endure a recovery period to get back to normal, though.
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
   * When out of water, the player is no longer suffocating.
   * The player does have to endure a recovery period to get back to normal, though.
   * @param obj the target
   * @param body the environment
   * @param effect na
   * @param time current time until completion of the next effect
   * @param percentage value to display in the drowning UI progress bar
   */
  private def recoverFromDrowning(
                                   obj: InteractsWithZone,
                                   body: PieceOfEnvironment,
                                   data: Option[Any],
                                   effect: Boolean,
                                   time: Long,
                                   percentage: Float
                                 ): Unit = {
    val cond = OxygenStateTarget(obj.GUID, body, OxygenState.Recovery, percentage)
    val extra = data.collect {
      case t: OxygenStateTarget => Some(t)
      case w: Watery => w.Condition
    }.flatten
    if (effect) {
      condition = Some(cond)
      waterInteractionTime = System.currentTimeMillis() + time
      obj.Actor ! RespondsToZoneEnvironment.StopTimer(WithWater.WaterAction)
      obj.Actor ! RespondsToZoneEnvironment.Timer(WithWater.WaterAction, delay = time milliseconds, obj.Actor, interaction.RecoveredFromEnvironmentInteraction(attribute))
      //inform the player
      displayOxygenState(obj, cond, extra)
    } else if (extra.isDefined) {
      //inform the player
      displayOxygenState(obj, cond, extra)
    }
  }

  /**
   * The recovery period is much faster than the drowning process.
   * Check for when the player fully recovers,
   * and that the player does not regress back to drowning.
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
  }

  /**
   * When out of water, the player is no longer suffocating.
   * He's even stopped wading.
   * The only thing we should let complete now is recovery.
   * @param obj the target
   * @param body the environment
   */
  override def stopInteractingWith(
                                    obj: InteractsWithZone,
                                    body: PieceOfEnvironment,
                                    data: Option[Any]
                                  ): Unit = {
    if (getExtra(data).nonEmpty) {
      inheritAndPushExtraData(obj, body, data)
    } else {
      stopInteractingWithAction(obj, body, data)
    }
  }

  /**
   * When out of water, the player is no longer suffocating.
   * He's even stopped wading.
   * The only thing we should let complete now is recovery.
   * @param obj the target
   * @param body the environment
   */
  private def stopInteractingWithAction(
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
    val cond = condition.map(_.state)
    //whether or not we were suffocating or recovering, we need to undo the visuals for that
    if (cond.nonEmpty) {
      obj.Actor ! RespondsToZoneEnvironment.StopTimer(WithWater.WaterAction)
      displayOxygenState(
        obj,
        OxygenStateTarget(obj.GUID, condition.map(_.body).get, OxygenState.Recovery, 100f),
        None
      )
    }
    condition = None
  }

  /**
   * From the "condition" of someone else's drowning status,
   * extract target information and progress.
   * @param data any information
   * @return target information and drowning progress
   */
  private def getExtra(data: Option[Any]): Option[OxygenStateTarget] = {
    data.collect {
      case t: OxygenStateTarget => Some(t)
      case w: Watery => w.Condition
    }.flatten
  }

  /**
   * Send the message regarding drowning and recovery
   * that includes additional information about a related target that is drowning or recovering.
   * @param obj the target
   * @param body the environment
   * @param data essential information about someone else's interaction with water
   */
  private def inheritAndPushExtraData(
                                       obj: InteractsWithZone,
                                       body: PieceOfEnvironment,
                                       data: Option[Any]
                                     ): Unit = {
    val state = condition.map(_.state).getOrElse(OxygenState.Recovery)
    val Some((_, _, percentage)) = state match {
      case OxygenState.Suffocation => Some(Watery.drowningInWateryConditions(obj, Some(state), waterInteractionTime))
      case OxygenState.Recovery => Some(Watery.recoveringFromWateryConditions(obj, Some(state), waterInteractionTime))
    }
    displayOxygenState(obj, OxygenStateTarget(obj.GUID, body, state, percentage), getExtra(data))
  }

  /**
   * Send the message regarding drowning and recovery.
   * @param obj the target
   * @param cond the environment
   */
  private def displayOxygenState(
                                  obj: InteractsWithZone,
                                  cond: OxygenStateTarget,
                                  data: Option[OxygenStateTarget]
                                ): Unit = {
    obj.Zone.AvatarEvents ! AvatarServiceMessage(channel, AvatarAction.OxygenState(cond, data))
  }
}

object WithWater {
  /** special environmental trait to queue actions independent from the primary wading test */
  case object WaterAction extends EnvironmentTrait {
    override def canInteractWith(obj: PlanetSideGameObject): Boolean = false
    override def testingDepth: Float = Float.PositiveInfinity
  }
}

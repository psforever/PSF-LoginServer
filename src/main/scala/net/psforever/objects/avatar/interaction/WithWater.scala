// Copyright (c) 2024 PSForever
package net.psforever.objects.avatar.interaction

import net.psforever.objects.Player
import net.psforever.objects.serverobject.environment.interaction.{InteractionWith, RespondsToZoneEnvironment}
import net.psforever.objects.serverobject.environment.interaction.common.Watery
import net.psforever.objects.serverobject.environment.interaction.common.Watery.OxygenStateTarget
import net.psforever.objects.serverobject.environment.{PieceOfEnvironment, interaction}
import net.psforever.objects.zones.InteractsWithZone
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.OxygenState

import scala.concurrent.duration._

class WithWater(val channel: String)
  extends InteractionWith
    with Watery {
  /**
   * Water causes players to slowly suffocate.
   * When they (finally) drown, they will die.
   * @param obj the target
   * @param body the environment
   */
  def doInteractingWith(
                         obj: InteractsWithZone,
                         body: PieceOfEnvironment,
                         data: Option[Any]
                       ): Unit = {
    val extra = data.collect {
      case t: OxygenStateTarget => Some(t)
      case w: Watery => w.Condition
    }.flatten
    if (extra.isDefined) {
      //inform the player that their mounted vehicle is in trouble (that they are in trouble (but not from drowning (yet)))
      stopInteractingWith(obj, body, data)
    } else {
      val (effect, time, percentage) = Watery.drowningInWateryConditions(obj, condition.map(_.state), waterInteractionTime)
      if (effect) {
        val cond = OxygenStateTarget(obj.GUID, body, OxygenState.Suffocation, percentage)
        waterInteractionTime = System.currentTimeMillis() + time
        condition = Some(cond)
        obj.Actor ! RespondsToZoneEnvironment.Timer(attribute, delay = time milliseconds, obj.Actor, Player.Die())
        //inform the player that they are in trouble
        obj.Zone.AvatarEvents ! AvatarServiceMessage(channel, AvatarAction.OxygenState(cond, extra))
      }
    }
  }

  /**
   * When out of water, the player is no longer suffocating.
   * The player does have to endure a recovery period to get back to normal, though.
   * @param obj the target
   * @param body the environment
   */
  override def stopInteractingWith(
                                    obj: InteractsWithZone,
                                    body: PieceOfEnvironment,
                                    data: Option[Any]
                                  ): Unit = {
    val (effect, time, percentage) = Watery.recoveringFromWateryConditions(obj, condition.map(_.state), waterInteractionTime)
    val cond = OxygenStateTarget(obj.GUID, body, OxygenState.Recovery, percentage)
    val extra = data.collect {
      case t: OxygenStateTarget => Some(t)
      case w: Watery => w.Condition
    }.flatten
    if (percentage > 99f) {
      recoverFromInteracting(obj)
    }
    if (effect) {
      condition = Some(cond)
      waterInteractionTime = System.currentTimeMillis() + time
      obj.Actor ! RespondsToZoneEnvironment.Timer(attribute, delay = time milliseconds, obj.Actor, interaction.RecoveredFromEnvironmentInteraction(attribute))
      //inform the player
      obj.Zone.AvatarEvents ! AvatarServiceMessage(channel, AvatarAction.OxygenState(cond, extra))
    } else if (extra.isDefined) {
      //inform the player
      obj.Zone.AvatarEvents ! AvatarServiceMessage(channel, AvatarAction.OxygenState(cond, extra))
    }
  }

  override def recoverFromInteracting(obj: InteractsWithZone): Unit = {
    super.recoverFromInteracting(obj)
    if (condition.exists(info => info.state == OxygenState.Suffocation && info.progress < 99f)) {
      stopInteractingWith(obj, condition.map(_.body).get, None)
    }
    waterInteractionTime = 0L
    condition = None
  }
}

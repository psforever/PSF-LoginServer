// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.environment

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.Default
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.types.OxygenState

import scala.collection.mutable

/**
  * The mixin code for any server object that responds to the game world around it.
  * Specific types of environmental region is bound by geometry,
  * designated by attributes,
  * and gets reacted to when coming into contact with that geometry.
  * Ideally, the target under control instigates the responses towards the environment
  * by independently re-evaluating the conditions of its interactions.
  * While a resersal of this trigger scheme is possible, it is not ideal.
  * @see `InteractsWithZoneEnvironment`
  * @see `PieceOfEnvironment`
  */
trait RespondsToZoneEnvironment {
  _: Actor =>
  /** how long the current interaction has been progressing in the current way */
  var interactionTime : Long = 0
  /** the attributes of the environment that we are currently in interaction with */
  var interactWith : Option[EnvironmentTrait] = None
  /** a gesture of automation added to the interaction */
  var interactionTimer : Cancellable = Default.Cancellable
  /** a mapping of responses when specific interactions occur */
  private var interactWithEnvironmentStart: mutable.HashMap[EnvironmentTrait, RespondsToZoneEnvironment.Interaction] =
    mutable.HashMap[EnvironmentTrait, RespondsToZoneEnvironment.Interaction]()
  /** a mapping of responses when specific interactions cease */
  private var interactWithEnvironmentStop: mutable.HashMap[EnvironmentTrait, RespondsToZoneEnvironment.Interaction] =
    mutable.HashMap[EnvironmentTrait, RespondsToZoneEnvironment.Interaction]()

  def InteractiveObject: PlanetSideServerObject with InteractsWithZoneEnvironment

  val environmentBehavior: Receive = {
    case InteractWithEnvironment(target, body, optional) =>
      doEnvironmentInteracting(target, body, optional)

    case EscapeFromEnvironment(target, body, optional) =>
      stopEnvironmentInteracting(target, body, optional)

    case RecoveredFromEnvironmentInteraction() =>
      recoverFromEnvironmentInteracting()
  }

  def InteractWith: Option[EnvironmentTrait] = interactWith

  def SetInteraction(attribute: EnvironmentTrait, action: RespondsToZoneEnvironment.Interaction): Unit = {
    interactWithEnvironmentStart += attribute -> action
  }

  def SetInteractionStop(attribute: EnvironmentTrait, action: RespondsToZoneEnvironment.Interaction): Unit = {
    interactWithEnvironmentStop += attribute -> action
  }

  def doEnvironmentInteracting(obj: PlanetSideServerObject, body: PieceOfEnvironment, data: Option[OxygenStateTarget]): Unit = {
    if (interactWith.isEmpty) {
      val attribute = body.attribute
      interactWith = Some(attribute)
      interactionTimer.cancel()
      interactWithEnvironmentStart.get(attribute) match {
        case Some(func) => func(obj, body, data)
        case None => ;
      }
    }
  }

  def stopEnvironmentInteracting(obj: PlanetSideServerObject, body: PieceOfEnvironment, data: Option[OxygenStateTarget]): Unit = {
    val attribute = body.attribute
    if (interactWith.contains(attribute)) {
      interactWith = None
      interactionTimer.cancel()
      interactWithEnvironmentStop.get(attribute) match {
        case Some(func) => func(obj, body, data)
        case _ => recoverFromEnvironmentInteracting()
      }
    }
  }

  /**
    * Reset the environment encounter fields and completely stop whatever is the current mechanic.
    * This does not perform messaging relay either with mounted occupants or with any other service.
    */
  def recoverFromEnvironmentInteracting(): Unit = {
    interactionTimer.cancel()
    interactionTime = 0
    interactWith = None
  }
}

object RespondsToZoneEnvironment {
  type Interaction = (PlanetSideServerObject, PieceOfEnvironment, Option[OxygenStateTarget]) => Unit

  def drowningInWateryConditions(
                                 obj: PlanetSideServerObject,
                                 condition: Option[OxygenState],
                                 ongoingTime: Long
                               ): (Boolean, Long, Float) = {
    condition match {
      case None =>
        //start suffocation process
        (true, obj.Definition.UnderwaterLifespan(OxygenState.Suffocation), 100f)
      case Some(OxygenState.Recovery) =>
        //switching from recovery to suffocation
        val oldDuration: Long = obj.Definition.UnderwaterLifespan(OxygenState.Recovery)
        val newDuration: Long = obj.Definition.UnderwaterLifespan(OxygenState.Suffocation)
        val oldTimeRemaining: Long = ongoingTime - System.currentTimeMillis()
        val oldTimeRatio: Float = 1f - oldTimeRemaining / oldDuration.toFloat
        val percentage: Float = oldTimeRatio * 100
        val newDrownTime: Long = (newDuration * oldTimeRatio).toLong
        (true, newDrownTime, percentage)
      case Some(OxygenState.Suffocation) =>
        //interrupted while suffocating, calculate the progress and keep suffocating
        val oldDuration: Long = obj.Definition.UnderwaterLifespan(OxygenState.Suffocation)
        val oldTimeRemaining: Long = ongoingTime - System.currentTimeMillis()
        val percentage: Float = (oldTimeRemaining / oldDuration.toFloat) * 100f
        (false, oldTimeRemaining, percentage)
      case _ =>
        (false, 0L, 0f)
    }
  }

  def recoveringFromWateryConditions(
                                     obj: PlanetSideServerObject,
                                     condition: Option[OxygenState],
                                     ongoingTime: Long
                                   ): (Boolean, Long, Float) = {
    condition match {
      case Some(OxygenState.Suffocation) =>
        //switching from suffocation to recovery
        val oldDuration: Long = obj.Definition.UnderwaterLifespan(OxygenState.Suffocation)
        val newDuration: Long = obj.Definition.UnderwaterLifespan(OxygenState.Recovery)
        val oldTimeRemaining: Long = ongoingTime - System.currentTimeMillis()
        val oldTimeRatio: Float = oldTimeRemaining / oldDuration.toFloat
        val percentage: Float = oldTimeRatio * 100
        val recoveryTime: Long = newDuration - (newDuration * oldTimeRatio).toLong
        (true, recoveryTime, percentage)
      case Some(OxygenState.Recovery) =>
        //interrupted while recovering, calculate the progress and keep recovering
        val currTime = System.currentTimeMillis()
        val duration: Long = obj.Definition.UnderwaterLifespan(OxygenState.Recovery)
        val startTime: Long = ongoingTime - duration
        val timeRemaining: Long = ongoingTime - currTime
        val percentage: Float = ((currTime - startTime) / duration.toFloat) * 100f
        (false, timeRemaining, percentage)
      case _ =>
        (false, 0L, 100f)
    }
  }
}

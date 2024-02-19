// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.environment.interaction

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.serverobject.environment.EnvironmentTrait
import net.psforever.objects.zones.InteractsWithZone

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

/**
  * The mixin code for any server entity that responds to environmental representations in the game world.
  * Specific types of environmental region is bound by geometry,
  * designated by attributes,
  * and targets react when maneuvering contact with it.
  * @see `EnvironmentTrait`
  * @see `EscapeFromEnvironment`
  * @see `InteractingWithEnvironment`
  * @see `InteractionWith`
  * @see `InteractsWithEnvironment`
  * @see `PieceOfEnvironment`
  * @see `RecoveredFromEnvironmentInteraction`
  */
trait RespondsToZoneEnvironment {
  _: Actor =>
  /** a gesture of automation added to the interaction */
  private val interactionTimers: mutable.HashMap[EnvironmentTrait, Cancellable] = mutable.HashMap[EnvironmentTrait, Cancellable]()

  def InteractiveObject: InteractsWithZone

  private lazy val applicableInteractions: Map[EnvironmentTrait, InteractionWith] = InteractiveObject
    .interaction()
    .collectFirst { case inter: InteractWithEnvironment => inter.Interactions }
    .getOrElse(RespondsToZoneEnvironment.defaultInteractions)

  val environmentBehavior: Receive = {
    case RespondsToZoneEnvironment.Timer(attribute, delay, to, msg) =>
      import scala.concurrent.ExecutionContext.Implicits.global
      interactionTimers.get(attribute).foreach(_.cancel())
      interactionTimers.update(attribute, context.system.scheduler.scheduleOnce(delay, to, msg))

    case RespondsToZoneEnvironment.StopTimer(attribute) =>
      interactionTimers.get(attribute).foreach(_.cancel())

    case InteractingWithEnvironment(body, optional) =>
      applicableInteractions
        .get(body.attribute)
        .foreach(_.doInteractingWith(InteractiveObject, body, optional))

    case EscapeFromEnvironment(body, optional) =>
      applicableInteractions
        .get(body.attribute)
        .foreach(_.stopInteractingWith(InteractiveObject, body, optional))

    case RecoveredFromEnvironmentInteraction(attribute) =>
      applicableInteractions
        .get(attribute)
        .foreach(_.recoverFromInteracting(InteractiveObject))

    case ResetAllEnvironmentInteractions =>
      applicableInteractions.values.foreach(_.recoverFromInteracting(InteractiveObject))
      interactionTimers.values.foreach(_.cancel())
  }

  def respondToEnvironmentPostStop(): Unit = {
    interactionTimers.values.foreach(_.cancel())
  }
}

object RespondsToZoneEnvironment {
  val defaultInteractions: Map[EnvironmentTrait, InteractionWith] = Map.empty[EnvironmentTrait, InteractionWith]

  final case class Timer(attribute: EnvironmentTrait, delay: FiniteDuration, to: ActorRef, msg: Any)

  final case class StopTimer(attribute: EnvironmentTrait)
}

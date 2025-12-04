// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.environment.interaction

import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.environment.{EnvironmentTrait, PieceOfEnvironment}
import net.psforever.objects.zones._
import net.psforever.objects.zones.blockmap.{BlockMapEntity, SectorGroup, SectorPopulation}
import net.psforever.objects.zones.interaction.{InteractsWithZone, ZoneInteraction, ZoneInteractionType}
import net.psforever.types.Vector3

import scala.collection.mutable

case object EnvironmentInteraction extends ZoneInteractionType

/**
  * This game entity may infrequently test whether it may interact with game world environment.
  */
class InteractWithEnvironment()
  extends ZoneInteraction {
  private var interactionBehavior: InteractionBehavior = OnStableEnvironment()

  def Type: EnvironmentInteraction.type = EnvironmentInteraction

  def range: Float = 0f

  /** the environment that we are currently in interaction with */
  private[environment] var interactWith: Set[PieceOfEnvironment] = Set[PieceOfEnvironment]()

  /**
    * The method by which zone interactions are tested or a current interaction maintained.
    * Utilize a function literal that, when called, returns a function literal of the same type;
    * the function that is returned will not necessarily be the same as the one that was used
    * but will represent the existing and ongoing status of interaction with the environment.
    * Calling one function and exchanging it for another function to be called like this creates a procedure
    * that controls and limits the interactions with the environment to only what is necessary.
    * @see `AwaitOngoingInteraction`
    * @see `BlockedFromInteracting`
    * @see `OnStableEnvironment`
    * @param sector the portion of the block map being tested
    * @param target the fixed element in this test
    */
  def interaction(sector: SectorPopulation, target: InteractsWithZone): Unit = {
    target match {
      case t: InteractsWithZone =>
        interactWith = interactionBehavior.perform(t, sector, interactWith, allow=true)
        interactionBehavior = interactionBehavior.next
      case _ => ()
    }
  }

  /**
    * Suspend any current interaction procedures through the proper channels
    * or deactivate a previously flagged interaction blocking procedure
    * and reset the system to its neutral state.
    * The main difference between resetting and flagging the blocking procedure
    * is that resetting will (probably) restore the previously active procedure on the next `zoneInteraction` call
    * while blocking will halt all attempts to establish a new active interaction procedure
    * and unblocking will immediately install whatever is the current active interaction.
    * @see `AwaitOngoingInteraction`
    * @see `OnStableEnvironment`
    */
  def resetInteraction(target: InteractsWithZone) : Unit = {
    target match {
      case t: InteractsWithZone with BlockMapEntity =>
        AwaitOngoingInteraction(target.Zone).perform(t, SectorGroup.emptySector, interactWith, allow=false)
      case _ => ()
    }
    interactWith = Set()
    interactionBehavior = OnStableEnvironment()
  }

  private val interactWithEnvironment: mutable.HashMap[EnvironmentTrait, InteractionWith] =
    mutable.HashMap[EnvironmentTrait, InteractionWith]()

  def Interactions: Map[EnvironmentTrait, InteractionWith] = interactWithEnvironment.toMap

  def SetInteraction(attribute: EnvironmentTrait, action: InteractionWith): Unit = {
    interactWithEnvironment += attribute -> action
  }

  def SetInteractionStop(attribute: EnvironmentTrait, action: InteractionWith): Unit = {
    interactWithEnvironment += attribute -> action
  }

  def doEnvironmentInteracting(obj: InteractsWithZone, body: PieceOfEnvironment): Unit = {
    val attribute = body.attribute
    if (interactWith.isEmpty || interactWith.exists(_.attribute == attribute)) {
      interactWithEnvironment
        .get(attribute)
        .foreach(_.doInteractingWith(obj, body, None))
    }
  }

  def stopEnvironmentInteracting(obj: InteractsWithZone, body: PieceOfEnvironment): Unit = {
    val attribute = body.attribute
    if (interactWith.exists(_.attribute == attribute)) {
      interactWithEnvironment
        .get(attribute)
        .foreach(_.stopInteractingWith(obj, body, None))
    }
  }

  def OngoingInteractions: Set[EnvironmentTrait] = interactWith.map(_.attribute)
}

object InteractWithEnvironment {
  def apply(list: Iterable[InteractionWith]): InteractWithEnvironment = {
    val obj = new InteractWithEnvironment()
    list.foreach(env => obj.SetInteraction(env.attribute, env))
    obj
  }

  /**
   * Test whether any special terrain component has an affect upon the target entity.
   * @param obj the target entity
   * @param sector the portion of the block map being tested
   * @return any unstable, interactive, or special terrain that is being interacted
   */
  def checkAllEnvironmentInteractions(
                                       obj: PlanetSideServerObject,
                                       sector: SectorPopulation
                                     ): Set[PieceOfEnvironment] = {
    sector.environmentList
      .filter(body => body.attribute.canInteractWith(obj) && body.testInteraction(obj, body.attribute.testingDepth(obj)))
      .distinctBy(_.attribute)
      .toSet
  }

  /**
   * Test whether a special terrain component has an affect upon the target entity.
   * @param zone the zone in which the terrain is located
   * @param body the special terrain
   * @param obj the target entity
   * @return any unstable, interactive, or special terrain that is being interacted
   */
  def checkSpecificEnvironmentInteraction(
                                           zone: Zone,
                                           body: PieceOfEnvironment,
                                           obj: PlanetSideServerObject
                                         ): Option[PieceOfEnvironment] = {
    if ((obj.Zone eq zone) && body.testInteraction(obj, body.attribute.testingDepth(obj))) {
      Some(body)
    } else {
      None
    }
  }
}

trait InteractionBehavior {
  protected var nextstep: InteractionBehavior = this

  def perform(
               obj: InteractsWithZone,
               sector: SectorPopulation,
               existing: Set[PieceOfEnvironment],
               allow: Boolean
             ): Set[PieceOfEnvironment]

  def next: InteractionBehavior = {
    val out = nextstep
    nextstep = this
    out
  }
}

case class OnStableEnvironment() extends InteractionBehavior {
  /**
   * While on stable non-interactive terrain,
   * test whether any special terrain component has an affect upon the target entity.
   * If so, instruct the target that an interaction should occur.
   * Considered tail recursive, but not treated that way.
   * @see `BlockedFromInteracting`
   * @see `InteractWithEnvironment.checkAllEnvironmentInteractions`
   * @see `AwaitOngoingInteraction`
   * @see `OnStableEnvironment`
   * @param obj target entity
   * @param sector the portion of the block map being tested
   * @param existing not applicable
   * @param allow is this permitted, or will it be blocked?
   * @return applicable interactive environmental fields
   */
  def perform(
               obj: InteractsWithZone,
               sector: SectorPopulation,
               existing: Set[PieceOfEnvironment],
               allow: Boolean
             ): Set[PieceOfEnvironment] = {
    if (obj.Position != Vector3.Zero && allow) {
      val interactions = obj.interaction().collectFirst { case inter: InteractWithEnvironment => inter.Interactions }
      val bodies = InteractWithEnvironment.checkAllEnvironmentInteractions(obj, sector)
      bodies.foreach(body => interactions.flatMap(_.get(body.attribute)).foreach(_.doInteractingWith(obj, body, None)))
      if (bodies.nonEmpty) {
        nextstep = AwaitOngoingInteraction(obj.Zone)
      }
      bodies
    } else {
      nextstep = BlockedFromInteracting()
      Set()
    }
  }
}

final case class AwaitOngoingInteraction(zone: Zone) extends InteractionBehavior {
  /**
   * While on unstable, interactive, or special terrain,
   * test whether that special terrain component has an affect upon the target entity.
   * If no interaction exists,
   * treat the target as if it had been previously affected by the given terrain,
   * and instruct it to cease that assumption.
   * Transition between the affects of different special terrains is possible.
   * Considered tail recursive, but not treated that way.
   * @see `BlockedFromInteracting`
   * @see `InteractWithEnvironment.checkAllEnvironmentInteractions`
   * @see `InteractWithEnvironment.checkSpecificEnvironmentInteraction`
   * @see `OnStableEnvironment`
   * @param obj target entity
   * @param sector the portion of the block map being tested
   * @param existing environment fields from the previous step
   * @param allow is this permitted, or will it be blocked?
   * @return applicable interactive environmental fields
   */
  def perform(
               obj: InteractsWithZone,
               sector: SectorPopulation,
               existing: Set[PieceOfEnvironment],
               allow: Boolean
             ): Set[PieceOfEnvironment] = {
    val interactions = obj.interaction().collectFirst { case inter: InteractWithEnvironment => inter.Interactions }
    if (obj.Position != Vector3.Zero && allow) {
      val bodies = InteractWithEnvironment.checkAllEnvironmentInteractions(obj, sector)
      val (in, out) = existing.partition(body => InteractWithEnvironment.checkSpecificEnvironmentInteraction(zone, body, obj).nonEmpty)
      val inAttrs = bodies.map(_.attribute)
      out
        .filterNot(e => inAttrs.contains(e.attribute))
        .foreach(body => interactions.flatMap(_.get(body.attribute)).foreach(_.stopInteractingWith(obj, body, None)))
      bodies
        .diff(in)
        .foreach(body => interactions.flatMap(_.get(body.attribute)).foreach(_.doInteractingWith(obj, body, None)))
      if (bodies.isEmpty) {
        val n = OnStableEnvironment()
        val out = n.perform(obj, sector, Set(), allow)
        nextstep = n.next
        out
      } else {
        bodies
      }
    } else {
      existing.foreach(body => interactions.flatMap(_.get(body.attribute)).foreach(_.stopInteractingWith(obj, body, None)))
      nextstep = BlockedFromInteracting()
      Set()
    }
  }
}

case class BlockedFromInteracting() extends InteractionBehavior {
  /**
   * Do not care whether on stable non-interactive terrain or on unstable interactive terrain.
   * Wait until allowed to test again (external flag).
   * Considered tail recursive, but not treated that way.
   * @see `OnStableEnvironment`
   * @param obj target entity
   * @param sector the portion of the block map being tested
   * @param existing not applicable
   * @param allow is this permitted, or will it be blocked?
   * @return an empty set
   */
  def perform(
               obj: InteractsWithZone,
               sector: SectorPopulation,
               existing: Set[PieceOfEnvironment],
               allow: Boolean
             ): Set[PieceOfEnvironment] = {
    if (obj.Position != Vector3.Zero && allow) {
      nextstep = OnStableEnvironment()
    }
    Set()
  }
}

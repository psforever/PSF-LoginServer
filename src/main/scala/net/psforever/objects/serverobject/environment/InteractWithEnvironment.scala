// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.environment

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.zones._
import net.psforever.objects.zones.blockmap.{BlockMapEntity, SectorPopulation}

case object EnvironmentInteraction extends ZoneInteractionType

/**
  * This game entity may infrequently test whether it may interact with game world environment.
  */
class InteractWithEnvironment()
  extends ZoneInteraction {
  private var interactingWithEnvironment: (PlanetSideServerObject, Boolean) => Any =
    InteractWithEnvironment.onStableEnvironment()

  def Type = EnvironmentInteraction

  def range: Float = 0f

  /**
    * The method by which zone interactions are tested or a current interaction maintained.
    * Utilize a function literal that, when called, returns a function literal of the same type;
    * the function that is returned will not necessarily be the same as the one that was used
    * but will represent the existing and ongoing status of interaction with the environment.
    * Calling one function and exchanging it for another function to be called like this creates a procedure
    * that controls and limits the interactions with the environment to only what is necessary.
    * @see `InteractsWithEnvironment.blockedFromInteracting`
    * @see `InteractsWithEnvironment.onStableEnvironment`
    * @see `InteractsWithEnvironment.awaitOngoingInteraction`
    * @param sector the portion of the block map being tested
    * @param target the fixed element in this test
    */
  def interaction(sector: SectorPopulation, target: InteractsWithZone): Unit = {
    interactingWithEnvironment = interactingWithEnvironment(target, true)
      .asInstanceOf[(PlanetSideServerObject, Boolean) => Any]
  }

  /**
    * Suspend any current interaction procedures through the proper channels
    * or deactivate a previously flagged interaction blocking procedure
    * and reset the system to its neutral state.
    * The main difference between resetting and flagging the blocking procedure
    * is that resetting will (probably) restore the previously active procedure on the next `zoneInteraction` call
    * while blocking will halt all attempts to establish a new active interaction procedure
    * and unblocking will immediately install whatever is the current active interaction.
    * @see `InteractsWithEnvironment.onStableEnvironment`
    */
  def resetInteraction(target: InteractsWithZone) : Unit = {
    interactingWithEnvironment(target, false)
    interactingWithEnvironment = InteractWithEnvironment.onStableEnvironment()
  }
}

object InteractWithEnvironment {
  /**
    * While on stable non-interactive terrain,
    * test whether any special terrain component has an affect upon the target entity.
    * If so, instruct the target that an interaction should occur.
    * Considered tail recursive, but not treated that way.
    * @see `blockedFromInteracting`
    * @see `checkAllEnvironmentInteractions`
    * @see `awaitOngoingInteraction`
    * @param obj the target entity
    * @return the function literal that represents the next iterative call of ongoing interaction testing;
    *         may return itself
    */
  def onStableEnvironment()(obj: PlanetSideServerObject, allow: Boolean): Any = {
    if(allow) {
      checkAllEnvironmentInteractions(obj) match {
        case Some(body) =>
          obj.Actor ! InteractingWithEnvironment(obj, body, None)
          awaitOngoingInteraction(obj.Zone, body)(_,_)
        case None =>
          onStableEnvironment()(_,_)
      }
    } else {
      blockedFromInteracting()(_,_)
    }
  }

  /**
    * While on unstable, interactive, or special terrain,
    * test whether that special terrain component has an affect upon the target entity.
    * If no interaction exists,
    * treat the target as if it had been previously affected by the given terrain,
    * and instruct it to cease that assumption.
    * Transition between the affects of different special terrains is possible.
    * Considered tail recursive, but not treated that way.
    * @see `blockedFromInteracting`
    * @see `checkAllEnvironmentInteractions`
    * @see `checkSpecificEnvironmentInteraction`
    * @see `onStableEnvironment`
    * @param zone the zone in which the terrain is located
    * @param body the special terrain
    * @param obj the target entity
    * @return the function literal that represents the next iterative call of ongoing interaction testing;
    *         may return itself
    */
  def awaitOngoingInteraction(zone: Zone, body: PieceOfEnvironment)(obj: PlanetSideServerObject, allow: Boolean): Any = {
    if (allow) {
      checkSpecificEnvironmentInteraction(zone, body)(obj) match {
        case Some(_) =>
          awaitOngoingInteraction(obj.Zone, body)(_, _)
        case None =>
          checkAllEnvironmentInteractions(obj) match {
            case Some(newBody) if newBody.attribute == body.attribute =>
              obj.Actor ! InteractingWithEnvironment(obj, newBody, None)
              awaitOngoingInteraction(obj.Zone, newBody)(_, _)
            case Some(newBody) =>
              obj.Actor ! EscapeFromEnvironment(obj, body, None)
              obj.Actor ! InteractingWithEnvironment(obj, newBody, None)
              awaitOngoingInteraction(obj.Zone, newBody)(_, _)
            case None =>
              obj.Actor ! EscapeFromEnvironment(obj, body, None)
              onStableEnvironment()(_, _)
          }
      }
    } else {
      obj.Actor ! EscapeFromEnvironment(obj, body, None)
      blockedFromInteracting()(_,_)
    }
  }

  /**
    * Do not care whether on stable non-interactive terrain or on unstable interactive terrain.
    * Wait until allowed to test again (external flag).
    * Considered tail recursive, but not treated that way.
    * @see `onStableEnvironment`
    * @param obj the target entity
    * @return the function literal that represents the next iterative call of ongoing interaction testing;
    *         may return itself
    */
  def blockedFromInteracting()(obj: PlanetSideServerObject, allow: Boolean): Any = {
    if (allow) {
      onStableEnvironment()(obj, allow)
    } else {
      blockedFromInteracting()(_,_)
    }
  }

  /**
    * Test whether any special terrain component has an affect upon the target entity.
    * @param obj the target entity
    * @return any unstable, interactive, or special terrain that is being interacted
    */
  def checkAllEnvironmentInteractions(obj: PlanetSideServerObject): Option[PieceOfEnvironment] = {
    val position = obj.Position
    val depth = GlobalDefinitions.MaxDepth(obj)
    (obj match {
      case bme: BlockMapEntity =>
        obj.Zone.blockMap.sector(bme).environmentList
      case _ =>
        obj.Zone.map.environment
    }).find { body =>
      body.attribute.canInteractWith(obj) && body.testInteraction(position, depth)
    }
  }

  /**
    * Test whether a special terrain component has an affect upon the target entity.
    * @param zone the zone in which the terrain is located
    * @param body the special terrain
    * @param obj the target entity
    * @return any unstable, interactive, or special terrain that is being interacted
    */
  private def checkSpecificEnvironmentInteraction(zone: Zone, body: PieceOfEnvironment)(obj: PlanetSideServerObject): Option[PieceOfEnvironment] = {
    if ((obj.Zone eq zone) && body.testInteraction(obj.Position, GlobalDefinitions.MaxDepth(obj))) {
      Some(body)
    } else {
      None
    }
  }
}

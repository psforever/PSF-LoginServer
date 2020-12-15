// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.environment

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.zones.Zone

trait InteractsWithZoneEnvironment {
  _: PlanetSideServerObject =>
  private var _allowZoneEnvironmentInteractions: Boolean = true

  def allowZoneEnvironmentInteractions: Boolean = _allowZoneEnvironmentInteractions

  def allowZoneEnvironmentInteractions_=(allow: Boolean): Boolean = {
    val before = _allowZoneEnvironmentInteractions
    _allowZoneEnvironmentInteractions = allow
    if (before != allow) {
      zoneInteraction()
    }
    _allowZoneEnvironmentInteractions
  }

  private var interactingWithEnvironment: (PlanetSideServerObject, Boolean) => Any =
    InteractsWithZoneEnvironment.onStableEnvironment()

  def zoneInteraction(): Unit = {
    //val func: (PlanetSideServerObject, Boolean) => Any = interactingWithEnvironment(this, allowZoneEnvironmentInteractions)
    interactingWithEnvironment = interactingWithEnvironment(this, allowZoneEnvironmentInteractions)
      .asInstanceOf[(PlanetSideServerObject, Boolean) => Any]
  }
}

object InteractsWithZoneEnvironment {
  /**
    * While on stable non-interactive terrain,
    * test whether any special terrain component has an affect upon the target entity.
    * If so, instruct the target that an interaction should occur.
    * Considered tail recursive, but not treated that way.
    * @see `blockedFromInteracting`
    * @see `checkAllEnvironmentInteractions`
    * @see `waitOnOngoingInteraction`
    * @param obj the target entity
    * @return the function literal that represents the next iterative call of ongoing interaction testing;
    *         may return itself
    */
  def onStableEnvironment()(obj: PlanetSideServerObject, allow: Boolean): Any = {
    if(allow) {
      checkAllEnvironmentInteractions(obj) match {
        case Some(body) =>
          obj.Actor ! InteractWithEnvironment(obj, body, None)
          waitOnOngoingInteraction(obj.Zone, body)(_,_)
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
  def waitOnOngoingInteraction(zone: Zone, body: PieceOfEnvironment)(obj: PlanetSideServerObject, allow: Boolean): Any = {
    if (allow) {
      checkSpecificEnvironmentInteraction(zone, body)(obj) match {
        case None =>
          waitOnOngoingInteraction(zone, body)(_, _)
        case Some(_) =>
          checkAllEnvironmentInteractions(obj) match {
            case Some(newBody) if newBody.attribute == body.attribute =>
              waitOnOngoingInteraction(obj.Zone, newBody)(_, _)
            case Some(newBody) =>
              obj.Actor ! EscapeFromEnvironment(obj, body, None)
              obj.Actor ! InteractWithEnvironment(obj, newBody, None)
              waitOnOngoingInteraction(obj.Zone, newBody)(_, _)
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
  private def checkAllEnvironmentInteractions(obj: PlanetSideServerObject): Option[PieceOfEnvironment] = {
    val position = obj.Position
    val depth = GlobalDefinitions.MaxDepth(obj)
    obj.Zone.map.environment.find { body => body.attribute.canInteractWith(obj) && body.testInteraction(position, depth) }
  }

  /**
    * Test whether a special terrain component has an affect upon the target entity.
    * @param zone the zone in which the terrain is located
    * @param body the special terrain
    * @param obj the target entity
    * @return any unstable, interactive, or special terrain that is being interacted
    */
  private def checkSpecificEnvironmentInteraction(zone: Zone, body: PieceOfEnvironment)(obj: PlanetSideServerObject): Option[PieceOfEnvironment] = {
    if ((obj.Zone eq zone) && !body.testInteraction(obj.Position, GlobalDefinitions.MaxDepth(obj))) {
      Some(body)
    } else {
      None
    }
  }
}

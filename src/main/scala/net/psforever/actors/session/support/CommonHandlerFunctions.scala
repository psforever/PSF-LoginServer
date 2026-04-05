// Copyright (c) 2026 PSForever
package net.psforever.actors.session.support

import akka.actor.Actor.Receive
import net.psforever.objects.Player
import net.psforever.services.Service
import net.psforever.services.base.message.EventResponse
import net.psforever.types.PlanetSideGUID

trait HandlerFilter {
  def resolvedPlayerGuid: PlanetSideGUID
  def otherPlayerGuid: PlanetSideGUID
  def isNotSameTarget: Boolean
  def isSameTarget: Boolean

  def set(resolved: PlanetSideGUID, other: PlanetSideGUID, notSame: Boolean, same: Boolean): HandlerFilter
  def set(filter: HandlerFilter): HandlerFilter
}

class HandlerFilterRules extends HandlerFilter {
  var resolvedPlayerGuid: PlanetSideGUID = Service.defaultPlayerGUID
  var otherPlayerGuid: PlanetSideGUID = Service.defaultPlayerGUID
  var isNotSameTarget: Boolean = false
  var isSameTarget: Boolean = false

  def set(resolved: PlanetSideGUID, other: PlanetSideGUID, notSame: Boolean, same: Boolean): HandlerFilter = {
    resolvedPlayerGuid = resolved
    otherPlayerGuid = other
    isNotSameTarget = notSame
    isSameTarget = same
    this
  }

  def set(filter: HandlerFilter): HandlerFilter = {
    set(filter.resolvedPlayerGuid, filter.otherPlayerGuid, filter.isNotSameTarget, filter.isSameTarget)
  }
}

object HandlerFilter {
  def set(filter: HandlerFilter, guid: PlanetSideGUID, player: Player): HandlerFilter = {
    if (player != null && player.HasGUID) {
      val pguid = player.GUID
      filter.set(pguid, guid, pguid != guid, pguid == guid)
    } else {
      filter.set(Service.defaultPlayerGUID, guid, notSame = true, same = false)
    }
    filter
  }

  final val NeverAllow: HandlerFilter = new HandlerFilterRules().set(PlanetSideGUID(-1), PlanetSideGUID(-2), notSame = false, same = false)
}

trait CommonHandlerFunctionsBase {
  /**
   * na
   * @param toChannel na
   * @param guid      na
   * @param reply     na
   */
  def handle(toChannel: String, guid: PlanetSideGUID, reply: EventResponse): Unit

  def receive: Receive

  def isDefinedAt(x: Any): Boolean = receive.isDefinedAt(x)

  def tryToApply(x: Any): Boolean = {
    var passed = true
    receive.applyOrElse(x, (_: Any) => { passed = false })
    passed
  }
}

trait CommonHandlerFunctions extends CommonHandlerFunctionsBase {
  _: CommonSessionInterfacingFunctionality =>
  def resolvedGuid: PlanetSideGUID = sessionLogic.handlerFilter.resolvedPlayerGuid

  def filterGuid: PlanetSideGUID = sessionLogic.handlerFilter.otherPlayerGuid

  def isNotSameTarget: Boolean = sessionLogic.handlerFilter.isNotSameTarget

  def isSameTarget: Boolean = sessionLogic.handlerFilter.isSameTarget

  /**
   * na
   * @param toChannel na
   * @param guid      na
   * @param reply     na
   */
  def handle(toChannel: String, guid: PlanetSideGUID, reply: EventResponse): Unit = {
    HandlerFilter.set(sessionLogic.handlerFilter, guid, player)
    receive.apply(reply)
  }

  def receive: Receive
}

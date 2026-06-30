// Copyright (c) 2026 PSForever
package net.psforever.actors.session.support

import akka.actor.Actor.Receive
import net.psforever.objects.{Default, Player}
import net.psforever.services.base.message.EventResponse
import net.psforever.types.PlanetSideGUID

/**
 * A shared filter container utilized by all response handlers connected by a certain mode.
 * It is expected that the filters are treated in a true-oriented context -
 * always check `isNotSameTarget` and not `!isSameTarget`, etc..
 */
protected[support] class CommonHandlerFilters {
  var resolvedGuid: PlanetSideGUID = Default.GUID0
  var filterGuid: PlanetSideGUID = Default.GUID0
  var isNotSameTarget: Boolean = false
  var isSameTarget: Boolean = false

  def Configure(player: Player, guid: PlanetSideGUID): Unit = {
    filterGuid = guid
    if (player != null && player.HasGUID) {
      resolvedGuid = player.GUID
      isNotSameTarget = resolvedGuid != filterGuid
      isSameTarget = resolvedGuid == filterGuid
    } else {
      resolvedGuid = Default.GUID0
      isNotSameTarget = false
      isSameTarget = false
    }
  }
}

trait CommonHandlerFunctions {
  _: CommonSessionInterfacingFunctionality =>
  final def ResolvedGuid: PlanetSideGUID = sessionLogic.handlerFilters.resolvedGuid
  final def FilterGuid: PlanetSideGUID = sessionLogic.handlerFilters.filterGuid
  final def NotSameTarget: Boolean = sessionLogic.handlerFilters.isNotSameTarget
  final def SameTarget: Boolean = sessionLogic.handlerFilters.isSameTarget

  val NotSameTargetTest: () => Boolean = () => NotSameTarget

  val SameTargetTest: () => Boolean = () => SameTarget

  private var ignoreFilter: Boolean = false

  final def IgnoreFilter: Boolean = ignoreFilter

  final def IgnoreFilter_=(state: Boolean): Boolean = {
    ignoreFilter = state
    IgnoreFilter
  }

  /**
   * Process the output of a received response envelope.
   * Sets the response handler filter.
   * @param toChannel set of subscribers on an event system bus the envelope should reach
   * @param guid a specific subscriber endpoint to be excluded
   * @param reply output payload transported by this envelope
   * @return `true`, if the response was processed; `false`, otherwise
   */
  final def handle(toChannel: String, guid: PlanetSideGUID, reply: EventResponse): Boolean = {
    sessionLogic.handlerFilters.Configure(player, guid)
    tryToHandle(reply)
  }

  /**
   * Can the response handler process this message (with the guard boolean permissions set as they currently are).
   * @see `Actor.isDefinedAt`
   * @param x payload for processing
   * @return `true`, if the payload was processed; `false`, otherwise
   */
  def isDefinedAt(x: Any): Boolean = receive.isDefinedAt(x)

  /**
   * Process the output.
   * @param x payload for processing
   * @return `true`, if the payload was processed; `false`, otherwise
   */
  final def tryToHandle(x: Any): Boolean = {
    var passed = true
    receive.applyOrElse(x, (_: Any) => { passed = false })
    passed
  }

  /**
   * If ignoring guard booleans (filters), always pass.
   * If not, test filters using the provided function.
   * @param filter contained guard booleans
   * @return `true`. if ignoring filter tests or the filter test passed; `false`, otherwise
   */
  final def TestFilter(filter: () => Boolean): Boolean = {
    ignoreFilter || filter()
  }

  /**
   * @see `Actor.receive`
   */
  def receive: Receive
}

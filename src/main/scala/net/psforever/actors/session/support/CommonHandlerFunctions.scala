// Copyright (c) 2026 PSForever
package net.psforever.actors.session.support

import akka.actor.Actor.Receive
import net.psforever.objects.Default
import net.psforever.services.base.message.EventResponse
import net.psforever.types.PlanetSideGUID

trait CommonHandlerFunctions {
  _: CommonSessionInterfacingFunctionality =>
  protected var resolvedGuid: PlanetSideGUID = Default.GUID0
  protected var filterGuid: PlanetSideGUID = Default.GUID0
  protected var isNotSameTarget: Boolean = false
  protected var isSameTarget: Boolean = false

  private var ignoreFilter: Boolean = false

  def IgnoreFilter: Boolean = ignoreFilter

  def IgnoreFilter_=(state: Boolean): Boolean = {
    ignoreFilter = state
    IgnoreFilter
  }

  /**
   * na
   * @param toChannel na
   * @param guid      na
   * @param reply     na
   */
  def handle(toChannel: String, guid: PlanetSideGUID, reply: EventResponse): Boolean = {
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
    tryToHandle(reply)
  }

  def receive: Receive

  def isDefinedAt(x: Any): Boolean = receive.isDefinedAt(x)

  final def tryToHandle(x: Any): Boolean = {
    var passed = true
    receive.applyOrElse(x, (_: Any) => { passed = false })
    passed
  }

  def TestFilter(filter: Unit => Boolean): Boolean = {
    ignoreFilter || filter()
  }
}

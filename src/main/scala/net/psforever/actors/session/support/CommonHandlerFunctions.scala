// Copyright (c) 2026 PSForever
package net.psforever.actors.session.support

import akka.actor.Actor.Receive
import net.psforever.objects.Player
import net.psforever.services.Service
import net.psforever.services.base.message.EventResponse
import net.psforever.types.PlanetSideGUID

trait HandlerFilter {
  def resolvedPlayerGuid: PlanetSideGUID
  def isNotSameTarget: Boolean
  def isSameTarget: Boolean
}

case class HandlerFilterRules(
                               resolvedPlayerGuid: PlanetSideGUID,
                               isNotSameTarget: Boolean,
                               isSameTarget: Boolean
                             ) extends HandlerFilter

object HandlerFilter {
  def apply(guid: PlanetSideGUID, isNotSameTarget: Boolean): HandlerFilter = {
    HandlerFilterRules(guid, isNotSameTarget, !isNotSameTarget)
  }

  def apply(guid1: PlanetSideGUID, guid2: PlanetSideGUID): HandlerFilter = {
    val resolvedPlayerGuid: PlanetSideGUID = guid2
    val isNotSameTarget: Boolean = resolvedPlayerGuid != guid1
    this(guid2, isNotSameTarget)
  }

  def apply(guid: PlanetSideGUID, player: Player): HandlerFilter = {
    this(guid, if (player != null && player.HasGUID) {
      player.GUID
    } else {
      Service.defaultPlayerGUID
    })
  }

  final val NeverAllow: HandlerFilter = HandlerFilterRules(PlanetSideGUID(-1), isNotSameTarget = false, isSameTarget = false)

  final def Allow(guid: PlanetSideGUID): HandlerFilter = HandlerFilterRules(guid, isNotSameTarget = true, isSameTarget = true)
}

trait CommonHandlerFunctionsBase {
  /**
   * na
   * @param toChannel na
   * @param guid      na
   * @param reply     na
   */
  def handle(toChannel: String, guid: PlanetSideGUID, reply: EventResponse): Unit

  def handleWith(guid: PlanetSideGUID): Receive

  def handleWith(filter: HandlerFilter): Receive

  def receive: Receive
}

trait CommonHandlerFunctions extends CommonHandlerFunctionsBase {
  _: CommonSessionInterfacingFunctionality =>
  private var filter: HandlerFilter = HandlerFilter.NeverAllow

  def resolvedGuid: PlanetSideGUID = filter.resolvedPlayerGuid

  def isNotSameTarget: Boolean = filter.isNotSameTarget

  def isSameTarget: Boolean = filter.isSameTarget

  /**
   * na
   * @param toChannel na
   * @param guid      na
   * @param reply     na
   */
  def handle(toChannel: String, guid: PlanetSideGUID, reply: EventResponse): Unit = {
    filter = HandlerFilter(guid, player)
    receive.apply(reply)
  }

  def handleWith(guid: PlanetSideGUID): Receive = {
    filter = HandlerFilter(guid, player)
    receive
  }

  def handleWith(giveFilter: HandlerFilter): Receive = {
    filter = giveFilter
    receive
  }

  def receive: Receive
}

object CommonHandlerFunctions {
  val HandleNothing: CommonHandlerFunctionsBase = new CommonHandlerFunctionsBase {
    def handle(toChannel: String, guid: PlanetSideGUID, reply: EventResponse): Unit = { }
    def handleWith(guid: PlanetSideGUID): Receive = receive
    def handleWith(filter: HandlerFilter): Receive = receive
    def receive: Receive = { case _: CommonHandlerFunctions => () }
  }

  val HandleAnything: CommonHandlerFunctionsBase = new CommonHandlerFunctionsBase {
    def handle(toChannel: String, guid: PlanetSideGUID, reply: EventResponse): Unit = receive.apply(reply)
    def handleWith(guid: PlanetSideGUID): Receive = receive
    def handleWith(filter: HandlerFilter): Receive = receive
    def receive: Receive = { case _ => () }
  }
}

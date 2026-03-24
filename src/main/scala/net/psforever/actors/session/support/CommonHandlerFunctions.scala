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
  def isSameTarget: Boolean = !isNotSameTarget
}

class HandlerFilterClass(guid1: PlanetSideGUID, guid2: PlanetSideGUID) extends HandlerFilter {
  val resolvedPlayerGuid: PlanetSideGUID = guid2
  val isNotSameTarget: Boolean = resolvedPlayerGuid != guid1
}

object HandlerFilter {
  def apply(guid1: PlanetSideGUID, guid2: PlanetSideGUID): HandlerFilter = {
    new HandlerFilterClass(guid1, guid2)
  }

  def apply(guid: PlanetSideGUID, player: Player): HandlerFilter = {
    this(guid, if (player != null && player.HasGUID) {
      player.GUID
    } else {
      Service.defaultPlayerGUID
    })
  }

  final val NeverAllow: HandlerFilter = new HandlerFilter {
    def resolvedPlayerGuid: PlanetSideGUID = PlanetSideGUID(-1)
    def isNotSameTarget: Boolean = false
    override def isSameTarget: Boolean = false
  }

  final def Allow(guid: PlanetSideGUID): HandlerFilter = new HandlerFilter {
    def resolvedPlayerGuid: PlanetSideGUID = guid
    def isNotSameTarget: Boolean = true
    override def isSameTarget: Boolean = true
  }
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
  protected var filter: HandlerFilter = HandlerFilter.NeverAllow

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

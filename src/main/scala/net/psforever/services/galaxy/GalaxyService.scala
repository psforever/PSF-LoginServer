// Copyright (c) 2017 PSForever
package net.psforever.services.galaxy

import akka.actor.Actor
import net.psforever.services.base.GenericEventBus
import net.psforever.services.Service

class GalaxyService extends Actor {
  private[this] val log = org.log4s.getLogger

  val GalaxyEvents = new GenericEventBus[GalaxyServiceResponse]

  private def correctChannelName(channel: String): String = {
    if (channel.isEmpty) {
      s"/Galaxy"
    } else {
      s"/$channel/Galaxy"
    }
  }

  def receive: Receive = {
    case Service.Join(channel) =>
      val path = correctChannelName(channel)
      GalaxyEvents.subscribe(sender(), path)

    case Service.Leave(None) =>
      GalaxyEvents.unsubscribe(sender())

    case Service.Leave(Some(channel)) =>
      val path = s"/$channel/Galaxy"
      GalaxyEvents.unsubscribe(sender(), path)

    case Service.LeaveAll() =>
      GalaxyEvents.unsubscribe(sender())

    case msg @ GalaxyServiceMessage(forChannel, _) =>
      GalaxyEvents.publish(msg.response(correctChannelName(forChannel)))

    case msg =>
      log.warn(s"Unhandled message $msg from ${sender()}")
  }
}

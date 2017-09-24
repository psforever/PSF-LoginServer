// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor

/**
  * na
  * @param zone the `Zone` governed by this `Actor`
  */
class ZoneActor(zone : Zone) extends Actor {
  private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case Zone.Init() =>
      zone.Init

    case msg =>
      log.warn(s"Received unexpected message - $msg")
  }
}

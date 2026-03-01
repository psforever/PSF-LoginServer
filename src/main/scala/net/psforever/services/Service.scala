// Copyright (c) 2017 PSForever
package net.psforever.services

import akka.actor.ActorRef
import net.psforever.types.PlanetSideGUID

object Service {
  final val defaultPlayerGUID: PlanetSideGUID = PlanetSideGUID(0)

  final case class Startup()

  final case class Join(channel: String, sendJoinConfirmation: Boolean)

  object Join {
    def apply(channel: String): Join = Join(channel, sendJoinConfirmation = false)
  }

  final case class JoinConfirmation(eventSystem: ActorRef, channel: String)

  final case class Leave(channel: Option[String] = None)

  final case class LeaveAll()
}

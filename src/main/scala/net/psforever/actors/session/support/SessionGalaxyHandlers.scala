// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.packet.game.FriendsResponse
import net.psforever.services.base.EventResponse

import scala.annotation.unused
//
import net.psforever.actors.session.AvatarActor

trait GalaxyHandlerFunctions extends CommonSessionInterfacingFunctionality {
  def ops: SessionGalaxyHandlers

  def handleUpdateIgnoredPlayers(pkt: FriendsResponse): Unit

  def handle(@unused reply: EventResponse): Unit
}

class SessionGalaxyHandlers(
                             val sessionLogic: SessionData,
                             val avatarActor: typed.ActorRef[AvatarActor.Command],
                             val galaxyService: ActorRef,
                             implicit val context: ActorContext
                           ) extends CommonSessionInterfacingFunctionality

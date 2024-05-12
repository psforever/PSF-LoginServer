// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.ActorContext
import net.psforever.services.local.LocalResponse
import net.psforever.types.PlanetSideGUID

trait LocalHandlerFunctions extends CommonSessionInterfacingFunctionality {
  def ops: SessionLocalHandlers

  def handle(toChannel: String, guid: PlanetSideGUID, reply: LocalResponse.Response): Unit
}

class SessionLocalHandlers(
                            val sessionLogic: SessionData,
                            implicit val context: ActorContext
                          ) extends CommonSessionInterfacingFunctionality

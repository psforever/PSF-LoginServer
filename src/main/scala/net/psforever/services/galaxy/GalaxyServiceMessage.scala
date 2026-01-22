// Copyright (c) 2017 PSForever
package net.psforever.services.galaxy

import net.psforever.services.Service
import net.psforever.services.base.{EventMessage, GenericMessageEnvelope}
import net.psforever.types.PlanetSideGUID

final case class GalaxyServiceMessage(channel: String, msg: EventMessage)
  extends GenericMessageEnvelope {
  def exclude: PlanetSideGUID = Service.defaultPlayerGUID

  def response(outChannel: String): GalaxyServiceResponse = {
    GalaxyServiceResponse(outChannel, msg.response())
  }
}

object GalaxyServiceMessage {
  def apply(actionMessage: EventMessage): GalaxyServiceMessage = GalaxyServiceMessage("", actionMessage)
}

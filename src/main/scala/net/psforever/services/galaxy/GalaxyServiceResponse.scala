// Copyright (c) 2017 PSForever
package net.psforever.services.galaxy

import net.psforever.types.PlanetSideGUID
import net.psforever.services.Service
import net.psforever.services.base.message.EventResponse
import net.psforever.services.base.EventSystemStamp
import net.psforever.services.base.envelope.GenericResponseEnvelope

final case class GalaxyServiceResponse(channel: String, reply: EventResponse)
    extends GenericResponseEnvelope {
  def filter: PlanetSideGUID = Service.defaultPlayerGUID

  def stamp: EventSystemStamp = GalaxyStamp
}

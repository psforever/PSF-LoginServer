// Copyright (c) 2017 PSForever
package net.psforever.services.galaxy

import net.psforever.types.PlanetSideGUID
import net.psforever.services.Service
import net.psforever.services.base.{EventResponse, GenericResponseEnvelope}

final case class GalaxyServiceResponse(channel: String, reply: EventResponse)
    extends GenericResponseEnvelope {
  def filter: PlanetSideGUID = Service.defaultPlayerGUID
}

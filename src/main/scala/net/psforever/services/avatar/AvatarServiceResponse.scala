// Copyright (c) 2017 PSForever
package net.psforever.services.avatar

import net.psforever.services.base.message.EventResponse
import net.psforever.types.PlanetSideGUID
import net.psforever.services.base.EventSystemStamp
import net.psforever.services.base.envelope.GenericResponseEnvelope

final case class AvatarServiceResponse(
                                        channel: String,
                                        filter: PlanetSideGUID,
                                        reply: EventResponse
                                      ) extends GenericResponseEnvelope {
  def stamp: EventSystemStamp = AvatarStamp
}

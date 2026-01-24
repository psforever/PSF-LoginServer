// Copyright (c) 2017 PSForever
package net.psforever.services.avatar

import net.psforever.types.PlanetSideGUID
import net.psforever.services.base.{EventResponse, GenericResponseEnvelope}

final case class AvatarServiceResponse(
                                        channel: String,
                                        filter: PlanetSideGUID,
                                        reply: EventResponse
                                      ) extends GenericResponseEnvelope

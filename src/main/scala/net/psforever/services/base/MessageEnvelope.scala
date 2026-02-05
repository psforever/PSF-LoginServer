// Copyright (c) 2026 PSForever
package net.psforever.services.base

import net.psforever.types.PlanetSideGUID

case class MessageEnvelope(channel: String, filter: PlanetSideGUID, msg: EventMessage)
  extends GenericMessageEnvelope

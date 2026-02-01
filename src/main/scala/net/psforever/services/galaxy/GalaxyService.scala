// Copyright (c) 2017-2026 PSForever
package net.psforever.services.galaxy

import net.psforever.services.base.{GenericEventService, GenericMessageEnvelope}

class GalaxyService
  extends GenericEventService[GalaxyServiceResponse](busName = "Galaxy") {
  protected def composeResponseEnvelope(msg: GenericMessageEnvelope): GalaxyServiceResponse = {
    GalaxyServiceResponse(formatChannelOnBusName(msg.channel), msg.msg.response())
  }

  override protected def formatChannelOnBusName(channel: String): String = {
    if (channel.trim.isEmpty) {
      s"/$BusName"
    } else {
      s"/$channel/$BusName"
    }
  }
}

// Copyright (c) 2017-2026 PSForever
package net.psforever.services.galaxy

import net.psforever.services.base.{EventSystemStamp, GenericEventService}

case object GalaxyStamp extends EventSystemStamp

class GalaxyService
  extends GenericEventService(stamp = GalaxyStamp) {
  override protected def formatChannel(channel: String): String = {
    if (channel.trim.isEmpty) {
      "/all"
    } else {
      s"/$channel"
    }
  }
}

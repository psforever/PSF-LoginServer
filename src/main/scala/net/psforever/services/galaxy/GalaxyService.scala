// Copyright (c) 2017-2026 PSForever
package net.psforever.services.galaxy

import net.psforever.services.base.{EventSystemStamp, GenericEventService}

case object GalaxyStamp extends EventSystemStamp {
  override def routing(channel: String): String = {
    if (channel.trim.isEmpty) {
      "/out"
    } else {
      s"/$channel/out"
    }
  }
}

class GalaxyService
  extends GenericEventService(stamp = GalaxyStamp)

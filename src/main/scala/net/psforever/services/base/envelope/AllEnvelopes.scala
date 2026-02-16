// Copyright (c) 2026 PSForever
package net.psforever.services.base.envelope

import net.psforever.types.PlanetSideGUID

trait AllEnvelopes {
  def channel: String
  def filter: PlanetSideGUID
}

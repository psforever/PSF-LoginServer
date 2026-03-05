// Copyright (c) 2026 PSForever
package net.psforever.services.base.envelope

import net.psforever.types.PlanetSideGUID

/**
 * Base of all envelope classes.
 * Defines a channel and a filter, both of which server the purpose of routing the message to its destination.
 */
trait AllEnvelopes {
  /** set of subscribers on an event system bus that the envelope should reach */
  def channel: String
  /** specific subscriber endpoint to be excluded (the subscriber should filter themselves upon receipt) */
  def filter: PlanetSideGUID
}

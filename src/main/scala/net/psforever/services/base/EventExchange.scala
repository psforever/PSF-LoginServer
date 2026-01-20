// Copyright (c) 2026 PSForever
package net.psforever.services.base

trait EventExchange

trait EventResponse extends EventExchange

trait EventMessage extends EventExchange {
  def response(): EventResponse
}

trait SelfResponseMessage
  extends EventMessage
    with EventResponse {
  def response(): EventResponse = this
}

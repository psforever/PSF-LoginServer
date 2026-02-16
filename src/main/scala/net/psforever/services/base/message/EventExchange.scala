// Copyright (c) 2026 PSForever
package net.psforever.services.base.message

trait EventResponse

trait EventMessage {
  def response(): EventResponse
}

trait SelfRespondingEvent
  extends EventMessage
    with EventResponse {
  def response(): EventResponse = this
}

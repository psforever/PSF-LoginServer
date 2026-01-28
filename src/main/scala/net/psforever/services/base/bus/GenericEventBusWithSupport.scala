// Copyright (c) 2026 PSForever
package net.psforever.services.base.bus

import scala.annotation.unused

trait GenericEventBusResponseToSupport
  extends GenericEventBusResponse {
  def supportLabel: String
  def supportMessage: Any
}

trait GenericEventBusResponseToSupportOnly
  extends GenericEventBusResponseToSupport {
  def channel: String = ""
}

trait GenericEventBusWithSupport[T <: GenericEventBusResponse] {
  bus: GenericEventBus[T] =>
  def publishingWithSupport(event: T): Unit = {
    event match {
      case msg: GenericEventBusResponseToSupportOnly =>
        forwardToExternalSupport(msg)
      case msg: GenericEventBusResponseToSupport =>
        forwardToExternalSupport(msg)
        bus.truePublish(event)
      case _ =>
        bus.truePublish(event)
    }
  }

  def forwardToExternalSupport(@unused msg: GenericEventBusResponseToSupport): Unit
}

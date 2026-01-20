// Copyright (c) 2017 PSForever
package net.psforever.services.base

import akka.event.{ActorEventBus, SubchannelClassification}
import akka.util.Subclassification

trait GenericEventBusMsg {
  def channel: String
}

class GenericEventBus[A <: GenericEventBusMsg] extends ActorEventBus with SubchannelClassification {
  type Event = A
  type Classifier = String

  protected def classify(event: Event): Classifier = event.channel

  protected def subclassification: Subclassification[String] =
    new Subclassification[Classifier] {
      def isEqual(x: Classifier, y: Classifier): Boolean = x == y

      def isSubclass(x: Classifier, y: Classifier): Boolean = x.startsWith(y)
    }

  protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event
  }
}

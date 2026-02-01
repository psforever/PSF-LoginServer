// Copyright (c) 2017 PSForever
package net.psforever.services.base.bus

import akka.event.{ActorEventBus, SubchannelClassification}
import akka.util.Subclassification
import net.psforever.types.PlanetSideGUID

trait AllGenericBusMsg {
  def channel: String
  def filter: PlanetSideGUID
}

trait GenericEventBusResponse
  extends AllGenericBusMsg

class GenericEventBus[A <: GenericEventBusResponse]
  extends ActorEventBus with SubchannelClassification {
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

  override def publish(event: Event): Unit = {
    truePublish(event)
  }

  def truePublish(event: Event): Unit = {
    super[SubchannelClassification].publish(event)
  }
}

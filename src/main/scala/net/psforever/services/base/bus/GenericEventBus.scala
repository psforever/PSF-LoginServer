// Copyright (c) 2017 PSForever
package net.psforever.services.base.bus

import akka.event.{ActorEventBus, SubchannelClassification}
import akka.util.Subclassification
import net.psforever.services.base.envelope.GenericResponseEnvelope

/**
 * Maintain a list of endpoints that have subscribed to a specific perspective
 * internally called a `Classifier` and externally referred to as a "channel".
 * When an `Event` - externally referred colloquially as a "message" - is received,
 * to be published,
 * match the "channel" to a list of known `Classifier` endpoints.
 * Each of these `Subscribers` should receive the message.
 * @see `GenericResponseEnvelope`
 * @see `ActorEventBus.Subscriber`
 */
class GenericEventBus
  extends ActorEventBus with SubchannelClassification {
  type Event = GenericResponseEnvelope
  type Classifier = String

  protected def classify(event: Event): Classifier = event.channel

  /*
  Example Classifiers: "foo", "foo.fizz", and "foo.buzz"
  In general, Classifier channels will perform left-pattern matching.
  "foo" will publish to "foo", "foo.fizz", and "foo.buzz"
   */
  protected def subclassification: Subclassification[String] =
    new Subclassification[Classifier] {
      def isEqual(x: Classifier, y: Classifier): Boolean = x == y

      def isSubclass(x: Classifier, y: Classifier): Boolean = x.startsWith(y)
    }

  override def publish(event: Event): Unit = {
    super[SubchannelClassification].publish(event)
  }

  protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event
  }
}

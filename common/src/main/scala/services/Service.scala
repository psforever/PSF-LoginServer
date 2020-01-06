// Copyright (c) 2017 PSForever
package services

import akka.event.{ActorEventBus, SubchannelClassification}
import akka.util.Subclassification
import net.psforever.types.PlanetSideGUID

object Service {
  final val defaultPlayerGUID : PlanetSideGUID = PlanetSideGUID(0)

  final case class Startup()
  final case class Join(channel : String)
  final case class Leave(channel : Option[String] = None)
  final case class LeaveAll()
}

trait GenericEventBusMsg {
  def toChannel : String
}

class GenericEventBus[A <: GenericEventBusMsg] extends ActorEventBus with SubchannelClassification {
  type Event = A
  type Classifier = String

  protected def classify(event: Event): Classifier = event.toChannel

  protected def subclassification = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier) = x == y
    def isSubclass(x: Classifier, y: Classifier) = x.startsWith(y)
  }

  protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event
  }
}

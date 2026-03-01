package service.base

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import net.psforever.services.Service
import net.psforever.services.base.message.SelfRespondingEvent
import net.psforever.services.base.{EventServiceSupport, EventSystemStamp, GenericSupportEnvelope}
import net.psforever.types.PlanetSideGUID

object EventServiceTestBase {
  case object TestStamp extends EventSystemStamp

  final case class TestMessage(value: Int) extends SelfRespondingEvent

  final case class SupportActorRepliesWith(msg: String, sendTo: ActorRef) extends SelfRespondingEvent

  class TestSupportActor extends Actor {
    def receive: Receive = {
      case SupportActorRepliesWith(msg, sendTo) =>
        sendTo ! msg
      case _ => ()
    }
  }

  case object TestSupportActorLoader extends EventServiceSupport {
    def label: String = "supportActor"
    def constructor(context: ActorContext): ActorRef = {
      context.actorOf(Props[TestSupportActor](), name = "supportActor")
    }
  }

  final case class TestSupportEnvelope(
      originalChannel: String,
      msg: SupportActorRepliesWith
  ) extends GenericSupportEnvelope {
    def filter: PlanetSideGUID = Service.defaultPlayerGUID
    def supportLabel: String   = "supportActor"
    def supportMessage: Any    = msg
  }
}

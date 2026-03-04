// Copyright (c) 2026 PSForever
package service.base

import net.psforever.services.base.EventSystemStamp
import net.psforever.services.base.envelope._
import net.psforever.services.base.message.{EventMessage, EventResponse, SelfRespondingEvent}
import net.psforever.types.PlanetSideGUID
import org.specs2.mutable.Specification

object EnvelopeTest {
  case object TestStamp extends EventSystemStamp

  val TestFilter: PlanetSideGUID = PlanetSideGUID(1)

  final def StringWithSlashes(str: String): String = s"/$str/"

  final case class TestMessage(value: Int) extends SelfRespondingEvent

  final case class TestOutputEvent(value: Int) extends EventResponse

  final case class TestInputMessage(value: Int) extends EventMessage {
    def response(): EventResponse = TestOutputEvent(value + 2)
  }
}

class EnvelopeTest extends Specification {
  import EnvelopeTest._

  "MessageEnvelope" should {
    "construct" in {
      MessageEnvelope("test", TestFilter, TestMessage(5))
      ok
    }

    "message match" in {
      val input = MessageEnvelope("test", TestFilter, TestMessage(5))
      input match {
        case GenericMessageEnvelope("test", TestFilter, TestMessage(5)) =>
          ok
        case _ =>
          ko
      }
    }

    "response (no stamp)" in {
      val input = MessageEnvelope("test", TestFilter, TestMessage(5))
      input match {
        case reply @ GenericResponseEnvelope("test", TestFilter, NoReply) =>
          reply.stamp mustEqual Undelivered
        case _ =>
          ko
      }
    }

    "response" in {
      val input = MessageEnvelope("test", TestFilter, TestMessage(5))
      val output = input.response(TestStamp)
      output match {
        case reply @ GenericResponseEnvelope("/test/out", TestFilter, TestMessage(5)) =>
          reply.stamp mustEqual TestStamp
        case _ =>
          ko
      }
    }

    "response (different from input)" in {
      val input = MessageEnvelope("test", TestFilter, TestInputMessage(5))
      val output = input.response(TestStamp)
      output match {
        case reply @ GenericResponseEnvelope("/test/out", TestFilter, TestOutputEvent(7)) =>
          reply.stamp mustEqual TestStamp
        case _ =>
          ko
      }
    }
  }

  "GenericResponseEnvelope" should {
    "construct (quick)" in {
      val input = GenericResponseEnvelope(TestStamp, "test", TestFilter, TestMessage(5))
      input match {
        case reply @ GenericResponseEnvelope("/test/out", TestFilter, TestMessage(5)) =>
          reply.stamp mustEqual TestStamp
        case _ =>
          ko
      }
    }
  }
}

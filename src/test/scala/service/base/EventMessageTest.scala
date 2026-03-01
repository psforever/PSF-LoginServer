// Copyright (c) 2026 PSForever
package service.base

import net.psforever.services.base.message.{EventMessage, EventResponse, SelfRespondingEvent}
import org.specs2.mutable.Specification

object EventMessageTest {
  final case class TestOutputEvent(value: Int) extends EventResponse

  final case class TestInputMessage(value: Int) extends EventMessage {
    def response(): EventResponse = TestOutputEvent(value + 2)
  }

  final case class TestMessage(value: Int) extends SelfRespondingEvent
}

class EventMessageTest extends Specification {
  import EventMessageTest._

  "EventMessage" should {
    "construct" in {
      TestInputMessage(5)
      ok
    }

    "response" in {
      TestInputMessage(5).response() mustEqual TestOutputEvent(7)
    }
  }

  "SelfResponseMessage" should {
    "construct" in {
      TestMessage(5)
      ok
    }

    "response (is same as self)" in {
      val msg = TestMessage(5)
      msg.response() mustEqual msg
    }
  }
}

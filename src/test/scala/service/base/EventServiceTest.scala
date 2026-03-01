// Copyright (c) 2026 PSForever
package service.base

import akka.actor.Props
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.services.Service
import net.psforever.services.base.GenericEventService
import net.psforever.services.base.envelope.GenericResponseEnvelope

import scala.concurrent.duration._

object EventServiceTest {
  class TestService() extends GenericEventService(EventServiceTestBase.TestStamp)
}

class EventServiceTestDefault extends ActorTest {
  import EventServiceTest._
  "GenericEventSystem" should {
    "construct" in {
      system.actorOf(Props[TestService](), name = "EventServiceTest.0")
    }
  }
}

class EventServiceTest1 extends ActorTest {
  import EventServiceTest._
  "GenericEventSystem" should {
    "be subscribed to by channel name" in {
      val probe = TestProbe("testProbe")
      val events = system.actorOf(Props[TestService](), name = "EventServiceTest.1")
      events.tell(Service.Join("test"), probe.ref)
      probe.expectNoMessage(100 milliseconds)
    }
  }
}

class EventServiceTest2 extends ActorTest {
  import EventServiceTest._
  import EventServiceTestBase._
  "GenericEventSystem" should {
    "receive messages from a subscribed channel" in {
      val probe = TestProbe("testProbe")
      val events = system.actorOf(Props[TestService](), name = "EventServiceTest.2")
      events.tell(Service.Join("test"), probe.ref)
      events ! MessageEnvelope("test", Service.defaultPlayerGUID, TestMessage(5))
      probe.receiveN(1, 100 milliseconds)
    }
  }
}

class EventServiceTest3 extends ActorTest {
  import EventServiceTest._
  import EventServiceTestBase._
  "GenericEventSystem" should {
    "receive messages that are responses to the original message from a subscribed channel" in {
      val probe = TestProbe("testProbe")
      val events = system.actorOf(Props[TestService](), name = "EventServiceTest.3")
      val msg = MessageEnvelope("test", Service.defaultPlayerGUID, TestMessage(5))
      // s => "/$s" is the default channel manipulation of the event system
      val formalReply = msg.response(EventServiceTestBase.TestStamp, s => "/$s")
      events.tell(Service.Join("test"), probe.ref)
      events ! MessageEnvelope("test", Service.defaultPlayerGUID, TestMessage(5))
      val reply = probe.receiveOne(100 milliseconds)
      assert(reply == formalReply, "(1) message expected but not received format")
    }
  }
}

class EventServiceTest4 extends ActorTest {
  import EventServiceTest._
  import EventServiceTestBase._
  "GenericEventSystem" should {
    "not receive messages from an unsubscribed channel" in {
      val probe = TestProbe("testProbe")
      val missedProbe = TestProbe("testProbe")
      val events = system.actorOf(Props[TestService](), name = "EventServiceTest.4")
      events.tell(Service.Join("test"), probe.ref)
      events.tell(Service.Join("notATest"), missedProbe.ref)
      events ! MessageEnvelope("test", Service.defaultPlayerGUID, TestMessage(5))
      val reply = probe.receiveOne(100 milliseconds)
      reply match {
        case GenericResponseEnvelope("/test", _, TestMessage(5)) => ()
        case _ => assert(false, "(2) message expected but not received")
      }
      missedProbe.expectNoMessage(100 milliseconds)
    }
  }
}

class EventServiceTest5 extends ActorTest {
  import EventServiceTest._
  import EventServiceTestBase._
  "GenericEventSystem" should {
    "ignore unexpected messages" in {
      val probe = TestProbe("testProbe")
      val events = system.actorOf(Props[TestService](), name = "EventServiceTest.5")
      events.tell(Service.Join("test"), probe.ref)
      events ! TestMessage(5)
      probe.expectNoMessage(250 milliseconds)
      //the warn log should show something
    }
  }
}

class EventServiceTest6 extends ActorTest {
  import EventServiceTest._
  import EventServiceTestBase._
  "GenericEventSystem" should {
    "leave single channels" in {
      val probe = TestProbe("testProbe")
      val events = system.actorOf(Props[TestService](), name = "EventServiceTest.6")
      events.tell(Service.Join("test"), probe.ref)
      events.tell(Service.Join("anotherTest"), probe.ref)

      events ! MessageEnvelope("test", Service.defaultPlayerGUID, TestMessage(5))
      events ! MessageEnvelope("anotherTest", Service.defaultPlayerGUID, TestMessage(5))
      val reply1 = probe.receiveN(2, 100 milliseconds)
      reply1.head match {
        case GenericResponseEnvelope("/test", _, _) => ()
        case _ => assert(false, "(3) message expected but not received")
      }
      reply1(1) match {
        case GenericResponseEnvelope("/anotherTest", _, _) => ()
        case _ => assert(false, "(4) message expected but not received")
      }

      events.tell(Service.Leave(Some("anotherTest")), probe.ref)
      events ! MessageEnvelope("test", Service.defaultPlayerGUID, TestMessage(5))
      events ! MessageEnvelope("anotherTest", Service.defaultPlayerGUID, TestMessage(6))
      val reply2 = probe.receiveOne(100 milliseconds)
      reply2 match {
        case GenericResponseEnvelope("/test", _, TestMessage(5)) => ()
        case _ => assert(false, "(5) message expected but not received")
      }
      probe.expectNoMessage(250 milliseconds)
    }
  }
}

class EventServiceTest7 extends ActorTest {
  import EventServiceTest._
  import EventServiceTestBase._
  "GenericEventSystem" should {
    "leave all channels (1)" in {
      val probe = TestProbe("testProbe")
      val events = system.actorOf(Props[TestService](), name = "EventServiceTest.7")
      events.tell(Service.Join("test"), probe.ref)
      events.tell(Service.Join("anotherTest"), probe.ref)

      events ! MessageEnvelope("test", Service.defaultPlayerGUID, TestMessage(5))
      events ! MessageEnvelope("anotherTest", Service.defaultPlayerGUID, TestMessage(6))
      val reply = probe.receiveN(2,100 milliseconds)
      reply.head match {
        case GenericResponseEnvelope("/test", _, TestMessage(5)) => ()
        case _ => assert(false, "(6) message expected but not received")
      }
      reply(1) match {
        case GenericResponseEnvelope("/anotherTest", _, TestMessage(6)) => ()
        case _ => assert(false, "(7) message expected but not received")
      }

      events.tell(Service.Leave(), probe.ref)
      events ! MessageEnvelope("test", Service.defaultPlayerGUID, TestMessage(5))
      events ! MessageEnvelope("anotherTest", Service.defaultPlayerGUID, TestMessage(6))
      probe.expectNoMessage(250 milliseconds)
    }
  }
}

class EventServiceTest8 extends ActorTest {
  import EventServiceTest._
  import EventServiceTestBase._
  "GenericEventSystem" should {
    "leave all channels" in {
      val probe = TestProbe("testProbe")
      val events = system.actorOf(Props[TestService](), name = "EventServiceTest.8")
      events.tell(Service.Join("test"), probe.ref)
      events.tell(Service.Join("anotherTest"), probe.ref)

      events ! MessageEnvelope("test", Service.defaultPlayerGUID, TestMessage(5))
      events ! MessageEnvelope("anotherTest", Service.defaultPlayerGUID, TestMessage(6))
      val reply = probe.receiveN(2,100 milliseconds)
      reply.head match {
        case GenericResponseEnvelope("/test", _, TestMessage(5)) => ()
        case _ => assert(false, "(6) message expected but not received")
      }
      reply(1) match {
        case GenericResponseEnvelope("/anotherTest", _, TestMessage(6)) => ()
        case _ => assert(false, "(7) message expected but not received")
      }

      events.tell(Service.LeaveAll(), probe.ref)
      events ! MessageEnvelope("test", Service.defaultPlayerGUID, TestMessage(5))
      events ! MessageEnvelope("anotherTest", Service.defaultPlayerGUID, TestMessage(6))
      probe.expectNoMessage(250 milliseconds)
    }
  }
}

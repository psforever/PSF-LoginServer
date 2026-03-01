// Copyright (c) 2026 PSForever
package service.base

import akka.actor.Props
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.services.Service
import net.psforever.services.base.{EventServiceSupport, GenericEventServiceWithSupport, GenericSupportEnvelopeOnly}

import scala.concurrent.duration._

object EventServiceSupportTest {
  class TestSupportService(eventSupportServices: List[EventServiceSupport])
    extends GenericEventServiceWithSupport(EventServiceTestBase.TestStamp, eventSupportServices)

  final case class TestSupportOnlyEnvelope(supportLabel: String, supportMessage: EventServiceTestBase.SupportActorRepliesWith)
    extends GenericSupportEnvelopeOnly
}

class EventServiceSupportTestDefault extends ActorTest {
  import EventServiceSupportTest._
  "GenericEventServiceWithSupport" should {
    "construct" in {
      system.actorOf(Props(classOf[TestSupportService], List()), name = "EventServiceSupportTest.0")
    }
  }
}

class EventServiceSupportTestLoadSupportClass extends ActorTest {
  import EventServiceSupportTest._
  import EventServiceTestBase._
  "GenericEventServiceWithSupport" should {
    "construct with setting up a test support class" in {
      system.actorOf(Props(classOf[TestSupportService], List(TestSupportActorLoader)), name = "EventServiceSupportTest.1")
    }
  }
}

class EventServiceSupportTestSendToSupportClass extends ActorTest {
  import EventServiceSupportTest._
  import EventServiceTestBase._
  "GenericEventServiceWithSupport" should {
    "send a valid message to both subscribed channel and support class" in {
      val mainProbe = TestProbe("MainProbe")
      val supportProbe = TestProbe("SupportProbe")
      val events = system.actorOf(Props(classOf[TestSupportService], List(TestSupportActorLoader)), name = "EventServiceSupportTest.2")
      events.tell(Service.Join("test"), mainProbe.ref)
      val originalMessage = TestSupportEnvelope("test", SupportActorRepliesWith("hello world", supportProbe.ref))
      events ! originalMessage
      //main reply
      val mainReply = mainProbe.receiveOne(250 milliseconds)
      mainReply match {
        case msg if msg == originalMessage => ()
        case badmsg =>
          assert(false, s"(1) expected delivery of test envelope, but received $badmsg instead")
      }
      //support reply
      val supportReply = supportProbe.receiveOne(250 milliseconds)
      supportReply match {
        case msg: String if msg.equals("hello world") => ()
        case badmsg =>
          assert(false, s"(2) expected delivery of test envelope payload 'hello world', but received '$badmsg' instead")
      }
    }
  }
}

class EventServiceSupportTestSendToSupportClassOnly1 extends ActorTest {
  import EventServiceSupportTest._
  import EventServiceTestBase._
  "GenericEventServiceWithSupport" should {
    "send a valid message to support class (but not to subscribed channel)" in {
      val mainProbe = TestProbe("MainProbe")
      val supportProbe = TestProbe("SupportProbe")
      val events = system.actorOf(Props(classOf[TestSupportService], List(TestSupportActorLoader)), name = "EventServiceSupportTest.3")
      events.tell(Service.Join("test"), mainProbe.ref)
      val originalMessage = TestSupportEnvelope("notatest", SupportActorRepliesWith("hello world", supportProbe.ref))
      events ! originalMessage
      //main reply (no reply)
      mainProbe.expectNoMessage(500 milliseconds)
      //support reply
      val supportReply = supportProbe.receiveOne(250 milliseconds)
      supportReply match {
        case msg: String if msg.equals("hello world") => ()
        case badmsg =>
          assert(false, s"(3) expected delivery of test envelope payload 'hello world', but received '$badmsg' instead")
      }
    }
  }
}

class EventServiceSupportTestSendToSupportClassOnly2 extends ActorTest {
  import EventServiceSupportTest._
  import EventServiceTestBase._
  "GenericEventServiceWithSupport" should {
    "send a valid message to support class (skip subscribed channel)" in {
      val mainProbe = TestProbe("MainProbe")
      val supportProbe = TestProbe("SupportProbe")
      val events = system.actorOf(Props(classOf[TestSupportService], List(TestSupportActorLoader)), name = "EventServiceSupportTest.4")
      events.tell(Service.Join("test"), mainProbe.ref)
      val originalMessage = TestSupportOnlyEnvelope("supportActor", SupportActorRepliesWith("hello world", supportProbe.ref))
      events ! originalMessage
      //main reply (no reply)
      mainProbe.expectNoMessage(500 milliseconds)
      //support reply
      val supportReply = supportProbe.receiveOne(250 milliseconds)
      supportReply match {
        case msg: String if msg.equals("hello world") => ()
        case badmsg =>
          assert(false, s"(4) expected delivery of test envelope payload 'hello world', but received '$badmsg' instead")
      }
    }
  }
}

class EventServiceSupportTestSendToNoOne extends ActorTest {
  import EventServiceSupportTest._
  import EventServiceTestBase._
  "GenericEventServiceWithSupport" should {
    "send a valid message that neither support class nor subscribed channel handle" in {
      val mainProbe = TestProbe("MainProbe")
      val supportProbe = TestProbe("SupportProbe")
      val events = system.actorOf(Props(classOf[TestSupportService], List(TestSupportActorLoader)), name = "EventServiceSupportTest.5")
      events.tell(Service.Join("test"), mainProbe.ref)
      val originalMessage = TestSupportOnlyEnvelope("notASupportActor", SupportActorRepliesWith("hello world", supportProbe.ref))
      events ! originalMessage
      //main reply (no reply)
      mainProbe.expectNoMessage(500 milliseconds)
      //support reply (no reply)
      supportProbe.expectNoMessage(500 milliseconds)
    }
  }
}

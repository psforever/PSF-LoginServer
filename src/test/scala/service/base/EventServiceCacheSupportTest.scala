// Copyright (c) 2026 PSForever
package service.base

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.services.Service
import net.psforever.services.base.message.EventMessage
import net.psforever.services.base.{CachedEnvelope, CachedGenericEventEnvelope, EventServiceSupport, GenericEventServiceWithCacheAndSupport, GenericSupportEnvelope}
import net.psforever.types.PlanetSideGUID
import service.base.EventServiceSupportTest.TestSupportService

import scala.concurrent.duration._

object EventServiceCacheSupportTest {
  final case class CachedSupportTestEnvelope(
                                              guid: PlanetSideGUID,
                                              originalChannel: String,
                                              override val msg: EventMessage,
                                              supportMessage: Any
                                            ) extends CachedGenericEventEnvelope with GenericSupportEnvelope {
    assert(guid != Service.defaultPlayerGUID, "can not cache message under default GUID")
    def filter: PlanetSideGUID = Service.defaultPlayerGUID
    def supportLabel: String = "supportActor"
  }

  class TestCacheService(eventSupportServices: List[EventServiceSupport])
    extends GenericEventServiceWithCacheAndSupport(EventServiceTestBase.TestStamp, eventSupportServices)

  def SpawnTestSystem(eventSupportServices: List[EventServiceSupport])(implicit system: ActorSystem, self: ActorRef): ActorRef = {
    val name = self.getClass.getSimpleName.replace("EventServiceCacheSupportTest", "")
    system.actorOf(Props(classOf[TestSupportService], eventSupportServices), name = s"EventServiceCacheSupportTest.$name")
  }
}

class EventServiceCacheSupportTestDefault extends ActorTest {
  "GenericEventServiceWithCacheAndSupport" should {
    "construct" in {
      EventServiceCacheSupportTest.SpawnTestSystem(List())
    }
  }
}

class EventServiceCacheSupportTestSupportNormally extends ActorTest {
  import EventServiceTestBase._
  "GenericEventServiceWithCacheAndSupport" should {
    "send a valid message to both subscribed channel and support class, like normal GenericEventServiceWithSupport" in {
      val mainProbe = TestProbe("MainProbe")
      val supportProbe = TestProbe("SupportProbe")
      val events = EventServiceCacheSupportTest.SpawnTestSystem(List(TestSupportActorLoader))
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

class EventServiceCacheSupportTestCachedMessages extends ActorTest {
  import EventServiceTestBase._
  "GenericEventServiceWithCacheAndSupport" should {
    "wait on sending designated cache-able messages after a few milliseconds" in {
      val mainProbe = TestProbe("MainProbe")
      val events = EventServiceCacheSupportTest.SpawnTestSystem(List())
      events.tell(Service.Join("test"), mainProbe.ref)
      val firstMessage = CachedEnvelope(PlanetSideGUID(1), "test", TestMessage(1))
      events ! firstMessage
      mainProbe.expectNoMessage(50 milliseconds)
      val reply = mainProbe.receiveOne(125 milliseconds)
      reply match {
        case msg if msg == firstMessage => ()
        case badmsg =>
          assert(false, s"(3) expected delivery of test envelope, but received $badmsg instead")
      }
    }
  }
}

class EventServiceCacheSupportTestMultipleCachedMessagesAtOnce extends ActorTest {
  import EventServiceTestBase._
  "GenericEventServiceWithCacheAndSupport" should {
    "send cache-able messages within a time span in bulk" in {
      val mainProbe = TestProbe("MainProbe")
      val events = EventServiceCacheSupportTest.SpawnTestSystem(List())
      events.tell(Service.Join("test"), mainProbe.ref)
      val firstMessage = CachedEnvelope(PlanetSideGUID(1), "test", TestMessage(1))
      val secondMessage = CachedEnvelope(PlanetSideGUID(2), "test", TestMessage(2))
      val thirdMessage = CachedEnvelope(PlanetSideGUID(3), "test", TestMessage(3))
      events ! firstMessage
      events ! secondMessage
      events ! thirdMessage
      mainProbe.expectNoMessage(50 milliseconds)
      val reply = mainProbe.receiveN(3, 125 milliseconds)
      reply.head match {
        case msg if msg == firstMessage => ()
        case badmsg =>
          assert(false, s"(4) expected delivery of test envelope, but received $badmsg instead")
      }
      reply(1) match {
        case msg if msg == secondMessage => ()
        case badmsg =>
          assert(false, s"(5) expected delivery of test envelope, but received $badmsg instead")
      }
      reply(2) match {
        case msg if msg == thirdMessage => ()
        case badmsg =>
          assert(false, s"(6) expected delivery of test envelope, but received $badmsg instead")
      }
      mainProbe.expectNoMessage(65 milliseconds)
    }
  }
}

class EventServiceCacheSupportTestMultipleCachedSameMessages extends ActorTest {
  import EventServiceTestBase._
  "GenericEventServiceWithCacheAndSupport" should {
    "only caches and dispatches the last message with a given filtering token" in {
      val mainProbe = TestProbe("MainProbe")
      val events = EventServiceCacheSupportTest.SpawnTestSystem(List())
      events.tell(Service.Join("test"), mainProbe.ref)
      val firstMessage = CachedEnvelope(PlanetSideGUID(1), "test", TestMessage(1))
      val secondMessage = CachedEnvelope(PlanetSideGUID(1), "test", TestMessage(2))
      val thirdMessage = CachedEnvelope(PlanetSideGUID(1), "test", TestMessage(3)) //this one!
      events ! firstMessage
      events ! secondMessage
      events ! thirdMessage
      mainProbe.expectNoMessage(50 milliseconds)
      val reply = mainProbe.receiveOne(125 milliseconds)
      reply match {
        case badmsg if badmsg == firstMessage =>
          assert(false, s"(1) received wrong message - $badmsg")
        case badmsg if badmsg == secondMessage =>
          assert(false, s"(2) received wrong message - $badmsg")
        case msg if msg == thirdMessage => ()
        case badmsg =>
          assert(false, s"(7) expected delivery of test envelope, but received $badmsg instead")
      }
    }
  }
}

class EventServiceCacheSupportTestMultipleCachedMessages extends ActorTest {
  import EventServiceTestBase._
  "GenericEventServiceWithCacheAndSupport" should {
    "send cache-able messages within a time span, separated if they are part of different caches" in {
      val mainProbe = TestProbe("MainProbe")
      val events = EventServiceCacheSupportTest.SpawnTestSystem(List())
      events.tell(Service.Join("test"), mainProbe.ref)
      val firstMessage = CachedEnvelope(PlanetSideGUID(1), "test", TestMessage(1))
      //first cache flush
      events ! firstMessage
      mainProbe.expectNoMessage(50 milliseconds)
      val firstReply = mainProbe.receiveOne(125 milliseconds)
      firstReply match {
        case msg if msg == firstMessage => ()
        case badmsg =>
          assert(false, s"(8) expected delivery of test envelope, but received $badmsg instead")
      }
      mainProbe.expectNoMessage(100 milliseconds)
      //second cache flush
      val secondMessage = CachedEnvelope(PlanetSideGUID(2), "test", TestMessage(2))
      events ! secondMessage
      mainProbe.expectNoMessage(50 milliseconds)
      val reply = mainProbe.receiveOne(125 milliseconds)
      reply match {
        case msg if msg == secondMessage => ()
        case badmsg =>
          assert(false, s"(9) expected delivery of test envelope, but received $badmsg instead")
      }
    }
  }
}

class EventServiceCacheSupportTestSupportDelayed extends ActorTest {
  import EventServiceCacheSupportTest._
  import EventServiceTestBase._
  "GenericEventServiceWithCacheAndSupport" should {
    "cache a message for a support actor, but only dispatch that message to the support actor when flushing the cache" in {
      val mainProbe = TestProbe("MainProbe")
      val supportProbe = TestProbe("SupportProbe")
      val events = EventServiceCacheSupportTest.SpawnTestSystem(List(TestSupportActorLoader))
      events.tell(Service.Join("test"), mainProbe.ref)
      val originalMessage = CachedSupportTestEnvelope(
        PlanetSideGUID(1),
        "test",
        TestMessage(1),
        SupportActorRepliesWith("hello world", supportProbe.ref)
      )
      events ! originalMessage
      //main reply
      supportProbe.expectNoMessage(50 milliseconds)
      mainProbe.expectNoMessage(10 milliseconds)
      val mainReply = mainProbe.receiveOne(125 milliseconds)
      mainReply match {
        case msg if msg == originalMessage => ()
        case badmsg =>
          assert(false, s"(10) expected delivery of test envelope, but received $badmsg instead")
      }
      //support reply
      val supportReply = supportProbe.receiveOne(10 milliseconds)
      supportReply match {
        case msg: String if msg.equals("hello world") => ()
        case badmsg =>
          assert(false, s"(11) expected delivery of test envelope payload 'hello world', but received '$badmsg' instead")
      }
    }
  }
}

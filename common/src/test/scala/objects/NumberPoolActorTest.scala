// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.NumberPoolHub
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import net.psforever.objects.guid.actor.{NumberPoolAccessorActor, NumberPoolActor, Register}
import net.psforever.objects.guid.pool.ExclusivePool
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.LimitedNumberSource
import org.specs2.specification.Scope

import scala.concurrent.duration.Duration
import scala.util.Success

abstract class ActorTest(sys : ActorSystem) extends TestKit(sys) with Scope with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}

class NumberPoolActorTest extends ActorTest(ActorSystem("test")) {
  "NumberPoolActor" should {
    "GetAnyNumber" in {
      val pool = new ExclusivePool((25 to 50).toList)
      pool.Selector = new RandomSelector
      val poolActor = system.actorOf(Props(classOf[NumberPoolActor], pool), name = "poolActor1")
      poolActor ! NumberPoolActor.GetAnyNumber()
      val msg = receiveOne(Duration.create(100, "ms"))
      assert(msg.isInstanceOf[NumberPoolActor.GiveNumber])
    }
  }
}

class NumberPoolActorTest1 extends ActorTest(ActorSystem("test")) {
  "NumberPoolActor" should {
    "GetSpecificNumber" in {
      val pool = new ExclusivePool((25 to 50).toList)
      pool.Selector = new RandomSelector
      val poolActor = system.actorOf(Props(classOf[NumberPoolActor], pool), name = "poolActor2")
      poolActor ! NumberPoolActor.GetSpecificNumber(37)
      expectMsg(NumberPoolActor.GiveNumber(37, None))
    }
  }
}

class NumberPoolActorTest2 extends ActorTest(ActorSystem("test")) {
  "NumberPoolActor" should {
    "NoNumber" in {
      val pool = new ExclusivePool((25 to 25).toList) //pool only has one number - 25
      pool.Selector = new RandomSelector
      val poolActor = system.actorOf(Props(classOf[NumberPoolActor], pool), name = "poolActor3")
      poolActor ! NumberPoolActor.GetAnyNumber()
      expectMsg(NumberPoolActor.GiveNumber(25, None))

      poolActor ! NumberPoolActor.GetAnyNumber()
      val msg = receiveOne(Duration.create(100, "ms"))
      assert(msg.isInstanceOf[NumberPoolActor.NoNumber])
    }
  }
}

class NumberPoolActorTest3 extends ActorTest(ActorSystem("test")) {
  "NumberPoolAccessorActor" should {
    class TestEntity extends IdentifiableEntity

    "register" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      val pool = hub.AddPool("test", (25 to 50).toList)
      pool.Selector = new RandomSelector
      val poolActor = system.actorOf(Props(classOf[NumberPoolActor], pool), name = "poolActor")
      val poolAccessor = system.actorOf(Props(classOf[NumberPoolAccessorActor], hub, pool, poolActor), name = "accessor")

      val obj : TestEntity = new TestEntity
      val probe = new TestProbe(system)
      poolAccessor ! Register(obj, probe.ref)
      probe.expectMsg(Success(obj))
      assert({obj.GUID; true}) //NoGUIDException if failure
    }
  }
}

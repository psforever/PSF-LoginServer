// Copyright (c) 2017 PSForever
package objects.number

import akka.actor.Props
import base.ActorTest
import net.psforever.objects.guid.pool.ExclusivePool
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.uns.NumberPoolActor

import scala.concurrent.duration.Duration

class NumberPoolActorTest extends ActorTest {
  "NumberPoolActor" should {
    "GetAnyNumber" in {
      val pool = new ExclusivePool((25 to 50).toList)
      pool.Selector = new RandomSelector
      val poolActor = system.actorOf(Props(classOf[NumberPoolActor], pool), name = "poolActor1")
      poolActor ! NumberPoolActor.GetAnyNumber()
      val msg = receiveOne(Duration.create(500, "ms"))
      assert(msg.isInstanceOf[NumberPoolActor.GiveNumber])
    }
  }
}

class NumberPoolActorTest1 extends ActorTest {
  "NumberPoolActor" should {
    "GetSpecificNumber" in {
      val pool = new ExclusivePool((25 to 50).toList)
      pool.Selector = new RandomSelector
      val poolActor = system.actorOf(Props(classOf[NumberPoolActor], pool), name = "poolActor2")
      poolActor ! NumberPoolActor.GetSpecificNumber(37)
      expectMsg(NumberPoolActor.GiveNumber(37))
    }
  }
}

class NumberPoolActorTest2 extends ActorTest {
  "NumberPoolActor" should {
    "NoNumber" in {
      val pool = new ExclusivePool((25 to 25).toList) //pool only has one number - 25
      pool.Selector = new RandomSelector
      val poolActor = system.actorOf(Props(classOf[NumberPoolActor], pool), name = "poolActor3")
      poolActor ! NumberPoolActor.GetAnyNumber()
      expectMsg(NumberPoolActor.GiveNumber(25))

      poolActor ! NumberPoolActor.GetAnyNumber()
      val msg = receiveOne(Duration.create(500, "ms"))
      assert(msg.isInstanceOf[NumberPoolActor.NoNumber])
    }
  }
}

// Copyright (c) 2017 PSForever
package objects.number

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.{NumberPoolHub, UniqueNumberOps, UniqueNumberSetup}
import net.psforever.objects.guid.source.MaxNumberSource
import org.scalatest.flatspec.AsyncFlatSpec
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.util.Success

class UniqueNumberOpsTest extends AsyncFlatSpec {
  behavior of "UniqueNumberOps"

  it should "UniqueNumberOpsTest" in {
    val promise: Promise[Any] = Promise()
    val sys = ActorSystem()
    val source = new MaxNumberSource(max = 21)
    val hub = new NumberPoolHub(source)
    hub.AddPool(name = "default", List(0,1,2,3,5,8,13,21))
    val entity = new UniqueNumberOpsTest.EntityTestClass()
    assert(!entity.HasGUID)
    assert(source.countUsed == 0)

    ask(sys.actorOf(Props[UniqueNumberOpsTest.NumberPoolBuilder](), "test"), hub)(Timeout(2.seconds)).onComplete {
      case Success(pools: Map[_,_]) =>
        val unops = new UniqueNumberOps(hub, pools.asInstanceOf[Map[String, ActorRef]])
        promise.completeWith { unops.Register(entity, poolName = "default") }
      case _ =>
        promise.failure(new Exception(""))
    }
    promise.future map { _ =>
      assert(entity.HasGUID)
      assert(source.countUsed == 1)
    }
  }
}

object UniqueNumberOpsTest {
  class EntityTestClass extends IdentifiableEntity

  class NumberPoolBuilder extends Actor {
    def receive: Receive = {
      case hub: NumberPoolHub =>
        sender() ! UniqueNumberSetup.AllocateNumberPoolActors(context, hub)
      case _ => ;
    }
  }
}

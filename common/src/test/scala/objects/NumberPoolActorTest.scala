// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.actor._

import scala.collection.JavaConverters._
import net.psforever.objects.guid.pool.ExclusivePool
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.LimitedNumberSource
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.util.{Failure, Try}
import scala.concurrent.ExecutionContext.Implicits.global

class NumberPoolActorTest extends Specification {
  val config : java.util.Map[String,Object] = Map(
    "akka.loggers" -> List("akka.event.slf4j.Slf4jLogger").asJava,
    "akka.loglevel" -> "INFO",
    "akka.logging-filter" -> "akka.event.slf4j.Slf4jLoggingFilter"
  ).asJava
  implicit val timeout = Timeout(100 milliseconds)

  class TestEntity extends IdentifiableEntity

  "NumberPoolActor" in {
    val system : akka.actor.ActorSystem = ActorSystem("ActorTest", ConfigFactory.parseMap(config))
    val pool = new ExclusivePool((25 to 50).toList)
    pool.Selector = new RandomSelector
    val poolActor = system.actorOf(Props(classOf[NumberPoolActor], pool), name = "poolActor")
    val future = (poolActor ? NumberPoolActor.GetAnyNumber()).mapTo[Try[Int]]
    future.onComplete(value => {
      system.terminate
      value.foreach {
        case Failure(_) =>
          ko
        case _ => ;
      }
    })
    Await.result(system.whenTerminated, Duration.Inf)
    ok
  }

  "NumberPoolAccessorActor" in {
    /*
    Notes:
    Receiver sets resultObject.complete to true and shuts down the ActorSystem.
    If Receiver never gets the appropriate message, Await.result will timeout (and the exception will be caught safely).
    */
    val system : akka.actor.ActorSystem = ActorSystem("ActorTest", ConfigFactory.parseMap(config))
    val hub = new NumberPoolHub(new LimitedNumberSource(51))
    val pool = hub.AddPool("test", (25 to 50).toList)
    pool.Selector = new RandomSelector
    val poolActor = system.actorOf(Props(classOf[NumberPoolActor], pool), name = "poolActor")
    val poolAccessor = system.actorOf(Props(classOf[NumberPoolAccessorActor], hub, pool, poolActor), name = "accessor")
    val resultObject = new ResolutionObject
    resultObject.complete mustEqual false
    val receiver = system.actorOf(Props(classOf[Receiver], system, resultObject), "receiver")

    val obj : TestEntity = new TestEntity
    poolAccessor ! Register(obj, receiver)
    try { Await.result(system.whenTerminated, 5 seconds) } catch { case _ : Exception => ; }
    resultObject.complete mustEqual true
  }
}

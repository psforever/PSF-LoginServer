package base

// Copyright (c) 2017 PSForever
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.specs2.specification.Scope

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

abstract class ActorTest(sys : ActorSystem = ActorSystem("system", ConfigFactory.parseMap(ActorTest.LoggingConfig)))
  extends TestKit(sys) with Scope with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}

object ActorTest {
  import scala.collection.JavaConverters._
  private val LoggingConfig = Map(
    "akka.loggers" -> List("akka.testkit.TestEventListener").asJava,
    "akka.loglevel" -> "OFF",
    "akka.stdout-loglevel" -> "OFF",
    "akka.log-dead-letters" -> "OFF"
  ).asJava

  /**
    * A (potential) workaround to a Travis CI issue involving polling a series of messages over a period of time.
    * Running the test in isolation works every time.
    * Running the test as part of a series produces mixed results.
    * Travis CI fails the test every time by not getting any messages.
    * @see TestKit.receiveN
    * @param n the number of messages to poll
    * @param timeout how long to wait for each message
    * @param sys what to poll
    * @return a list of messages
    */
  def receiveMultiple(n : Int, timeout : FiniteDuration, sys : TestKit) : List[Any] = {
    assert(0 < n, s"number of expected messages must be positive non-zero integer - $n")
    val out = {
      val msgs = mutable.ListBuffer[Any]()
      (0 until n).foreach(_ => {
        msgs += sys.receiveOne(timeout)
      })
      msgs.toList
    }
    out
  }
}

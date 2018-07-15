// Copyright (c) 2017 PSForever
package objects

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.specs2.specification.Scope

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
}

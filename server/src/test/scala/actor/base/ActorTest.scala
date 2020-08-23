// Copyright (c) 2017 PSForever
package actor.base

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.specs2.specification.Scope

abstract class ActorTest(sys: ActorSystem = ActorSystem("system", ConfigFactory.parseMap(ActorTest.LoggingConfig)))
    extends TestKit(sys)
    with Scope
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}

object ActorTest {
  import scala.jdk.CollectionConverters._
  private val LoggingConfig = Map(
    "akka.loggers"          -> List("akka.testkit.TestEventListener").asJava,
    "akka.loglevel"         -> "OFF",
    "akka.stdout-loglevel"  -> "OFF",
    "akka.log-dead-letters" -> "OFF"
  ).asJava
}

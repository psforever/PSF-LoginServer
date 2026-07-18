// Copyright (c) 2017 PSForever
package actor.base

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.{ImplicitSender, TestKit}
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
    "pekko.loggers"          -> List("org.apache.pekko.testkit.TestEventListener").asJava,
    "pekko.loglevel"         -> "OFF",
    "pekko.stdout-loglevel"  -> "OFF",
    "pekko.log-dead-letters" -> "OFF"
  ).asJava
}

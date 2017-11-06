// Copyright (c) 2017 PSForever
package objects

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.specs2.specification.Scope

abstract class ActorTest(sys : ActorSystem = ActorSystem("system")) extends TestKit(sys) with Scope with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}

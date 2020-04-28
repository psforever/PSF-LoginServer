// Copyright (c) 2020 PSForever
package base

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Await

/**
  * Create an `ActorTest` environment that has an `ActorContext` object.
  */
abstract class FreedContextActorTest extends ActorTest {
  /*
  Never do this in actual production code!
  ActorSystem and ActorContext offer similar mechanisms for instantiating actors.
  This is a consequence of their shared inheritance of the ActorRefFactory trait.
  They are not equivalent enough to be able to pass one as the other as a parameter.
  Because the ActorSystem has no context of its own,
  various bizarre mechanisms have to be developed to use any methods that would pass in a context object.
  We create a middleman Actor whose main purpose is to surrender its context object to the test environment directly
  and then direct all messages sent to that object to the test environment.
  */
  private val _testContextHandler = system.actorOf(Props(classOf[ContextSensitive]), "actor-test-cs")
  private implicit val timeout = Timeout(5 seconds)
  private val _testContextHandlerResult = ask(_testContextHandler, message = "", self)
  implicit val context = Await.result(_testContextHandlerResult, timeout.duration).asInstanceOf[ActorContext]
}

/**
  * Surrender your `context` object for a greater good!
  */
private class ContextSensitive extends Actor {
  var output : ActorRef = ActorRef.noSender

  def receive : Receive = {
    case _ =>
      context.become(PassThroughBehavior)
      output = sender
      sender ! context
  }

  /**
    * Once the `context` object has been leased,
    * this `Actor` becomes transparent.
    * Calling `context.parent` from whatever `Actor` was spurned by the previously provided `context`,
    * will now refer to whatever was the contact to gain access to it - the test environment.
    * @return something to `become`
    */
  def PassThroughBehavior : Receive = {
    case msg => output forward msg
  }
}

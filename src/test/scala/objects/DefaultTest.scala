// Copyright (c) 2020 PSForever
package objects

import akka.actor.DeadLetter
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.Default
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class DefaultTest extends Specification {
  "Default.Cancellable" should {
    "always act like it can be cancelled successfully" in {
      Default.Cancellable.cancel() mustEqual true
    }

    "always act like it was cancelled successfully" in {
      Default.Cancellable.isCancelled mustEqual true
    }
  }
}

class DefaultActorStartedTest extends ActorTest {
  "Default.Actor" should {
    "send messages to deadLetters" in {
      //after being started
      Default(system)
      val probe = new TestProbe(system)
      system.eventStream.subscribe(probe.ref, classOf[DeadLetter])
      Default.Actor ! "hello world"
      val msg1 = probe.receiveOne(250 milliseconds)
      assert(msg1.isInstanceOf[DeadLetter])
      assert(msg1.asInstanceOf[DeadLetter].message equals "hello world")

      //if it was stopped
      system.stop(Default.Actor)
      Default.Actor ! "hello world"
      val msg2 = probe.receiveOne(250 milliseconds)
      assert(msg2.isInstanceOf[DeadLetter])
      assert(msg2.asInstanceOf[DeadLetter].message equals "hello world")
    }
  }
}

object DefaultActorTest {
  //due to being a singleton, the original original value of the Default.Actor is cached here
  val Original = Default.Actor
}

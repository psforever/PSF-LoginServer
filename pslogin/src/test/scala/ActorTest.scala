// Copyright (c) 2017 PSForever

import akka.actor.{ActorRef, ActorSystem, MDCContextAware}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import net.psforever.packet.{ControlPacket, GamePacket}
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

  final case class MDCGamePacket(packet : GamePacket)

  final case class MDCControlPacket(packet : ControlPacket)

  class MDCTestProbe(probe : TestProbe) extends MDCContextAware {
    /*
    The way this test mediator works needs to be explained.

    MDCContextAware objects initialize themselves in a chain of ActorRefs defined in the HelloFriend message.
    As the iterator is consumed, it produces a right-neighbor (r-neighbor) that is much further along the chain.
    The HelloFriend is passed to that r-neighbor and that is how subsequent neighbors are initialized and chained.

    MDCContextAware objects consume and produce internal messages called MdcMsg that wrap around the payload.
    Normally inaccessible from the outside, the payload is unwrapped within the standard receive PartialFunction.
    By interacting with a TestProbe constructor param, information that would be concealed by MdcMsg can be polled.

    The l-neighbor of the MDCContextAware is the system of the base.ActorTest TestKit.
    The r-neighbor of the MDCContextAware is this MDCTestProbe and, indirectly, the TestProbe that was interjected.
    Pass l-input into the MDCContextAware itself.
    The r-output is a normal message that can be polled on that TestProbe.
    Pass r-input into this MDCTestProbe directly.
    The l-output is an MdcMsg that can be treated just as r-output, sending it to this Actor and polling the TestProbe.
    */
    private var left : ActorRef = ActorRef.noSender

    def receive : Receive = {
      case msg @ HelloFriend(_, _) =>
        left = sender()
        probe.ref ! msg

      case MDCGamePacket(msg) =>
        left ! msg

      case MDCControlPacket(msg) =>
        left ! msg

      case msg =>
        left ! msg
        probe.ref ! msg
    }
  }
}

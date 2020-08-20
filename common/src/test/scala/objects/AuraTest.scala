// Copyright (c) 2020 PSForever
package objects

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.aura.AuraEffectBehavior.Target
import net.psforever.objects.serverobject.aura.{Aura, AuraContainer, AuraEffectBehavior}
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class AuraContainerTest extends Specification {
  "AuraContainer" should {
    "have no default effects" in {
      new AuraTest.Entity().Aura.isEmpty mustEqual true
    }

    "add effects" in {
      val obj = new AuraTest.Entity()
      obj.Aura.size mustEqual 0
      obj.Aura.contains(Aura.Plasma) mustEqual false
      obj.AddEffectToAura(Aura.Plasma)
      obj.Aura.size mustEqual 1
      obj.Aura.contains(Aura.Plasma) mustEqual true
    }

    "do nothing if adding repeated effects" in {
      val obj = new AuraTest.Entity()
      obj.Aura.size mustEqual 0
      obj.AddEffectToAura(Aura.Plasma)
      obj.Aura.size mustEqual 1
      obj.AddEffectToAura(Aura.Plasma)
      obj.Aura.size mustEqual 1
    }

    "remove effects" in {
      val obj = new AuraTest.Entity()
      obj.Aura.size mustEqual 0
      obj.Aura.contains(Aura.Plasma) mustEqual false
      obj.AddEffectToAura(Aura.Plasma)
      obj.Aura.size mustEqual 1
      obj.Aura.contains(Aura.Plasma) mustEqual true
      obj.RemoveEffectFromAura(Aura.Plasma)
      obj.Aura.size mustEqual 0
      obj.Aura.contains(Aura.Plasma) mustEqual false
    }

    "do nothing if no effects" in {
      val obj = new AuraTest.Entity()
      obj.Aura.size mustEqual 0
      obj.Aura.contains(Aura.Plasma) mustEqual false
      obj.RemoveEffectFromAura(Aura.Plasma)
      obj.Aura.size mustEqual 0
      obj.Aura.contains(Aura.Plasma) mustEqual false
    }

    "do nothing if trying to remove wrong effect" in {
      val obj = new AuraTest.Entity()
      obj.Aura.size mustEqual 0
      obj.Aura.contains(Aura.Plasma) mustEqual false
      obj.Aura.contains(Aura.Fire) mustEqual false
      obj.AddEffectToAura(Aura.Plasma)
      obj.Aura.size mustEqual 1
      obj.Aura.contains(Aura.Plasma) mustEqual true
      obj.Aura.contains(Aura.Fire) mustEqual false
      obj.RemoveEffectFromAura(Aura.Fire)
      obj.Aura.size mustEqual 1
      obj.Aura.contains(Aura.Plasma) mustEqual true
      obj.Aura.contains(Aura.Fire) mustEqual false
    }
  }
}

class AuraEffectBehaviorInitTest extends ActorTest {
  val obj = new AuraTest.Entity()

  "AuraEffectBehavior" should {
    "init" in {
      obj.Actor = system.actorOf(Props(classOf[AuraTest.Agency], obj, ActorRef.noSender), "aura-test-actor")
      expectNoMessage(500 milliseconds)
    }
  }
}

class AuraEffectBehaviorStartEffectTest extends ActorTest {
  val obj = new AuraTest.Entity()
  val updateProbe = new TestProbe(system)
  obj.Actor = system.actorOf(Props(classOf[AuraTest.Agency], obj, updateProbe.ref), "aura-test-actor")

  "AuraEffectBehavior" should {
    "start effect (ends naturally)" in {
      assert(obj.Aura.isEmpty)
      obj.Actor ! AuraEffectBehavior.StartEffect(Aura.Plasma, 2500)
      val msg1 = updateProbe.receiveOne(100 milliseconds)
      assert(
        msg1 match {
          case AuraTest.DoUpdateAuraEffect() => true
          case _                             => false
        }
      )
      assert(obj.Aura.contains(Aura.Plasma))
      expectNoMessage(2000 milliseconds)
      assert(obj.Aura.contains(Aura.Plasma))
      val msg2 = updateProbe.receiveOne(750 milliseconds)
      assert(
        msg2 match {
          case AuraTest.DoUpdateAuraEffect() => true
          case _                             => false
        }
      )
      assert(obj.Aura.isEmpty)
    }
  }
}

class AuraEffectBehaviorStartLongerEffectTest extends ActorTest {
  val obj = new AuraTest.Entity()
  val updateProbe = new TestProbe(system)
  obj.Actor = system.actorOf(Props(classOf[AuraTest.Agency], obj, updateProbe.ref), "aura-test-actor")

  "AuraEffectBehavior" should {
    "replace a shorter effect with a longer one" in {
      assert(obj.Aura.isEmpty)
      obj.Actor ! AuraEffectBehavior.StartEffect(Aura.Plasma, 500)
      val msg1 = updateProbe.receiveOne(100 milliseconds)
      assert(
        msg1 match {
          case AuraTest.DoUpdateAuraEffect() => true
          case _                             => false
        }
      )
      assert(obj.Aura.contains(Aura.Plasma))
      obj.Actor ! AuraEffectBehavior.StartEffect(Aura.Plasma, 2500)
      updateProbe.expectNoMessage(2000 milliseconds)
      //first effect has not ended naturally (yet)
      assert(obj.Aura.contains(Aura.Plasma))
    }
  }
}

class AuraEffectBehaviorNoRedundantStartEffectTest extends ActorTest {
  val obj = new AuraTest.Entity()
  val updateProbe = new TestProbe(system)
  obj.Actor = system.actorOf(Props(classOf[AuraTest.Agency], obj, updateProbe.ref), "aura-test-actor")

  "AuraEffectBehavior" should {
    "not start an effect if already active" in {
      assert(obj.Aura.isEmpty)
      obj.Actor ! AuraEffectBehavior.StartEffect(Aura.Plasma, 2500)
      val msg1 = updateProbe.receiveOne(100 milliseconds)
      assert(
        msg1 match {
          case AuraTest.DoUpdateAuraEffect() => true
          case _                             => false
        }
      )
      assert(obj.Aura.contains(Aura.Plasma))
      expectNoMessage(1000 milliseconds) //wait for half of the effect's duration
      assert(obj.Aura.contains(Aura.Plasma))
      obj.Actor ! AuraEffectBehavior.StartEffect(Aura.Plasma, 2500)
      updateProbe.expectNoMessage(1500 milliseconds)
    }
  }
}

class AuraEffectBehaviorNoOverrideStartEffectTest extends ActorTest {
  val obj = new AuraTest.Entity()
  val updateProbe = new TestProbe(system)
  obj.Actor = system.actorOf(Props(classOf[AuraTest.Agency], obj, updateProbe.ref), "aura-test-actor")

  "AuraEffectBehavior" should {
    "not replace a long-running effect with a short-running effect" in {
      assert(obj.Aura.isEmpty)
      obj.Actor ! AuraEffectBehavior.StartEffect(Aura.Plasma, 2500)
      val msg1 = updateProbe.receiveOne(100 milliseconds)
      assert(
        msg1 match {
          case AuraTest.DoUpdateAuraEffect() => true
          case _                             => false
        }
      )
      assert(obj.Aura.contains(Aura.Plasma))
      obj.Actor ! AuraEffectBehavior.StartEffect(Aura.Plasma, 500)
      updateProbe.expectNoMessage(1500 milliseconds)
      //effect has not ended naturally
      assert(obj.Aura.contains(Aura.Plasma))
    }
  }
}

class AuraEffectBehaviorNoStartUnsupportedEffectTest extends ActorTest {
  val obj = new AuraTest.Entity()
  val updateProbe = new TestProbe(system)
  obj.Actor = system.actorOf(Props(classOf[AuraTest.Agency], obj, updateProbe.ref), "aura-test-actor") //supports Plasma only

  "AuraEffectBehavior" should {
    "not start an effect that is not approved" in {
      assert(obj.Aura.isEmpty)
      obj.Actor ! AuraEffectBehavior.StartEffect(Aura.Fire, 2500)
      assert(obj.Aura.isEmpty)
      updateProbe.expectNoMessage(2000 milliseconds)
    }
  }
}



class AuraEffectBehaviorEndEarlyTest extends ActorTest {
  val obj = new AuraTest.Entity()
  val updateProbe = new TestProbe(system)
  obj.Actor = system.actorOf(Props(classOf[AuraTest.Agency], obj, updateProbe.ref), "aura-test-actor")

  "AuraEffectBehavior" should {
    "start effect (ends early)" in {
      assert(obj.Aura.isEmpty)
      obj.Actor ! AuraEffectBehavior.StartEffect(Aura.Plasma, 2500)
      val msg1 = updateProbe.receiveOne(100 milliseconds)
      assert(
        msg1 match {
          case AuraTest.DoUpdateAuraEffect() => true
          case _                             => false
        }
      )
      assert(obj.Aura.contains(Aura.Plasma))
      obj.Actor ! AuraEffectBehavior.EndEffect(Aura.Plasma)
      val msg2 = updateProbe.receiveOne(100 milliseconds)
      assert(
        msg2 match {
          case AuraTest.DoUpdateAuraEffect() => true
          case _                             => false
        }
      )
      assert(obj.Aura.isEmpty)
    }
  }
}

class AuraEffectBehaviorEndNothingTest extends ActorTest {
  val obj = new AuraTest.Entity()
  val updateProbe = new TestProbe(system)
  obj.Actor = system.actorOf(Props(classOf[AuraTest.Agency], obj, updateProbe.ref), "aura-test-actor")

  "AuraEffectBehavior" should {
    "can not end an effect that is not supported (hence, not started)" in {
      assert(obj.Aura.isEmpty)
      obj.Actor ! AuraEffectBehavior.StartEffect(Aura.Plasma, 2500)
      val msg1 = updateProbe.receiveOne(100 milliseconds)
      assert(
        msg1 match {
          case AuraTest.DoUpdateAuraEffect() => true
          case _                             => false
        }
      )
      assert(obj.Aura.size == 1)
      obj.Actor ! AuraEffectBehavior.EndEffect(Aura.Fire)
      updateProbe.expectNoMessage(1000 milliseconds)
      assert(obj.Aura.size == 1)
    }
  }
}

object AuraTest {
  class Agency(obj: AuraEffectBehavior.Target, updateRef: ActorRef) extends Actor with AuraEffectBehavior {
    def AuraTargetObject : Target = obj
    ApplicableEffect(Aura.Plasma)

    def receive: Receive = auraBehavior.orElse {
      case _ => ;
    }

    def UpdateAuraEffect(target : Target) : Unit = {
      updateRef ! DoUpdateAuraEffect()
    }
  }

  class Entity extends PlanetSideServerObject with AuraContainer {
    def Faction = PlanetSideEmpire.NEUTRAL
    def Definition = null
  }

  final case class DoUpdateAuraEffect()
}

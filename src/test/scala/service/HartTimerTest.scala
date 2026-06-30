// Copyright (c) 2021 PSForever
package service

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import _root_.base.ActorTest
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.services.hart.HartTimer
import net.psforever.types.PlanetSideGUID

import scala.concurrent.duration._

class HartTimerNotScheduled extends ActorTest {
  "HartTimer" should {
    val catchall = new TestProbe(system).ref
    val zone = new Zone("test", new ZoneMap("test"), 0) {
      override def SetupNumberPools(): Unit = {}
      override def AvatarEvents: ActorRef = catchall
      override def LocalEvents: ActorRef = catchall
      override def VehicleEvents: ActorRef = catchall
      override def Activity: ActorRef = catchall
    }
    val timer = system.actorOf(Props(classOf[HartTimer], zone), "hart-timer")

    "not do anything if paired before having a schedule set" in {
      val probe = new TestProbe(system)
      timer ! HartTimer.PairWith(zone, PlanetSideGUID(1), PlanetSideGUID(2), probe.ref)
      probe.expectNoMessage(max = 3 seconds)
    }
  }
}

class HartTimerInitializedPairingScheduled extends ActorTest {
  "HartTimer" should {
    val catchall = new TestProbe(system).ref
    val zone = new Zone("test", new ZoneMap("test"), 0) {
      override def SetupNumberPools(): Unit = {}
      override def AvatarEvents: ActorRef = catchall
      override def LocalEvents: ActorRef = catchall
      override def VehicleEvents: ActorRef = catchall
      override def Activity: ActorRef = catchall
    }
    val timer = system.actorOf(Props(classOf[HartTimer], zone), "hart-timer")

    "perform some initialization when paired" in {
      val probe = new TestProbe(system)
      timer ! HartTimer.SetEventDurations("test", 55000, 10000)
      timer ! HartTimer.PairWith(zone, PlanetSideGUID(1), PlanetSideGUID(2), probe.ref)
      probe.receiveOne(1 seconds) match {
        case HartTimer.ShuttleDocked("test") => assert(true)
        case _                               => assert(false)
      }
    }
  }
}

object HartTimerTest { /* initially left empty */ }
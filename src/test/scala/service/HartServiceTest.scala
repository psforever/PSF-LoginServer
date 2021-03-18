// Copyright (c) 2021 PSForever
package service

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.services.hart.{HartService, HartTimer}
import net.psforever.types.PlanetSideGUID

import scala.concurrent.duration._

class HartServiceTest extends ActorTest {
  "HartService" should {
    val hart = system.actorOf(Props[HartService](), name = "hart")
    val catchall = new TestProbe(system).ref
    val zone = new Zone("test", new ZoneMap("test"), zoneNumber = 0) {
      override def SetupNumberPools(): Unit = {}
      override def AvatarEvents: ActorRef = catchall
      override def LocalEvents: ActorRef = catchall
      override def VehicleEvents: ActorRef = catchall
      override def Activity: ActorRef = catchall
    }

    "pass messages back upon pairing" in {
      val probe = new TestProbe(system)
      hart ! HartTimer.PairWith(zone, PlanetSideGUID(1), PlanetSideGUID(2), probe.ref)
      probe.receiveOne(max = 1 seconds) match {
        case HartTimer.ShuttleDocked("test") => assert(true)
        case _                               => assert(false)
      }
    }
  }
}

object HartServiceTest { /* initially left empty */ }

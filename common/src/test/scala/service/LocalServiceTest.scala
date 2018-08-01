package service

// Copyright (c) 2017 PSForever
import akka.actor.Props
import base.ActorTest
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{PlanetSideEmpire, Vector3}
import services.Service
import services.local._

class LocalService1Test extends ActorTest {
  "LocalService" should {
    "construct" in {
      system.actorOf(Props[LocalService], "service")
      assert(true)
    }
  }
}

class LocalService2Test extends ActorTest {
  "LocalService" should {
    "subscribe" in {
      val service = system.actorOf(Props[LocalService], "service")
      service ! Service.Join("test")
      assert(true)
    }
  }
}

class LocalService3Test extends ActorTest {
  "LocalService" should {
    "subscribe to a specific channel" in {
      val service = system.actorOf(Props[LocalService], "service")
      service ! Service.Join("test")
      service ! Service.Leave()
      assert(true)
    }
  }
}

class LocalService4Test extends ActorTest {
  "LocalService" should {
    "subscribe" in {
      val service = system.actorOf(Props[LocalService], "service")
      service ! Service.Join("test")
      service ! Service.LeaveAll()
      assert(true)
    }
  }
}

class LocalService5Test extends ActorTest {
  "LocalService" should {
    "pass an unhandled message" in {
      val service = system.actorOf(Props[LocalService], "service")
      service ! Service.Join("test")
      service ! "hello"
      expectNoMsg()
    }
  }
}

class DoorClosesTest extends ActorTest {
  "LocalService" should {
    "pass DoorCloses" in {
      val service = system.actorOf(Props[LocalService], "service")
      service ! Service.Join("test")
      service ! LocalServiceMessage("test", LocalAction.DoorCloses(PlanetSideGUID(10), PlanetSideGUID(40)))
      expectMsg(LocalServiceResponse("/test/Local", PlanetSideGUID(10), LocalResponse.DoorCloses(PlanetSideGUID(40))))
    }
  }
}

class HackClearTest extends ActorTest {
  val obj = new PlanetSideServerObject() {
    def Faction  = PlanetSideEmpire.NEUTRAL
    def Definition = null
    GUID = PlanetSideGUID(40)
  }

  "LocalService" should {
    "pass HackClear" in {
      val service = system.actorOf(Props[LocalService], "service")
      service ! Service.Join("test")
      service ! LocalServiceMessage("test", LocalAction.HackClear(PlanetSideGUID(10), obj, 0L, 1000L))
      expectMsg(LocalServiceResponse("/test/Local", PlanetSideGUID(10), LocalResponse.HackClear(PlanetSideGUID(40), 0L, 1000L)))
    }
  }
}

class ProximityTerminalEffectTest extends ActorTest {
  "LocalService" should {
    "pass ProximityTerminalEffect" in {
      val service = system.actorOf(Props[LocalService], "service")
      service ! Service.Join("test")
      service ! LocalServiceMessage("test", LocalAction.ProximityTerminalEffect(PlanetSideGUID(10), PlanetSideGUID(40), true))
      expectMsg(LocalServiceResponse("/test/Local", PlanetSideGUID(10), LocalResponse.ProximityTerminalEffect(PlanetSideGUID(40), true)))
    }
  }
}

class TriggerSoundTest extends ActorTest {
  import net.psforever.packet.game.TriggeredSound

  "LocalService" should {
    "pass TriggerSound" in {
      val service = system.actorOf(Props[LocalService], "service")
      service ! Service.Join("test")
      service ! LocalServiceMessage("test", LocalAction.TriggerSound(PlanetSideGUID(10), TriggeredSound.LockedOut, Vector3(1.1f, 2.2f, 3.3f), 0, 0.75f))
      expectMsg(LocalServiceResponse("/test/Local", PlanetSideGUID(10), LocalResponse.TriggerSound(TriggeredSound.LockedOut, Vector3(1.1f, 2.2f, 3.3f), 0, 0.75f)))
    }
  }
}

object LocalServiceTest {
  //decoy
}

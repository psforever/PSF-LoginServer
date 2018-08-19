// Copyright (c) 2017 PSForever
package service

import akka.actor.Props
import base.ActorTest
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.packet.game._
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

class DeployableMapIconTest extends ActorTest {
  "LocalService" should {
    "pass DeployableMapIcon" in {
      val service = system.actorOf(Props[LocalService], "service")
      service ! Service.Join("test")
      service ! LocalServiceMessage("test",
        LocalAction.DeployableMapIcon(
          PlanetSideGUID(10),
          DeploymentAction.Build,
          DeployableInfo(PlanetSideGUID(40), DeployableIcon.Boomer, Vector3(1,2,3), PlanetSideGUID(11))
        )
      )
      expectMsg(LocalServiceResponse("/test/Local", PlanetSideGUID(10),
        LocalResponse.DeployableMapIcon(
          DeploymentAction.Build,
          DeployableInfo(PlanetSideGUID(40), DeployableIcon.Boomer, Vector3(1,2,3), PlanetSideGUID(11))
        )
      ))
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

class TriggerEffectTest extends ActorTest {
  "LocalService" should {
    "pass TriggerEffect (1)" in {
      val service = system.actorOf(Props[LocalService], "service")
      service ! Service.Join("test")
      service ! LocalServiceMessage("test", LocalAction.TriggerEffect(PlanetSideGUID(10), "on", PlanetSideGUID(40)))
      expectMsg(LocalServiceResponse("/test/Local", PlanetSideGUID(10), LocalResponse.TriggerEffect(PlanetSideGUID(40), "on", None, None)))
    }
  }
}

class TriggerEffectInfoTest extends ActorTest {
  "LocalService" should {
    "pass TriggerEffect (2)" in {
      val service = system.actorOf(Props[LocalService], "service")
      service ! Service.Join("test")
      service ! LocalServiceMessage("test", LocalAction.TriggerEffectInfo(PlanetSideGUID(10), "on", PlanetSideGUID(40), true, 1000))
      expectMsg(LocalServiceResponse("/test/Local", PlanetSideGUID(10), LocalResponse.TriggerEffect(PlanetSideGUID(40), "on", Some(TriggeredEffect(true, 1000)), None)))
    }
  }
}

class TriggerEffectLocationTest extends ActorTest {
  "LocalService" should {
    "pass TriggerEffect (3)" in {
      val service = system.actorOf(Props[LocalService], "service")
      service ! Service.Join("test")
      service ! LocalServiceMessage("test", LocalAction.TriggerEffectLocation(PlanetSideGUID(10), "spawn_object_failed_effect", Vector3(1.1f, 2.2f, 3.3f), Vector3(4.4f, 5.5f, 6.6f)))
      expectMsg(LocalServiceResponse("/test/Local", PlanetSideGUID(10), LocalResponse.TriggerEffect(PlanetSideGUID(0), "spawn_object_failed_effect", None, Some(TriggeredEffectLocation(Vector3(1.1f, 2.2f, 3.3f), Vector3(4.4f, 5.5f, 6.6f))))))
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

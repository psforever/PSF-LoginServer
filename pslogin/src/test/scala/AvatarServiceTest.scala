// Copyright (c) 2017 PSForever
import akka.actor.Props
import akka.routing.RandomPool
import net.psforever.objects._
import net.psforever.objects.guid.{GUIDTask, TaskResolver}
import net.psforever.objects.zones.{Zone, ZoneActor, ZoneMap}
import net.psforever.packet.game.{PlanetSideGUID, PlayerStateMessageUpstream}
import net.psforever.types.{CharacterGender, ExoSuitType, PlanetSideEmpire, Vector3}
import services.{Service, ServiceManager}
import services.avatar._

import scala.concurrent.duration._

class AvatarService1Test extends ActorTest {
  "AvatarService" should {
    "construct" in {
      ServiceManager.boot(system)
      system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      assert(true)
    }
  }
}

class AvatarService2Test extends ActorTest {
  "AvatarService" should {
    "subscribe" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      assert(true)
    }
  }
}

class AvatarService3Test extends ActorTest {
  "AvatarService" should {
    ServiceManager.boot(system)
    "subscribe to a specific channel" in {
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! Service.Leave()
      assert(true)
    }
  }
}

class AvatarService4Test extends ActorTest {
  "AvatarService" should {
    "subscribe" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! Service.LeaveAll()
      assert(true)
    }
  }
}

class AvatarService5Test extends ActorTest {
  "AvatarService" should {
    "pass an unhandled message" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! "hello"
      expectNoMsg()
    }
  }
}

class ArmorChangedTest extends ActorTest {
  "AvatarService" should {
    "pass ArmorChanged" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ArmorChanged(PlanetSideGUID(10), ExoSuitType.Reinforced, 0))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ArmorChanged(ExoSuitType.Reinforced, 0)))
    }
  }
}

class ConcealPlayerTest extends ActorTest {
  "AvatarService" should {
    "pass ConcealPlayer" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ConcealPlayer(PlanetSideGUID(10)))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ConcealPlayer()))
    }
  }
}

class EquipmentInHandTest extends ActorTest {
  val tool = Tool(GlobalDefinitions.beamer)

  "AvatarService" should {
    "pass EquipmentInHand" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.EquipmentInHand(PlanetSideGUID(10), PlanetSideGUID(11), 2, tool))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.EquipmentInHand(PlanetSideGUID(11), 2, tool)))
    }
  }
}

class EquipmentOnGroundTest extends ActorTest {
  val toolDef = GlobalDefinitions.beamer
  val tool = Tool(toolDef)
  tool.AmmoSlots.head.Box.GUID = PlanetSideGUID(1)
  val cdata = toolDef.Packet.ConstructorData(tool).get

  "AvatarService" should {
    "pass EquipmentOnGround" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.EquipmentOnGround(PlanetSideGUID(10), Vector3(300f, 200f, 100f), Vector3(450f, 300f, 150f), toolDef.ObjectId, PlanetSideGUID(11), cdata))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.EquipmentOnGround(Vector3(300f, 200f, 100f), Vector3(450f, 300f, 150f), toolDef.ObjectId, PlanetSideGUID(11), cdata)))
    }
  }
}

class LoadPlayerTest extends ActorTest {
  val obj = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, 1))
  obj.GUID = PlanetSideGUID(10)
  obj.Slot(5).Equipment.get.GUID = PlanetSideGUID(11)
  val pdata = obj.Definition.Packet.DetailedConstructorData(obj).get

  "AvatarService" should {
    "pass LoadPlayer" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.LoadPlayer(PlanetSideGUID(10), pdata))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.LoadPlayer(pdata)))
    }
  }
}

class ObjectDeleteTest extends ActorTest {
  "AvatarService" should {
    "pass ObjectDelete" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ObjectDelete(PlanetSideGUID(10), PlanetSideGUID(11)))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ObjectDelete(PlanetSideGUID(11), 0)))

      service ! AvatarServiceMessage("test", AvatarAction.ObjectDelete(PlanetSideGUID(10), PlanetSideGUID(11), 55))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ObjectDelete(PlanetSideGUID(11), 55)))
    }
  }
}

class ObjectHeldTest extends ActorTest {
  "AvatarService" should {
    "pass ObjectHeld" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ObjectHeld(PlanetSideGUID(10), 1))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ObjectHeld(1)))
    }
  }
}

class PlanetsideAttributeTest extends ActorTest {
  "AvatarService" should {
    "pass PlanetsideAttribute" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.PlanetsideAttribute(PlanetSideGUID(10), 5, 1200L))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.PlanetsideAttribute(5, 1200L)))
    }
  }
}

class PlayerStateTest extends ActorTest {
  val msg = PlayerStateMessageUpstream(PlanetSideGUID(75), Vector3(3694.1094f, 2735.4531f, 90.84375f), Some(Vector3(4.375f, 2.59375f, 0.0f)), 61.875f, 351.5625f, 0.0f, 136, 0, false, false, false, false, 112, 0)

  "AvatarService" should {
    "pass PlayerState" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.PlayerState(PlanetSideGUID(10), msg, false, false))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.PlayerState(msg, false, false)))
    }
  }
}

class ReloadTest extends ActorTest {
  "AvatarService" should {
    "pass Reload" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.Reload(PlanetSideGUID(10), PlanetSideGUID(40)))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.Reload(PlanetSideGUID(40))))
    }
  }
}

class ChangeAmmoTest extends ActorTest {
  val ammoDef = GlobalDefinitions.energy_cell
  val ammoBox = AmmoBox(ammoDef)

  "AvatarService" should {
    "pass ChangeAmmo" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ChangeAmmo(PlanetSideGUID(10), PlanetSideGUID(40), 0, PlanetSideGUID(40), ammoDef.ObjectId, PlanetSideGUID(41), ammoDef.Packet.ConstructorData(ammoBox).get))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ChangeAmmo(PlanetSideGUID(40), 0, PlanetSideGUID(40), ammoDef.ObjectId, PlanetSideGUID(41), ammoDef.Packet.ConstructorData(ammoBox).get)))
    }
  }
}

class ChangeFireModeTest extends ActorTest {
  val ammoDef = GlobalDefinitions.energy_cell
  val ammoBox = AmmoBox(ammoDef)

  "AvatarService" should {
    "pass ChangeFireMode" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ChangeFireMode(PlanetSideGUID(10), PlanetSideGUID(40), 0))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ChangeFireMode(PlanetSideGUID(40), 0)))
    }
  }
}

class ChangeFireStateStartTest extends ActorTest {
  "AvatarService" should {
    "pass ChangeFireState_Start" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ChangeFireState_Start(PlanetSideGUID(10), PlanetSideGUID(40)))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ChangeFireState_Start(PlanetSideGUID(40))))
    }
  }
}

class ChangeFireStateStopTest extends ActorTest {
  "AvatarService" should {
    "pass ChangeFireState_Stop" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ChangeFireState_Stop(PlanetSideGUID(10), PlanetSideGUID(40)))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ChangeFireState_Stop(PlanetSideGUID(40))))
    }
  }
}

class WeaponDryFireTest extends ActorTest {
  "AvatarService" should {
    "pass WeaponDryFire" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props[AvatarService], AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.WeaponDryFire(PlanetSideGUID(10), PlanetSideGUID(40)))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.WeaponDryFire(PlanetSideGUID(40))))
    }
  }
}

/*
Preparation for these three Release tests is involved.
The ServiceManager must not only be set up correctly, but must be given a TaskResolver.
The AvatarService is started and that starts CorpseRemovalActor, an essential part of this test.
The CorpseRemovalActor needs that TaskResolver created by the ServiceManager;
but, another independent TaskResolver will be needed for manual parts of the test.
(The ServiceManager's TaskResolver can be "borrowed" but that requires writing code to intercept it.)
The Zone needs to be set up and initialized properly with a ZoneActor.
The ZoneActor builds the GUID Actor and the ZonePopulationActor.

ALL of these Actors will talk to each other.
The lines of communication can short circuit if the next Actor does not have the correct information.
Putting Actor startup in the main class, outside of the body of the test, helps.
Frequent pauses to allow everything to sort their messages also helps.
Even with all this work, the tests have a high chance of failure just due to being asynchronous.
 */
class AvatarReleaseTest extends ActorTest {
  ServiceManager.boot(system) ! ServiceManager.Register(RandomPool(1).props(Props[TaskResolver]), "taskResolver")
  val service = system.actorOf(Props[AvatarService], "release-test-service")
  val zone = new Zone("test", new ZoneMap("test-map"), 0)
  val taskResolver = system.actorOf(Props[TaskResolver], "release-test-resolver")
  zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "release-test-zone")
  zone.Actor ! Zone.Init()
  val obj = Player(Avatar("TestCharacter", PlanetSideEmpire.VS, CharacterGender.Female, 1, 1))
  obj.Continent = "test"
  obj.Release

  "AvatarService" should {
    "pass Release" in {
      expectNoMsg(100 milliseconds) //spacer

      service ! Service.Join("test")
      taskResolver ! GUIDTask.RegisterObjectTask(obj)(zone.GUID)
      assert(zone.Corpses.isEmpty)
      zone.Population ! Zone.Corpse.Add(obj)
      expectNoMsg(100 milliseconds) //spacer

      assert(zone.Corpses.size == 1)
      assert(obj.HasGUID)
      val guid = obj.GUID
      service ! AvatarServiceMessage("test", AvatarAction.Release(obj, zone, Some(1000000000))) //alive for one second

      val reply1 = receiveOne(100 milliseconds)
      assert(reply1.isInstanceOf[AvatarServiceResponse])
      val reply1msg = reply1.asInstanceOf[AvatarServiceResponse]
      assert(reply1msg.toChannel == "/test/Avatar")
      assert(reply1msg.avatar_guid == guid)
      assert(reply1msg.replyMessage.isInstanceOf[AvatarResponse.Release])
      assert(reply1msg.replyMessage.asInstanceOf[AvatarResponse.Release].player == obj)

      val reply2 = receiveOne(2 seconds)
      assert(reply2.isInstanceOf[AvatarServiceResponse])
      val reply2msg = reply2.asInstanceOf[AvatarServiceResponse]
      assert(reply2msg.toChannel.equals("/test/Avatar"))
      assert(reply2msg.avatar_guid == Service.defaultPlayerGUID)
      assert(reply2msg.replyMessage.isInstanceOf[AvatarResponse.ObjectDelete])
      assert(reply2msg.replyMessage.asInstanceOf[AvatarResponse.ObjectDelete].item_guid == guid)

      expectNoMsg(1000 milliseconds)
      assert(zone.Corpses.isEmpty)
      assert(!obj.HasGUID)
    }
  }
}

class AvatarReleaseEarly1Test extends ActorTest {
  ServiceManager.boot(system) ! ServiceManager.Register(RandomPool(1).props(Props[TaskResolver]), "taskResolver")
  val service = system.actorOf(Props[AvatarService], "release-test-service")
  val zone = new Zone("test", new ZoneMap("test-map"), 0)
  val taskResolver = system.actorOf(Props[TaskResolver], "release-test-resolver")
  zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "release-test-zone")
  zone.Actor ! Zone.Init()
  val obj = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, 1))
  obj.Continent = "test"
  obj.Release

  "AvatarService" should {
    "pass Release" in {
      expectNoMsg(100 milliseconds) //spacer

      service ! Service.Join("test")
      taskResolver ! GUIDTask.RegisterObjectTask(obj)(zone.GUID)
      assert(zone.Corpses.isEmpty)
      zone.Population ! Zone.Corpse.Add(obj)
      expectNoMsg(100 milliseconds) //spacer

      assert(zone.Corpses.size == 1)
      assert(obj.HasGUID)
      val guid = obj.GUID
      service ! AvatarServiceMessage("test", AvatarAction.Release(obj, zone)) //3+ minutes!

      val reply1 = receiveOne(100 milliseconds)
      assert(reply1.isInstanceOf[AvatarServiceResponse])
      val reply1msg = reply1.asInstanceOf[AvatarServiceResponse]
      assert(reply1msg.toChannel == "/test/Avatar")
      assert(reply1msg.avatar_guid == guid)
      assert(reply1msg.replyMessage.isInstanceOf[AvatarResponse.Release])
      assert(reply1msg.replyMessage.asInstanceOf[AvatarResponse.Release].player == obj)

      service ! AvatarServiceMessage.RemoveSpecificCorpse(List(obj)) //IMPORTANT: ONE ENTRY
      val reply2 = receiveOne(100 milliseconds)
      assert(reply2.isInstanceOf[AvatarServiceResponse])
      val reply2msg = reply2.asInstanceOf[AvatarServiceResponse]
      assert(reply2msg.toChannel.equals("/test/Avatar"))
      assert(reply2msg.avatar_guid == Service.defaultPlayerGUID)
      assert(reply2msg.replyMessage.isInstanceOf[AvatarResponse.ObjectDelete])
      assert(reply2msg.replyMessage.asInstanceOf[AvatarResponse.ObjectDelete].item_guid == guid)

      expectNoMsg(600 milliseconds)
      assert(zone.Corpses.isEmpty)
      assert(!obj.HasGUID)
    }
  }
}

class AvatarReleaseEarly2Test extends ActorTest {
  ServiceManager.boot(system) ! ServiceManager.Register(RandomPool(1).props(Props[TaskResolver]), "taskResolver")
  val service = system.actorOf(Props[AvatarService], "release-test-service")
  val zone = new Zone("test", new ZoneMap("test-map"), 0)
  val taskResolver = system.actorOf(Props[TaskResolver], "release-test-resolver")
  zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "release-test-zone")
  zone.Actor ! Zone.Init()
  val objAlt = Player(Avatar("TestCharacter2", PlanetSideEmpire.NC, CharacterGender.Male, 1, 1)) //necessary clutter
  val obj = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, 1))
  obj.Continent = "test"
  obj.Release

  "AvatarService" should {
    "pass Release" in {
      expectNoMsg(100 milliseconds) //spacer

      service ! Service.Join("test")
      taskResolver ! GUIDTask.RegisterObjectTask(obj)(zone.GUID)
      assert(zone.Corpses.isEmpty)
      zone.Population ! Zone.Corpse.Add(obj)
      expectNoMsg(100 milliseconds) //spacer

      assert(zone.Corpses.size == 1)
      assert(obj.HasGUID)
      val guid = obj.GUID
      service ! AvatarServiceMessage("test", AvatarAction.Release(obj, zone)) //3+ minutes!

      val reply1 = receiveOne(100 milliseconds)
      assert(reply1.isInstanceOf[AvatarServiceResponse])
      val reply1msg = reply1.asInstanceOf[AvatarServiceResponse]
      assert(reply1msg.toChannel == "/test/Avatar")
      assert(reply1msg.avatar_guid == guid)
      assert(reply1msg.replyMessage.isInstanceOf[AvatarResponse.Release])
      assert(reply1msg.replyMessage.asInstanceOf[AvatarResponse.Release].player == obj)

      service ! AvatarServiceMessage.RemoveSpecificCorpse(List(objAlt, obj)) //IMPORTANT: TWO ENTRIES
      val reply2 = receiveOne(100 milliseconds)
      assert(reply2.isInstanceOf[AvatarServiceResponse])
      val reply2msg = reply2.asInstanceOf[AvatarServiceResponse]
      assert(reply2msg.toChannel.equals("/test/Avatar"))
      assert(reply2msg.avatar_guid == Service.defaultPlayerGUID)
      assert(reply2msg.replyMessage.isInstanceOf[AvatarResponse.ObjectDelete])
      assert(reply2msg.replyMessage.asInstanceOf[AvatarResponse.ObjectDelete].item_guid == guid)

      expectNoMsg(600 milliseconds)
      assert(zone.Corpses.isEmpty)
      assert(!obj.HasGUID)
    }
  }
}

object AvatarServiceTest {
  import java.util.concurrent.atomic.AtomicInteger
  private val number = new AtomicInteger(1)
  
  def TestName : String = {
    s"service${number.getAndIncrement()}"
  }
}

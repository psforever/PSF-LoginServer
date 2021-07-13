// Copyright (c) 2017 PSForever
package actor.service

import akka.actor.Props
import akka.testkit.TestProbe
import scala.concurrent.duration._

import actor.base.{ActorTest, FreedContextActorTest}
import net.psforever.objects._
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.packet.game.objectcreate.{DroppedItemData, ObjectClass, ObjectCreateMessageParent, PlacementData}
import net.psforever.packet.game.{ObjectCreateMessage, PlayerStateMessageUpstream}
import net.psforever.types._
import net.psforever.services.{RemoverActor, Service, ServiceManager}
import net.psforever.services.avatar._

class AvatarService1Test extends ActorTest {
  "AvatarService" should {
    "construct" in {
      ServiceManager.boot(system)
      system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      assert(true)
    }
  }
}

class AvatarService2Test extends ActorTest {
  "AvatarService" should {
    "subscribe" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      assert(true)
    }
  }
}

class AvatarService3Test extends ActorTest {
  "AvatarService" should {
    ServiceManager.boot(system)
    "subscribe to a specific channel" in {
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
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
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
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
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! "hello"
      expectNoMessage()
    }
  }
}

class ArmorChangedTest extends ActorTest {
  "AvatarService" should {
    "pass ArmorChanged" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ArmorChanged(PlanetSideGUID(10), ExoSuitType.Reinforced, 0))
      expectMsg(
        AvatarServiceResponse(
          "/test/Avatar",
          PlanetSideGUID(10),
          AvatarResponse.ArmorChanged(ExoSuitType.Reinforced, 0)
        )
      )
    }
  }
}

class ConcealPlayerTest extends ActorTest {
  "AvatarService" should {
    "pass ConcealPlayer" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ConcealPlayer(PlanetSideGUID(10)))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ConcealPlayer()))
    }
  }
}

class EquipmentInHandTest extends ActorTest {
  ServiceManager.boot(system)
  val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), "release-test-service")
  val toolDef = GlobalDefinitions.beamer
  val tool    = Tool(toolDef)
  tool.GUID = PlanetSideGUID(40)
  tool.AmmoSlots.head.Box.GUID = PlanetSideGUID(41)
  val pkt = ObjectCreateMessage(
    toolDef.ObjectId,
    tool.GUID,
    ObjectCreateMessageParent(PlanetSideGUID(11), 2),
    toolDef.Packet.ConstructorData(tool).get
  )

  "AvatarService" should {
    "pass EquipmentInHand" in {
      service ! Service.Join("test")
      service ! AvatarServiceMessage(
        "test",
        AvatarAction.EquipmentInHand(PlanetSideGUID(10), PlanetSideGUID(11), 2, tool)
      )
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.EquipmentInHand(pkt)))
    }
  }
}

class DeployItemTest extends ActorTest {
  ServiceManager.boot(system)
  val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), "deploy-item-test-service")
  val objDef  = GlobalDefinitions.motionalarmsensor
  val obj     = new SensorDeployable(objDef)
  obj.Position = Vector3(1, 2, 3)
  obj.Orientation = Vector3(4, 5, 6)
  obj.GUID = PlanetSideGUID(40)
  val pkt = ObjectCreateMessage(
    objDef.ObjectId,
    obj.GUID,
    objDef.Packet.ConstructorData(obj).get
  )

  "AvatarService" should {
    "pass DeployItem" in {
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.DeployItem(PlanetSideGUID(10), obj))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.DropItem(pkt)))
    }
  }
}

class DroptItemTest extends ActorTest {
  ServiceManager.boot(system)
  val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), "release-test-service")
  val toolDef = GlobalDefinitions.beamer
  val tool    = Tool(toolDef)
  tool.Position = Vector3(1, 2, 3)
  tool.Orientation = Vector3(4, 5, 6)
  tool.GUID = PlanetSideGUID(40)
  tool.AmmoSlots.head.Box.GUID = PlanetSideGUID(41)
  val pkt = ObjectCreateMessage(
    toolDef.ObjectId,
    tool.GUID,
    DroppedItemData(
      PlacementData(tool.Position, tool.Orientation),
      toolDef.Packet.ConstructorData(tool).get
    )
  )

  "AvatarService" should {
    "pass DropItem" in {
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.DropItem(PlanetSideGUID(10), tool))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.DropItem(pkt)))
    }
  }
}

class LoadPlayerTest extends ActorTest {
  val obj = Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.VS, CharacterSex.Female, 1, CharacterVoice.Voice1))
  obj.GUID = PlanetSideGUID(10)
  obj.Slot(5).Equipment.get.GUID = PlanetSideGUID(11)
  val c1data = obj.Definition.Packet.DetailedConstructorData(obj).get
  val pkt1   = ObjectCreateMessage(ObjectClass.avatar, PlanetSideGUID(10), c1data)
  val parent = ObjectCreateMessageParent(PlanetSideGUID(12), 0)
  obj.VehicleSeated = PlanetSideGUID(12)
  val c2data = obj.Definition.Packet.DetailedConstructorData(obj).get
  val pkt2   = ObjectCreateMessage(ObjectClass.avatar, PlanetSideGUID(10), parent, c2data)

  "AvatarService" should {
    "pass LoadPlayer" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      //no parent data
      service ! AvatarServiceMessage(
        "test",
        AvatarAction.LoadPlayer(PlanetSideGUID(20), ObjectClass.avatar, PlanetSideGUID(10), c1data, None)
      )
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(20), AvatarResponse.LoadPlayer(pkt1)))
      //parent data
      service ! AvatarServiceMessage(
        "test",
        AvatarAction.LoadPlayer(PlanetSideGUID(20), ObjectClass.avatar, PlanetSideGUID(10), c2data, Some(parent))
      )
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(20), AvatarResponse.LoadPlayer(pkt2)))
    }
  }
}

class ObjectDeleteTest extends ActorTest {
  "AvatarService" should {
    "pass ObjectDelete" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ObjectDelete(PlanetSideGUID(10), PlanetSideGUID(11)))
      expectMsg(
        AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ObjectDelete(PlanetSideGUID(11), 0))
      )

      service ! AvatarServiceMessage("test", AvatarAction.ObjectDelete(PlanetSideGUID(10), PlanetSideGUID(11), 55))
      expectMsg(
        AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ObjectDelete(PlanetSideGUID(11), 55))
      )
    }
  }
}

class ObjectHeldTest extends ActorTest {
  "AvatarService" should {
    "pass ObjectHeld" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ObjectHeld(PlanetSideGUID(10), 1))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ObjectHeld(1)))
    }
  }
}

class PutDownFDUTest extends ActorTest {
  "AvatarService" should {
    "pass PutDownFDU" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.PutDownFDU(PlanetSideGUID(10)))
      expectMsg(
        AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.PutDownFDU(PlanetSideGUID(10)))
      )
    }
  }
}

class PlanetsideAttributeTest extends ActorTest {
  "AvatarService" should {
    "pass PlanetsideAttribute" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.PlanetsideAttribute(PlanetSideGUID(10), 5, 1200L))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.PlanetsideAttribute(5, 1200L)))
    }
  }
}

class PlayerStateTest extends ActorTest {
  val msg = PlayerStateMessageUpstream(
    PlanetSideGUID(75),
    Vector3(3694.1094f, 2735.4531f, 90.84375f),
    Some(Vector3(4.375f, 2.59375f, 0.0f)),
    61.875f,
    351.5625f,
    0.0f,
    136,
    0,
    false,
    false,
    false,
    false,
    112,
    0
  )

  "AvatarService" should {
    "pass PlayerState" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage(
        "test",
        AvatarAction.PlayerState(
          PlanetSideGUID(10),
          Vector3(3694.1094f, 2735.4531f, 90.84375f),
          Some(Vector3(4.375f, 2.59375f, 0.0f)),
          61.875f,
          351.5625f,
          0.0f,
          136,
          false,
          false,
          false,
          false,
          false,
          false
        )
      )
      expectMsg(
        AvatarServiceResponse(
          "/test/Avatar",
          PlanetSideGUID(10),
          AvatarResponse.PlayerState(
            Vector3(3694.1094f, 2735.4531f, 90.84375f),
            Some(Vector3(4.375f, 2.59375f, 0.0f)),
            61.875f,
            351.5625f,
            0.0f,
            136,
            false,
            false,
            false,
            false,
            false,
            false
          )
        )
      )
    }
  }
}

class PickupItemTest extends ActorTest {
  val obj  = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.VS, CharacterSex.Female, 1, CharacterVoice.Voice1))
  val tool = Tool(GlobalDefinitions.beamer)
  tool.GUID = PlanetSideGUID(40)

  "pass PickUpItem" in {
    ServiceManager.boot(system)
    val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
    service ! Service.Join("test")
    service ! AvatarServiceMessage("test", AvatarAction.PickupItem(PlanetSideGUID(10), tool))
    expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ObjectDelete(tool.GUID, 0)))
  }
}

class ReloadTest extends ActorTest {
  "AvatarService" should {
    "pass Reload" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
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
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage(
        "test",
        AvatarAction.ChangeAmmo(
          PlanetSideGUID(10),
          PlanetSideGUID(40),
          0,
          PlanetSideGUID(40),
          ammoDef.ObjectId,
          PlanetSideGUID(41),
          ammoDef.Packet.ConstructorData(ammoBox).get
        )
      )
      expectMsg(
        AvatarServiceResponse(
          "/test/Avatar",
          PlanetSideGUID(10),
          AvatarResponse.ChangeAmmo(
            PlanetSideGUID(40),
            0,
            PlanetSideGUID(40),
            ammoDef.ObjectId,
            PlanetSideGUID(41),
            ammoDef.Packet.ConstructorData(ammoBox).get
          )
        )
      )
    }
  }
}

class ChangeFireModeTest extends ActorTest {
  val ammoDef = GlobalDefinitions.energy_cell
  val ammoBox = AmmoBox(ammoDef)

  "AvatarService" should {
    "pass ChangeFireMode" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ChangeFireMode(PlanetSideGUID(10), PlanetSideGUID(40), 0))
      expectMsg(
        AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ChangeFireMode(PlanetSideGUID(40), 0))
      )
    }
  }
}

class ChangeFireStateStartTest extends ActorTest {
  "AvatarService" should {
    "pass ChangeFireState_Start" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ChangeFireState_Start(PlanetSideGUID(10), PlanetSideGUID(40)))
      expectMsg(
        AvatarServiceResponse(
          "/test/Avatar",
          PlanetSideGUID(10),
          AvatarResponse.ChangeFireState_Start(PlanetSideGUID(40))
        )
      )
    }
  }
}

class ChangeFireStateStopTest extends ActorTest {
  "AvatarService" should {
    "pass ChangeFireState_Stop" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ChangeFireState_Stop(PlanetSideGUID(10), PlanetSideGUID(40)))
      expectMsg(
        AvatarServiceResponse(
          "/test/Avatar",
          PlanetSideGUID(10),
          AvatarResponse.ChangeFireState_Stop(PlanetSideGUID(40))
        )
      )
    }
  }
}

class WeaponDryFireTest extends ActorTest {
  "AvatarService" should {
    "pass WeaponDryFire" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.WeaponDryFire(PlanetSideGUID(10), PlanetSideGUID(40)))
      expectMsg(
        AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.WeaponDryFire(PlanetSideGUID(40)))
      )
    }
  }
}

class AvatarStowEquipmentTest extends ActorTest {
  val tool = Tool(GlobalDefinitions.beamer)

  "AvatarService" should {
    "pass StowEquipment" in {
      ServiceManager.boot(system)
      val service = system.actorOf(Props(classOf[AvatarService], Zone.Nowhere), AvatarServiceTest.TestName)
      service ! Service.Join("test")
      service ! AvatarServiceMessage(
        "test",
        AvatarAction.StowEquipment(PlanetSideGUID(10), PlanetSideGUID(11), 2, tool)
      )
      expectMsg(
        AvatarServiceResponse(
          "/test/Avatar",
          PlanetSideGUID(10),
          AvatarResponse.StowEquipment(PlanetSideGUID(11), 2, tool)
        )
      )
    }
  }
}

/*
Preparation for these three Release tests is involved.
The ServiceManager must be set up correctly.
The Zone needs to be set up and initialized properly with a ZoneActor.
The ZoneActor builds the GUID Actor and the ZonePopulationActor.

ALL of these Actors will talk to each other.
The lines of communication can short circuit if the next Actor does not have the correct information.
Putting Actor startup in the main class, outside of the body of the test, helps.
Frequent pauses to allow everything to sort their messages also helps.
Even with all this work, the tests have a high chance of failure just due to being asynchronous.
 */
class AvatarReleaseTest extends FreedContextActorTest {
  val guid: NumberPoolHub = new NumberPoolHub(new MaxNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test-map"), 0) {
    override def SetupNumberPools() : Unit = { }
    GUID(guid)
  }
  zone.init(context)
  val obj = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.VS, CharacterSex.Female, 1, CharacterVoice.Voice1))
  guid.register(obj)
  guid.register(obj.Slot(5).Equipment.get)
  obj.Zone = zone
  obj.Release
  val subscriber = new TestProbe(system)

  "AvatarService" should {
    "pass Release" in {
      expectNoMessage(100 milliseconds) //spacer

      zone.AvatarEvents.tell(Service.Join("test"), subscriber.ref)
      assert(zone.Corpses.isEmpty)
      zone.Population ! Zone.Corpse.Add(obj)
      subscriber.expectNoMessage(200 milliseconds) //spacer

      assert(zone.Corpses.size == 1)
      assert(obj.HasGUID)
      val guid = obj.GUID
      zone.AvatarEvents ! AvatarServiceMessage("test", AvatarAction.Release(obj, zone, Some(1 second))) //alive for one second

      val reply1 = subscriber.receiveOne(200 milliseconds)
      assert(reply1.isInstanceOf[AvatarServiceResponse])
      val reply1msg = reply1.asInstanceOf[AvatarServiceResponse]
      assert(reply1msg.channel == "/test/Avatar")
      assert(reply1msg.avatar_guid == guid)
      assert(reply1msg.replyMessage.isInstanceOf[AvatarResponse.Release])
      assert(reply1msg.replyMessage.asInstanceOf[AvatarResponse.Release].player == obj)

      val reply2 = subscriber.receiveOne(2 seconds)
      assert(reply2.isInstanceOf[AvatarServiceResponse])
      val reply2msg = reply2.asInstanceOf[AvatarServiceResponse]
      assert(reply2msg.channel.equals("/test/Avatar"))
      assert(reply2msg.avatar_guid == Service.defaultPlayerGUID)
      assert(reply2msg.replyMessage.isInstanceOf[AvatarResponse.ObjectDelete])
      assert(reply2msg.replyMessage.asInstanceOf[AvatarResponse.ObjectDelete].item_guid == guid)

      subscriber.expectNoMessage(1 seconds)
      assert(zone.Corpses.isEmpty)
      assert(!obj.HasGUID)
    }
  }
}

class AvatarReleaseEarly1Test extends FreedContextActorTest {
  val guid: NumberPoolHub = new NumberPoolHub(new MaxNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test-map"), 0) {
    override def SetupNumberPools() : Unit = { }
    GUID(guid)
  }
  zone.init(context)
  val obj = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.VS, CharacterSex.Female, 1, CharacterVoice.Voice1))
  guid.register(obj)
  guid.register(obj.Slot(5).Equipment.get)
  obj.Zone = zone
  obj.Release
  val subscriber = new TestProbe(system)

  "AvatarService" should {
    "pass Release" in {
      expectNoMessage(100 milliseconds) //spacer

      zone.AvatarEvents.tell(Service.Join("test"), subscriber.ref)
      assert(zone.Corpses.isEmpty)
      zone.Population ! Zone.Corpse.Add(obj)
      subscriber.expectNoMessage(200 milliseconds) //spacer

      assert(zone.Corpses.size == 1)
      assert(obj.HasGUID)
      val guid = obj.GUID
      zone.AvatarEvents ! AvatarServiceMessage("test", AvatarAction.Release(obj, zone)) //3+ minutes!

      val reply1 = subscriber.receiveOne(200 milliseconds)
      assert(reply1.isInstanceOf[AvatarServiceResponse])
      val reply1msg = reply1.asInstanceOf[AvatarServiceResponse]
      assert(reply1msg.channel == "/test/Avatar")
      assert(reply1msg.avatar_guid == guid)
      assert(reply1msg.replyMessage.isInstanceOf[AvatarResponse.Release])
      assert(reply1msg.replyMessage.asInstanceOf[AvatarResponse.Release].player == obj)

      zone.AvatarEvents ! AvatarServiceMessage.Corpse(RemoverActor.HurrySpecific(List(obj), zone)) //IMPORTANT: ONE ENTRY
      val reply2 = subscriber.receiveOne(200 milliseconds)
      assert(reply2.isInstanceOf[AvatarServiceResponse])
      val reply2msg = reply2.asInstanceOf[AvatarServiceResponse]
      assert(reply2msg.channel.equals("/test/Avatar"))
      assert(reply2msg.avatar_guid == Service.defaultPlayerGUID)
      assert(reply2msg.replyMessage.isInstanceOf[AvatarResponse.ObjectDelete])
      assert(reply2msg.replyMessage.asInstanceOf[AvatarResponse.ObjectDelete].item_guid == guid)

      subscriber.expectNoMessage(1 seconds)
      assert(zone.Corpses.isEmpty)
      assert(!obj.HasGUID)
    }
  }
}

class AvatarReleaseEarly2Test extends FreedContextActorTest {
  val guid: NumberPoolHub = new NumberPoolHub(new MaxNumberSource(15))
  val zone = new Zone("test", new ZoneMap("test-map"), 0) {
    override def SetupNumberPools() : Unit = { }
    GUID(guid)
  }
  zone.init(context)
  val obj = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.VS, CharacterSex.Female, 1, CharacterVoice.Voice1))
  guid.register(obj)
  guid.register(obj.Slot(5).Equipment.get)
  obj.Zone = zone
  obj.Release
  val objAlt = Player(
    Avatar(0, "TestCharacter2", PlanetSideEmpire.NC, CharacterSex.Male, 1, CharacterVoice.Voice1)
  ) //necessary clutter
  objAlt.GUID = PlanetSideGUID(3)
  objAlt.Slot(5).Equipment.get.GUID = PlanetSideGUID(4)
  objAlt.Zone = zone
  val subscriber = new TestProbe(system)

  "AvatarService" should {
    "pass Release" in {
      expectNoMessage(100 milliseconds) //spacer

      zone.AvatarEvents.tell(Service.Join("test"), subscriber.ref)
      assert(zone.Corpses.isEmpty)
      zone.Population ! Zone.Corpse.Add(obj)
      subscriber.expectNoMessage(200 milliseconds) //spacer

      assert(zone.Corpses.size == 1)
      assert(obj.HasGUID)
      val guid = obj.GUID
      zone.AvatarEvents ! AvatarServiceMessage("test", AvatarAction.Release(obj, zone)) //3+ minutes!

      val reply1 = subscriber.receiveOne(200 milliseconds)
      assert(reply1.isInstanceOf[AvatarServiceResponse])
      val reply1msg = reply1.asInstanceOf[AvatarServiceResponse]
      assert(reply1msg.channel == "/test/Avatar")
      assert(reply1msg.avatar_guid == guid)
      assert(reply1msg.replyMessage.isInstanceOf[AvatarResponse.Release])
      assert(reply1msg.replyMessage.asInstanceOf[AvatarResponse.Release].player == obj)

      zone.AvatarEvents ! AvatarServiceMessage.Corpse(
        RemoverActor.HurrySpecific(List(objAlt, obj), zone)
      ) //IMPORTANT: TWO ENTRIES
      val reply2 = subscriber.receiveOne(100 milliseconds)
      assert(reply2.isInstanceOf[AvatarServiceResponse])
      val reply2msg = reply2.asInstanceOf[AvatarServiceResponse]
      assert(reply2msg.channel.equals("/test/Avatar"))
      assert(reply2msg.avatar_guid == Service.defaultPlayerGUID)
      assert(reply2msg.replyMessage.isInstanceOf[AvatarResponse.ObjectDelete])
      assert(reply2msg.replyMessage.asInstanceOf[AvatarResponse.ObjectDelete].item_guid == guid)

      subscriber.expectNoMessage(1 seconds)
      assert(zone.Corpses.isEmpty)
      assert(!obj.HasGUID)
    }
  }
}

object AvatarServiceTest {
  import java.util.concurrent.atomic.AtomicInteger
  private val number = new AtomicInteger(1)

  def TestName: String = {
    s"service${number.getAndIncrement()}"
  }
}

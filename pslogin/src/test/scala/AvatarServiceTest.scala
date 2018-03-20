// Copyright (c) 2017 PSForever
import akka.actor.Props
import net.psforever.objects._
import net.psforever.packet.game.{PlanetSideGUID, PlayerStateMessageUpstream}
import net.psforever.types.{CharacterGender, ExoSuitType, PlanetSideEmpire, Vector3}
import services.Service
import services.avatar._

class AvatarService1Test extends ActorTest {
  "AvatarService" should {
    "construct" in {
      system.actorOf(Props[AvatarService], "service")
      assert(true)
    }
  }
}

class AvatarService2Test extends ActorTest {
  "AvatarService" should {
    "subscribe" in {
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      assert(true)
    }
  }
}

class AvatarService3Test extends ActorTest {
  "AvatarService" should {
    "subscribe to a specific channel" in {
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      service ! Service.Leave()
      assert(true)
    }
  }
}

class AvatarService4Test extends ActorTest {
  "AvatarService" should {
    "subscribe" in {
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      service ! Service.LeaveAll()
      assert(true)
    }
  }
}

class AvatarService5Test extends ActorTest {
  "AvatarService" should {
    "pass an unhandled message" in {
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      service ! "hello"
      expectNoMsg()
    }
  }
}

class ArmorChangedTest extends ActorTest {
  "AvatarService" should {
    "pass ArmorChanged" in {
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ArmorChanged(PlanetSideGUID(10), ExoSuitType.Reinforced, 0))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ArmorChanged(ExoSuitType.Reinforced, 0)))
    }
  }
}

class ConcealPlayerTest extends ActorTest {
  "AvatarService" should {
    "pass ConcealPlayer" in {
      val service = system.actorOf(Props[AvatarService], "service")
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
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.EquipmentInHand(PlanetSideGUID(10), 2, tool))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.EquipmentInHand(2, tool)))
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
      val service = system.actorOf(Props[AvatarService], "service")
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
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.LoadPlayer(PlanetSideGUID(10), pdata))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.LoadPlayer(pdata)))
    }
  }
}

class ObjectDeleteTest extends ActorTest {
  "AvatarService" should {
    "pass ObjectDelete" in {
      val service = system.actorOf(Props[AvatarService], "service")
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
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ObjectHeld(PlanetSideGUID(10), 1))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ObjectHeld(1)))
    }
  }
}

class PlanetsideAttributeTest extends ActorTest {
  "AvatarService" should {
    "pass PlanetsideAttribute" in {
      val service = system.actorOf(Props[AvatarService], "service")
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
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.PlayerState(PlanetSideGUID(10), msg, false, false))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.PlayerState(msg, false, false)))
    }
  }
}

class ReloadTest extends ActorTest {
  "AvatarService" should {
    "pass Reload" in {
      val service = system.actorOf(Props[AvatarService], "service")
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
      val service = system.actorOf(Props[AvatarService], "service")
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
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ChangeFireMode(PlanetSideGUID(10), PlanetSideGUID(40), 0))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ChangeFireMode(PlanetSideGUID(40), 0)))
    }
  }
}

class ChangeFireStateStartTest extends ActorTest {
  "AvatarService" should {
    "pass ChangeFireState_Start" in {
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ChangeFireState_Start(PlanetSideGUID(10), PlanetSideGUID(40)))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ChangeFireState_Start(PlanetSideGUID(40))))
    }
  }
}

class ChangeFireStateStopTest extends ActorTest {
  "AvatarService" should {
    "pass ChangeFireState_Stop" in {
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.ChangeFireState_Stop(PlanetSideGUID(10), PlanetSideGUID(40)))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.ChangeFireState_Stop(PlanetSideGUID(40))))
    }
  }
}

class WeaponDryFireTest extends ActorTest {
  "AvatarService" should {
    "pass WeaponDryFire" in {
      val service = system.actorOf(Props[AvatarService], "service")
      service ! Service.Join("test")
      service ! AvatarServiceMessage("test", AvatarAction.WeaponDryFire(PlanetSideGUID(10), PlanetSideGUID(40)))
      expectMsg(AvatarServiceResponse("/test/Avatar", PlanetSideGUID(10), AvatarResponse.WeaponDryFire(PlanetSideGUID(40))))
    }
  }
}

object AvatarServiceTest {
  //decoy
}

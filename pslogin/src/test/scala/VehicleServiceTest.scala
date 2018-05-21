// Copyright (c) 2017 PSForever
import akka.actor.Props
import net.psforever.objects._
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types._
import services.{Service, ServiceManager}
import services.vehicle._

class VehicleService1Test extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "construct" in {
      system.actorOf(Props[VehicleService], "v-service")
      assert(true)
    }
  }
}

class VehicleService2Test extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "subscribe" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      assert(true)
    }
  }
}

class VehicleService3Test extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "subscribe to a specific channel" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! Service.Leave()
      assert(true)
    }
  }
}

class VehicleService4Test extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "subscribe" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! Service.LeaveAll()
      assert(true)
    }
  }
}

class VehicleService5Test extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "pass an unhandled message" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! "hello"
      expectNoMsg()
    }
  }
}

class AwarenessTest extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "pass Awareness" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.Awareness(PlanetSideGUID(10), PlanetSideGUID(11)))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.Awareness(PlanetSideGUID(11))))
    }
  }
}

class ChildObjectStateTest extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "pass ChildObjectState" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.ChildObjectState(PlanetSideGUID(10), PlanetSideGUID(11), 1.2f, 3.4f))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.ChildObjectState(PlanetSideGUID(11), 1.2f, 3.4f)))
    }
  }
}

class DeployRequestTest extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "pass DeployRequest" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.DeployRequest(PlanetSideGUID(10), PlanetSideGUID(11), DriveState.Mobile, 0, false, Vector3(1.2f, 3.4f, 5.6f)))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.DeployRequest(PlanetSideGUID(11), DriveState.Mobile, 0, false, Vector3(1.2f, 3.4f, 5.6f))))
    }
  }
}

class DismountVehicleTest extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "pass DismountVehicle" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.DismountVehicle(PlanetSideGUID(10), BailType.Normal, false))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.DismountVehicle(BailType.Normal, false)))
    }
  }
}

class InventoryStateTest extends ActorTest {
  ServiceManager.boot(system)
  val tool = Tool(GlobalDefinitions.beamer)
  tool.AmmoSlots.head.Box.GUID = PlanetSideGUID(13)
  val cdata = tool.Definition.Packet.ConstructorData(tool).get

  "VehicleService" should {
    "pass InventoryState" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.InventoryState(PlanetSideGUID(10), tool, PlanetSideGUID(11), 0, cdata))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.InventoryState(tool, PlanetSideGUID(11), 0, cdata)))
    }
  }
}

class InventoryState2Test extends ActorTest {
  ServiceManager.boot(system)
  val tool = Tool(GlobalDefinitions.beamer)
  tool.AmmoSlots.head.Box.GUID = PlanetSideGUID(13)
  val cdata = tool.Definition.Packet.ConstructorData(tool).get

  "VehicleService" should {
    "pass InventoryState2" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.InventoryState2(PlanetSideGUID(10), PlanetSideGUID(11), PlanetSideGUID(12), 13))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.InventoryState2(PlanetSideGUID(11), PlanetSideGUID(12), 13)))
    }
  }
}

class KickPassengerTest extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "pass KickPassenger" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.KickPassenger(PlanetSideGUID(10), 0, false, PlanetSideGUID(11)))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.KickPassenger(0, false, PlanetSideGUID(11))))
    }
  }
}

class LoadVehicleTest extends ActorTest {
  ServiceManager.boot(system)
  val vehicle = Vehicle(GlobalDefinitions.quadstealth)
  val cdata = vehicle.Definition.Packet.ConstructorData(vehicle).get

  "VehicleService" should {
    "pass LoadVehicle" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.LoadVehicle(PlanetSideGUID(10), vehicle, 12, PlanetSideGUID(11), cdata))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.LoadVehicle(vehicle, 12, PlanetSideGUID(11), cdata)))
    }
  }
}

class MountVehicleTest extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "pass MountVehicle" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.MountVehicle(PlanetSideGUID(10), PlanetSideGUID(11), 0))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.MountVehicle(PlanetSideGUID(11), 0)))
    }
  }
}

class SeatPermissionsTest extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "pass SeatPermissions" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.SeatPermissions(PlanetSideGUID(10), PlanetSideGUID(11), 0, 12L))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.SeatPermissions(PlanetSideGUID(11), 0, 12L)))
    }
  }
}

class StowEquipmentTest extends ActorTest {
  ServiceManager.boot(system)
  val tool = Tool(GlobalDefinitions.beamer)
  tool.GUID = PlanetSideGUID(12)
  tool.AmmoSlots.head.Box.GUID = PlanetSideGUID(13)
  val toolDef = tool.Definition
  val cdata = tool.Definition.Packet.DetailedConstructorData(tool).get

  "StowEquipment" should {
    "pass StowEquipment" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.StowEquipment(PlanetSideGUID(10), PlanetSideGUID(11), 0, tool))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.StowEquipment(PlanetSideGUID(11), 0, toolDef.ObjectId, tool.GUID, cdata)))
    }
  }
}

class UnstowEquipmentTest extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "pass UnstowEquipment" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.UnstowEquipment(PlanetSideGUID(10), PlanetSideGUID(11)))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.UnstowEquipment(PlanetSideGUID(11))))
    }
  }
}

class VehicleStateTest extends ActorTest {
  ServiceManager.boot(system)

  "VehicleService" should {
    "pass VehicleState" in {
      val service = system.actorOf(Props[VehicleService], "v-service")
      service ! Service.Join("test")
      service ! VehicleServiceMessage("test", VehicleAction.VehicleState(PlanetSideGUID(10), PlanetSideGUID(11), 0, Vector3(1.2f, 3.4f, 5.6f), Vector3(7.8f, 9.1f, 2.3f), Some(Vector3(4.5f, 6.7f, 8.9f)), Option(1), 2, 3, 4, false, true))
      expectMsg(VehicleServiceResponse("/test/Vehicle", PlanetSideGUID(10), VehicleResponse.VehicleState(PlanetSideGUID(11), 0, Vector3(1.2f, 3.4f, 5.6f), Vector3(7.8f, 9.1f, 2.3f), Some(Vector3(4.5f, 6.7f, 8.9f)), Option(1), 2, 3, 4, false, true)))
    }
  }
}

object VehicleServiceTest {
  //decoy
}

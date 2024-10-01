// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import base.{ActorTest, FreedContextActorTest}
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.{GlobalDefinitions, Ntu, Player, Vehicle}
import net.psforever.objects.serverobject.resourcesilo.{ResourceSilo, ResourceSiloControl, ResourceSiloDefinition}
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.serverobject.transfer.TransferBehavior
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.packet.game.UseItemMessage
import net.psforever.types._
import org.specs2.mutable.Specification
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.objects.avatar.Avatar
import net.psforever.services.{InterstellarClusterService, Service, ServiceManager}
import net.psforever.services.galaxy.GalaxyService

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._

class ResourceSiloTest extends Specification {
  "Resource Silo" should {
    "define" in {
      val obj = new ResourceSiloDefinition
      obj.ObjectId mustEqual 731
    }

    "construct" in {
      val obj = ResourceSilo()
      obj.Definition mustEqual GlobalDefinitions.resource_silo
      obj.MaxNtuCapacitor mustEqual 1000
      obj.NtuCapacitor mustEqual 1000
      obj.LowNtuWarningOn mustEqual true
      obj.CapacitorDisplay mustEqual 10
      //
      obj.NtuCapacitor = 50
      obj.LowNtuWarningOn = false
      obj.NtuCapacitor mustEqual 50
      obj.LowNtuWarningOn mustEqual false
      obj.CapacitorDisplay mustEqual 1
    }

    "charge level can not exceed limits(0 to maximum)" in {
      val obj = ResourceSilo()
      obj.NtuCapacitor mustEqual 1000
      obj.NtuCapacitor = -5
      obj.NtuCapacitor mustEqual 0

      obj.NtuCapacitor = obj.MaxNtuCapacitor + 100
      obj.NtuCapacitor mustEqual 1000
      obj.NtuCapacitor mustEqual obj.MaxNtuCapacitor
    }

    "using the silo generates a charge event" in {
      val msg = UseItemMessage(
        PlanetSideGUID(1),
        PlanetSideGUID(0),
        PlanetSideGUID(2),
        0L,
        unk3 = false,
        Vector3(0f, 0f, 0f),
        Vector3(0f, 0f, 0f),
        0,
        0,
        0,
        0L
      ) //faked
      ResourceSilo().Use(ResourceSiloTest.player, msg) mustEqual ResourceSilo.ChargeEvent()
    }
  }
}

class ResourceSiloControlStartupTest extends ActorTest {
  val obj: ResourceSilo = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val buildingEvents: TestProbe = TestProbe("test-building-events")
  obj.Owner =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building) {
      Actor = buildingEvents.ref
    }
  obj.Owner.GUID = PlanetSideGUID(6)

  "Resource silo" should {
    "startup properly" in {
      obj.Actor ! Service.Startup()
      expectNoMessage(max = 1000 milliseconds)
    }
  }
}

class ResourceSiloControlStartupMessageNoneTest extends ActorTest {
  val obj: ResourceSilo = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val buildingEvents: TestProbe = TestProbe("test-building-events")
  obj.Owner =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building) {
      Actor = buildingEvents.ref
    }
  obj.Owner.GUID = PlanetSideGUID(6)

  "Resource silo" should {
    "report if it has no NTU on startup" in {
      obj.NtuCapacitor = 0
      assert(obj.NtuCapacitor == 0)
      obj.Actor ! Service.Startup()
      val ownerMsg = buildingEvents.receiveOne(200 milliseconds)
      assert(ownerMsg match {
        case BuildingActor.NtuDepleted() => true
        case _ => false
      })
    }
  }
}

class ResourceSiloControlStartupMessageSomeTest extends ActorTest {
  val obj: ResourceSilo = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val buildingEvents: TestProbe = TestProbe("test-building-events")
  obj.Owner =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building) {
      Actor = buildingEvents.ref
    }
  obj.Owner.GUID = PlanetSideGUID(6)

  "Resource silo" should {
    "report if it has any NTU on startup" in {
      obj.NtuCapacitor = 1
      assert(obj.NtuCapacitor == 1)
      obj.Actor ! Service.Startup()
      val ownerMsg = buildingEvents.receiveOne(200 milliseconds)
      assert(ownerMsg match {
        case BuildingActor.SuppliedWithNtu() => true
        case _ => false
      })
    }
  }
}

class ResourceSiloControlUseTest extends FreedContextActorTest {
  import akka.actor.typed.scaladsl.adapter._
  ServiceManager.boot(system) ! ServiceManager.Register(Props[GalaxyService](), "galaxy")
  expectNoMessage(1000 milliseconds)
  var buildingMap = new TrieMap[Int, Building]()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 10))
  val player: Player = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
  val ant: Vehicle = Vehicle(GlobalDefinitions.ant)
  val silo = new ResourceSilo()
  val catchall: ActorRef = new TestProbe(system).ref
  val zone: Zone = new Zone("test", new ZoneMap("test-map"), 0) {
    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def AvatarEvents: ActorRef = catchall
    override def LocalEvents: ActorRef = catchall
    override def VehicleEvents: ActorRef = catchall
    override def Activity: ActorRef = catchall
    override def Vehicles: List[Vehicle] = List(ant)
    override def Buildings: Map[Int, Building] = { buildingMap.toMap }
  }
  val building = new Building(
    name = "integ-fac-test-building",
    building_guid = 6,
    map_id = 0,
    zone,
    StructureType.Facility,
    GlobalDefinitions.cryo_facility
  )
  buildingMap += 6 -> building
  system.spawn(InterstellarClusterService(Seq(zone)), InterstellarClusterService.InterstellarClusterServiceKey.id)
  building.Actor = context.spawn(BuildingActor(zone, building), "integ-fac-test-building-control").toClassic
  building.Invalidate()

  guid.register(player, number = 1)
  guid.register(ant, number = 2)
  guid.register(silo, number = 5)
  guid.register(building, number = 6)

  val maxNtuCap: Float = ant.Definition.MaxNtuCapacitor
  player.Spawn()
  ant.NtuCapacitor = maxNtuCap
  val probe = new TestProbe(system)
  ant.Actor = probe.ref
  ant.Zone = zone
  ant.Seats(0).mount(player)
  ant.DeploymentState = DriveState.Deployed
  building.Amenities = silo
  silo.Actor = system.actorOf(Props(classOf[ResourceSiloControl], silo), "test-silo")
  silo.Actor ! Service.Startup()

  "Resource silo" should {
    "respond when being used" in {
      expectNoMessage(1 seconds)
      silo.Actor ! CommonMessages.Use(ResourceSiloTest.player, Some(ant))
      val reply = probe.receiveOne(3000 milliseconds)
      reply match {
        case TransferBehavior.Discharging(Ntu.Nanites) => ()
        case _                                         => assert(ResourceSiloTest.fail, "")
      }
    }
  }
}

class ResourceSiloControlNtuWarningTest extends ActorTest {
  val obj: ResourceSilo = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  obj.Owner =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building) {
      Actor = TestProbe("building-events").ref
    }
  obj.Owner.GUID = PlanetSideGUID(6)

  val zoneEvents: TestProbe = TestProbe("zone-events")
  zone.AvatarEvents = zoneEvents.ref
  obj.Actor ! Service.Startup()
  obj.Actor ! ResourceSilo.UpdateChargeLevel(-obj.NtuCapacitor)
  zoneEvents.receiveN(3, 500.milliseconds) //events from setup

  "Resource silo" should {
    "announce high ntu" in {
      assert(obj.LowNtuWarningOn)
      obj.Actor ! ResourceSilo.LowNtuWarning(false)

      val reply = zoneEvents.receiveOne(5000 milliseconds)
      reply match {
        case AvatarServiceMessage("nowhere", AvatarAction.PlanetsideAttribute(PlanetSideGUID(6), 47, 0)) => ;
        case _ => assert(ResourceSiloTest.fail, s"$reply is wrong")
      }
      assert(!obj.LowNtuWarningOn)
    }
  }
}

class ResourceSiloControlUpdate1Test extends ActorTest {
  val obj: ResourceSilo = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val bldg =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
  bldg.GUID = PlanetSideGUID(6)
  obj.Owner = bldg
  val zoneEvents: TestProbe = TestProbe("zone-events")
  val buildingEvents: TestProbe = TestProbe("building-events")
  zone.AvatarEvents = zoneEvents.ref
  bldg.Actor = buildingEvents.ref
  obj.Actor ! Service.Startup()
  buildingEvents.receiveOne(500 milliseconds) //message caused by "startup"
  obj.Actor ! ResourceSilo.UpdateChargeLevel(-obj.NtuCapacitor)
  zoneEvents.receiveN(3, 500.milliseconds) //events from setup
  buildingEvents.receiveN(3, 500.milliseconds) //events from setup

  "Resource silo" should {
    "update the charge level and capacitor display (report high ntu, power restored)" in {
      assert(obj.NtuCapacitor == 0)
      assert(obj.CapacitorDisplay == 0)
      assert(obj.LowNtuWarningOn)
      obj.Actor ! ResourceSilo.UpdateChargeLevel(305)

      val reply1 = zoneEvents.receiveN(2,500 milliseconds)
      val reply2 = buildingEvents.receiveOne(500 milliseconds)
      assert(obj.NtuCapacitor == 305)
      assert(obj.CapacitorDisplay == 3)
      reply1.head match {
        case AvatarServiceMessage("nowhere", AvatarAction.PlanetsideAttribute(PlanetSideGUID(1), 45, 3)) => ;
        case _ => assert(ResourceSiloTest.fail, s"$reply1 is wrong")
      }
      assert(reply2.isInstanceOf[BuildingActor.MapUpdate], s"$reply2 is wrong")
      reply1(1) match {
        case AvatarServiceMessage("nowhere", AvatarAction.PlanetsideAttribute(PlanetSideGUID(6), 47, 0)) => ;
        case _ => assert(ResourceSiloTest.fail, s"${reply1(1)} is wrong")
      }
      assert(!obj.LowNtuWarningOn)
    }
  }
}

class ResourceSiloControlUpdate2Test extends ActorTest {
  val obj: ResourceSilo = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val bldg =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
  bldg.GUID = PlanetSideGUID(6)
  obj.Owner = bldg
  val zoneEvents: TestProbe = TestProbe("zone-events")
  val buildingEvents: TestProbe = TestProbe("building-events")
  zone.AvatarEvents = zoneEvents.ref
  bldg.Actor = buildingEvents.ref
  obj.Actor ! Service.Startup()
  buildingEvents.receiveOne(500 milliseconds) //message caused by "startup"
  obj.Actor ! ResourceSilo.UpdateChargeLevel(-obj.NtuCapacitor + 100)
  zoneEvents.receiveN(3, 500.milliseconds) //events from setup
  buildingEvents.receiveN(1, 500.milliseconds) //events from setup

  "Resource silo" should {
    "update the charge level and capacitor display (report good ntu)" in {
      assert(obj.NtuCapacitor == 100)
      assert(obj.CapacitorDisplay == 1)
      assert(obj.LowNtuWarningOn)
      obj.Actor ! ResourceSilo.UpdateChargeLevel(105)

      val reply1 = zoneEvents.receiveN(2, 1000 milliseconds)
      val reply2 = buildingEvents.receiveOne(1000 milliseconds)
      assert(obj.NtuCapacitor == 205)
      assert(obj.CapacitorDisplay == 2)
      reply1.head match {
        case AvatarServiceMessage("nowhere", AvatarAction.PlanetsideAttribute(PlanetSideGUID(1), 45, 2)) => ;
        case _ => assert(ResourceSiloTest.fail, s"$reply1 is wrong")
      }
      assert(reply2.isInstanceOf[BuildingActor.MapUpdate])
      reply1(1) match {
        case AvatarServiceMessage("nowhere", AvatarAction.PlanetsideAttribute(PlanetSideGUID(6), 47, 0)) => ;
        case _ => assert(ResourceSiloTest.fail, s"${reply1(1)} is wrong")
      }
      assert(!obj.LowNtuWarningOn)
    }
  }
}

class ResourceSiloControlNoUpdateTest extends ActorTest {
  val obj: ResourceSilo = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val bldg =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
  bldg.GUID = PlanetSideGUID(6)
  obj.Owner = bldg
  val zoneEvents: TestProbe = TestProbe("zone-events")
  val buildingEvents: TestProbe = TestProbe("building-events")
  zone.AvatarEvents = zoneEvents.ref
  bldg.Actor = buildingEvents.ref
  obj.Actor ! Service.Startup()
  obj.NtuCapacitor = 0

  "Resource silo" should {
    "update, but not sufficiently to change the capacitor display" in {
      buildingEvents.receiveOne(500 milliseconds) //message caused by "startup"
      obj.Actor ! ResourceSilo.UpdateChargeLevel(250)
      zoneEvents.receiveN(2, 500.milliseconds) //events from setup
      buildingEvents.receiveN(3, 500.milliseconds) //events from setup
      obj.LowNtuWarningOn = false
      assert(obj.NtuCapacitor == 250)
      assert(obj.CapacitorDisplay == 2)
      assert(!obj.LowNtuWarningOn)
      obj.Actor ! ResourceSilo.UpdateChargeLevel(49)

      expectNoMessage(500 milliseconds)
      zoneEvents.expectNoMessage(500 milliseconds)
      assert(obj.CapacitorDisplay == 2)
      assert(obj.NtuCapacitor < 300)
      assert(!obj.LowNtuWarningOn)
      buildingEvents.expectNoMessage(500 milliseconds)
    }
  }
}

object ResourceSiloTest {
  val player: Player = Player(
    Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute).copy(stamina = 0)
  )

  val fail: Boolean = false
}

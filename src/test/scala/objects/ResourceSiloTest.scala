// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
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
import akka.actor.typed.scaladsl.adapter._
import net.psforever.objects.avatar.Avatar

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
      obj.NtuCapacitor mustEqual 0
      obj.LowNtuWarningOn mustEqual true
      obj.CapacitorDisplay mustEqual 0
      //
      obj.NtuCapacitor = 50
      obj.LowNtuWarningOn = false
      obj.NtuCapacitor mustEqual 50
      obj.LowNtuWarningOn mustEqual false
      obj.CapacitorDisplay mustEqual 1
    }

    "charge level can not exceed limits(0 to maximum)" in {
      val obj = ResourceSilo()
      obj.NtuCapacitor mustEqual 0
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
        false,
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
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val buildingEvents = TestProbe("test-building-events")
  obj.Owner =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building) {
      Actor = buildingEvents.ref
    }
  obj.Owner.GUID = PlanetSideGUID(6)

  "Resource silo" should {
    "startup properly" in {
      obj.Actor ! "startup"
      expectNoMessage(max = 1000 milliseconds)
    }
  }
}

class ResourceSiloControlStartupMessageNoneTest extends ActorTest {
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val buildingEvents = TestProbe("test-building-events")
  obj.Owner =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building) {
      Actor = buildingEvents.ref
    }
  obj.Owner.GUID = PlanetSideGUID(6)

  "Resource silo" should {
    "report if it has no NTU on startup" in {
      assert(obj.NtuCapacitor == 0)
      obj.Actor ! "startup"
      val ownerMsg = buildingEvents.receiveOne(200 milliseconds)
      assert(ownerMsg match {
        case BuildingActor.NtuDepleted() => true
        case _ => false
      })
    }
  }
}

class ResourceSiloControlStartupMessageSomeTest extends ActorTest {
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val buildingEvents = TestProbe("test-building-events")
  obj.Owner =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building) {
      Actor = buildingEvents.ref
    }
  obj.Owner.GUID = PlanetSideGUID(6)

  "Resource silo" should {
    "report if it has any NTU on startup" in {
      obj.NtuCapacitor = 1
      assert(obj.NtuCapacitor == 1)
      obj.Actor ! "startup"
      val ownerMsg = buildingEvents.receiveOne(200 milliseconds)
      assert(ownerMsg match {
        case BuildingActor.SuppliedWithNtu() => true
        case _ => false
      })
    }
  }
}

class ResourceSiloControlUseTest extends ActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val map  = new ZoneMap("test")
  val zone = new Zone("test", map, 0) {
    override def SetupNumberPools() = {}
    GUID(guid)
  }
  zone.actor = system.spawnAnonymous(ZoneActor(zone))
  val building = new Building(
    "Building",
    building_guid = 0,
    map_id = 0,
    zone,
    StructureType.Building,
    GlobalDefinitions.building
  ) //guid=1
  building.Actor = TestProbe("building-actor").ref

  val obj = ResourceSilo() //guid=2
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  obj.Owner = building
  obj.Actor ! "startup"

  val player = Player(
    new Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)
  ) //guid=3
  val vehicle = Vehicle(GlobalDefinitions.ant) //guid=4
  val probe   = new TestProbe(system)

  guid.register(building, 1)
  guid.register(obj, 2)
  guid.register(player, 3)
  guid.register(vehicle, 4)
  expectNoMessage(200 milliseconds)
  zone.Transport ! Zone.Vehicle.Spawn(vehicle)
  vehicle.Seats(0).Occupant = player
  player.VehicleSeated = vehicle.GUID
  expectNoMessage(200 milliseconds)
  system.stop(vehicle.Actor)
  vehicle.Actor = probe.ref

  "Resource silo" should {
    "respond when being used" in {
      expectNoMessage(1 seconds)
      obj.Actor ! CommonMessages.Use(ResourceSiloTest.player)

      val reply = probe.receiveOne(2000 milliseconds)
      assert(reply match {
        case TransferBehavior.Discharging(Ntu.Nanites) => true
        case _                                         => false
      })
    }
  }
}

class ResourceSiloControlNtuWarningTest extends ActorTest {
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  obj.Owner =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building) {
      Actor = TestProbe("building-events").ref
    }
  obj.Owner.GUID = PlanetSideGUID(6)

  val zoneEvents = TestProbe("zone-events")
  zone.AvatarEvents = zoneEvents.ref
  obj.Actor ! "startup"

  "Resource silo" should {
    "announce high ntu" in {
      assert(obj.LowNtuWarningOn)
      obj.Actor ! ResourceSilo.LowNtuWarning(false)

      val reply = zoneEvents.receiveOne(5000 milliseconds)
      assert(reply.isInstanceOf[AvatarServiceMessage])
      assert(reply.asInstanceOf[AvatarServiceMessage].forChannel == "nowhere")
      assert(reply.asInstanceOf[AvatarServiceMessage].actionMessage.isInstanceOf[AvatarAction.PlanetsideAttribute])
      assert(
        reply
          .asInstanceOf[AvatarServiceMessage]
          .actionMessage
          .asInstanceOf[AvatarAction.PlanetsideAttribute]
          .player_guid == PlanetSideGUID(6)
      )
      assert(
        reply
          .asInstanceOf[AvatarServiceMessage]
          .actionMessage
          .asInstanceOf[AvatarAction.PlanetsideAttribute]
          .attribute_type == 47
      )
      assert(
        reply
          .asInstanceOf[AvatarServiceMessage]
          .actionMessage
          .asInstanceOf[AvatarAction.PlanetsideAttribute]
          .attribute_value == 0
      )
      assert(!obj.LowNtuWarningOn)
    }
  }
}

class ResourceSiloControlUpdate1Test extends ActorTest {
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val bldg =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
  bldg.GUID = PlanetSideGUID(6)
  obj.Owner = bldg
  val zoneEvents     = TestProbe("zone-events")
  val buildingEvents = TestProbe("building-events")
  zone.AvatarEvents = zoneEvents.ref
  bldg.Actor = buildingEvents.ref
  obj.Actor ! "startup"

  "Resource silo" should {
    "update the charge level and capacitor display (report high ntu, power restored)" in {
      buildingEvents.receiveOne(500 milliseconds) //message caused by "startup"
      assert(obj.NtuCapacitor == 0)
      assert(obj.CapacitorDisplay == 0)
      assert(obj.LowNtuWarningOn)
      obj.Actor ! ResourceSilo.UpdateChargeLevel(305)

      val reply1 = zoneEvents.receiveOne(500 milliseconds)
      val reply2 = buildingEvents.receiveOne(500 milliseconds)
      assert(obj.NtuCapacitor == 305)
      assert(obj.CapacitorDisplay == 4)
      assert(reply1 match {
        case AvatarServiceMessage("nowhere", AvatarAction.PlanetsideAttribute(PlanetSideGUID(1), 45, 4)) => true
        case _                                                                                           => false
      })
      assert(reply2.isInstanceOf[BuildingActor.MapUpdate])

      val reply3 = zoneEvents.receiveOne(500 milliseconds)
      assert(!obj.LowNtuWarningOn)
      assert(reply3 match {
        case AvatarServiceMessage("nowhere", AvatarAction.PlanetsideAttribute(PlanetSideGUID(6), 47, 0)) => true
        case _                                                                                           => false
      })
    }
  }
}

class ResourceSiloControlUpdate2Test extends ActorTest {
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val bldg =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
  bldg.GUID = PlanetSideGUID(6)
  obj.Owner = bldg
  val zoneEvents     = TestProbe("zone-events")
  val buildingEvents = TestProbe("building-events")
  zone.AvatarEvents = zoneEvents.ref
  bldg.Actor = buildingEvents.ref
  obj.Actor ! "startup"

  "Resource silo" should {
    "update the charge level and capacitor display (report good ntu)" in {
      buildingEvents.receiveOne(500 milliseconds) //message caused by "startup"
      obj.NtuCapacitor = 100
      obj.LowNtuWarningOn = true
      assert(obj.NtuCapacitor == 100)
      assert(obj.CapacitorDisplay == 1)
      assert(obj.LowNtuWarningOn)
      obj.Actor ! ResourceSilo.UpdateChargeLevel(105)

      val reply1 = zoneEvents.receiveOne(1000 milliseconds)
      val reply2 = buildingEvents.receiveOne(1000 milliseconds)
      assert(obj.NtuCapacitor == 205)
      assert(obj.CapacitorDisplay == 3)
      assert(reply1.isInstanceOf[AvatarServiceMessage])
      assert(reply1.asInstanceOf[AvatarServiceMessage].forChannel == "nowhere")
      assert(reply1.asInstanceOf[AvatarServiceMessage].actionMessage.isInstanceOf[AvatarAction.PlanetsideAttribute])
      assert(
        reply1
          .asInstanceOf[AvatarServiceMessage]
          .actionMessage
          .asInstanceOf[AvatarAction.PlanetsideAttribute]
          .player_guid == PlanetSideGUID(1)
      )
      assert(
        reply1
          .asInstanceOf[AvatarServiceMessage]
          .actionMessage
          .asInstanceOf[AvatarAction.PlanetsideAttribute]
          .attribute_type == 45
      )
      assert(
        reply1
          .asInstanceOf[AvatarServiceMessage]
          .actionMessage
          .asInstanceOf[AvatarAction.PlanetsideAttribute]
          .attribute_value == 3
      )

      assert(reply2.isInstanceOf[BuildingActor.MapUpdate])

      val reply3 = zoneEvents.receiveOne(500 milliseconds)
      assert(!obj.LowNtuWarningOn)
      assert(reply3.isInstanceOf[AvatarServiceMessage])
      assert(reply3.asInstanceOf[AvatarServiceMessage].forChannel == "nowhere")
      assert(reply3.asInstanceOf[AvatarServiceMessage].actionMessage.isInstanceOf[AvatarAction.PlanetsideAttribute])
      assert(
        reply3
          .asInstanceOf[AvatarServiceMessage]
          .actionMessage
          .asInstanceOf[AvatarAction.PlanetsideAttribute]
          .player_guid == PlanetSideGUID(6)
      )
      assert(
        reply3
          .asInstanceOf[AvatarServiceMessage]
          .actionMessage
          .asInstanceOf[AvatarAction.PlanetsideAttribute]
          .attribute_type == 47
      )
      assert(
        reply3
          .asInstanceOf[AvatarServiceMessage]
          .actionMessage
          .asInstanceOf[AvatarAction.PlanetsideAttribute]
          .attribute_value == 0
      )
    }
  }
}

class ResourceSiloControlNoUpdateTest extends ActorTest {
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val bldg =
    new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
  bldg.GUID = PlanetSideGUID(6)
  obj.Owner = bldg
  val zoneEvents     = TestProbe("zone-events")
  val buildingEvents = TestProbe("building-events")
  zone.AvatarEvents = zoneEvents.ref
  bldg.Actor = buildingEvents.ref
  obj.Actor ! "startup"

  "Resource silo" should {
    "update, but not sufficiently to change the capacitor display" in {
      buildingEvents.receiveOne(500 milliseconds) //message caused by "startup"
      obj.NtuCapacitor = 250
      obj.LowNtuWarningOn = false
      assert(obj.NtuCapacitor == 250)
      assert(obj.CapacitorDisplay == 3)
      assert(!obj.LowNtuWarningOn)
      obj.Actor ! ResourceSilo.UpdateChargeLevel(50)

      expectNoMessage(500 milliseconds)
      zoneEvents.expectNoMessage(500 milliseconds)
      buildingEvents.expectNoMessage(500 milliseconds)
      assert(
        obj.NtuCapacitor == 299 || obj.NtuCapacitor == 300
      ) // Just in case the capacitor level drops while waiting for the message check 299 & 300
      assert(obj.CapacitorDisplay == 3)
      assert(!obj.LowNtuWarningOn)
    }
  }
}

object ResourceSiloTest {
  val player = Player(
    new Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)
  )

  class ProbedAvatarService(probe: TestProbe) extends Actor {
    override def receive: Receive = {
      case msg =>
        probe.ref ! msg
    }
  }

  class ProbedBuildingControl(probe: TestProbe) extends Actor {
    override def receive: Receive = {
      case msg =>
        probe.ref ! msg
    }
  }

  class ProbedResourceSiloControl(silo: ResourceSilo, probe: TestProbe) extends ResourceSiloControl(silo) {
    override def receive: Receive = {
      case msg =>
        super.receive.apply(msg)
        probe.ref ! msg
    }
  }
}

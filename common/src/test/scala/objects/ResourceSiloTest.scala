// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, Props}
import akka.routing.RandomPool
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.guid.TaskResolver
import net.psforever.objects.{Avatar, GlobalDefinitions, Player}
import net.psforever.objects.serverobject.resourcesilo.{ResourceSilo, ResourceSiloControl, ResourceSiloDefinition}
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.packet.game.UseItemMessage
import net.psforever.types._
import org.specs2.mutable.Specification
import services.ServiceManager
import services.avatar.{AvatarAction, AvatarServiceMessage}

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
      obj.MaximumCharge mustEqual 1000
      obj.ChargeLevel mustEqual 0
      obj.LowNtuWarningOn mustEqual true
      obj.CapacitorDisplay mustEqual 0
      //
      obj.ChargeLevel = 50
      obj.LowNtuWarningOn = false
      obj.CapacitorDisplay = 75
      obj.ChargeLevel mustEqual 50
      obj.LowNtuWarningOn mustEqual false
      obj.CapacitorDisplay mustEqual 75
    }

    "charge level can not exceed limits(0 to maximum)" in {
      val obj = ResourceSilo()
      obj.ChargeLevel mustEqual 0
      obj.ChargeLevel = -5
      obj.ChargeLevel mustEqual 0

      obj.ChargeLevel = obj.MaximumCharge + 100
      obj.ChargeLevel mustEqual 1000
      obj.ChargeLevel mustEqual obj.MaximumCharge
    }

    "using the silo generates a charge event" in {
      val msg = UseItemMessage(PlanetSideGUID(1), PlanetSideGUID(0), PlanetSideGUID(2), 0L, false, Vector3(0f,0f,0f),Vector3(0f,0f,0f),0,0,0,0L) //faked
      ResourceSilo().Use(ResourceSiloTest.player, msg) mustEqual ResourceSilo.ChargeEvent()
    }
  }
}

class ResourceSiloControlStartupTest extends ActorTest {
  val serviceManager = ServiceManager.boot(system)
  serviceManager ! ServiceManager.Register(RandomPool(1).props(Props[TaskResolver]), "taskResolver")
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  val probe = TestProbe()
  serviceManager ! ServiceManager.Register(Props(classOf[ResourceSiloTest.ProbedAvatarService], probe), "avatar")

  "Resource silo" should {
    "startup properly" in {
      expectNoMsg(500 milliseconds)
      system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
      expectNoMsg(1 seconds)
    }
  }
}

class ResourceSiloControlUseTest extends ActorTest {
  val serviceManager = ServiceManager.boot(system)
  serviceManager ! ServiceManager.Register(RandomPool(1).props(Props[TaskResolver]), "taskResolver")
  val probe = TestProbe()
  serviceManager ! ServiceManager.Register(Props(classOf[ResourceSiloTest.ProbedAvatarService], probe), "avatar")
  val msg = UseItemMessage(PlanetSideGUID(1), PlanetSideGUID(0), PlanetSideGUID(2), 0L, false, Vector3(0f,0f,0f),Vector3(0f,0f,0f),0,0,0,0L) //faked
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  obj.Actor ! "startup"

  "Resource silo" should {
    "respond when being used" in {
      expectNoMsg(1 seconds)
      obj.Actor ! ResourceSilo.Use(ResourceSiloTest.player, msg)

      val reply = receiveOne(500 milliseconds)
      assert(reply.isInstanceOf[ResourceSilo.ResourceSiloMessage])
      assert(reply.asInstanceOf[ResourceSilo.ResourceSiloMessage].player == ResourceSiloTest.player)
      assert(reply.asInstanceOf[ResourceSilo.ResourceSiloMessage].msg == msg)
      assert(reply.asInstanceOf[ResourceSilo.ResourceSiloMessage].response == ResourceSilo.ChargeEvent())
    }
  }
}

class ResourceSiloControlNtuWarningTest extends ActorTest {
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  obj.Actor ! "startup"
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  obj.Owner = new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
  obj.Owner.GUID = PlanetSideGUID(6)
  val zoneEvents = TestProbe("zone-events")

  "Resource silo" should {
    "announce high ntu" in {
      zone.AvatarEvents = zoneEvents.ref
      assert(obj.LowNtuWarningOn)
      obj.Actor ! ResourceSilo.LowNtuWarning(false)

      val reply = zoneEvents.receiveOne(500 milliseconds)
      assert(!obj.LowNtuWarningOn)
      assert(reply.isInstanceOf[AvatarServiceMessage])
      assert(reply.asInstanceOf[AvatarServiceMessage].forChannel == "nowhere")
      assert(reply.asInstanceOf[AvatarServiceMessage]
        .actionMessage.isInstanceOf[AvatarAction.PlanetsideAttribute])
      assert(reply.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].player_guid == PlanetSideGUID(6))
      assert(reply.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].attribute_type == 47)
      assert(reply.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].attribute_value == 0)
    }
  }
}

class ResourceSiloControlUpdate1Test extends ActorTest {
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  obj.Actor ! "startup"
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val bldg = new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
  bldg.GUID = PlanetSideGUID(6)
  obj.Owner = bldg
  val zoneEvents = TestProbe("zone-events")
  val buildingEvents = TestProbe("building-events")

  "Resource silo" should {
    "update the charge level and capacitor display (report high ntu, power restored)" in {
      zone.AvatarEvents = zoneEvents.ref
      bldg.Actor = buildingEvents.ref

      assert(obj.ChargeLevel == 0)
      assert(obj.CapacitorDisplay == 0)
      obj.Actor ! ResourceSilo.UpdateChargeLevel(305)

      val reply1 = zoneEvents.receiveOne(500 milliseconds)
      val reply2 = buildingEvents.receiveOne(500 milliseconds)
      assert(obj.ChargeLevel == 305)
      assert(obj.CapacitorDisplay == 4)
      assert(reply1.isInstanceOf[AvatarServiceMessage])
      assert(reply1.asInstanceOf[AvatarServiceMessage].forChannel == "nowhere")
      assert(reply1.asInstanceOf[AvatarServiceMessage]
        .actionMessage.isInstanceOf[AvatarAction.PlanetsideAttribute])
      assert(reply1.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].player_guid == PlanetSideGUID(1))
      assert(reply1.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].attribute_type == 45)
      assert(reply1.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].attribute_value == 4)

      assert(reply2.isInstanceOf[Building.SendMapUpdate])

      val reply3 = zoneEvents.receiveOne(500 milliseconds)
      assert(reply3.isInstanceOf[AvatarServiceMessage])
      assert(reply3.asInstanceOf[AvatarServiceMessage].forChannel == "nowhere")
      assert(reply3.asInstanceOf[AvatarServiceMessage]
        .actionMessage.isInstanceOf[AvatarAction.PlanetsideAttribute])
      assert(reply3.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].player_guid == PlanetSideGUID(6))
      assert(reply3.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].attribute_type == 48)
      assert(reply3.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].attribute_value == 0)

      val reply4 = zoneEvents.receiveOne(500 milliseconds)
      assert(!obj.LowNtuWarningOn)
      assert(reply4.isInstanceOf[AvatarServiceMessage])
      assert(reply4.asInstanceOf[AvatarServiceMessage].forChannel == "nowhere")
      assert(reply4.asInstanceOf[AvatarServiceMessage]
        .actionMessage.isInstanceOf[AvatarAction.PlanetsideAttribute])
      assert(reply4.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].player_guid == PlanetSideGUID(6))
      assert(reply4.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].attribute_type == 47)
      assert(reply4.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].attribute_value == 0)
    }
  }
}

class ResourceSiloControlUpdate2Test extends ActorTest {
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  obj.Actor ! "startup"
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val bldg = new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
  bldg.GUID = PlanetSideGUID(6)
  obj.Owner = bldg
  val zoneEvents = TestProbe("zone-events")
  val buildingEvents = TestProbe("building-events")

  "Resource silo" should {
    "update the charge level and capacitor display (report good ntu)" in {
      zone.AvatarEvents = zoneEvents.ref
      bldg.Actor = buildingEvents.ref

      obj.ChargeLevel = 100
      obj.CapacitorDisplay = 1
      obj.LowNtuWarningOn = true
      assert(obj.ChargeLevel == 100)
      assert(obj.CapacitorDisplay == 1)
      assert(obj.LowNtuWarningOn)
      obj.Actor ! ResourceSilo.UpdateChargeLevel(105)

      val reply1 = zoneEvents.receiveOne(1000 milliseconds)
      val reply2 = buildingEvents.receiveOne(1000 milliseconds)
      assert(obj.ChargeLevel == 205)
      assert(obj.CapacitorDisplay == 3)
      assert(reply1.isInstanceOf[AvatarServiceMessage])
      assert(reply1.asInstanceOf[AvatarServiceMessage].forChannel == "nowhere")
      assert(reply1.asInstanceOf[AvatarServiceMessage]
        .actionMessage.isInstanceOf[AvatarAction.PlanetsideAttribute])
      assert(reply1.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].player_guid == PlanetSideGUID(1))
      assert(reply1.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].attribute_type == 45)
      assert(reply1.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].attribute_value == 3)

      assert(reply2.isInstanceOf[Building.SendMapUpdate])

      val reply3 = zoneEvents.receiveOne(500 milliseconds)
      assert(!obj.LowNtuWarningOn)
      assert(reply3.isInstanceOf[AvatarServiceMessage])
      assert(reply3.asInstanceOf[AvatarServiceMessage].forChannel == "nowhere")
      assert(reply3.asInstanceOf[AvatarServiceMessage]
        .actionMessage.isInstanceOf[AvatarAction.PlanetsideAttribute])
      assert(reply3.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].player_guid == PlanetSideGUID(6))
      assert(reply3.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].attribute_type == 47)
      assert(reply3.asInstanceOf[AvatarServiceMessage]
        .actionMessage.asInstanceOf[AvatarAction.PlanetsideAttribute].attribute_value == 0)
    }
  }
}

class ResourceSiloControlNoUpdateTest extends ActorTest {
  val obj = ResourceSilo()
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[ResourceSiloControl], obj), "test-silo")
  obj.Actor ! "startup"
  val zone = new Zone("nowhere", new ZoneMap("nowhere-map"), 0)
  val bldg = new Building("Building", building_guid = 6, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
  bldg.GUID = PlanetSideGUID(6)
  obj.Owner = bldg
  val zoneEvents = TestProbe("zone-events")
  val buildingEvents = TestProbe("building-events")

  "Resource silo" should {
    "update, but not sufficiently to change the capacitor display" in {
      zone.AvatarEvents = zoneEvents.ref
      bldg.Actor = buildingEvents.ref

      obj.ChargeLevel = 250
      obj.CapacitorDisplay = 3
      obj.LowNtuWarningOn = false
      assert(obj.ChargeLevel == 250)
      assert(obj.CapacitorDisplay == 3)
      assert(!obj.LowNtuWarningOn)
      obj.Actor ! ResourceSilo.UpdateChargeLevel(50)

      expectNoMsg(500 milliseconds)
      zoneEvents.expectNoMsg(500 milliseconds)
      buildingEvents.expectNoMsg(500 milliseconds)
      assert(obj.ChargeLevel == 299 || obj.ChargeLevel == 300) // Just in case the capacitor level drops while waiting for the message check 299 & 300
      assert(obj.CapacitorDisplay == 3)
      assert(!obj.LowNtuWarningOn)
    }
  }
}

object ResourceSiloTest {
  val player = Player(new Avatar(0L, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))

  class ProbedAvatarService(probe : TestProbe) extends Actor {
    override def receive : Receive = {
      case msg =>
        probe.ref ! msg
    }
  }

  class ProbedBuildingControl(probe : TestProbe) extends Actor {
    override def receive : Receive = {
      case msg =>
        probe.ref ! msg
    }
  }

  class ProbedResourceSiloControl(silo : ResourceSilo, probe : TestProbe) extends ResourceSiloControl(silo) {
    override def receive : Receive = {
      case msg =>
          super.receive.apply(msg)
          probe.ref ! msg
      }
  }
}

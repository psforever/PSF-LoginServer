// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorRef, Props}
import base.ActorTest
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.doors.{Door, DoorControl}
import net.psforever.objects.serverobject.structures._
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable.Specification
import services.ServiceManager
import services.galaxy.GalaxyService

import scala.concurrent.duration._

class AmenityTest extends Specification {
  class AmenityObject extends Amenity {
    def Definition : ObjectDefinition = null
  }

  "Amenity" should {
    "construct" in {
      val ao = new AmenityObject()
      ao.Owner mustEqual Building.NoBuilding
    }

    "can be owned by a building" in {
      val ao = new AmenityObject()
      val bldg = Building(0, 10, Zone.Nowhere, StructureType.Building)

      ao.Owner = bldg
      ao.Owner mustEqual bldg
    }

    "be owned by a vehicle" in {
      import net.psforever.objects.Vehicle
      val ao = new AmenityObject()
      val veh = Vehicle(GlobalDefinitions.quadstealth)

      ao.Owner = veh
      ao.Owner mustEqual veh
    }

    "not be owned by an unexpected object" in {
      val ao = new AmenityObject()
      //ao.Owner = net.psforever.objects.serverobject.mblocker.Locker() //will not compile
      ok
    }

    "confer faction allegiance through ownership" in {
      //see FactionAffinityTest
      val ao = new AmenityObject()
      val bldg = Building(0, 10, Zone.Nowhere, StructureType.Building)
      ao.Owner = bldg
      bldg.Faction mustEqual PlanetSideEmpire.NEUTRAL
      ao.Faction mustEqual PlanetSideEmpire.NEUTRAL

      bldg.Faction = PlanetSideEmpire.TR
      bldg.Faction mustEqual PlanetSideEmpire.TR
      ao.Faction mustEqual PlanetSideEmpire.TR
    }
  }
}

class BuildingTest extends Specification {
  "Building" should {
    "construct" in {
      val bldg = Building(0, 10, Zone.Nowhere, StructureType.Building)
      bldg.MapId mustEqual 10
      bldg.Actor mustEqual ActorRef.noSender
      bldg.Amenities mustEqual Nil
      bldg.Zone mustEqual Zone.Nowhere
      bldg.Faction mustEqual PlanetSideEmpire.NEUTRAL
    }

    "change faction affinity" in {
      val bldg = Building(0, 10, Zone.Nowhere, StructureType.Building)
      bldg.Faction mustEqual PlanetSideEmpire.NEUTRAL

      bldg.Faction = PlanetSideEmpire.TR
      bldg.Faction mustEqual PlanetSideEmpire.TR
    }

    "keep track of amenities" in {
      val bldg = Building(0, 10, Zone.Nowhere, StructureType.Building)
      val door1 = Door(GlobalDefinitions.door)
      val door2 = Door(GlobalDefinitions.door)

      bldg.Amenities mustEqual Nil
      bldg.Amenities = door2
      bldg.Amenities mustEqual List(door2)
      bldg.Amenities = door1
      bldg.Amenities mustEqual List(door2, door1)
      door1.Owner mustEqual bldg
      door2.Owner mustEqual bldg
    }
  }
}

class WarpGateTest extends Specification {
  "WarpGate" should {
    "construct" in {
      val bldg = WarpGate(0, 10, Zone.Nowhere, GlobalDefinitions.warpgate)
      bldg.MapId mustEqual 10
      bldg.Actor mustEqual ActorRef.noSender
      bldg.Amenities mustEqual Nil
      bldg.Zone mustEqual Zone.Nowhere
      bldg.Faction mustEqual PlanetSideEmpire.NEUTRAL
    }
  }
}

class BuildingControl1Test extends ActorTest {
  "Building Control" should {
    "construct" in {
      val bldg = Building(0, 10, Zone.Nowhere, StructureType.Building)
      bldg.Actor = system.actorOf(Props(classOf[BuildingControl], bldg), "test")
      assert(bldg.Actor != ActorRef.noSender)
    }
  }
}

class BuildingControl2Test extends ActorTest {
  ServiceManager.boot(system) ! ServiceManager.Register(Props[GalaxyService], "galaxy")
  val bldg = Building(0, 10, Zone.Nowhere, StructureType.Building)
  bldg.Faction = PlanetSideEmpire.TR
  bldg.Actor = system.actorOf(Props(classOf[BuildingControl], bldg), "test")
  bldg.Actor ! "startup"

  "Building Control" should {
    "convert and assert faction affinity on convert request" in {
      expectNoMsg(500 milliseconds)

      assert(bldg.Faction == PlanetSideEmpire.TR)
      bldg.Actor ! FactionAffinity.ConvertFactionAffinity(PlanetSideEmpire.VS)
      val reply = receiveOne(500 milliseconds)
      assert(reply.isInstanceOf[FactionAffinity.AssertFactionAffinity])
      assert(reply.asInstanceOf[FactionAffinity.AssertFactionAffinity].obj == bldg)
      assert(reply.asInstanceOf[FactionAffinity.AssertFactionAffinity].faction == PlanetSideEmpire.VS)
      assert(bldg.Faction == PlanetSideEmpire.VS)
    }
  }
}

class BuildingControl3Test extends ActorTest {
  ServiceManager.boot(system) ! ServiceManager.Register(Props[GalaxyService], "galaxy")
  val bldg = Building(0, 10, Zone.Nowhere, StructureType.Building)
  bldg.Faction = PlanetSideEmpire.TR
  bldg.Actor = system.actorOf(Props(classOf[BuildingControl], bldg), "test")
  val door1 = Door(GlobalDefinitions.door)
  door1.GUID = PlanetSideGUID(1)
  door1.Actor = system.actorOf(Props(classOf[DoorControl], door1), "door1-test")
  val door2 = Door(GlobalDefinitions.door)
  door2.GUID = PlanetSideGUID(2)
  door2.Actor = system.actorOf(Props(classOf[DoorControl], door2), "door2-test")
  bldg.Amenities = door2
  bldg.Amenities = door1
  bldg.Actor ! "startup"

  "Building Control" should {
    "convert and assert faction affinity on convert request, and for each of its amenities" in {
      expectNoMsg(500 milliseconds)

      assert(bldg.Faction == PlanetSideEmpire.TR)
      assert(bldg.Amenities.length == 2)
      assert(bldg.Amenities.head == door2)
      assert(bldg.Amenities(1) == door1)

      bldg.Actor ! FactionAffinity.ConvertFactionAffinity(PlanetSideEmpire.VS)
      val reply = ActorTest.receiveMultiple(3, 500 milliseconds, this)
      //val reply = receiveN(3, Duration.create(5000, "ms"))
      assert(reply.length == 3)
      var building_count = 0
      var door_count = 0
      reply.foreach(item => {
        assert(item.isInstanceOf[FactionAffinity.AssertFactionAffinity])
        val item2 = item.asInstanceOf[FactionAffinity.AssertFactionAffinity]
        item2.obj match {
          case _ : Building =>
            building_count += 1
          case _ : Door =>
            door_count += 1
          case _ =>
            assert(false)
        }
        assert(item2.faction == PlanetSideEmpire.VS)
      })
      assert(building_count == 1 && door_count == 2)
    }
  }
}

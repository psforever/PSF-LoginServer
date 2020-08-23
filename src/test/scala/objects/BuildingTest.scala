// Copyright (c) 2017 PSForever
package objects

import base.ActorTest
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.{Default, GlobalDefinitions}
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.structures._
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable.Specification
import akka.actor.typed.scaladsl.adapter._

class AmenityTest extends Specification {
  val definition = new AmenityDefinition(0) {
    //intentionally blank
  }
  class AmenityObject extends Amenity {
    def Definition: AmenityDefinition = definition
  }

  "Amenity" should {
    "construct" in {
      val ao = new AmenityObject()
      ao.Owner mustEqual Building.NoBuilding
    }

    "can be owned by a building" in {
      val ao   = new AmenityObject()
      val bldg = Building("Building", 0, 10, Zone.Nowhere, StructureType.Building)

      ao.Owner = bldg
      ao.Owner mustEqual bldg
    }

    "be owned by a vehicle" in {
      import net.psforever.objects.Vehicle
      val ao  = new AmenityObject()
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
      val ao   = new AmenityObject()
      val bldg = Building("Building", 0, 10, Zone.Nowhere, StructureType.Building)
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
      val bldg = Building("Building", 0, 10, Zone.Nowhere, StructureType.Building)
      bldg.MapId mustEqual 10
      bldg.Actor mustEqual Default.Actor
      bldg.Amenities mustEqual Nil
      bldg.Zone mustEqual Zone.Nowhere
      bldg.Faction mustEqual PlanetSideEmpire.NEUTRAL
    }

    "change faction affinity" in {
      val bldg = Building("Building", 0, 10, Zone.Nowhere, StructureType.Building)
      bldg.Faction mustEqual PlanetSideEmpire.NEUTRAL

      bldg.Faction = PlanetSideEmpire.TR
      bldg.Faction mustEqual PlanetSideEmpire.TR
    }

    "keep track of amenities" in {
      val bldg  = Building("Building", 0, 10, Zone.Nowhere, StructureType.Building)
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
      val bldg = WarpGate("WarpGate", 0, 10, Zone.Nowhere, GlobalDefinitions.warpgate)
      bldg.MapId mustEqual 10
      bldg.Actor mustEqual Default.Actor
      bldg.Amenities mustEqual Nil
      bldg.Zone mustEqual Zone.Nowhere
      bldg.Faction mustEqual PlanetSideEmpire.NEUTRAL
    }
  }
}

class BuildingActor1Test extends ActorTest {
  "Building Control" should {
    "construct" in {
      val bldg = Building("Building", 0, 10, Zone.Nowhere, StructureType.Building)
      bldg.Actor = system.spawn(BuildingActor(Zone.Nowhere, bldg), "test").toClassic
      assert(bldg.Actor != Default.Actor)
    }
  }
}

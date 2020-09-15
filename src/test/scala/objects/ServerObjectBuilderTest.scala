// Copyright (c) 2017 PSForever
package objects

import akka.actor.ActorContext
import base.FreedContextActorTest
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.serverobject.ServerObjectBuilder
import net.psforever.objects.serverobject.structures.{Building, FoundationBuilder, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals.ProximityTerminal
import net.psforever.objects.zones.Zone
import net.psforever.types.{PlanetSideGUID, Vector3}

class BuildingBuilderTest extends FreedContextActorTest {
  "Building object" should {
    "build" in {
      val structure: (String, Int, Int, Zone, ActorContext) => Building = Building.Structure(StructureType.Building)
      val building = FoundationBuilder(structure).Build("building", 10, 10, Zone.Nowhere)(context)
      assert(building ne null)
      assert(building.isInstanceOf[Building])
      assert(building.MapId == 10)
      assert(building.Zone == Zone.Nowhere)
    }
  }
}

class WarpGateBuilderTest extends FreedContextActorTest {
  "WarpGate object" should {
    "build" in {
      val structure: (String, Int, Int, Zone, ActorContext) => Building = WarpGate.Structure
      val building = FoundationBuilder(structure).Build("wgate", 10, 10, Zone.Nowhere)(context)
      assert(building ne null)
      assert(building.isInstanceOf[WarpGate])
      assert(building.MapId == 10)
      assert(building.Zone == Zone.Nowhere)
    }
  }
}

class DoorObjectBuilderTest1 extends FreedContextActorTest {
  import net.psforever.objects.serverobject.doors.Door
  "Door object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val obj = ServerObjectBuilder(1, Door.Constructor).Build(context, hub)
      assert(obj.isInstanceOf[Door])
      assert(obj.HasGUID)
      assert(obj.GUID == PlanetSideGUID(1))
      assert(obj == hub(1).get)
    }
  }
}

class DoorObjectBuilderTest2 extends FreedContextActorTest {
  import net.psforever.objects.serverobject.doors.Door
  "Door object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val obj = ServerObjectBuilder(1, Door.Constructor(Vector3(1, 2, 3))).Build(context, hub)
      assert(obj.isInstanceOf[Door])
      assert(obj.HasGUID)
      assert(obj.GUID == PlanetSideGUID(1))
      assert(obj == hub(1).get)
      assert(obj.Position == Vector3(1, 2, 3))
    }
  }
}

class IFFLockObjectBuilderTest extends FreedContextActorTest {
  import net.psforever.objects.serverobject.locks.IFFLock
  "IFFLock object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val obj = ServerObjectBuilder(1, IFFLock.Constructor(Vector3(1f, 1f, 1f), Vector3(2f, 2f, 2f))).Build(context, hub)
      assert(obj.isInstanceOf[IFFLock])
      assert(obj.HasGUID)
      assert(obj.GUID == PlanetSideGUID(1))
      assert(obj.Position == Vector3(1,1,1))
      assert(obj.Outwards == Vector3(0.034899496f, 0.99939084f, 0.0f))
      assert(obj == hub(1).get)
    }
  }
}

class ImplantTerminalMechObjectBuilderTest extends FreedContextActorTest {
  import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
  "Implant terminal mech object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val obj = ServerObjectBuilder(1, ImplantTerminalMech.Constructor(Vector3.Zero)).Build(context, hub)
      assert(obj.isInstanceOf[ImplantTerminalMech])
      assert(obj.HasGUID)
      assert(obj.GUID == PlanetSideGUID(1))
      assert(obj == hub(1).get)
    }
  }
}

class TerminalObjectBuilderTest extends FreedContextActorTest {
  import net.psforever.objects.GlobalDefinitions.order_terminal
  import net.psforever.objects.serverobject.terminals.Terminal
  "Terminal object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val obj = ServerObjectBuilder(1, Terminal.Constructor(Vector3(1.1f, 2.2f, 3.3f), order_terminal)).Build(context, hub)
      assert(obj.isInstanceOf[Terminal])
      assert(obj.HasGUID)
      assert(obj.GUID == PlanetSideGUID(1))
      assert(obj.Position == Vector3(1.1f, 2.2f, 3.3f))
      assert(obj == hub(1).get)
    }
  }
}

class ProximityTerminalObjectBuilderTest extends FreedContextActorTest {
  import net.psforever.objects.GlobalDefinitions.medical_terminal
  import net.psforever.objects.serverobject.terminals.Terminal
  "Terminal object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val obj = ServerObjectBuilder(1, ProximityTerminal.Constructor(medical_terminal)).Build(context, hub)
      assert(obj.isInstanceOf[Terminal])
      assert(obj.HasGUID)
      assert(obj.GUID == PlanetSideGUID(1))
      assert(obj == hub(1).get)
    }
  }
}

class VehicleSpawnPadObjectBuilderTest extends FreedContextActorTest {
  import net.psforever.objects.serverobject.pad.VehicleSpawnPad
  "Vehicle spawn pad object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val obj = ServerObjectBuilder(1,
        VehicleSpawnPad.Constructor(
          Vector3(1.1f, 2.2f, 3.3f), GlobalDefinitions.mb_pad_creation, Vector3(4.4f, 5.5f, 6.6f)
        )
      ).Build(context, hub)
      assert(obj.isInstanceOf[VehicleSpawnPad])
      assert(obj.HasGUID)
      assert(obj.GUID == PlanetSideGUID(1))
      assert(obj.Position == Vector3(1.1f, 2.2f, 3.3f))
      assert(obj.Orientation == Vector3(4.4f, 5.5f, 6.6f))
      assert(obj == hub(1).get)
    }
  }
}

class LocalProjectileBuilderTest extends FreedContextActorTest {
  import net.psforever.objects.LocalProjectile
  "Local projectile object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val obj = ServerObjectBuilder(1, LocalProjectile.Constructor).Build(context, hub)
      assert(obj.isInstanceOf[LocalProjectile])
      assert(obj.HasGUID)
      assert(obj.GUID == PlanetSideGUID(1))
      assert(obj == hub(1).get)
    }
  }
}

class LockerObjectBuilderTest extends FreedContextActorTest {
  import net.psforever.objects.serverobject.mblocker.Locker
  "Locker object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val obj = ServerObjectBuilder(1, Locker.Constructor).Build(context, hub)
      assert(obj.isInstanceOf[Locker])
      assert(obj.HasGUID)
      assert(obj.GUID == PlanetSideGUID(1))
      assert(obj == hub(1).get)
    }
  }
}

class ResourceSiloObjectBuilderTest extends FreedContextActorTest {
  import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
  "Resource silo object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val obj = ServerObjectBuilder(1, ResourceSilo.Constructor(Vector3(1f, 1f, 1f))).Build(context, hub)
      assert(obj.isInstanceOf[ResourceSilo])
      assert(obj.HasGUID)
      assert(obj.GUID == PlanetSideGUID(1))
      assert(obj.Position == Vector3(1,1,1))
      assert(obj == hub(1).get)
    }
  }
}

class SpawnTubeObjectBuilderTest extends FreedContextActorTest {
  import net.psforever.objects.serverobject.tube.SpawnTube
  "Spawn tube object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val obj = ServerObjectBuilder(
        1,
        SpawnTube.Constructor(Vector3(3980.4062f, 4267.3047f, 257.5625f), Vector3(0, 0, 90))
      ).Build(context, hub)
      assert(obj.isInstanceOf[SpawnTube])
      assert(obj.HasGUID)
      assert(obj.GUID == PlanetSideGUID(1))
      assert(obj.Position == Vector3(3980.4062f, 4267.3047f, 257.5625f))
      assert(obj.Orientation == Vector3(0, 0, 90))
      assert(obj == hub(1).get)
    }
  }
}

class FacilityTurretObjectBuilderTest extends FreedContextActorTest {
  import net.psforever.objects.GlobalDefinitions.manned_turret
  import net.psforever.objects.serverobject.turret.FacilityTurret
  "FacilityTurretObjectBuilder" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val obj = ServerObjectBuilder(1, FacilityTurret.Constructor(manned_turret)).Build(context, hub)
      assert(obj.isInstanceOf[FacilityTurret])
      assert(obj.HasGUID)
      assert(obj.GUID == PlanetSideGUID(1))
      assert(obj == hub(1).get)
    }
  }
}

object ServerObjectBuilderTest {
  import net.psforever.objects.guid.source.MaxNumberSource
  def NumberPoolHub: NumberPoolHub = {
    val obj = new NumberPoolHub(new MaxNumberSource(2))
    obj
  }
}

// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, ActorContext, Props}
import base.ActorTest
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.objects.serverobject.ServerObjectBuilder
import net.psforever.objects.serverobject.structures.{Building, FoundationBuilder, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals.ProximityTerminal
import net.psforever.objects.zones.Zone
import net.psforever.types.Vector3

import scala.concurrent.duration.Duration

class BuildingBuilderTest extends ActorTest {
  "Building object" should {
    "build" in {
      val structure : (Int,Int,Zone,ActorContext)=>Building = Building.Structure(StructureType.Building)
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuildingTestActor], structure, 10, 10, Zone.Nowhere), "building")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[Building])
      assert(reply.asInstanceOf[Building].MapId == 10)
      assert(reply.asInstanceOf[Building].Zone == Zone.Nowhere)
    }
  }
}

class WarpGateBuilderTest extends ActorTest {
  "WarpGate object" should {
    "build" in {
      val structure : (Int,Int,Zone,ActorContext)=>Building = WarpGate.Structure
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuildingTestActor], structure, 10, 10, Zone.Nowhere), "wgate")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[Building])
      assert(reply.asInstanceOf[Building].MapId == 10)
      assert(reply.asInstanceOf[Building].Zone == Zone.Nowhere)
    }
  }
}

class DoorObjectBuilderTest1 extends ActorTest {
  import net.psforever.objects.serverobject.doors.Door
  "Door object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1, Door.Constructor), hub), "door")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[Door])
      assert(reply.asInstanceOf[Door].HasGUID)
      assert(reply.asInstanceOf[Door].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class DoorObjectBuilderTest2 extends ActorTest {
  import net.psforever.objects.serverobject.doors.Door
  "Door object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1, Door.Constructor(Vector3(1, 2, 3))), hub), "door")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[Door])
      assert(reply.asInstanceOf[Door].Position == Vector3(1, 2, 3))
      assert(reply.asInstanceOf[Door].HasGUID)
      assert(reply.asInstanceOf[Door].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class IFFLockObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.locks.IFFLock
  "IFFLock object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1, IFFLock.Constructor(Vector3(0f, 0f, 0f), Vector3(0f, 0f, 0f))), hub), "lock")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[IFFLock])
      assert(reply.asInstanceOf[IFFLock].HasGUID)
      assert(reply.asInstanceOf[IFFLock].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class ImplantTerminalMechObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
  "Implant terminal mech object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1, ImplantTerminalMech.Constructor), hub), "mech")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[ImplantTerminalMech])
      assert(reply.asInstanceOf[ImplantTerminalMech].HasGUID)
      assert(reply.asInstanceOf[ImplantTerminalMech].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class TerminalObjectBuilderTest extends ActorTest {
  import net.psforever.objects.GlobalDefinitions.order_terminal
  import net.psforever.objects.serverobject.terminals.Terminal
  "Terminal object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1, Terminal.Constructor(order_terminal)), hub), "term")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[Terminal])
      assert(reply.asInstanceOf[Terminal].HasGUID)
      assert(reply.asInstanceOf[Terminal].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class ProximityTerminalObjectBuilderTest extends ActorTest {
  import net.psforever.objects.GlobalDefinitions.medical_terminal
  import net.psforever.objects.serverobject.terminals.Terminal
  "Terminal object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1,
        ProximityTerminal.Constructor(medical_terminal)), hub), "term")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[Terminal])
      assert(reply.asInstanceOf[Terminal].HasGUID)
      assert(reply.asInstanceOf[Terminal].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class VehicleSpawnPadObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.pad.VehicleSpawnPad
  "Vehicle spawn pad object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1,
        VehicleSpawnPad.Constructor(GlobalDefinitions.mb_pad_creation, Vector3(1.1f, 2.2f, 3.3f), Vector3(4.4f, 5.5f, 6.6f))
      ), hub), "pad")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[VehicleSpawnPad])
      assert(reply.asInstanceOf[VehicleSpawnPad].HasGUID)
      assert(reply.asInstanceOf[VehicleSpawnPad].GUID == PlanetSideGUID(1))
      assert(reply.asInstanceOf[VehicleSpawnPad].Position == Vector3(1.1f, 2.2f, 3.3f))
      assert(reply.asInstanceOf[VehicleSpawnPad].Orientation == Vector3(4.4f, 5.5f, 6.6f))
      assert(reply == hub(1).get)
    }
  }
}

class LocalProjectileBuilderTest extends ActorTest {
  import net.psforever.objects.LocalProjectile
  "Local projectile object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1,
        LocalProjectile.Constructor), hub), "locker")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[LocalProjectile])
      assert(reply.asInstanceOf[LocalProjectile].HasGUID)
      assert(reply.asInstanceOf[LocalProjectile].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class LockerObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.mblocker.Locker
  "Locker object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1,
        Locker.Constructor), hub), "locker")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[Locker])
      assert(reply.asInstanceOf[Locker].HasGUID)
      assert(reply.asInstanceOf[Locker].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class ResourceSiloObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
  "Resource silo object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1,
        ResourceSilo.Constructor), hub), "spawn-tube")
      actor ! "startup"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[ResourceSilo])
      assert(reply.asInstanceOf[ResourceSilo].HasGUID)
      assert(reply.asInstanceOf[ResourceSilo].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class SpawnTubeObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.tube.SpawnTube
  "Spawn tube object" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1,
        SpawnTube.Constructor(Vector3(3980.4062f, 4267.3047f, 257.5625f), Vector3(0, 0, 90))), hub), "spawn-tube")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[SpawnTube])
      assert(reply.asInstanceOf[SpawnTube].HasGUID)
      assert(reply.asInstanceOf[SpawnTube].GUID == PlanetSideGUID(1))
      assert(reply.asInstanceOf[SpawnTube].Position == Vector3(3980.4062f, 4267.3047f, 257.5625f))
      assert(reply.asInstanceOf[SpawnTube].Orientation == Vector3(0, 0, 90))
      assert(reply == hub(1).get)
    }
  }
}

class FacilityTurretObjectBuilderTest extends ActorTest {
  import net.psforever.objects.GlobalDefinitions.manned_turret
  import net.psforever.objects.serverobject.turret.FacilityTurret
  "FacilityTurretObjectBuilder" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1,
        FacilityTurret.Constructor(manned_turret)), hub), "spawn-tube")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[FacilityTurret])
      assert(reply.asInstanceOf[FacilityTurret].HasGUID)
      assert(reply.asInstanceOf[FacilityTurret].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

object ServerObjectBuilderTest {
  import net.psforever.objects.guid.source.LimitedNumberSource
  def NumberPoolHub : NumberPoolHub = {
    val obj = new NumberPoolHub(new LimitedNumberSource(2))
    obj
  }

  class BuilderTestActor(builder : ServerObjectBuilder[_], hub : NumberPoolHub) extends Actor {
    def receive : Receive = {
      case _ =>
        sender ! builder.Build(context, hub)
    }
  }

  class BuildingTestActor(structure_con : (Int,Int,Zone,ActorContext)=>Building, building_guid : Int, map_id : Int, zone : Zone) extends Actor {
    def receive : Receive = {
      case _ =>
        sender ! FoundationBuilder(structure_con).Build(building_guid, map_id, zone)(context)
    }
  }
}

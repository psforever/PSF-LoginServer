// Copyright (c) 2021 PSForever
package objects

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import base.FreedContextActorTest
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.objects.guid.{NumberPoolHub, UniqueNumberOps, UniqueNumberSetup}
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.shuttle.{OrbitalShuttle, OrbitalShuttlePad, OrbitalShuttlePadControl, ShuttleAmenity}
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.zones.{Zone, ZoneMap, ZoneVehicleActor}
import net.psforever.services.{InterstellarClusterService, Service, ServiceManager}
import net.psforever.services.galaxy.GalaxyService
import net.psforever.services.hart.HartService
import net.psforever.types.PlanetSideEmpire

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.duration._

class OrbitalShuttlePadControlTest extends FreedContextActorTest {
  import akka.actor.typed.scaladsl.adapter._
  val services: ActorRef = ServiceManager.boot(system)
  services ! ServiceManager.Register(Props[GalaxyService](), "galaxy")
  services ! ServiceManager.Register(Props[HartService](), "hart")
  expectNoMessage(1000 milliseconds)
  var buildingMap = new TrieMap[Int, Building]()
  val vehicles: mutable.ListBuffer[Vehicle] = mutable.ListBuffer[Vehicle]()
  val guid = new NumberPoolHub(new MaxNumberSource(max = 20))
  guid.AddPool("vehicles", (11 to 15).toList)
  guid.AddPool("tools", (16 to 19).toList)
  val catchall: ActorRef = new TestProbe(system).ref
  val unops: UniqueNumberOps = new UniqueNumberOps(guid, UniqueNumberSetup.AllocateNumberPoolActors(context, guid))
  val zone: Zone = new Zone("test", new ZoneMap("test-map"), 0) {
    val transport: ActorRef = context.actorOf(Props(classOf[ZoneVehicleActor], this, vehicles, mutable.HashMap[Int, Int]()), s"zone-test-vehicles")

    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def GUID: UniqueNumberOps = { unops }
    override def AvatarEvents: ActorRef = catchall
    override def LocalEvents: ActorRef = catchall
    override def VehicleEvents: ActorRef = catchall
    override def Activity: ActorRef = catchall
    override def Transport: ActorRef = { transport }
    override def Vehicles:List[Vehicle] = { vehicles.toList }
    override def Buildings: Map[Int, Building] = { buildingMap.toMap }

    import akka.actor.typed.scaladsl.adapter._
    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }
  val building = new Building(
    name = "test-orbital-building-tr",
    building_guid = 1,
    map_id = 0,
    zone,
    StructureType.Building,
    GlobalDefinitions.orbital_building_tr
  )
  buildingMap += 1 -> building
  system.spawn(InterstellarClusterService(Seq(zone)), InterstellarClusterService.InterstellarClusterServiceKey.id)
  building.Faction = PlanetSideEmpire.TR
  building.Actor = context.spawn(BuildingActor(zone, building), "test-orbital-building-tr-control").toClassic
  building.Invalidate()
  guid.register(building, number = 1)

  (3 to 10).foreach { index =>
    val door = Door(GlobalDefinitions.gr_door_mb_orb)
    building.Amenities = door
    door.Actor = catchall
    guid.register(door, index)
  }

  val pad = new OrbitalShuttlePad(GlobalDefinitions.obbasemesh)
  guid.register(pad, number = 2)
  pad.Actor = system.actorOf(Props(classOf[OrbitalShuttlePadControl], pad), "test-shuttle-pad")
  building.Amenities = pad

  "OrbitalShuttlePad" should {
    "startup and create the shuttle" in {
      assert(building.Amenities.size == 9)
      assert(vehicles.isEmpty)
      pad.Actor ! Service.Startup()
      expectNoMessage(max = 5 seconds)
      assert(building.Amenities.size == 10)
      assert(vehicles.size == 1)
      assert(building.Amenities(9).isInstanceOf[ShuttleAmenity]) //the shuttle is an amenity of the building now
      assert(vehicles.head.isInstanceOf[OrbitalShuttle]) //here is the shuttle
    }
  }
}

object OrbitalShuttlePadTest { /* intentionally blank */ }

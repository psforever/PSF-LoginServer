// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.serverobject.structures.StructureType
import net.psforever.objects.{Avatar, GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterVoice, PlanetSideEmpire, Vector3}
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class VehicleSpawnPadTest extends Specification {
  "VehicleSpawnPadDefinition" should {
    "define" in {
      GlobalDefinitions.mb_pad_creation.ObjectId mustEqual 525
    }
  }

  "VehicleSpawnPad" should {
    "construct" in {
      val obj = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
      obj.Actor mustEqual ActorRef.noSender
      obj.Definition mustEqual GlobalDefinitions.mb_pad_creation
      obj.Railed mustEqual true
    }

    "un-railed" in {
      val obj = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
      obj.Railed mustEqual true
      obj.Railed = false
      obj.Railed mustEqual false
    }
  }
}

class VehicleSpawnControl1Test extends ActorTest {
  "VehicleSpawnControl" should {
    "construct" in {
      val obj = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
      obj.Actor = system.actorOf(Props(classOf[VehicleSpawnControl], obj), "mb_pad_creation")
      assert(obj.Actor != ActorRef.noSender)
    }
  }
}

class VehicleSpawnControl2aTest extends ActorTest {
  // This runs for a long time.
  "VehicleSpawnControl" should {
    "complete on a vehicle order (block a second one until the first is done and the spawn pad is cleared)" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      //we can recycle the vehicle and the player for each order
      val probe1 = new TestProbe(system, "first-order")
      val probe2 = new TestProbe(system, "second-order")
      val probe3 = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe3.ref

      pad.Actor.tell(VehicleSpawnPad.VehicleOrder(player, vehicle), probe1.ref) //first order
      pad.Actor.tell(VehicleSpawnPad.VehicleOrder(player, vehicle), probe2.ref) //second order

      val probe2Msg1 = probe2.receiveOne(100 milliseconds)
      assert(probe2Msg1.isInstanceOf[VehicleSpawnPad.PeriodicReminder])
      assert(probe2Msg1.asInstanceOf[VehicleSpawnPad.PeriodicReminder].reason == VehicleSpawnPad.Reminders.Queue)
      assert(probe2Msg1.asInstanceOf[VehicleSpawnPad.PeriodicReminder].data.contains("2"))

      val probe3Msg1 = probe3.receiveOne(3 seconds)
      assert(probe3Msg1.isInstanceOf[VehicleSpawnPad.ConcealPlayer])

      val probe3Msg2 = probe3.receiveOne(3 seconds)
      assert(probe3Msg2.isInstanceOf[VehicleSpawnPad.LoadVehicle])

      val probe3Msg3 = probe3.receiveOne(200 milliseconds)
      assert(probe3Msg3.isInstanceOf[VehicleSpawnPad.AttachToRails])

      val probe1Msg1 = probe1.receiveOne(200 milliseconds)
      assert(probe1Msg1.isInstanceOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      val probe1Msg2 = probe1.receiveOne(200 milliseconds)
      assert(probe1Msg2.isInstanceOf[Mountable.MountMessages])
      val probe1Msg2Contents = probe1Msg2.asInstanceOf[Mountable.MountMessages]
      assert(probe1Msg2Contents.response.isInstanceOf[Mountable.CanMount])
      val probe1Msg3 = probe1.receiveOne(3 seconds)
      assert(probe1Msg3.isInstanceOf[VehicleSpawnPad.PlayerSeatedInVehicle])

      val probe3Msg4 = probe3.receiveOne(1 seconds)
      assert(probe3Msg4.isInstanceOf[VehicleSpawnPad.DetachFromRails])

      val probe1Msg4 = probe1.receiveOne(1 seconds)
      assert(probe1Msg4.isInstanceOf[VehicleSpawnPad.ServerVehicleOverrideStart])
      val probe1Msg5 = probe1.receiveOne(4 seconds)
      assert(probe1Msg5.isInstanceOf[VehicleSpawnPad.ServerVehicleOverrideEnd])

      val probe1Msg6 = probe1.receiveOne(11 seconds)
      assert(probe1Msg6.isInstanceOf[VehicleSpawnPad.PeriodicReminder])
      assert(probe1Msg6.asInstanceOf[VehicleSpawnPad.PeriodicReminder].reason == VehicleSpawnPad.Reminders.Blocked)
      val probe2Msg2 = probe2.receiveOne(100 milliseconds)
      assert(probe2Msg2.isInstanceOf[VehicleSpawnPad.PeriodicReminder])
      assert(probe2Msg2.asInstanceOf[VehicleSpawnPad.PeriodicReminder].reason == VehicleSpawnPad.Reminders.Blocked)

      //if we move the vehicle more than 25m away from the pad, we should receive a ResetSpawnPad, and a second ConcealPlayer message
      //that means that the first order has cleared and the spawn pad is now working on the second order successfully
      player.VehicleSeated = None //since shared between orders, is necessary
      vehicle.Position = Vector3(12,0,0)
      val probe3Msg5 = probe3.receiveOne(4 seconds)
      assert(probe3Msg5.isInstanceOf[VehicleSpawnPad.ResetSpawnPad])
      val probe3Msg6 = probe3.receiveOne(4 seconds)
      assert(probe3Msg6.isInstanceOf[VehicleSpawnPad.ConcealPlayer])
    }
  }
}

class VehicleSpawnControl2bTest extends ActorTest {
  // This runs for a long time.
  "VehicleSpawnControl" should {
    "complete on a vehicle order (railless)" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      //we can recycle the vehicle and the player for each order
      val probe1 = new TestProbe(system, "first-order")
      val probe2 = new TestProbe(system, "second-order")
      val probe3 = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe3.ref
      pad.Railed = false

      pad.Actor.tell(VehicleSpawnPad.VehicleOrder(player, vehicle), probe1.ref) //first order
      pad.Actor.tell(VehicleSpawnPad.VehicleOrder(player, vehicle), probe2.ref) //second order

      val probe2Msg1 = probe2.receiveOne(100 milliseconds)
      assert(probe2Msg1.isInstanceOf[VehicleSpawnPad.PeriodicReminder])
      assert(probe2Msg1.asInstanceOf[VehicleSpawnPad.PeriodicReminder].reason == VehicleSpawnPad.Reminders.Queue)
      assert(probe2Msg1.asInstanceOf[VehicleSpawnPad.PeriodicReminder].data.contains("2"))

      val probe3Msg1 = probe3.receiveOne(3 seconds)
      assert(probe3Msg1.isInstanceOf[VehicleSpawnPad.ConcealPlayer])

      val probe3Msg2 = probe3.receiveOne(3 seconds)
      assert(probe3Msg2.isInstanceOf[VehicleSpawnPad.LoadVehicle])

      val probe1Msg1 = probe1.receiveOne(200 milliseconds)
      assert(probe1Msg1.isInstanceOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      val probe1Msg2 = probe1.receiveOne(200 milliseconds)
      assert(probe1Msg2.isInstanceOf[Mountable.MountMessages])
      val probe1Msg2Contents = probe1Msg2.asInstanceOf[Mountable.MountMessages]
      assert(probe1Msg2Contents.response.isInstanceOf[Mountable.CanMount])
      val probe1Msg3 = probe1.receiveOne(4 seconds)
      assert(probe1Msg3.isInstanceOf[VehicleSpawnPad.PlayerSeatedInVehicle])

      val probe1Msg4 = probe1.receiveOne(1 seconds)
      assert(probe1Msg4.isInstanceOf[VehicleSpawnPad.ServerVehicleOverrideStart])
      val probe1Msg5 = probe1.receiveOne(4 seconds)
      assert(probe1Msg5.isInstanceOf[VehicleSpawnPad.ServerVehicleOverrideEnd])

      val probe1Msg6 = probe1.receiveOne(11 seconds)
      assert(probe1Msg6.isInstanceOf[VehicleSpawnPad.PeriodicReminder])
      assert(probe1Msg6.asInstanceOf[VehicleSpawnPad.PeriodicReminder].reason == VehicleSpawnPad.Reminders.Blocked)
      val probe2Msg2 = probe2.receiveOne(100 milliseconds)
      assert(probe2Msg2.isInstanceOf[VehicleSpawnPad.PeriodicReminder])
      assert(probe2Msg2.asInstanceOf[VehicleSpawnPad.PeriodicReminder].reason == VehicleSpawnPad.Reminders.Blocked)

      //if we move the vehicle more than 10m away from the pad, we should receive a second ConcealPlayer message
      //that means that the first order has cleared and the spawn pad is now working on the second order successfully
      player.VehicleSeated = None //since shared between orders, is necessary
      vehicle.Position = Vector3(12,0,0)
      val probe3Msg6 = probe3.receiveOne(10 seconds)
      assert(probe3Msg6.isInstanceOf[VehicleSpawnPad.ConcealPlayer])
    }
  }
}

class VehicleSpawnControl3Test extends ActorTest {
  "VehicleSpawnControl" should {
    "player is on wrong continent before vehicle can partially load; vehicle is cleaned up" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe1 = new TestProbe(system, "first-order")
      val probe3 = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe3.ref
      player.Continent = "problem" //problem

      assert(vehicle.HasGUID)
      pad.Actor.tell(VehicleSpawnPad.VehicleOrder(player, vehicle), probe1.ref)

      val probe3Msg1 = probe3.receiveOne(3 seconds)
      assert(probe3Msg1.isInstanceOf[VehicleSpawnPad.RevealPlayer])
      probe3.expectNoMsg(5 seconds)
      assert(!vehicle.HasGUID) //vehicle has been unregistered
    }
  }
}

class VehicleSpawnControl4Test extends ActorTest() {
  "VehicleSpawnControl" should {
    "the player is on wrong continent when the vehicle tries to load; vehicle is cleaned up" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe1 = new TestProbe(system, "first-order")
      val probe3 = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe3.ref

      pad.Actor.tell(VehicleSpawnPad.VehicleOrder(player, vehicle), probe1.ref)

      val probe3Msg1 = probe3.receiveOne(3 seconds)
      assert(probe3Msg1.isInstanceOf[VehicleSpawnPad.ConcealPlayer])
      player.Continent = "problem" //problem
      assert(vehicle.HasGUID)

      val probe3Msg2 = probe3.receiveOne(3 seconds)
      assert(probe3Msg2.isInstanceOf[VehicleSpawnPad.RevealPlayer])
      probe3.expectNoMsg(5 seconds)
      assert(!vehicle.HasGUID) //vehicle has been unregistered
    }
  }
}

//class VehicleSpawnControl5aTest extends ActorTest() {
//  "VehicleSpawnControl" should {
//    "the vehicle is destroyed before being fully loaded; the vehicle is cleaned up" in {
//      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
//      //we can recycle the vehicle and the player for each order
//      val probe1 = new TestProbe(system, "first-order")
//      val probe3 = new TestProbe(system, "zone-events")
//      zone.VehicleEvents = probe3.ref
//
//      pad.Actor.tell(VehicleSpawnPad.VehicleOrder(player, vehicle), probe1.ref)
//
//      val probe3Msg1 = probe3.receiveOne(3 seconds)
//      assert(probe3Msg1.isInstanceOf[VehicleSpawnPad.ConcealPlayer])
//
//      val probe3Msg2 = probe3.receiveOne(3 seconds)
//      assert(probe3Msg2.isInstanceOf[VehicleSpawnPad.LoadVehicle])
//      vehicle.Health = 0 //problem
//
//      val probe3Msg3 = probe3.receiveOne(3 seconds)
//      assert(probe3Msg3.isInstanceOf[VehicleSpawnPad.DisposeVehicle])
//      val probe3Msg4 = probe3.receiveOne(100 milliseconds)
//      assert(probe3Msg4.isInstanceOf[VehicleSpawnPad.RevealPlayer])
//      //note: the vehicle will not be unregistered by this logic alone
//      //since LoadVehicle should introduce it into the game world properly, it has to be handled properly
//    }
//  }
//}

class VehicleSpawnControl5Test extends ActorTest {
  "VehicleSpawnControl" should {
    "player dies right after vehicle partially loads; the vehicle spawns and blocks the pad" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      //we can recycle the vehicle and the player for each order
      val probe1 = new TestProbe(system, "first-order")
      val probe3 = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe3.ref

      pad.Actor.tell(VehicleSpawnPad.VehicleOrder(player, vehicle), probe1.ref)

      val probe3Msg1 = probe3.receiveOne(3 seconds)
      assert(probe3Msg1.isInstanceOf[VehicleSpawnPad.ConcealPlayer])

      val probe3Msg2 = probe3.receiveOne(3 seconds)
      assert(probe3Msg2.isInstanceOf[VehicleSpawnPad.LoadVehicle])
      player.Die //problem

      val probe3Msg3 = probe3.receiveOne(3 seconds)
      assert(probe3Msg3.isInstanceOf[VehicleSpawnPad.AttachToRails])
      val probe3Msg4 = probe3.receiveOne(3 seconds)
      assert(probe3Msg4.isInstanceOf[VehicleSpawnPad.DetachFromRails])

      val probe3Msg5 = probe3.receiveOne(1 seconds)
      assert(probe3Msg5.isInstanceOf[VehicleSpawnPad.RevealPlayer])

      val probe1Msg = probe1.receiveOne(12 seconds)
      assert(probe1Msg.isInstanceOf[VehicleSpawnPad.PeriodicReminder])
      assert(probe1Msg.asInstanceOf[VehicleSpawnPad.PeriodicReminder].reason == VehicleSpawnPad.Reminders.Blocked)
    }
  }
}

class VehicleSpawnControl6Test extends ActorTest {
  "VehicleSpawnControl" should {
    "the player can not sit in vehicle; vehicle spawns and blocks the pad" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      //we can recycle the vehicle and the player for each order
      val probe1 = new TestProbe(system, "first-order")
      val probe3 = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe3.ref

      pad.Actor.tell(VehicleSpawnPad.VehicleOrder(player, vehicle), probe1.ref)

      val probe3Msg1 = probe3.receiveOne(3 seconds)
      assert(probe3Msg1.isInstanceOf[VehicleSpawnPad.ConcealPlayer])

      val probe3Msg2 = probe3.receiveOne(3 seconds)
      assert(probe3Msg2.isInstanceOf[VehicleSpawnPad.LoadVehicle])

      val probe3Msg3 = probe3.receiveOne(3 seconds)
      assert(probe3Msg3.isInstanceOf[VehicleSpawnPad.AttachToRails])

      val probe1Msg1 = probe1.receiveOne(200 milliseconds)
      assert(probe1Msg1.isInstanceOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      player.Continent = "problem" //problem
      probe1.receiveOne(200 milliseconds) //Mountable.MountMessage

      val probe3Msg4 = probe3.receiveOne(3 seconds)
      assert(probe3Msg4.isInstanceOf[VehicleSpawnPad.DetachFromRails])
      val probe3Msg5 = probe3.receiveOne(3 seconds)
      assert(probe3Msg5.isInstanceOf[VehicleSpawnPad.RevealPlayer])

      val probe1Msg3 = probe1.receiveOne(12 seconds)
      assert(probe1Msg3.isInstanceOf[VehicleSpawnPad.PeriodicReminder])
      assert(probe1Msg3.asInstanceOf[VehicleSpawnPad.PeriodicReminder].reason == VehicleSpawnPad.Reminders.Blocked)
    }
  }
}

object VehicleSpawnPadControlTest {
  import net.psforever.objects.zones.ZoneMap
  private val map = new ZoneMap("test-map")

  def SetUpAgents(faction : PlanetSideEmpire.Value)(implicit system : ActorSystem) : (Vehicle, Player, VehicleSpawnPad, Zone) = {
    import net.psforever.objects.guid.NumberPoolHub
    import net.psforever.objects.guid.source.LimitedNumberSource
    import net.psforever.objects.serverobject.structures.Building
    import net.psforever.objects.vehicles.VehicleControl
    import net.psforever.objects.zones.ZoneActor
    import net.psforever.objects.Tool
    import net.psforever.types.CharacterGender

    val zone = new Zone("test-zone", map, 0)  { override def SetupNumberPools() = { } }
    val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
    val weapon = vehicle.WeaponControlledFromSeat(1).get.asInstanceOf[Tool]
    val guid : NumberPoolHub = new NumberPoolHub(LimitedNumberSource(5))
    guid.AddPool("test-pool", (0 to 2).toList)
    guid.register(vehicle, "test-pool")
    guid.register(weapon, "test-pool")
    guid.register(weapon.AmmoSlot.Box, "test-pool")
    zone.GUID(guid)
    zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), s"test-zone-${System.nanoTime()}")
    zone.Actor ! Zone.Init()
    vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), s"vehicle-control-${System.nanoTime()}")

    val pad = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
    pad.Actor = system.actorOf(Props(classOf[VehicleSpawnControl], pad), s"test-pad-${System.nanoTime()}")
    pad.Owner = new Building(building_guid = 0, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
    pad.Owner.Faction = faction
    val player = Player(Avatar("test", faction, CharacterGender.Male, 0, CharacterVoice.Mute))
    player.GUID = PlanetSideGUID(10)
    player.Continent = zone.Id
    player.Spawn
    //note: pad and vehicle are both at Vector3(1,0,0) so they count as blocking
    pad.Position = Vector3(1,0,0)
    vehicle.Position = Vector3(1,0,0)
    (vehicle, player, pad, zone)
  }
}

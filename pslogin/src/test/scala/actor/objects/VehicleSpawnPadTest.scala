// Copyright (c) 2017 PSForever
package actor.objects

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import actor.base.ActorTest
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.serverobject.structures.StructureType
import net.psforever.objects.{Avatar, GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.zones.Zone
import net.psforever.types.{PlanetSideGUID, _}
import services.RemoverActor
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.duration._

class VehicleSpawnControl1Test extends ActorTest {
  "VehicleSpawnControl" should {
    "construct" in {
      val obj = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
      obj.Actor = system.actorOf(Props(classOf[VehicleSpawnControl], obj), "mb_pad_creation")
      assert(obj.Actor != ActorRef.noSender)
    }
  }
}

class VehicleSpawnControl2Test extends ActorTest {
  "VehicleSpawnControl" should {
    "complete a vehicle order" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe = new TestProbe(system, "zone-events")

      zone.VehicleEvents = probe.ref //zone events
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle) //order

      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.LoadVehicle])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      vehicle.Seats(0).Occupant = player
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.PlayerSeatedInVehicle])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ServerVehicleOverrideStart])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ServerVehicleOverrideEnd])
      //if we move the vehicle away from the pad, we should receive a ResetSpawnPad message
      //that means that the first order has cleared and the spawn pad is now waiting for additional orders
      vehicle.Position = Vector3(12,0,0)
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ResetSpawnPad])
    }
  }
}

class VehicleSpawnControl3Test extends ActorTest {
  "VehicleSpawnControl" should {
    "block the second vehicle order until the first is completed" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      //we can recycle the vehicle and the player for each order
      val probe = new TestProbe(system, "zone-events")
      val player2 = Player(Avatar("test2", player.Faction, CharacterGender.Male, 0, CharacterVoice.Mute))
      player2.GUID = PlanetSideGUID(11)
      player2.Continent = zone.Id
      player2.Spawn

      zone.VehicleEvents = probe.ref //zone events
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle) //first order
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player2, vehicle) //second order (vehicle shared)

      assert(probe.receiveOne(1 seconds) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Queue, _) => true
        case _ => false
      })
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.LoadVehicle])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      vehicle.Seats(0).Occupant = player
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.PlayerSeatedInVehicle])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ServerVehicleOverrideStart])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ServerVehicleOverrideEnd])
      assert(probe.receiveOne(1 minute) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Blocked, _) => true
        case _ => false
      })
      assert(probe.receiveOne(1 minute) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Blocked, _) => true
        case _ => false
      })

      //if we move the vehicle away from the pad, we should receive a second ConcealPlayer message
      //that means that the first order has cleared and the spawn pad is now working on the second order successfully
      player.VehicleSeated = None //since shared between orders, as necessary
      vehicle.Seats(0).Occupant = None
      vehicle.Position = Vector3(12,0,0)
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ResetSpawnPad])
      probe.expectMsgClass(3 seconds, classOf[VehicleSpawnPad.ConcealPlayer])
    }
  }
}

class VehicleSpawnControl4Test extends ActorTest {
  "VehicleSpawnControl" should {
    "clean up the vehicle if the driver-to-be is on the wrong continent" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe.ref
      player.Continent = "problem" //problem

      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle) //order

      val msg = probe.receiveOne(1 minute)
      assert(
        msg match {
          case VehicleServiceMessage.Decon(RemoverActor.AddTask(v, z , _)) => (v == vehicle) && (z == zone)
          case _ => false
        }
      )
      probe.expectNoMsg(5 seconds)
    }
  }
}

class VehicleSpawnControl5Test extends ActorTest() {
  "VehicleSpawnControl" should {
    "abandon a destroyed vehicle on the spawn pad (blocking)" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)

      val probe = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe.ref
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle) //order

      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.LoadVehicle])
      vehicle.Health = 0 //problem
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.RevealPlayer])
      assert(probe.receiveOne(1 minute) match {
        case VehicleServiceMessage(_, VehicleAction.LoadVehicle(_,_,_,_,_)) => true
        case _ => false
      })
      assert(probe.receiveOne(1 minute) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Blocked, _) => true
        case _ => false
      })
    }
  }
}

class VehicleSpawnControl6Test extends ActorTest() {
  "VehicleSpawnControl" should {
    "abandon a vehicle on the spawn pad if driver is unfit to drive (blocking)" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)

      val probe = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe.ref
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle) //order

      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.LoadVehicle])
      player.Die
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.RevealPlayer])
      assert(probe.receiveOne(1 minute) match {
        case VehicleServiceMessage(_, VehicleAction.LoadVehicle(_,_,_,_,_)) => true
        case _ => false
      })
      assert(probe.receiveOne(1 minute) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Blocked, _) => true
        case _ => false
      })
    }
  }
}

class VehicleSpawnControl7Test extends ActorTest {
  "VehicleSpawnControl" should {
    "abandon a vehicle on the spawn pad if driver is unfit to drive (blocking)" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe = new TestProbe(system, "zone-events")
      player.ExoSuit = ExoSuitType.MAX

      zone.VehicleEvents = probe.ref //zone events
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle) //order

      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.LoadVehicle])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.RevealPlayer])
      assert(probe.receiveOne(1 minute) match {
        case VehicleServiceMessage(_, VehicleAction.LoadVehicle(_,_,_,_,_)) => true
        case _ => false
      })
      assert(probe.receiveOne(1 minute) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Blocked, _) => true
        case _ => false
      })
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

    val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
    val weapon = vehicle.WeaponControlledFromSeat(1).get.asInstanceOf[Tool]
    val guid : NumberPoolHub = new NumberPoolHub(LimitedNumberSource(5))
    guid.AddPool("test-pool", (0 to 5).toList)
    guid.register(vehicle, "test-pool")
    guid.register(weapon, "test-pool")
    guid.register(weapon.AmmoSlot.Box, "test-pool")
    val zone = new Zone("test-zone", map, 0)  {
      override def SetupNumberPools() : Unit = { }
    }
    zone.GUID(guid)
    zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), s"test-zone-${System.nanoTime()}")
    zone.Actor ! Zone.Init()

    // Hack: Wait for the Zone to finish booting, otherwise later tests will fail randomly due to race conditions
    // with actor probe setting
    // TODO(chord): Remove when Zone supports notification of booting being complete
    Thread.sleep(5000)

    vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), s"vehicle-control-${System.nanoTime()}")

    val pad = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
    pad.Actor = system.actorOf(Props(classOf[VehicleSpawnControl], pad), s"test-pad-${System.nanoTime()}")
    pad.Owner = new Building("Building", building_guid = 0, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
    pad.Owner.Faction = faction
    pad.Zone = zone
    guid.register(pad, "test-pool")
    val player = Player(Avatar("test", faction, CharacterGender.Male, 0, CharacterVoice.Mute))
    guid.register(player, "test-pool")
    player.Zone = zone
    player.Spawn
    //note: pad and vehicle are both at Vector3(1,0,0) so they count as blocking
    pad.Position = Vector3(1,0,0)
    vehicle.Position = Vector3(1,0,0)
    (vehicle, player, pad, zone)
  }
}

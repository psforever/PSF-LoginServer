// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.serverobject.structures.StructureType
import net.psforever.objects.{Avatar, GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterVoice, ExoSuitType, PlanetSideEmpire, Vector3}
import org.specs2.mutable.Specification
import services.vehicle.{VehicleAction, VehicleResponse, VehicleServiceMessage}

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

class VehicleSpawnControl2Test extends ActorTest {
  "VehicleSpawnControl" should {
    "complete a vehicle order" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe = new TestProbe(system, "zone-events")

      zone.VehicleEvents = probe.ref //zone events
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle) //order

      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.LoadVehicle])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      vehicle.Seats(0).Occupant = player
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.PlayerSeatedInVehicle])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ServerVehicleOverrideStart])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ServerVehicleOverrideEnd])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ResetSpawnPad])
    }
  }
}

class VehicleSpawnControl3Test extends ActorTest {
  "VehicleSpawnControl" should {
    "block the second vehicle order until the first is completed" in {
      val (vehicle, player, pad, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      //we can recycle the vehicle and the player for each order
      val probe = new TestProbe(system, "zone-events")

      zone.VehicleEvents = probe.ref //zone events
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle) //first order
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle) //second order (vehicle shared)

      assert(probe.receiveOne(1 seconds) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Queue, _) => true
        case _ => false
      })
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.LoadVehicle])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      vehicle.Seats(0).Occupant = player
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.PlayerSeatedInVehicle])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ServerVehicleOverrideStart])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ServerVehicleOverrideEnd])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ResetSpawnPad])
      assert(probe.receiveOne(20 seconds) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Blocked, _) => true
        case _ => false
      })
      assert(probe.receiveOne(20 seconds) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Blocked, _) => true
        case _ => false
      })

      //if we move the vehicle away from the pad, we should receive a second ConcealPlayer message
      //that means that the first order has cleared and the spawn pad is now working on the second order successfully
      player.VehicleSeated = None //since shared between orders, as necessary
      vehicle.Seats(0).Occupant = None
      vehicle.Position = Vector3(12,0,0)
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

      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.DisposeVehicle])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.RevealPlayer])
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

      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.LoadVehicle])
      vehicle.Health = 0 //problem
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.RevealPlayer])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ResetSpawnPad])
      assert(probe.receiveOne(20 seconds) match {
        case VehicleServiceMessage(_, VehicleAction.LoadVehicle(_,_,_,_,_)) => true
        case _ => false
      })
      assert(probe.receiveOne(20 seconds) match {
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

      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.LoadVehicle])
      player.Die
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.RevealPlayer])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ResetSpawnPad])
      assert(probe.receiveOne(20 seconds) match {
        case VehicleServiceMessage(_, VehicleAction.LoadVehicle(_,_,_,_,_)) => true
        case _ => false
      })
      assert(probe.receiveOne(20 seconds) match {
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

      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.LoadVehicle])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.RevealPlayer])
      probe.expectMsgClass(20 seconds, classOf[VehicleSpawnPad.ResetSpawnPad])
      assert(probe.receiveOne(20 seconds) match {
        case VehicleServiceMessage(_, VehicleAction.LoadVehicle(_,_,_,_,_)) => true
        case _ => false
      })
      assert(probe.receiveOne(20 seconds) match {
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
    val zone = new Zone("test-zone", map, 0)  {
      override def SetupNumberPools() = {
        val guid : NumberPoolHub = new NumberPoolHub(LimitedNumberSource(5))
        guid.AddPool("test-pool", (0 to 2).toList)
        //do not do this under normal conditions
        guid.register(vehicle, "test-pool")
        guid.register(weapon, "test-pool")
        guid.register(weapon.AmmoSlot.Box, "test-pool")
        GUID(guid)
      }
    }
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

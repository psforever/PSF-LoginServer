// Copyright (c) 2017 PSForever
package actor.objects

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import actor.base.ActorTest
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.serverobject.structures.StructureType
import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.zones.Zone
import net.psforever.types.{PlanetSideGUID, _}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import akka.actor.typed.scaladsl.adapter._
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.serverobject.terminals.Terminal

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
      val (vehicle, player, pad, terminal, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe                        = new TestProbe(system, "zone-events")

      zone.VehicleEvents = probe.ref                                      //zone events
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle, terminal) //order

      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(1 minute, classOf[VehicleServiceMessage])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      vehicle.Seats(0).mount(player)
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.PlayerSeatedInVehicle])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ServerVehicleOverrideStart])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ServerVehicleOverrideEnd])
      //if we move the vehicle away from the pad, we should receive a ResetSpawnPad message
      //that means that the first order has cleared and the spawn pad is now waiting for additional orders
      vehicle.Position = Vector3(12, 0, 0)
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ResetSpawnPad])
    }
  }
}

class VehicleSpawnControl3Test extends ActorTest {
  "VehicleSpawnControl" should {
    "block the second vehicle order until the first is completed" in {
      val (vehicle, player, pad, terminal, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      //we can recycle the vehicle and the player for each order
      val probe   = new TestProbe(system, "zone-events")
      val player2 = Player(Avatar(0, "test2", player.Faction, CharacterSex.Male, 0, CharacterVoice.Mute))
      player2.GUID = PlanetSideGUID(11)
      player2.Continent = zone.id
      player2.Spawn()

      zone.VehicleEvents = probe.ref                                       //zone events
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle, terminal)  //first order
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player2, vehicle, terminal) //second order (vehicle shared)

      assert(probe.receiveOne(1 seconds) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Queue, _) => true
        case _                                                                       => false
      })
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(1 minute, classOf[VehicleServiceMessage])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      vehicle.Seats(0).mount(player)
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.PlayerSeatedInVehicle])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ServerVehicleOverrideStart])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ServerVehicleOverrideEnd])
      assert(probe.receiveOne(1 minute) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Blocked, _) => true
        case _                                                                         => false
      })
      assert(probe.receiveOne(1 minute) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Blocked, _) => true
        case _                                                                         => false
      })

      //if we move the vehicle away from the pad, we should receive a second ConcealPlayer message
      //that means that the first order has cleared and the spawn pad is now working on the second order successfully
      player.VehicleSeated = None //since shared between orders, as necessary
      vehicle.Seats(0).unmount(player)
      vehicle.Position = Vector3(12, 0, 0)
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ResetSpawnPad])
      probe.expectMsgClass(3 seconds, classOf[VehicleSpawnPad.ConcealPlayer])
    }
  }
}

class VehicleSpawnControl4Test extends ActorTest {
  "VehicleSpawnControl" should {
    "clean up the vehicle if the driver-to-be is on the wrong continent" in {
      val (vehicle, player, pad, terminal, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe                        = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe.ref
      player.Continent = "problem" //problem

      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle, terminal) //order

      val msg = probe.receiveOne(1 minute)
//      assert(
//        msg match {
//          case VehicleServiceMessage.Decon(RemoverActor.AddTask(v, z, _)) => (v == vehicle) && (z == zone)
//          case _                                                          => false
//        }
//      )
      probe.expectNoMessage(5 seconds)
    }
  }
}

class VehicleSpawnControl5Test extends ActorTest() {
  "VehicleSpawnControl" should {
    "abandon a destroyed vehicle on the spawn pad (blocking)" in {
      val (vehicle, player, pad, terminal, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)

      val probe = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe.ref
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle, terminal) //order

      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(1 minute, classOf[VehicleServiceMessage])
      vehicle.Health = 0 //problem
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.RevealPlayer])
      assert(probe.receiveOne(1 minute) match {
        case VehicleServiceMessage(_, VehicleAction.LoadVehicle(_, _, _, _, _)) => true
        case _                                                                  => false
      })
      assert(probe.receiveOne(1 minute) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Blocked, _) => true
        case _                                                                         => false
      })
    }
  }
}

class VehicleSpawnControl6Test extends ActorTest() {
  "VehicleSpawnControl" should {
    "abandon a vehicle on the spawn pad if driver is unfit to drive (blocking)" in {
      val (vehicle, player, pad, terminal, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)

      val probe = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe.ref
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle, terminal) //order

      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(1 minute, classOf[VehicleServiceMessage])
      player.Die
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.RevealPlayer])
      assert(probe.receiveOne(1 minute) match {
        case VehicleServiceMessage(_, VehicleAction.LoadVehicle(_, _, _, _, _)) => true
        case _                                                                  => false
      })
      assert(probe.receiveOne(1 minute) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Blocked, _) => true
        case _                                                                         => false
      })
    }
  }
}

class VehicleSpawnControl7Test extends ActorTest {
  "VehicleSpawnControl" should {
    "abandon a vehicle on the spawn pad if driver is unfit to drive (blocking)" in {
      val (vehicle, player, pad, terminal, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe                        = new TestProbe(system, "zone-events")
      player.ExoSuit = ExoSuitType.MAX

      zone.VehicleEvents = probe.ref                                      //zone events
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle, terminal) //order

      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.ConcealPlayer])
      probe.expectMsgClass(1 minute, classOf[VehicleServiceMessage])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.AttachToRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.DetachFromRails])
      probe.expectMsgClass(1 minute, classOf[VehicleSpawnPad.RevealPlayer])
      assert(probe.receiveOne(1 minute) match {
        case VehicleServiceMessage(_, VehicleAction.LoadVehicle(_, _, _, _, _)) => true
        case _                                                                  => false
      })
      assert(probe.receiveOne(1 minute) match {
        case VehicleSpawnPad.PeriodicReminder(_, VehicleSpawnPad.Reminders.Blocked, _) => true
        case _                                                                         => false
      })
    }
  }
}

object VehicleSpawnPadControlTest {
  import net.psforever.objects.zones.ZoneMap
  private val map = new ZoneMap("test-map")

  def SetUpAgents(
      faction: PlanetSideEmpire.Value
  )(implicit system: ActorSystem): (Vehicle, Player, VehicleSpawnPad, Terminal, Zone) = {
    import net.psforever.objects.guid.NumberPoolHub
    import net.psforever.objects.guid.source.MaxNumberSource
    import net.psforever.objects.serverobject.structures.Building
    import net.psforever.objects.vehicles.control.VehicleControl
    import net.psforever.objects.Tool
    import net.psforever.types.CharacterSex

    val terminal            = Terminal(GlobalDefinitions.vehicle_terminal_combined)
    val vehicle             = Vehicle(GlobalDefinitions.two_man_assault_buggy)
    val weapon              = vehicle.WeaponControlledFromSeat(1).get.asInstanceOf[Tool]
    val guid: NumberPoolHub = new NumberPoolHub(MaxNumberSource(5))
    guid.AddPool("test-pool", (0 to 5).toList)
    guid.register(vehicle, "test-pool")
    guid.register(weapon, "test-pool")
    guid.register(weapon.AmmoSlot.Box, "test-pool")
    val zone = new Zone("test-zone", map, 0) {
      override def SetupNumberPools(): Unit = {}
    }
    zone.GUID(guid)
    zone.actor = system.spawn(ZoneActor(zone), s"test-zone-${System.nanoTime()}")

    // Hack: Wait for the Zone to finish booting, otherwise later tests will fail randomly due to race conditions
    // with actor probe setting
    // TODO(chord): Remove when Zone supports notification of booting being complete
    Thread.sleep(5000)

    vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), s"vehicle-control-${System.nanoTime()}")

    val pad = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
    pad.Actor = system.actorOf(Props(classOf[VehicleSpawnControl], pad), s"test-pad-${System.nanoTime()}")
    pad.Owner =
      new Building("Building", building_guid = 0, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
    pad.Owner.Faction = faction
    pad.Zone = zone
    guid.register(pad, "test-pool")
    val player = Player(Avatar(0, "test", faction, CharacterSex.Male, 0, CharacterVoice.Mute))
    guid.register(player, "test-pool")
    player.Zone = zone
    player.Spawn()
    //note: pad and vehicle are both at Vector3(1,0,0) so they count as blocking
    pad.Position = Vector3(1, 0, 0)
    vehicle.Position = Vector3(1, 0, 0)
    (vehicle, player, pad, terminal, zone)
  }
}

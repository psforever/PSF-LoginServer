// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorRef, Props}
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.vehicles.VehicleControl
import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterGender, PlanetSideEmpire, Vector3}
import org.specs2.mutable.Specification

import scala.concurrent.duration.Duration

class VehicleSpawnPadTest extends Specification {
  "VehicleSpawnPadDefinition" should {
    "define" in {
      GlobalDefinitions.spawn_pad.ObjectId mustEqual 800
    }
  }

  "VehicleSpawnPad" should {
    "construct" in {
      val obj = VehicleSpawnPad(GlobalDefinitions.spawn_pad)
      obj.Actor mustEqual ActorRef.noSender
      obj.Definition mustEqual GlobalDefinitions.spawn_pad
    }
  }
}

class VehicleSpawnControl1Test extends ActorTest() {
  "VehicleSpawnControl" should {
    "construct" in {
      val obj = VehicleSpawnPad(GlobalDefinitions.spawn_pad)
      obj.Actor = system.actorOf(Props(classOf[VehicleSpawnControl], obj), "door")
      assert(obj.Actor != ActorRef.noSender)
    }
  }
}

class VehicleSpawnControl2Test extends ActorTest() {
  "VehicleSpawnControl" should {
    "spawn a vehicle" in {
      val obj = VehicleSpawnPad(GlobalDefinitions.spawn_pad)
      obj.Actor = system.actorOf(Props(classOf[VehicleSpawnControl], obj), "door")
      val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      player.Spawn
      val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      vehicle.GUID = PlanetSideGUID(1)
      vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle")

      obj.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle)
      val reply = receiveOne(Duration.create(10000, "ms"))
      assert(reply == VehicleSpawnPad.ConcealPlayer) //explicit: isInstanceOf does not work

      val reply2 = receiveOne(Duration.create(10000, "ms"))
      assert(reply2.isInstanceOf[VehicleSpawnPad.LoadVehicle])
      assert(reply2.asInstanceOf[VehicleSpawnPad.LoadVehicle].vehicle == vehicle)
      assert(reply2.asInstanceOf[VehicleSpawnPad.LoadVehicle].pad == obj)

      player.VehicleOwned = vehicle
      val reply3 = receiveOne(Duration.create(10000, "ms"))
      assert(reply3.isInstanceOf[VehicleSpawnPad.PlayerSeatedInVehicle])
      assert(reply3.asInstanceOf[VehicleSpawnPad.PlayerSeatedInVehicle].vehicle == vehicle)

      val reply4 = receiveOne(Duration.create(10000, "ms"))
      assert(reply4.isInstanceOf[VehicleSpawnPad.SpawnPadBlockedWarning])
      assert(reply4.asInstanceOf[VehicleSpawnPad.SpawnPadBlockedWarning].vehicle == vehicle)
      assert(reply4.asInstanceOf[VehicleSpawnPad.SpawnPadBlockedWarning].warning_count > 0)

      vehicle.Position = Vector3(11f, 0f, 0f) //greater than 10m
      val reply5 = receiveOne(Duration.create(10000, "ms"))
      assert(reply5.isInstanceOf[VehicleSpawnPad.SpawnPadUnblocked])
      assert(reply5.asInstanceOf[VehicleSpawnPad.SpawnPadUnblocked].vehicle_guid == vehicle.GUID)
    }
  }
}

class VehicleSpawnControl3Test extends ActorTest() {
  "VehicleSpawnControl" should {
    "not spawn a vehicle if player is dead" in {
      val obj = VehicleSpawnPad(GlobalDefinitions.spawn_pad)
      obj.Actor = system.actorOf(Props(classOf[VehicleSpawnControl], obj), "door")
      val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      vehicle.GUID = PlanetSideGUID(1)
      vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle")

      obj.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle)
      val reply = receiveOne(Duration.create(5000, "ms"))
      assert(reply == null)
    }
  }
}

class VehicleSpawnControl4Test extends ActorTest() {
  "VehicleSpawnControl" should {
    "not spawn a vehicle if vehicle Actor is missing" in {
      val obj = VehicleSpawnPad(GlobalDefinitions.spawn_pad)
      obj.Actor = system.actorOf(Props(classOf[VehicleSpawnControl], obj), "door")
      val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      player.Spawn
      val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      vehicle.GUID = PlanetSideGUID(1)

      obj.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle)
      val reply = receiveOne(Duration.create(5000, "ms"))
      assert(reply == null)
    }
  }
}

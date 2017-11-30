// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.{ActorRef, Props}
import net.psforever.objects.definition.SeatDefinition
import net.psforever.objects.mount.Mountable
import net.psforever.objects.serverobject.implantmech.{ImplantTerminalMech, ImplantTerminalMechControl}
import net.psforever.objects.vehicles.Seat
import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.types.{CharacterGender, PlanetSideEmpire}
import objects.ActorTest
import org.specs2.mutable.Specification

import scala.concurrent.duration.Duration

class ImplantTerminalMechTest extends Specification {
  "Implant_Terminal_Mech" should {
    "define" in {
      val implant_terminal_mech = GlobalDefinitions.implant_terminal_mech
      implant_terminal_mech.ObjectId mustEqual 410
      implant_terminal_mech.MountPoints mustEqual Map(1 -> 0)
      implant_terminal_mech.Seats.keySet mustEqual Set(0)
      implant_terminal_mech.Seats(0).isInstanceOf[SeatDefinition] mustEqual true
      implant_terminal_mech.Seats(0).ArmorRestriction mustEqual net.psforever.objects.vehicles.SeatArmorRestriction.NoMax
      implant_terminal_mech.Seats(0).Bailable mustEqual false
      implant_terminal_mech.Seats(0).ControlledWeapon mustEqual None
    }
  }

  "VehicleSpawnPad" should {
    "construct" in {
      val obj = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.Actor mustEqual ActorRef.noSender
      obj.Definition mustEqual GlobalDefinitions.implant_terminal_mech
      obj.Seats.keySet mustEqual Set(0)
      obj.Seats(0).isInstanceOf[Seat] mustEqual true
    }

    "get seat from mount points" in {
      val obj = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.GetSeatFromMountPoint(0) mustEqual None
      obj.GetSeatFromMountPoint(1) mustEqual Some(0)
      obj.GetSeatFromMountPoint(2) mustEqual None
    }

    "get passenger in a seat" in {
      val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val obj = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.PassengerInSeat(player) mustEqual None
      obj.Seats(0).Occupant = player
      obj.PassengerInSeat(player) mustEqual Some(0)
      obj.Seats(0).Occupant = None
      obj.PassengerInSeat(player) mustEqual None
    }
  }
}

class ImplantTerminalMechControl1Test extends ActorTest() {
  "ImplantTerminalMechControl" should {
    "construct" in {
      val obj = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.Actor = system.actorOf(Props(classOf[ImplantTerminalMechControl], obj), "mech")
      assert(obj.Actor != ActorRef.noSender)
    }
  }
}

class ImplantTerminalMechControl2Test extends ActorTest() {
  "ImplantTerminalMechControl" should {
    "let a player mount" in {
      val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val obj = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.Actor = system.actorOf(Props(classOf[ImplantTerminalMechControl], obj), "mech")
      val msg = Mountable.TryMount(player, 0)

      obj.Actor ! msg
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Mountable.MountMessages])
      val reply2 = reply.asInstanceOf[Mountable.MountMessages]
      assert(reply2.player == player)
      assert(reply2.response.isInstanceOf[Mountable.CanMount])
      val reply3 = reply2.response.asInstanceOf[Mountable.CanMount]
      assert(reply3.obj == obj)
      assert(reply3.seat_num == 0)
    }
  }
}

class ImplantTerminalMechControl3Test extends ActorTest() {
  "ImplantTerminalMechControl" should {
    "block a player from mounting" in {
      val player1 = Player("test1", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val player2 = Player("test2", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val obj = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.Actor = system.actorOf(Props(classOf[ImplantTerminalMechControl], obj), "mech")
      obj.Actor ! Mountable.TryMount(player1, 0)
      receiveOne(Duration.create(100, "ms")) //consume reply

      obj.Actor ! Mountable.TryMount(player2, 0)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Mountable.MountMessages])
      val reply2 = reply.asInstanceOf[Mountable.MountMessages]
      assert(reply2.player == player2)
      assert(reply2.response.isInstanceOf[Mountable.CanNotMount])
      val reply3 = reply2.response.asInstanceOf[Mountable.CanNotMount]
      assert(reply3.obj == obj)
      assert(reply3.seat_num == 0)
    }
  }
}

// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.{ActorSystem, Props}
import base.ActorTest
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.{Default, GlobalDefinitions, Player}
import net.psforever.objects.definition.SeatDefinition
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.implantmech.{ImplantTerminalMech, ImplantTerminalMechControl}
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.vehicles.Seat
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire, Vector3}
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
      implant_terminal_mech
        .Seats(0)
        .ArmorRestriction mustEqual net.psforever.objects.vehicles.SeatArmorRestriction.NoMax
      implant_terminal_mech.Seats(0).Bailable mustEqual false
      implant_terminal_mech.Seats(0).ControlledWeapon.isEmpty mustEqual true
    }
  }

  "Implant_Terminal_Mech" should {
    "construct" in {
      val obj = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.Actor mustEqual Default.Actor
      obj.Definition mustEqual GlobalDefinitions.implant_terminal_mech
      obj.Seats.keySet mustEqual Set(0)
      obj.Seats(0).isInstanceOf[Seat] mustEqual true
    }

    "get seat from mount points" in {
      val obj = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.GetSeatFromMountPoint(0).isEmpty mustEqual true
      obj.GetSeatFromMountPoint(1).contains(0) mustEqual true
      obj.GetSeatFromMountPoint(2).isEmpty mustEqual true
    }

    "get passenger in a seat" in {
      val player = Player(Avatar(0, "test", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      val obj    = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.PassengerInSeat(player).isEmpty mustEqual true
      obj.Seats(0).Occupant = player
      obj.PassengerInSeat(player).contains(0) mustEqual true
      obj.Seats(0).Occupant = None
      obj.PassengerInSeat(player).isEmpty mustEqual true
    }
  }
}

class ImplantTerminalMechControl1Test extends ActorTest {
  "ImplantTerminalMechControl" should {
    "construct" in {
      val obj = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.Actor = system.actorOf(Props(classOf[ImplantTerminalMechControl], obj), "mech")
      assert(obj.Actor != Default.Actor)
    }
  }
}

class ImplantTerminalMechControl2Test extends ActorTest {
  "ImplantTerminalMechControl" should {
    "let a player mount" in {
      val (player, mech) = ImplantTerminalMechTest.SetUpAgents(PlanetSideEmpire.TR)
      val msg            = Mountable.TryMount(player, 0)

      mech.Actor ! msg
      val reply = receiveOne(Duration.create(200, "ms"))
      assert(reply.isInstanceOf[Mountable.MountMessages])
      val reply2 = reply.asInstanceOf[Mountable.MountMessages]
      assert(reply2.player == player)
      assert(reply2.response.isInstanceOf[Mountable.CanMount])
      val reply3 = reply2.response.asInstanceOf[Mountable.CanMount]
      assert(reply3.obj == mech)
      assert(reply3.seat_num == 0)
    }
  }
}

class ImplantTerminalMechControl3Test extends ActorTest {
  import net.psforever.types.CharacterGender
  "ImplantTerminalMechControl" should {
    "block a player from mounting" in {
      val (player1, mech) = ImplantTerminalMechTest.SetUpAgents(PlanetSideEmpire.TR)
      val player2         = Player(Avatar(1, "test2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))

      mech.Actor ! Mountable.TryMount(player1, 0)
      receiveOne(Duration.create(100, "ms")) //consume reply

      mech.Actor ! Mountable.TryMount(player2, 0)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Mountable.MountMessages])
      val reply2 = reply.asInstanceOf[Mountable.MountMessages]
      assert(reply2.player == player2)
      assert(reply2.response.isInstanceOf[Mountable.CanNotMount])
      val reply3 = reply2.response.asInstanceOf[Mountable.CanNotMount]
      assert(reply3.obj == mech)
      assert(reply3.seat_num == 0)
    }
  }
}

class ImplantTerminalMechControl4Test extends ActorTest {
  "ImplantTerminalMechControl" should {
    "dismount player after mounting" in {
      val (player, mech) = ImplantTerminalMechTest.SetUpAgents(PlanetSideEmpire.TR)
      mech.Actor ! Mountable.TryMount(player, 0)
      receiveOne(Duration.create(200, "ms")) //consume reply
      assert(mech.Seat(0).get.isOccupied)

      mech.Actor ! Mountable.TryDismount(player, 0)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Mountable.MountMessages])
      val reply2 = reply.asInstanceOf[Mountable.MountMessages]
      assert(reply2.player == player)
      assert(reply2.response.isInstanceOf[Mountable.CanDismount])
      val reply3 = reply2.response.asInstanceOf[Mountable.CanDismount]
      assert(reply3.obj == mech)
      assert(reply3.seat_num == 0)
      assert(!mech.Seat(0).get.isOccupied)
    }
  }
}

class ImplantTerminalMechControl5Test extends ActorTest {
  "ImplantTerminalMechControl" should {
    "block a player from dismounting" in {
      val (player, mech) = ImplantTerminalMechTest.SetUpAgents(PlanetSideEmpire.TR)
      mech.Actor ! Mountable.TryMount(player, 0)
      receiveOne(Duration.create(100, "ms")) //consume reply
      assert(mech.Seat(0).get.isOccupied)

      mech.Velocity = Vector3(1, 0, 0) //makes no sense, but it works as the "seat" is not bailable
      mech.Actor ! Mountable.TryDismount(player, 0)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Mountable.MountMessages])
      val reply2 = reply.asInstanceOf[Mountable.MountMessages]
      assert(reply2.player == player)
      assert(reply2.response.isInstanceOf[Mountable.CanNotDismount])
      val reply3 = reply2.response.asInstanceOf[Mountable.CanNotDismount]
      assert(reply3.obj == mech)
      assert(reply3.seat_num == 0)
      assert(mech.Seat(0).get.isOccupied)
    }
  }
}

object ImplantTerminalMechTest {
  def SetUpAgents(faction: PlanetSideEmpire.Value)(implicit system: ActorSystem): (Player, ImplantTerminalMech) = {
    val guid = new NumberPoolHub(new MaxNumberSource(10))
    val map  = new ZoneMap("test")
    val zone = new Zone("test", map, 0) {
      override def SetupNumberPools() = {}
      GUID(guid)
    }
    val building = new Building(
      "Building",
      building_guid = 0,
      map_id = 0,
      zone,
      StructureType.Building,
      GlobalDefinitions.building
    ) //guid=3
    building.Faction = faction

    val interface = Terminal(GlobalDefinitions.implant_terminal_interface) //guid=2
    interface.Owner = building
    val terminal = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech) //guid=1
    terminal.Owner = building

    guid.register(terminal, 1)
    guid.register(interface, 2)
    guid.register(building, 3)
    map.linkTerminalToInterface(1, 2)
    terminal.Actor = system.actorOf(Props(classOf[ImplantTerminalMechControl], terminal), "terminal-control")

    (Player(Avatar(0, "test", faction, CharacterGender.Male, 0, CharacterVoice.Mute)), terminal)
  }
}

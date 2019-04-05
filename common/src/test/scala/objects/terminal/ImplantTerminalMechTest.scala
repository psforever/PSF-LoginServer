// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.{ActorRef, ActorSystem, Props}
import base.ActorTest
import net.psforever.objects.definition.SeatDefinition
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.implantmech.{ImplantTerminalMech, ImplantTerminalMechControl}
import net.psforever.objects.serverobject.structures.StructureType
import net.psforever.objects.vehicles.Seat
import net.psforever.objects.{Avatar, GlobalDefinitions, Player}
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
      implant_terminal_mech.Seats(0).ArmorRestriction mustEqual net.psforever.objects.vehicles.SeatArmorRestriction.NoMax
      implant_terminal_mech.Seats(0).Bailable mustEqual false
      implant_terminal_mech.Seats(0).ControlledWeapon mustEqual None
    }
  }

  "Implant_Terminal_Mech" should {
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
      val player = Player(Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      val obj = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.PassengerInSeat(player) mustEqual None
      obj.Seats(0).Occupant = player
      obj.PassengerInSeat(player) mustEqual Some(0)
      obj.Seats(0).Occupant = None
      obj.PassengerInSeat(player) mustEqual None
    }
  }
}

class ImplantTerminalMechControl1Test extends ActorTest {
  "ImplantTerminalMechControl" should {
    "construct" in {
      val obj = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.Actor = system.actorOf(Props(classOf[ImplantTerminalMechControl], obj), "mech")
      assert(obj.Actor != ActorRef.noSender)
    }
  }
}

class ImplantTerminalMechControl2Test extends ActorTest {
  "ImplantTerminalMechControl" should {
    "let a player mount" in {
      val (player, mech) = ImplantTerminalMechTest.SetUpAgents(PlanetSideEmpire.TR)
      val msg = Mountable.TryMount(player, 0)

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
      val player2 = Player(Avatar("test2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))

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
      receiveOne(Duration.create(100, "ms")) //consume reply
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

      mech.Velocity = Vector3(1,0,0) //makes no sense, but it works as the "seat" is not bailable
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
  def SetUpAgents(faction : PlanetSideEmpire.Value)(implicit system : ActorSystem) : (Player, ImplantTerminalMech) = {
    import net.psforever.objects.serverobject.structures.Building
    import net.psforever.objects.zones.Zone
    import net.psforever.packet.game.PlanetSideGUID

    val terminal = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
    terminal.Actor = system.actorOf(Props(classOf[ImplantTerminalMechControl], terminal), "mech")
    terminal.Owner = new Building(building_guid = 0, map_id = 0, Zone.Nowhere, StructureType.Building, GlobalDefinitions.building)
    terminal.Owner.Faction = faction
    terminal.GUID = PlanetSideGUID(1)
    (Player(Avatar("test", faction, CharacterGender.Male, 0, CharacterVoice.Mute)), terminal)
  }
}

// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.{Default, GlobalDefinitions, Player}
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.terminals.implant.{ImplantTerminalMech, ImplantTerminalMechControl}
import net.psforever.objects.serverobject.mount.{MountInfo, Mountable, Seat, SeatDefinition}
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.types.{CharacterSex, CharacterVoice, PlanetSideEmpire, Vector3}
import org.specs2.mutable.Specification

import scala.concurrent.duration.Duration

class ImplantTerminalMechTest extends Specification {
  "Implant_Terminal_Mech" should {
    "define" in {
      val implant_terminal_mech = GlobalDefinitions.implant_terminal_mech
      implant_terminal_mech.ObjectId mustEqual 410
      implant_terminal_mech.MountPoints.get(1).contains(MountInfo(0, Vector3.Zero)) mustEqual true
      implant_terminal_mech.Seats.keySet mustEqual Set(0)
      implant_terminal_mech.Seats(0).isInstanceOf[SeatDefinition] mustEqual true
      implant_terminal_mech.Seats(0).bailable mustEqual false
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

    "get mount from mount points" in {
      val obj = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.GetSeatFromMountPoint(0).isEmpty mustEqual true
      obj.GetSeatFromMountPoint(1).contains(0) mustEqual true
      obj.GetSeatFromMountPoint(2).isEmpty mustEqual true
    }

    "get passenger in a mount" in {
      val player = Player(Avatar(0, "test", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      val obj    = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech)
      obj.PassengerInSeat(player).isEmpty mustEqual true
      obj.Seats(0).mount(player)
      obj.PassengerInSeat(player).contains(0) mustEqual true
      obj.Seats(0).unmount(player)
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
      val msg            = Mountable.TryMount(player, 1)

      mech.Actor ! msg
      val reply = receiveOne(Duration.create(200, "ms"))
      assert(reply.isInstanceOf[Mountable.MountMessages])
      val reply2 = reply.asInstanceOf[Mountable.MountMessages]
      assert(reply2.player == player)
      assert(reply2.response.isInstanceOf[Mountable.CanMount])
      val reply3 = reply2.response.asInstanceOf[Mountable.CanMount]
      assert(reply3.obj == mech)
      assert(reply3.seat_number == 0)
    }
  }
}

class ImplantTerminalMechControl3Test extends ActorTest {
  import net.psforever.types.CharacterSex
  "ImplantTerminalMechControl" should {
    "block a player from mounting" in {
      val (player1, mech) = ImplantTerminalMechTest.SetUpAgents(PlanetSideEmpire.TR)
      val player2         = Player(Avatar(1, "test2", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))

      mech.Actor ! Mountable.TryMount(player1, 1)
      receiveOne(Duration.create(100, "ms")) //consume reply

      mech.Actor ! Mountable.TryMount(player2, 1)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Mountable.MountMessages])
      val reply2 = reply.asInstanceOf[Mountable.MountMessages]
      assert(reply2.player == player2)
      assert(reply2.response.isInstanceOf[Mountable.CanNotMount])
      val reply3 = reply2.response.asInstanceOf[Mountable.CanNotMount]
      assert(reply3.obj == mech)
      assert(reply3.mount_point == 1)
    }
  }
}

class ImplantTerminalMechControl4Test extends ActorTest {
  "ImplantTerminalMechControl" should {
    "dismount player after mounting" in {
      val (player, mech) = ImplantTerminalMechTest.SetUpAgents(PlanetSideEmpire.TR)
      mech.Actor ! Mountable.TryMount(player, 1)
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
      mech.Actor ! Mountable.TryMount(player, 1)
      receiveOne(Duration.create(100, "ms")) //consume reply
      assert(mech.Seat(0).get.isOccupied)

      mech.Velocity = Vector3(1, 0, 0) //makes no sense, but it works as the "mount" is not bailable
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
    import akka.actor.typed.scaladsl.adapter._

    val guid = new NumberPoolHub(new MaxNumberSource(10))
    val terminal = ImplantTerminalMech(GlobalDefinitions.implant_terminal_mech) //guid=1
    val interface = Terminal(GlobalDefinitions.implant_terminal_interface) //guid=2
    val building = new Building(
      "Building",
      building_guid = 0,
      map_id = 0,
      Zone.Nowhere,
      StructureType.Building,
      GlobalDefinitions.building
    ) //guid=3
    val zone = new Zone(
      "test",
      new ZoneMap("test") {
      },
      0) {
      override def SetupNumberPools() = {}
      GUID(guid)
      this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
    }
    //zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
    zone.map.linkTerminalToInterface(1, 2)
    building.Zone = zone
    building.Faction = faction
    interface.Zone = zone
    building.Amenities = interface
    terminal.Zone = zone
    building.Amenities = terminal

    guid.register(terminal, 1)
    guid.register(interface, 2)
    guid.register(building, 3)
    terminal.Actor = system.actorOf(Props(classOf[ImplantTerminalMechControl], terminal), "terminal-control")

    (Player(Avatar(0, "test", faction, CharacterSex.Male, 0, CharacterVoice.Mute)), terminal)
  }
}

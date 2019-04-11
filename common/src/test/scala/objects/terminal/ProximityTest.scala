// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.Props
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.guid.TaskResolver
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.serverobject.terminals.{ProximityTerminal, ProximityTerminalControl, ProximityUnit, Terminal}
import net.psforever.objects.zones.{Zone, ZoneActor, ZoneMap}
import net.psforever.objects.{Avatar, GlobalDefinitions, Player}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire}
import org.specs2.mutable.Specification
import services.{Service, ServiceManager}
import services.local.{LocalResponse, LocalService, LocalServiceResponse}

import scala.concurrent.duration._

class ProximityTest extends Specification {
  "ProximityUnit" should {
    "construct (with a Terminal object)" in {
      val obj = new ProximityTest.SampleTerminal()
      obj.NumberUsers mustEqual 0
    }

    "keep track of users (add)" in {
      val avatar1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
      avatar1.Spawn
      avatar1.Health = 50
      val avatar2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
      avatar2.Spawn
      avatar2.Health = 50

      val obj = new ProximityTerminal(GlobalDefinitions.medical_terminal)
      obj.NumberUsers mustEqual 0
      obj.AddUser(avatar1) mustEqual true
      obj.NumberUsers mustEqual 1
      obj.AddUser(avatar2) mustEqual true
      obj.NumberUsers mustEqual 2
    }

    "keep track of users (remove)" in {
      val avatar1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
      avatar1.Spawn
      avatar1.Health = 50
      val avatar2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
      avatar2.Spawn
      avatar2.Health = 50

      val obj = new ProximityTerminal(GlobalDefinitions.medical_terminal)
      obj.NumberUsers mustEqual 0
      obj.AddUser(avatar1) mustEqual true
      obj.NumberUsers mustEqual 1
      obj.AddUser(avatar2) mustEqual true
      obj.NumberUsers mustEqual 2

      obj.RemoveUser(avatar1) mustEqual true
      obj.NumberUsers mustEqual 1
      obj.RemoveUser(avatar2) mustEqual true
      obj.NumberUsers mustEqual 0
    }

    "can not add a user twice" in {
      val avatar = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
      avatar.Spawn
      avatar.Health = 50

      val obj = new ProximityTerminal(GlobalDefinitions.medical_terminal)
      obj.AddUser(avatar) mustEqual true
      obj.NumberUsers mustEqual 1
      obj.AddUser(avatar)// mustEqual false
      obj.NumberUsers mustEqual 1
    }

    "can not remove a user that was not added" in {
      val avatar = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
      avatar.Spawn
      avatar.Health = 50

      val obj = new ProximityTest.SampleTerminal()
      obj.RemoveUser(avatar) mustEqual false
      obj.NumberUsers mustEqual 0
    }
  }

  "ProximityTerminal" should {
    "construct" in {
      ProximityTerminal(GlobalDefinitions.medical_terminal)
      ok
    }
  }
}

class ProximityTerminalControlStartTest extends ActorTest {
  "ProximityTerminalControl" should {
    //setup
    val probe = new TestProbe(system)
    val service = ServiceManager.boot(system)
    service ! ServiceManager.Register(Props(classOf[ProximityTest.ProbedLocalService], probe), "local")
    service ! ServiceManager.Register(Props[TaskResolver], "taskResolver")
    service ! ServiceManager.Register(Props[TaskResolver], "cluster")
    val terminal = new ProximityTerminal(GlobalDefinitions.medical_terminal)
    terminal.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], terminal), "test-prox")
    val zone : Zone = new Zone("test", new ZoneMap("test-map"), 0) {
      Actor = system.actorOf(Props(classOf[ZoneActor], this), "test-zone")
      override def SetupNumberPools() = {
        AddPool("dynamic", 1 to 10)
      }
    }
    new Building(building_guid = 0, map_id = 0, zone, StructureType.Facility, GlobalDefinitions.building) {
      Amenities = terminal
      Faction = PlanetSideEmpire.VS
    }
    val avatar = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
    avatar.Continent = "test"
    avatar.Spawn
    avatar.Health = 50

    avatar.GUID = PlanetSideGUID(1)
    terminal.GUID = PlanetSideGUID(2)
    terminal.Actor ! Service.Startup()
    expectNoMsg(500 milliseconds) //spacer

    "send out a start message" in {
      assert(terminal.NumberUsers == 0)
      assert(terminal.Owner.Continent.equals("test"))
      terminal.Actor ! CommonMessages.Use(avatar, Some(avatar))

      val msg = probe.receiveOne(500 milliseconds)
      assert(terminal.NumberUsers == 1)
      assert(msg.isInstanceOf[LocalServiceResponse])
      val resp = msg.asInstanceOf[LocalServiceResponse]
      assert(resp.replyMessage == LocalResponse.ProximityTerminalEffect(PlanetSideGUID(2), true))
    }
  }
}

class ProximityTerminalControlTwoUsersTest extends ActorTest {
  "ProximityTerminalControl" should {
    //setup
    val probe = new TestProbe(system)
    val service = ServiceManager.boot(system)
    service ! ServiceManager.Register(Props(classOf[ProximityTest.ProbedLocalService], probe), "local")
    service ! ServiceManager.Register(Props[TaskResolver], "taskResolver")
    service ! ServiceManager.Register(Props[TaskResolver], "cluster")
    val terminal = new ProximityTerminal(GlobalDefinitions.medical_terminal)
    terminal.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], terminal), "test-prox")
    val zone : Zone = new Zone("test", new ZoneMap("test-map"), 0) {
      Actor = system.actorOf(Props(classOf[ZoneActor], this), "test-zone")
      override def SetupNumberPools() = {
        AddPool("dynamic", 1 to 10)
      }
    }
    new Building(building_guid = 0, map_id = 0, zone, StructureType.Facility, GlobalDefinitions.building) {
      Amenities = terminal
      Faction = PlanetSideEmpire.VS
    }
    val avatar = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
    avatar.Continent = "test"
    avatar.Spawn
    avatar.Health = 50

    avatar.GUID = PlanetSideGUID(1)
    terminal.GUID = PlanetSideGUID(2)
    terminal.Actor ! Service.Startup()
    expectNoMsg(500 milliseconds) //spacer

    "will not send out a start message if not the first user" in {
      assert(terminal.NumberUsers == 0)
      assert(terminal.Owner.Continent.equals("test"))

      terminal.Actor ! CommonMessages.Use(avatar, Some(avatar))
      val msg = probe.receiveOne(500 milliseconds)
      assert(terminal.NumberUsers == 1)
      assert(msg.isInstanceOf[LocalServiceResponse])
      val resp = msg.asInstanceOf[LocalServiceResponse]
      assert(resp.replyMessage == LocalResponse.ProximityTerminalEffect(PlanetSideGUID(2), true))

      val avatar2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
      avatar2.Continent = "test"
      avatar2.Spawn
      avatar2.Health = 50
      terminal.Actor ! CommonMessages.Use(avatar2, Some(avatar2))
      probe.expectNoMsg(500 milliseconds)
      assert(terminal.NumberUsers == 2)
    }
  }
}

class ProximityTerminalControlStopTest extends ActorTest {
  "ProximityTerminalControl" should {
    //setup
    val probe = new TestProbe(system)
    val service = ServiceManager.boot(system)
    service ! ServiceManager.Register(Props(classOf[ProximityTest.ProbedLocalService], probe), "local")
    service ! ServiceManager.Register(Props[TaskResolver], "taskResolver")
    service ! ServiceManager.Register(Props[TaskResolver], "cluster")
    val terminal = new ProximityTerminal(GlobalDefinitions.medical_terminal)
    terminal.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], terminal), "test-prox")
    val zone : Zone = new Zone("test", new ZoneMap("test-map"), 0) {
      Actor = system.actorOf(Props(classOf[ZoneActor], this), "test-zone")
      override def SetupNumberPools() = {
        AddPool("dynamic", 1 to 10)
      }
    }
    new Building(building_guid = 0, map_id = 0, zone, StructureType.Facility, GlobalDefinitions.building) {
      Amenities = terminal
      Faction = PlanetSideEmpire.VS
    }
    val avatar = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
    avatar.Continent = "test"
    avatar.Spawn
    avatar.Health = 50

    avatar.GUID = PlanetSideGUID(1)
    terminal.GUID = PlanetSideGUID(2)
    terminal.Actor ! Service.Startup()
    expectNoMsg(500 milliseconds) //spacer

    "send out a stop message" in {
      assert(terminal.NumberUsers == 0)
      assert(terminal.Owner.Continent.equals("test"))

      terminal.Actor ! CommonMessages.Use(avatar, Some(avatar))
      val msg1 = probe.receiveOne(500 milliseconds)
      assert(terminal.NumberUsers == 1)
      assert(msg1.isInstanceOf[LocalServiceResponse])
      val resp1 = msg1.asInstanceOf[LocalServiceResponse]
      assert(resp1.replyMessage == LocalResponse.ProximityTerminalEffect(PlanetSideGUID(2), true))

      terminal.Actor ! CommonMessages.Unuse(avatar, Some(avatar))
      val msg2 = probe.receiveWhile(500 milliseconds) {
        case LocalServiceResponse(_, _, replyMessage) => replyMessage
      }
      assert(terminal.NumberUsers == 0)
      assert(msg2.last == LocalResponse.ProximityTerminalEffect(PlanetSideGUID(2), false))
    }
  }
}

class ProximityTerminalControlNotStopTest extends ActorTest {
  "ProximityTerminalControl" should {
    //setup
    val probe = new TestProbe(system)
    val service = ServiceManager.boot(system)
    service ! ServiceManager.Register(Props(classOf[ProximityTest.ProbedLocalService], probe), "local")
    service ! ServiceManager.Register(Props[TaskResolver], "taskResolver")
    service ! ServiceManager.Register(Props[TaskResolver], "cluster")
    val terminal = new ProximityTerminal(GlobalDefinitions.medical_terminal)
    terminal.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], terminal), "test-prox")
    val zone : Zone = new Zone("test", new ZoneMap("test-map"), 0) {
      Actor = system.actorOf(Props(classOf[ZoneActor], this), "test-zone")
      override def SetupNumberPools() = {
        AddPool("dynamic", 1 to 10)
      }
    }
    new Building(building_guid = 0, map_id = 0, zone, StructureType.Facility, GlobalDefinitions.building) {
      Amenities = terminal
      Faction = PlanetSideEmpire.VS
    }
    val avatar = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
    avatar.Continent = "test"
    avatar.Spawn
    avatar.Health = 50

    avatar.GUID = PlanetSideGUID(1)
    terminal.GUID = PlanetSideGUID(2)
    terminal.Actor ! Service.Startup()
    expectNoMsg(500 milliseconds) //spacer

    "will not send out one stop message until last user" in {
      assert(terminal.NumberUsers == 0)
      assert(terminal.Owner.Continent.equals("test"))

      terminal.Actor ! CommonMessages.Use(avatar, Some(avatar))
      val msg = probe.receiveOne(500 milliseconds)
      assert(terminal.NumberUsers == 1)
      assert(msg.isInstanceOf[LocalServiceResponse])
      val resp = msg.asInstanceOf[LocalServiceResponse]
      assert(resp.replyMessage == LocalResponse.ProximityTerminalEffect(PlanetSideGUID(2), true))

      val avatar2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
      avatar2.Continent = "test"
      avatar2.Spawn
      avatar2.Health = 50
      terminal.Actor ! CommonMessages.Use(avatar2, Some(avatar2))
      probe.expectNoMsg(500 milliseconds)
      assert(terminal.NumberUsers == 2)

      terminal.Actor ! CommonMessages.Unuse(avatar, Some(avatar))
      val msg2 = probe.receiveWhile(500 milliseconds) {
        case LocalServiceResponse(_, _, replyMessage) => replyMessage
      }
      assert(terminal.NumberUsers == 1)
      assert(!msg2.contains(LocalResponse.ProximityTerminalEffect(PlanetSideGUID(2), false)))
    }
  }
}

object ProximityTest {
  class SampleTerminal extends Terminal(GlobalDefinitions.dropship_vehicle_terminal) with ProximityUnit

  class ProbedLocalService(probe : TestProbe) extends LocalService {
    self.tell(Service.Join("test"), probe.ref)
  }
}

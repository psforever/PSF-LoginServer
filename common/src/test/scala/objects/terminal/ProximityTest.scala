// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.Props
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.serverobject.terminals.{ProximityTerminal, ProximityTerminalControl, ProximityUnit, Terminal}
import net.psforever.objects.zones.{Zone, ZoneActor, ZoneMap}
import net.psforever.objects.{Avatar, GlobalDefinitions, Player}
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire, PlanetSideGUID}
import org.specs2.mutable.Specification
import services.Service
import services.local.LocalService

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
    val zone : Zone = new Zone("test", new ZoneMap("test-map"), 0) {
      Actor = system.actorOf(Props(classOf[ZoneActor], this), "test-zone")
      override def SetupNumberPools() = {
        AddPool("dynamic", 1 to 10)
      }
    }
    val terminal = new ProximityTerminal(GlobalDefinitions.medical_terminal)
    terminal.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], terminal), "test-prox")
    new Building("Building", building_guid = 0, map_id = 0, zone, StructureType.Facility, GlobalDefinitions.building) {
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
    val probe1 = new TestProbe(system, "local-events")
    val probe2 = new TestProbe(system, "target-callback")
    zone.LocalEvents = probe1.ref

    "send out a start message" in {
      assert(terminal.NumberUsers == 0)
      assert(terminal.Owner.Continent.equals("test"))

      terminal.Actor.tell(CommonMessages.Use(avatar, Some(avatar)), probe2.ref)
      probe1.expectMsgClass(1 second, classOf[Terminal.StartProximityEffect])
      probe2.expectMsgClass(1 second, classOf[ProximityUnit.Action])
      assert(terminal.NumberUsers == 1)
    }
  }
}

class ProximityTerminalControlTwoUsersTest extends ActorTest {
  "ProximityTerminalControl" should {
    //setup
    val zone : Zone = new Zone("test", new ZoneMap("test-map"), 0) {
      Actor = system.actorOf(Props(classOf[ZoneActor], this), "test-zone")
      override def SetupNumberPools() = {
        AddPool("dynamic", 1 to 10)
      }
    }
    val terminal = new ProximityTerminal(GlobalDefinitions.medical_terminal)
    terminal.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], terminal), "test-prox")
    new Building("Building", building_guid = 0, map_id = 0, zone, StructureType.Facility, GlobalDefinitions.building) {
      Amenities = terminal
      Faction = PlanetSideEmpire.VS
    }

    val avatar = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
    avatar.Continent = "test"
    avatar.Spawn
    avatar.Health = 50
    val avatar2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
    avatar2.Continent = "test"
    avatar2.Spawn
    avatar2.Health = 50

    avatar.GUID = PlanetSideGUID(1)
    avatar2.GUID = PlanetSideGUID(2)
    terminal.GUID = PlanetSideGUID(3)
    terminal.Actor ! Service.Startup()
    expectNoMsg(500 milliseconds) //spacer
    val probe1 = new TestProbe(system, "local-events")
    val probe2 = new TestProbe(system, "target-callback-1")
    val probe3 = new TestProbe(system, "target-callback-2")
    zone.LocalEvents = probe1.ref

    "not send out a start message if not the first user" in {
      assert(terminal.NumberUsers == 0)
      assert(terminal.Owner.Continent.equals("test"))

      terminal.Actor.tell(CommonMessages.Use(avatar, Some(avatar)), probe2.ref)
      probe1.expectMsgClass(1 second, classOf[Terminal.StartProximityEffect])
      probe2.expectMsgClass(1 second, classOf[ProximityUnit.Action])

      terminal.Actor.tell(CommonMessages.Use(avatar2, Some(avatar2)), probe3.ref)
      probe1.expectNoMsg(1 second)
      probe2.expectMsgClass(1 second, classOf[ProximityUnit.Action])
      probe3.expectMsgClass(1 second, classOf[ProximityUnit.Action])
      assert(terminal.NumberUsers == 2)
    }
  }
}

class ProximityTerminalControlStopTest extends ActorTest {
  "ProximityTerminalControl" should {
    //setup
    val zone : Zone = new Zone("test", new ZoneMap("test-map"), 0) {
      Actor = system.actorOf(Props(classOf[ZoneActor], this), "test-zone")
      override def SetupNumberPools() = {
        AddPool("dynamic", 1 to 10)
      }
    }
    val terminal = new ProximityTerminal(GlobalDefinitions.medical_terminal)
    terminal.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], terminal), "test-prox")
    new Building("Building", building_guid = 0, map_id = 0, zone, StructureType.Facility, GlobalDefinitions.building) {
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
    val probe1 = new TestProbe(system, "local-events")
    val probe2 = new TestProbe(system, "target-callback-1")
    zone.LocalEvents = probe1.ref

    "send out a stop message" in {
      assert(terminal.NumberUsers == 0)
      assert(terminal.Owner.Continent.equals("test"))

      terminal.Actor.tell(CommonMessages.Use(avatar, Some(avatar)), probe2.ref)
      probe1.expectMsgClass(1 second, classOf[Terminal.StartProximityEffect])
      probe2.expectMsgClass(1 second, classOf[ProximityUnit.Action])

      terminal.Actor ! CommonMessages.Unuse(avatar, Some(avatar))
      probe1.expectMsgClass(1 second, classOf[Terminal.StopProximityEffect])
      assert(terminal.NumberUsers == 0)
    }
  }
}

class ProximityTerminalControlNotStopTest extends ActorTest {
  "ProximityTerminalControl" should {
    //setup
    val zone : Zone = new Zone("test", new ZoneMap("test-map"), 0) {
      Actor = system.actorOf(Props(classOf[ZoneActor], this), "test-zone")
      override def SetupNumberPools() = {
        AddPool("dynamic", 1 to 10)
      }
    }
    val terminal = new ProximityTerminal(GlobalDefinitions.medical_terminal)
    terminal.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], terminal), "test-prox")
    new Building("Building", building_guid = 0, map_id = 0, zone, StructureType.Facility, GlobalDefinitions.building) {
      Amenities = terminal
      Faction = PlanetSideEmpire.VS
    }

    val avatar = Player(Avatar("TestCharacter1", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
    avatar.Continent = "test"
    avatar.Spawn
    avatar.Health = 50
    val avatar2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.VS, CharacterGender.Female, 1, CharacterVoice.Voice1))
    avatar2.Continent = "test"
    avatar2.Spawn
    avatar2.Health = 50

    avatar.GUID = PlanetSideGUID(1)
    avatar2.GUID = PlanetSideGUID(2)
    terminal.GUID = PlanetSideGUID(3)
    terminal.Actor ! Service.Startup()
    expectNoMsg(500 milliseconds) //spacer
    val probe1 = new TestProbe(system, "local-events")
    val probe2 = new TestProbe(system, "target-callback-1")
    val probe3 = new TestProbe(system, "target-callback-2")
    zone.LocalEvents = probe1.ref

    "will not send out one stop message until last user" in {
      assert(terminal.NumberUsers == 0)
      assert(terminal.Owner.Continent.equals("test"))

      terminal.Actor.tell(CommonMessages.Use(avatar, Some(avatar)), probe2.ref)
      probe1.expectMsgClass(100 millisecond, classOf[Terminal.StartProximityEffect])
      assert(terminal.NumberUsers == 1)

      terminal.Actor.tell(CommonMessages.Use(avatar2, Some(avatar2)), probe3.ref)
      probe1.expectNoMsg(100 millisecond)
      assert(terminal.NumberUsers == 2)

      terminal.Actor ! CommonMessages.Unuse(avatar, Some(avatar))
      probe1.expectNoMsg(100 millisecond)
      assert(terminal.NumberUsers == 1)

      terminal.Actor ! CommonMessages.Unuse(avatar2, Some(avatar2))
      probe1.expectMsgClass(100 millisecond, classOf[Terminal.StopProximityEffect])
      assert(terminal.NumberUsers == 0)
    }
  }
}

object ProximityTest {
  class SampleTerminal extends Terminal(GlobalDefinitions.dropship_vehicle_terminal) with ProximityUnit

  class ProbedLocalService(probe : TestProbe, zone : Zone) extends LocalService(zone) {
    self.tell(Service.Join("test"), probe.ref)
  }
}

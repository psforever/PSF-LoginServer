// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.{Default, GlobalDefinitions, Player}
import net.psforever.objects.serverobject.doors.{Door, DoorControl}
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.services.local.{LocalAction, LocalResponse, LocalServiceMessage, LocalServiceResponse}
import net.psforever.types._
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class DoorTest extends Specification {
  private val player: Player = Player(Avatar(0, "test", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))

  "Door" should {
    "construct" in {
      Door(GlobalDefinitions.door)
      ok
    }

    "starts as closed (false)" in {
      val door = Door(GlobalDefinitions.door)
      door.Open.isEmpty mustEqual true
      door.isOpen mustEqual false
    }

    "be opened and closed (1; manual)" in {
      val door = Door(GlobalDefinitions.door)
      door.isOpen mustEqual false
      door.Open.isEmpty mustEqual true

      door.Open = Some(player)
      door.isOpen mustEqual true
      door.Open.contains(player) mustEqual true

      door.Open = None
      door.isOpen mustEqual false
      door.Open.isEmpty mustEqual true
    }

    "be opened and closed (2; toggle)" in {
      val door = Door(GlobalDefinitions.door)
      door.Open.isEmpty mustEqual true
      door.Open = player
      door.isOpen mustEqual true
      door.Open.contains(player) mustEqual true
      door.Open = None
      door.Open.isEmpty mustEqual true
      door.isOpen mustEqual false
    }
  }
}

class DoorControlConstructTest extends ActorTest {
  "DoorControl" should {
    "construct" in {
      val door = Door(GlobalDefinitions.door)
      door.Actor = system.actorOf(Props(classOf[DoorControl], door), "door")
      assert(door.Actor != Default.Actor)
    }
  }
}

class DoorControlOpenTest extends ActorTest {
  "DoorControl" should {
    "open on use" in {
      val (player, door, probe) = DoorControlTest.SetUpAgents(PlanetSideEmpire.TR)
      door.Actor ! CommonMessages.Use(player)
      val reply = probe.receiveOne(1000 milliseconds)
      assert(reply match {
        case LocalServiceMessage("test", LocalAction.DoorOpens(PlanetSideGUID(0), _, d)) => d eq door
        case _ => false
      })
      assert(door.Open.isDefined)
    }
  }
}

class DoorControlTooFarTest extends ActorTest {
  "DoorControl" should {
    "do not open if the player is too far away" in {
      val (player, door, probe) = DoorControlTest.SetUpAgents(PlanetSideEmpire.TR)
      player.Position = Vector3(10,0,0)
      door.Actor ! CommonMessages.Use(player)
      probe.expectNoMessage(Duration.create(500, "ms"))
      assert(door.Open.isEmpty)
    }
  }
}

class DoorControlAlreadyOpenTest extends ActorTest {
  "DoorControl" should {
    "is already open" in {
      val (player, door, probe) = DoorControlTest.SetUpAgents(PlanetSideEmpire.TR)
      door.Open = player //door thinks it is open
      door.Actor.tell(CommonMessages.Use(player), probe.ref)
      val reply = probe.receiveOne(1000 milliseconds)
      assert(reply match {
        case LocalServiceResponse("test", _, LocalAction.DoorOpens(guid)) => guid == door.GUID
        case _ => false
      })
    }
  }
}

class DoorControlGarbageDataTest extends ActorTest {
  "DoorControl" should {
    "do nothing if given garbage data" in {
      val (_, door, probe) = DoorControlTest.SetUpAgents(PlanetSideEmpire.TR)
      assert(door.Open.isEmpty)

      door.Actor ! "trash"
      probe.expectNoMessage(Duration.create(500, "ms"))
      assert(door.Open.isEmpty)
    }
  }
}

object DoorControlTest {
  def SetUpAgents(faction: PlanetSideEmpire.Value)(implicit system: ActorSystem): (Player, Door, TestProbe) = {
    val eventsProbe = new TestProbe(system)
    val door = Door(GlobalDefinitions.door)
    val guid = new NumberPoolHub(new MaxNumberSource(5))
    val zone = new Zone(id = "test", new ZoneMap(name = "test"), zoneNumber = 0) {
      override def SetupNumberPools(): Unit = {}
      GUID(guid)
      override def LocalEvents: ActorRef = eventsProbe.ref
    }
    guid.register(door, 1)
    door.Actor = system.actorOf(Props(classOf[DoorControl], door), "door")
    door.Owner = new Building(
      "Building",
      building_guid = 0,
      map_id = 0,
      zone,
      StructureType.Building,
      GlobalDefinitions.building
    )
    door.Owner.Faction = faction
    val player = Player(Avatar(0, "test", faction, CharacterSex.Male, 0, CharacterVoice.Mute))
    player.Zone = zone
    guid.register(player, 2)
    (player, door, eventsProbe)
  }
}

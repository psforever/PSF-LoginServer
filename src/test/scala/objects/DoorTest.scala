// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorSystem, Props}
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
import net.psforever.packet.game.UseItemMessage
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types._
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class DoorTest extends Specification {
  val player = Player(Avatar(0, "test", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))

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
      val msg = UseItemMessage(
        PlanetSideGUID(6585),
        PlanetSideGUID(0),
        PlanetSideGUID(372),
        4294967295L,
        false,
        Vector3(5.0f, 0.0f, 0.0f),
        Vector3(0.0f, 0.0f, 0.0f),
        11,
        25,
        0,
        364
      )
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

class DoorControl1Test extends ActorTest {
  "DoorControl" should {
    "construct" in {
      val door = Door(GlobalDefinitions.door)
      door.Actor = system.actorOf(Props(classOf[DoorControl], door), "door")
      assert(door.Actor != Default.Actor)
    }
  }
}

class DoorControl2Test extends ActorTest {
  "DoorControl" should {
    "open on use" in {
      val (player, door) = DoorControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe = new TestProbe(system)
      door.Zone.LocalEvents = probe.ref
      val msg = UseItemMessage(
        PlanetSideGUID(1),
        PlanetSideGUID(0),
        PlanetSideGUID(2),
        0L,
        false,
        Vector3(0f, 0f, 0f),
        Vector3(0f, 0f, 0f),
        0,
        0,
        0,
        0L
      ) //faked
      assert(door.Open.isEmpty)

      door.Actor ! CommonMessages.Use(player, Some(msg))
      val reply = probe.receiveOne(1000 milliseconds)
      assert(reply match {
        case LocalServiceMessage("test", LocalAction.DoorOpens(PlanetSideGUID(0), _, d)) => d eq door
        case _ => false
      })
      assert(door.Open.isDefined)
    }
  }
}

class DoorControl3Test extends ActorTest {
  "DoorControl" should {
    "do nothing if given garbage" in {
      val (_, door) = DoorControlTest.SetUpAgents(PlanetSideEmpire.TR)
      assert(door.Open.isEmpty)

      door.Actor ! "trash"
      expectNoMessage(Duration.create(500, "ms"))
      assert(door.Open.isEmpty)
    }
  }
}

object DoorControlTest {
  def SetUpAgents(faction: PlanetSideEmpire.Value)(implicit system: ActorSystem): (Player, Door) = {
    val door = Door(GlobalDefinitions.door)
    val guid = new NumberPoolHub(new MaxNumberSource(5))
    val zone = new Zone("test", new ZoneMap("test"), 0) {
      override def SetupNumberPools() = {}
      GUID(guid)
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
    val player = Player(Avatar(0, "test", faction, CharacterGender.Male, 0, CharacterVoice.Mute))
    guid.register(player, 2)
    (player, door)
  }
}

// Copyright (c) 2017 PSForever
package objects

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorContext
import base.ActorTest
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects._
import net.psforever.types._
import net.psforever.objects.serverobject.structures.{Building, FoundationBuilder, StructureType}
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.objects.Vehicle
import org.specs2.mutable.Specification
import akka.actor.typed.scaladsl.adapter._
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.avatar.Avatar
import net.psforever.services.ServiceManager

import scala.concurrent.duration._

class ZoneTest extends Specification {
  def test(a: String, b: Int, c: Int, d: Zone, e: ActorContext): Building = {
    Building.NoBuilding
  }

  "ZoneMap" should {
    "construct" in {
      new ZoneMap("map13")
      ok
    }

    "references bases by a positive building id (defaults to 0)" in {
      val map = new ZoneMap("map13")
      map.localBuildings mustEqual Map.empty
      map.addLocalBuilding("Building", buildingGuid = 10, mapId = 0, FoundationBuilder(test))
      map.localBuildings.keySet.contains(("Building", 10, 0)) mustEqual true
      map.addLocalBuilding("Building", buildingGuid = -1, mapId = 0, FoundationBuilder(test))
      map.localBuildings.keySet.contains(("Building", 10, 0)) mustEqual true
      map.localBuildings.keySet.contains(("Building", -1, 0)) mustEqual false
    }

    "associates objects to bases (doesn't check numbers)" in {
      val map = new ZoneMap("map13")
      map.objectToBuilding mustEqual Map.empty
      map.linkObjectToBuilding(1, 2)
      map.objectToBuilding mustEqual Map(1 -> 2)
      map.linkObjectToBuilding(3, 4)
      map.objectToBuilding mustEqual Map(1 -> 2, 3 -> 4)
    }

    "associates doors to door locks (doesn't check numbers)" in {
      val map = new ZoneMap("map13")
      map.doorToLock mustEqual Map.empty
      map.linkDoorToLock(1, 2)
      map.doorToLock mustEqual Map(1 -> 2)
      map.linkDoorToLock(3, 4)
      map.doorToLock mustEqual Map(1 -> 2, 3 -> 4)
    }

    "associates terminals to spawn pads (doesn't check numbers)" in {
      val map = new ZoneMap("map13")
      map.terminalToSpawnPad mustEqual Map.empty
      map.linkTerminalToSpawnPad(1, 2)
      map.terminalToSpawnPad mustEqual Map(1 -> 2)
      map.linkTerminalToSpawnPad(3, 4)
      map.terminalToSpawnPad mustEqual Map(1 -> 2, 3 -> 4)
    }

    "associates mechanical components to implant terminals (doesn't check numbers)" in {
      val map = new ZoneMap("map13")
      map.terminalToInterface mustEqual Map.empty
      map.linkTerminalToInterface(1, 2)
      map.terminalToInterface mustEqual Map(1 -> 2)
      map.linkTerminalToInterface(3, 4)
      map.terminalToInterface mustEqual Map(1 -> 2, 3 -> 4)
    }

    "associate turrets to weapons" in {
      val map = new ZoneMap("map13")
      map.turretToWeapon mustEqual Map.empty
      map.linkTurretToWeapon(1, 2)
      map.turretToWeapon mustEqual Map(1 -> 2)
      map.linkTurretToWeapon(3, 4)
      map.turretToWeapon mustEqual Map(1 -> 2, 3 -> 4)
    }
  }

  val map13 = new ZoneMap("map13")
  map13.addLocalBuilding("Building", buildingGuid = 0, mapId = 10, FoundationBuilder(test))
  class TestObject extends IdentifiableEntity

  "Zone" should {
    "construct" in {
      val zone = new Zone("home3", map13, 13)
      zone.EquipmentOnGround mustEqual List.empty[Equipment]
      zone.Vehicles mustEqual List.empty[Vehicle]
      zone.Players mustEqual List.empty[Player]
      zone.Corpses mustEqual List.empty[Player]
    }

    "can have its unique identifier system changed if no objects were added to it" in {
      val zone                 = new Zone("home3", map13, 13)
      val guid1: NumberPoolHub = new NumberPoolHub(new MaxNumberSource(100))
      zone.GUID(guid1) mustEqual true
      zone.AddPool("pool1", (0 to 50).toList)
      zone.AddPool("pool2", (51 to 75).toList)

      val obj = new TestObject()
      val registration =  guid1.register(obj, "pool2")
      registration.isSuccess mustEqual true
      guid1.WhichPool(obj).contains("pool2") mustEqual true

      zone.GUID(new NumberPoolHub(new MaxNumberSource(150))) mustEqual false
    }
  }
}

class ZoneActorTest extends ActorTest {
  ServiceManager.boot
  "Zone" should {
    "refuse new number pools after the Actor is started" in {
      val zone = new Zone("test", new ZoneMap("map6"), 1) { override def SetupNumberPools() = {} }
      zone.GUID(new NumberPoolHub(new MaxNumberSource(40150)))
      zone.actor = system.spawn(ZoneActor(zone), "test-add-pool-actor-init")
      expectNoMessage(Duration.create(500, "ms"))

      assert(!zone.AddPool("test1", 1 to 2))
    }

    "refuse to remove number pools after the Actor is started" in {
      val zone = new Zone("test", new ZoneMap("map6"), 1) { override def SetupNumberPools() = {} }

      zone.GUID(new NumberPoolHub(new MaxNumberSource(10)))
      zone.AddPool("test", 1 to 2)
      zone.actor = system.spawn(ZoneActor(zone), "test-remove-pool-actor-init")
      expectNoMessage(Duration.create(300, "ms"))

      assert(!zone.RemovePool("test"))
    }

    "set up spawn groups based on buildings" in {
      val map6 = new ZoneMap("map6") {
        addLocalBuilding(
          "Building",
          buildingGuid = 1,
          mapId = 1,
          FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1, 1, 1)))
        )
        addLocalObject(2, SpawnTube.Constructor(Vector3(1, 0, 0), Vector3.Zero))
        addLocalObject(3, Terminal.Constructor(Vector3.Zero, GlobalDefinitions.dropship_vehicle_terminal))
        addLocalObject(4, SpawnTube.Constructor(Vector3(1, 0, 0), Vector3.Zero))
        linkObjectToBuilding(2, 1)
        linkObjectToBuilding(3, 1)
        linkObjectToBuilding(4, 1)

        addLocalBuilding(
          "Building",
          buildingGuid = 5,
          mapId = 2,
          FoundationBuilder(Building.Structure(StructureType.Building))
        )
        addLocalObject(6, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        linkObjectToBuilding(6, 5)

        addLocalBuilding(
          "Building",
          buildingGuid = 7,
          mapId = 3,
          FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1, 1, 1)))
        )
        addLocalObject(8, Terminal.Constructor(Vector3.Zero, GlobalDefinitions.dropship_vehicle_terminal))
        addLocalObject(9, SpawnTube.Constructor(Vector3(1, 0, 0), Vector3.Zero))
        addLocalObject(10, Terminal.Constructor(Vector3.Zero, GlobalDefinitions.dropship_vehicle_terminal))
        linkObjectToBuilding(8, 7)
        linkObjectToBuilding(9, 7)
        linkObjectToBuilding(10, 7)
      }
      val zone = new Zone("test", map6, 1) { override def SetupNumberPools() = {} }
      zone.actor = system.spawn(ZoneActor(zone), "test-init")
      expectNoMessage(Duration.create(1, "seconds"))

      val groups = zone.SpawnGroups()
      assert(groups.size == 2)
      zone
        .SpawnGroups()
        .foreach({
          case (building, tubes) =>
            if (building.MapId == 1) {
              val building1 = zone.SpawnGroups(building)
              assert(tubes.length == 2)
              assert(tubes.head == building1.head)
              assert(tubes.head.GUID == PlanetSideGUID(2))
              assert(tubes(1) == building1(1))
              assert(tubes(1).GUID == PlanetSideGUID(4))
            } else if (building.MapId == 3) {
              val building2 = zone.SpawnGroups(building)
              assert(tubes.length == 1)
              assert(tubes.head == building2.head)
              assert(tubes.head.GUID == PlanetSideGUID(9))
            } else {
              assert(false)
            }
        })
    }
  }
}

class ZonePopulationTest extends ActorTest {
  "ZonePopulationActor" should {
    "add new user to zones" in {
      val zone = new Zone("test", new ZoneMap(""), 0) {
        override def SetupNumberPools() = {}
      }
      val avatar = Avatar(0, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player = Player(avatar)
      player.GUID = PlanetSideGUID(1)
      zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
      expectNoMessage(200 milliseconds)

      assert(zone.Players.isEmpty)
      assert(zone.LivePlayers.isEmpty)
      zone.Population ! Zone.Population.Join(avatar)
      zone.Population ! Zone.Population.Spawn(avatar, player, null)
      expectNoMessage(Duration.create(200, "ms"))
      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      // assert(zone.LivePlayers.isEmpty)
    }

    "remove user from zones" in {
      val zone = new Zone("test", new ZoneMap(""), 0) {
        override def SetupNumberPools() = {}
      }
      val avatar = Avatar(1, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player = Player(avatar)
      player.GUID = PlanetSideGUID(1)
      zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
      receiveOne(Duration.create(200, "ms")) //consume
      zone.Population ! Zone.Population.Join(avatar)
      zone.Population ! Zone.Population.Spawn(avatar, player, null)
      expectNoMessage(Duration.create(100, "ms"))

      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      zone.Population ! Zone.Population.Leave(avatar)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Zone.Population.PlayerHasLeft])
      assert(zone.Players.isEmpty)
    }

    /* TODO they need AvatarActor, which has further dependencies
    "associate user with a character" in {
      val zone   = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = {} }
      val avatar = Avatar(0, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player = Player(avatar)
      player.GUID = PlanetSideGUID(1)
      zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
      expectNoMessage(200 milliseconds)
      zone.Population ! Zone.Population.Join(avatar)
      expectNoMessage(Duration.create(100, "ms"))

      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.isEmpty)
      zone.Population ! Zone.Population.Spawn(avatar, player)
      expectNoMessage(Duration.create(100, "ms"))
      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.size == 1)
      assert(zone.LivePlayers.head == player)
    }

    "disassociate character from a user" in {
      val zone   = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = {} }
      val avatar = Avatar(0, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player = Player(avatar)
      player.GUID = PlanetSideGUID(1)
      zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
      expectNoMessage(200 milliseconds)
      zone.Population ! Zone.Population.Join(avatar)
      expectNoMessage(Duration.create(100, "ms"))
      zone.Population ! Zone.Population.Spawn(avatar, player)
      expectNoMessage(Duration.create(100, "ms"))

      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.size == 1)
      assert(zone.LivePlayers.head == player)
      zone.Population ! Zone.Population.Release(avatar)
      expectNoMessage(Duration.create(100, "ms"))
      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.isEmpty)
    }

    "user tries to Leave, but still has an associated character" in {
      val zone   = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = {} }
      val avatar = Avatar(0, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player = Player(avatar)
      player.GUID = PlanetSideGUID(1)
      zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
      expectNoMessage(200 milliseconds)
      zone.Population ! Zone.Population.Join(avatar)
      expectNoMessage(Duration.create(100, "ms"))
      zone.Population ! Zone.Population.Spawn(avatar, player)
      expectNoMessage(Duration.create(100, "ms"))

      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.size == 1)
      assert(zone.LivePlayers.head == player)
      zone.Population ! Zone.Population.Leave(avatar)
      val reply = receiveOne(Duration.create(500, "ms"))
      assert(zone.Players.isEmpty)
      assert(zone.LivePlayers.isEmpty)
      assert(reply.isInstanceOf[Zone.Population.PlayerHasLeft])
      assert(reply.asInstanceOf[Zone.Population.PlayerHasLeft].zone == zone)
      assert(reply.asInstanceOf[Zone.Population.PlayerHasLeft].player.contains(player))
    }

    "user tries to Spawn a character, but an associated character already exists" in {
      val zone    = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = {} }
      val avatar  = Avatar(0, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player1 = Player(avatar)
      player1.GUID = PlanetSideGUID(1)
      val player2 = Player(avatar)
      player2.GUID = PlanetSideGUID(2)
      zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
      expectNoMessage(200 milliseconds)
      zone.Population ! Zone.Population.Join(avatar)
      expectNoMessage(Duration.create(100, "ms"))
      zone.Population ! Zone.Population.Spawn(avatar, player1)
      expectNoMessage(Duration.create(100, "ms"))

      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.size == 1)
      assert(zone.LivePlayers.head == player1)
      zone.Population ! Zone.Population.Spawn(avatar, player2)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.size == 1)
      assert(zone.LivePlayers.head == player1)
      assert(reply.isInstanceOf[Zone.Population.PlayerAlreadySpawned])
      assert(reply.asInstanceOf[Zone.Population.PlayerAlreadySpawned].player == player1)
    }

    "user tries to Spawn a character, but did not Join first" in {
      val zone   = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = {} }
      val avatar = Avatar(0, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player = Player(avatar)
      player.GUID = PlanetSideGUID(1)
      zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
      expectNoMessage(200 milliseconds)

      assert(zone.Players.isEmpty)
      assert(zone.LivePlayers.isEmpty)
      zone.Population ! Zone.Population.Spawn(avatar, player)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(zone.Players.isEmpty)
      assert(zone.LivePlayers.isEmpty)
      assert(reply.isInstanceOf[Zone.Population.PlayerCanNotSpawn])
      assert(reply.asInstanceOf[Zone.Population.PlayerCanNotSpawn].zone == zone)
      assert(reply.asInstanceOf[Zone.Population.PlayerCanNotSpawn].player == player)
    }
     */

    "user tries to Release a character, but did not Spawn a character first" in {
      val zone = new Zone("test", new ZoneMap(""), 0) {
        override def SetupNumberPools() = {}
      }
      val avatar = Avatar(2, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player = Player(avatar)
      player.GUID = PlanetSideGUID(1)
      zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
      expectNoMessage(200 milliseconds)
      zone.Population ! Zone.Population.Join(avatar)
      zone.Population ! Zone.Population.Spawn(avatar, player, null)
      expectNoMessage(Duration.create(100, "ms"))

      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      // assert(zone.LivePlayers.isEmpty)
      zone.Population ! Zone.Population.Release(avatar)
      val reply = receiveOne(Duration.create(100, "ms"))
      // assert(zone.Players.size == 1)
      // assert(zone.Players.head == avatar)
      // assert(zone.LivePlayers.isEmpty)
      assert(reply.isInstanceOf[Zone.Population.PlayerHasLeft])
      assert(reply.asInstanceOf[Zone.Population.PlayerHasLeft].zone == zone)
      // assert(reply.asInstanceOf[Zone.Population.PlayerHasLeft].player.isEmpty)
    }

    "user adds character to list of retired characters" in {
      val zone = new Zone("test", new ZoneMap(""), 0) {
        override def SetupNumberPools() = {}
      }
      val player = Player(Avatar(3, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5))
      player.GUID = PlanetSideGUID(1)
      player.Release
      zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
      expectNoMessage(200 milliseconds)

      assert(zone.Corpses.isEmpty)
      zone.Population ! Zone.Corpse.Add(player)
      expectNoMessage(Duration.create(500, "ms"))
      assert(zone.Corpses.size == 1)
      assert(zone.Corpses.head == player)
    }

    "user removes character from the list of retired characters" in {
      val zone = new Zone("test", new ZoneMap(""), 0) {
        override def SetupNumberPools() = {}
      }
      val player = Player(Avatar(4, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5))
      player.GUID = PlanetSideGUID(1)
      player.Release
      zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
      expectNoMessage(200 milliseconds)
      zone.Population ! Zone.Corpse.Add(player)
      expectNoMessage(Duration.create(500, "ms"))

      assert(zone.Corpses.size == 1)
      assert(zone.Corpses.head == player)
      zone.Population ! Zone.Corpse.Remove(player)
      expectNoMessage(Duration.create(200, "ms"))
      assert(zone.Corpses.isEmpty)
    }

    "user removes THE CORRECT character from the list of retired characters" in {
      val zone = new Zone("test", new ZoneMap(""), 0) {
        override def SetupNumberPools() = {}
      }
      val player1 = Player(Avatar(5, "Chord1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5))
      player1.GUID = PlanetSideGUID(1)
      player1.Release
      val player2 = Player(Avatar(6, "Chord2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5))
      player2.GUID = PlanetSideGUID(2)
      player2.Release
      val player3 = Player(Avatar(7, "Chord3", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5))
      player3.GUID = PlanetSideGUID(3)
      player3.Release
      zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
      expectNoMessage(200 milliseconds)
      zone.Population ! Zone.Corpse.Add(player1)
      zone.Population ! Zone.Corpse.Add(player2)
      zone.Population ! Zone.Corpse.Add(player3)
      expectNoMessage(Duration.create(500, "ms"))

      assert(zone.Corpses.size == 3)
      assert(zone.Corpses.head == player1)
      assert(zone.Corpses(1) == player2)
      assert(zone.Corpses(2) == player3)
      zone.Population ! Zone.Corpse.Remove(player2)
      expectNoMessage(Duration.create(200, "ms"))
      assert(zone.Corpses.size == 2)
      assert(zone.Corpses.head == player1)
      assert(zone.Corpses(1) == player3)
    }

    "user tries to add character to list of retired characters, but is not in correct state" in {
      val zone = new Zone("test", new ZoneMap(""), 0) {
        override def SetupNumberPools() = {}
      }
      val player = Player(Avatar(8, "Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5))
      player.GUID = PlanetSideGUID(1)
      //player.Release !!important
      zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
      expectNoMessage(200 milliseconds)

      assert(zone.Corpses.isEmpty)
      zone.Population ! Zone.Corpse.Add(player)
      expectNoMessage(Duration.create(200, "ms"))
      assert(zone.Corpses.isEmpty)
    }
  }
}

class ZoneGroundDropItemTest extends ActorTest {
  val item = AmmoBox(GlobalDefinitions.bullet_9mm)
  val hub  = new NumberPoolHub(new MaxNumberSource(20))
  hub.register(item, 10)
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = {} }
  zone.GUID(hub)
  zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
  expectNoMessage(200 milliseconds)

  "DropItem" should {
    "drop item on ground" in {
      receiveOne(1 second) //consume
      assert(!zone.EquipmentOnGround.contains(item))
      zone.Ground ! Zone.Ground.DropItem(item, Vector3(1.1f, 2.2f, 3.3f), Vector3(4.4f, 5.5f, 6.6f))

      val reply = receiveOne(200 milliseconds)
      assert(reply.isInstanceOf[Zone.Ground.ItemOnGround])
      assert(reply.asInstanceOf[Zone.Ground.ItemOnGround].item == item)
      assert(reply.asInstanceOf[Zone.Ground.ItemOnGround].pos == Vector3(1.1f, 2.2f, 3.3f))
      assert(reply.asInstanceOf[Zone.Ground.ItemOnGround].orient == Vector3(4.4f, 5.5f, 6.6f))
      assert(zone.EquipmentOnGround.contains(item))
    }
  }
}

class ZoneGroundCanNotDropItem1Test extends ActorTest {
  val item = AmmoBox(GlobalDefinitions.bullet_9mm)
  val hub  = new NumberPoolHub(new MaxNumberSource(20))
  //hub.register(item, 10) //!important
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = {} }
  zone.GUID(hub)
  zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
  expectNoMessage(200 milliseconds)

  "DropItem" should {
    "not drop an item that is not registered" in {
      receiveOne(1 second) //consume
      assert(!zone.EquipmentOnGround.contains(item))
      zone.Ground ! Zone.Ground.DropItem(item, Vector3.Zero, Vector3.Zero)

      val reply = receiveOne(300 milliseconds)
      assert(reply.isInstanceOf[Zone.Ground.CanNotDropItem])
      assert(reply.asInstanceOf[Zone.Ground.CanNotDropItem].item == item)
      assert(reply.asInstanceOf[Zone.Ground.CanNotDropItem].zone == zone)
      assert(reply.asInstanceOf[Zone.Ground.CanNotDropItem].reason == "not registered yet")
      assert(!zone.EquipmentOnGround.contains(item))
    }
  }
}

class ZoneGroundCanNotDropItem2Test extends ActorTest {
  val item = AmmoBox(GlobalDefinitions.bullet_9mm)
  val hub  = new NumberPoolHub(new MaxNumberSource(20))
  hub.register(item, 10) //!important
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = {} }
  //zone.GUID(hub) //!important
  zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
  expectNoMessage(200 milliseconds)

  "DropItem" should {
    "not drop an item that is not registered to the zone" in {
      receiveOne(1 second) //consume
      assert(!zone.EquipmentOnGround.contains(item))
      zone.Ground ! Zone.Ground.DropItem(item, Vector3.Zero, Vector3.Zero)

      val reply = receiveOne(300 milliseconds)
      assert(reply.isInstanceOf[Zone.Ground.CanNotDropItem])
      assert(reply.asInstanceOf[Zone.Ground.CanNotDropItem].item == item)
      assert(reply.asInstanceOf[Zone.Ground.CanNotDropItem].zone == zone)
      assert(reply.asInstanceOf[Zone.Ground.CanNotDropItem].reason == "registered to some other zone")
      assert(!zone.EquipmentOnGround.contains(item))
    }
  }
}

class ZoneGroundCanNotDropItem3Test extends ActorTest {
  val item = AmmoBox(GlobalDefinitions.bullet_9mm)
  val hub  = new NumberPoolHub(new MaxNumberSource(20))
  hub.register(item, 10) //!important
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = {} }
  zone.GUID(hub) //!important
  zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
  expectNoMessage(200 milliseconds)

  "DropItem" should {
    "not drop an item that has already been dropped" in {
      receiveOne(1 second) //consume
      assert(!zone.EquipmentOnGround.contains(item))
      assert(zone.EquipmentOnGround.isEmpty)
      zone.Ground ! Zone.Ground.DropItem(item, Vector3.Zero, Vector3.Zero)

      val reply1 = receiveOne(300 milliseconds)
      assert(reply1.isInstanceOf[Zone.Ground.ItemOnGround])
      assert(reply1.asInstanceOf[Zone.Ground.ItemOnGround].item == item)
      assert(zone.EquipmentOnGround.contains(item))
      assert(zone.EquipmentOnGround.size == 1)
      zone.Ground ! Zone.Ground.DropItem(item, Vector3.Zero, Vector3.Zero)

      val reply2 = receiveOne(300 milliseconds)
      assert(reply2.isInstanceOf[Zone.Ground.CanNotDropItem])
      assert(reply2.asInstanceOf[Zone.Ground.CanNotDropItem].item == item)
      assert(reply2.asInstanceOf[Zone.Ground.CanNotDropItem].zone == zone)
      assert(reply2.asInstanceOf[Zone.Ground.CanNotDropItem].reason == "already dropped")
      assert(zone.EquipmentOnGround.size == 1)
    }
  }
}

class ZoneGroundPickupItemTest extends ActorTest {
  val item = AmmoBox(GlobalDefinitions.bullet_9mm)
  val hub  = new NumberPoolHub(new MaxNumberSource(20))
  hub.register(item, 10)
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = {} }
  zone.GUID(hub)
  zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
  expectNoMessage(200 milliseconds)

  "PickupItem" should {
    "pickup an item from ground" in {
      receiveOne(1 second) //consume
      assert(!zone.EquipmentOnGround.contains(item))
      zone.Ground ! Zone.Ground.DropItem(item, Vector3.Zero, Vector3.Zero)

      val reply1 = receiveOne(200 milliseconds)
      assert(reply1.isInstanceOf[Zone.Ground.ItemOnGround])
      assert(zone.EquipmentOnGround.contains(item))
      zone.Ground ! Zone.Ground.PickupItem(item.GUID)

      val reply2 = receiveOne(200 milliseconds)
      assert(reply2.isInstanceOf[Zone.Ground.ItemInHand])
      assert(reply2.asInstanceOf[Zone.Ground.ItemInHand].item == item)
      assert(!zone.EquipmentOnGround.contains(item))
    }
  }
}

class ZoneGroundCanNotPickupItemTest extends ActorTest {
  val item = AmmoBox(GlobalDefinitions.bullet_9mm)
  val hub  = new NumberPoolHub(new MaxNumberSource(20))
  hub.register(item, 10)
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = {} }
  zone.GUID(hub) //still registered to this zone
  zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
  expectNoMessage(200 milliseconds)

  "PickupItem" should {
    "not pickup an item if it can not be found" in {
      receiveOne(1 second) //consume
      assert(!zone.EquipmentOnGround.contains(item))
      zone.Ground ! Zone.Ground.PickupItem(item.GUID)

      val reply2 = receiveOne(200 milliseconds)
      assert(reply2.isInstanceOf[Zone.Ground.CanNotPickupItem])
      assert(reply2.asInstanceOf[Zone.Ground.CanNotPickupItem].item_guid == item.GUID)
      assert(reply2.asInstanceOf[Zone.Ground.CanNotPickupItem].zone == zone)
      assert(reply2.asInstanceOf[Zone.Ground.CanNotPickupItem].reason == "can not find")
    }
  }
}

class ZoneGroundRemoveItemTest extends ActorTest {
  val item = AmmoBox(GlobalDefinitions.bullet_9mm)
  val hub  = new NumberPoolHub(new MaxNumberSource(20))
  hub.register(item, 10)
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = {} }
  zone.GUID(hub) //still registered to this zone
  zone.actor = system.spawn(ZoneActor(zone), ZoneTest.TestName)
  expectNoMessage(200 milliseconds)

  "RemoveItem" should {
    "remove an item from the ground without callback (even if the item is not found)" in {
      receiveOne(1 second)
      assert(!zone.EquipmentOnGround.contains(item))
      zone.Ground ! Zone.Ground.DropItem(item, Vector3.Zero, Vector3.Zero)
      receiveOne(200 milliseconds)
      assert(zone.EquipmentOnGround.contains(item)) //dropped

      zone.Ground ! Zone.Ground.RemoveItem(item.GUID)
      expectNoMessage(500 milliseconds)
      assert(!zone.EquipmentOnGround.contains(item))

      zone.Ground ! Zone.Ground.RemoveItem(item.GUID) //repeat
      expectNoMessage(500 milliseconds)
      assert(!zone.EquipmentOnGround.contains(item))
    }
  }
}

object ZoneTest {
  val testNum          = new AtomicInteger(1)
  def TestName: String = s"test${testNum.getAndIncrement()}"
}

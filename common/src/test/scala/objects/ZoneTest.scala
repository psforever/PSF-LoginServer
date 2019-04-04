// Copyright (c) 2017 PSForever
package objects

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorContext, ActorRef, Props}
import base.ActorTest
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects._
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire, Vector3}
import net.psforever.objects.serverobject.structures.{Building, FoundationBuilder, StructureType}
import net.psforever.objects.zones.{Zone, ZoneActor, ZoneMap}
import net.psforever.objects.Vehicle
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class ZoneTest extends Specification {
  def test(a: Int, b: Int, c : Zone, d : ActorContext) : Building = { Building.NoBuilding }

  "ZoneMap" should {
    "construct" in {
      new ZoneMap("map13")
      ok
    }

    "references bases by a positive building id (defaults to 0)" in {
      val map = new ZoneMap("map13")
      map.LocalBuildings mustEqual Map.empty
      map.LocalBuilding(building_guid = 10, map_id = 0, FoundationBuilder(test))
      map.LocalBuildings.keySet.contains((10, 0)) mustEqual true
      map.LocalBuilding(building_guid = -1, map_id = 0, FoundationBuilder(test))
      map.LocalBuildings.keySet.contains((10, 0)) mustEqual true
      map.LocalBuildings.keySet.contains((-1, 0)) mustEqual false
    }

    "associates objects to bases (doesn't check numbers)" in {
      val map = new ZoneMap("map13")
      map.ObjectToBuilding mustEqual Nil
      map.ObjectToBuilding(1, 2)
      map.ObjectToBuilding mustEqual Map(1 -> 2)
      map.ObjectToBuilding(3, 4)
      map.ObjectToBuilding mustEqual Map(1 -> 2, 3 -> 4)
    }

    "associates doors to door locks (doesn't check numbers)" in {
      val map = new ZoneMap("map13")
      map.DoorToLock mustEqual Map.empty
      map.DoorToLock(1, 2)
      map.DoorToLock mustEqual Map(1 -> 2)
      map.DoorToLock(3, 4)
      map.DoorToLock mustEqual Map(1 -> 2, 3 -> 4)
    }

    "associates terminals to spawn pads (doesn't check numbers)" in {
      val map = new ZoneMap("map13")
      map.TerminalToSpawnPad mustEqual Map.empty
      map.TerminalToSpawnPad(1, 2)
      map.TerminalToSpawnPad mustEqual Map(1 -> 2)
      map.TerminalToSpawnPad(3, 4)
      map.TerminalToSpawnPad mustEqual Map(1 -> 2, 3 -> 4)
    }

    "associates mechanical components to implant terminals (doesn't check numbers)" in {
      val map = new ZoneMap("map13")
      map.TerminalToInterface mustEqual Map.empty
      map.TerminalToInterface(1, 2)
      map.TerminalToInterface mustEqual Map(1 -> 2)
      map.TerminalToInterface(3, 4)
      map.TerminalToInterface mustEqual Map(1 -> 2, 3 -> 4)
    }

    "associate turrets to weapons" in {
      val map = new ZoneMap("map13")
      map.TurretToWeapon mustEqual Map.empty
      map.TurretToWeapon(1, 2)
      map.TurretToWeapon mustEqual Map(1 -> 2)
      map.TurretToWeapon(3, 4)
      map.TurretToWeapon mustEqual Map(1 -> 2, 3 -> 4)
    }
  }

  val map13 = new ZoneMap("map13")
  map13.LocalBuilding(building_guid = 0, map_id = 10, FoundationBuilder(test))
  class TestObject extends IdentifiableEntity

  "Zone" should {
    "construct" in {
      val zone = new Zone("home3", map13, 13)
      zone.GUID mustEqual ActorRef.noSender
      zone.Ground mustEqual ActorRef.noSender
      zone.Transport mustEqual ActorRef.noSender
      //zone also has a unique identifier system but it can't be accessed without its the Actor GUID being initialized
      zone.EquipmentOnGround mustEqual List.empty[Equipment]
      zone.Vehicles mustEqual List.empty[Vehicle]
      zone.Players mustEqual List.empty[Player]
      zone.Corpses mustEqual List.empty[Player]
    }

    "can have its unique identifier system changed if no objects were added to it" in {
      val zone = new Zone("home3", map13, 13)
      val guid1 : NumberPoolHub = new NumberPoolHub(new LimitedNumberSource(100))
      zone.GUID(guid1) mustEqual true
      zone.AddPool("pool1", (0 to 50).toList)
      zone.AddPool("pool2", (51 to 75).toList)

      val obj = new TestObject()
      guid1.register(obj, "pool2").isSuccess mustEqual true
      guid1.WhichPool(obj) mustEqual Some("pool2")

      zone.GUID(new NumberPoolHub(new LimitedNumberSource(150))) mustEqual false
    }
  }
}

class ZoneActorTest extends ActorTest {
  "Zone" should {
    "have an Actor" in {
      val zone = new Zone("test", new ZoneMap("map6"), 1) { override def SetupNumberPools() = { } }
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "test-actor")
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Actor != ActorRef.noSender)
    }

    "create new number pools before the Actor is started" in {
      val zone = new Zone("test", new ZoneMap("map6"), 1) { override def SetupNumberPools() = { } }
      zone.GUID(new NumberPoolHub(new LimitedNumberSource(10)))
      assert( zone.AddPool("test1", 1 to 2) )

      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "test-add-pool-actor") //note: not Init'd yet
      assert( zone.AddPool("test2", 3 to 4) )
    }

    "remove existing number pools before the Actor is started" in {
      val zone = new Zone("test", new ZoneMap("map6"), 1) { override def SetupNumberPools() = { } }
      zone.GUID(new NumberPoolHub(new LimitedNumberSource(10)))
      assert( zone.AddPool("test1", 1 to 2) )
      assert( zone.RemovePool("test1") )

      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "test-remove-pool-actor") //note: not Init'd yet
      assert( zone.AddPool("test2", 3 to 4) )
      assert( zone.RemovePool("test2") )
    }

    "refuse new number pools after the Actor is started" in {
      val zone = new Zone("test", new ZoneMap("map6"), 1) { override def SetupNumberPools() = { } }
      zone.GUID(new NumberPoolHub(new LimitedNumberSource(40150)))
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "test-add-pool-actor-init")
      zone.Actor ! Zone.Init()
      expectNoMsg(Duration.create(500, "ms"))

      assert( !zone.AddPool("test1", 1 to 2) )
    }

    "refuse to remove number pools after the Actor is started" in {
      val zone = new Zone("test", new ZoneMap("map6"), 1) { override def SetupNumberPools() = { } }

      zone.GUID(new NumberPoolHub(new LimitedNumberSource(10)))
      zone.AddPool("test", 1 to 2)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "test-remove-pool-actor-init")
      zone.Actor ! Zone.Init()
      expectNoMsg(Duration.create(300, "ms"))

      assert( !zone.RemovePool("test") )
    }

    "set up spawn groups based on buildings" in {
      val map6 = new ZoneMap("map6") {
        LocalBuilding(building_guid = 1, map_id = 1, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1,1,1))))
        LocalObject(1, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        LocalObject(2, Terminal.Constructor(GlobalDefinitions.dropship_vehicle_terminal))
        LocalObject(3, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        ObjectToBuilding(1, 1)
        ObjectToBuilding(2, 1)
        ObjectToBuilding(3, 1)

        LocalBuilding(building_guid = 2, map_id = 2, FoundationBuilder(Building.Structure(StructureType.Building)))
        LocalObject(7, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        ObjectToBuilding(7, 2)

        LocalBuilding(building_guid = 3, map_id = 3, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1,1,1))))
        LocalObject(4, Terminal.Constructor(GlobalDefinitions.dropship_vehicle_terminal))
        LocalObject(5, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        LocalObject(6, Terminal.Constructor(GlobalDefinitions.dropship_vehicle_terminal))
        ObjectToBuilding(4, 3)
        ObjectToBuilding(5, 3)
        ObjectToBuilding(6, 3)
      }
      val zone = new Zone("test", map6, 1) { override def SetupNumberPools() = { } }
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "test-init")
      zone.Actor ! Zone.Init()
      expectNoMsg(Duration.create(300, "ms"))

      val groups = zone.SpawnGroups()
      assert(groups.size == 2)
      zone.SpawnGroups().foreach({ case(building, tubes) =>
        if(building.MapId == 1) {
          val building1 = zone.SpawnGroups(building)
          assert(tubes.length == 2)
          assert(tubes.head == building1.head)
          assert(tubes.head.GUID == PlanetSideGUID(1))
          assert(tubes(1) == building1(1))
          assert(tubes(1).GUID == PlanetSideGUID(3))
        }
        else if(building.MapId == 3) {
          val building2 = zone.SpawnGroups(building)
          assert(tubes.length == 1)
          assert(tubes.head == building2.head)
          assert(tubes.head.GUID == PlanetSideGUID(5))
        }
        else {
          assert(false)
        }
      })
    }

    "select spawn points based on the position of the player in reference to buildings" in {
      val map6 = new ZoneMap("map6") {
        LocalBuilding(building_guid = 1, map_id = 1, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1,1,1))))
        LocalObject(1, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        ObjectToBuilding(1, 1)

        LocalBuilding(building_guid = 3, map_id = 3, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4,4,4))))
        LocalObject(5, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        ObjectToBuilding(5, 3)
      }
      val zone = new Zone("test", map6, 1) { override def SetupNumberPools() = { } }
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "test-spawn")
      zone.Actor ! Zone.Init()
      expectNoMsg(Duration.create(300, "ms"))
      val player = Player(Avatar("Chord", PlanetSideEmpire.NEUTRAL, CharacterGender.Male, 0, CharacterVoice.Voice5))

      val bldg1 = zone.Building(1).get
      val bldg3 = zone.Building(3).get
      player.Position = Vector3(1,1,1) //closer to bldg1
      zone.Actor ! Zone.Lattice.RequestSpawnPoint(1, player, 7)
      val reply1 = receiveOne(Duration.create(200, "ms"))
      assert(reply1.isInstanceOf[Zone.Lattice.SpawnPoint])
      assert(reply1.asInstanceOf[Zone.Lattice.SpawnPoint].zone_id == "test")
      assert(reply1.asInstanceOf[Zone.Lattice.SpawnPoint].spawn_tube.Owner == bldg1)

      player.Position = Vector3(3,3,3) //closer to bldg3
      zone.Actor ! Zone.Lattice.RequestSpawnPoint(1, player, 7)
      val reply3 = receiveOne(Duration.create(200, "ms"))
      assert(reply3.isInstanceOf[Zone.Lattice.SpawnPoint])
      assert(reply3.asInstanceOf[Zone.Lattice.SpawnPoint].zone_id == "test")
      assert(reply3.asInstanceOf[Zone.Lattice.SpawnPoint].spawn_tube.Owner == bldg3)
    }

    "will report if no spawn points have been found in a zone" in {
      val map6 = new ZoneMap("map6") {
        LocalBuilding(building_guid = 1, map_id = 1, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1,1,1))))

        LocalBuilding(building_guid = 3, map_id = 3, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4,4,4))))
        LocalObject(5, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        ObjectToBuilding(5, 3)
      }
      val zone = new Zone("test", map6, 1) { override def SetupNumberPools() = { } }
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "test-no-spawn")
      zone.Actor ! Zone.Init()
      expectNoMsg(Duration.create(300, "ms"))
      val player = Player(Avatar("Chord", PlanetSideEmpire.NEUTRAL, CharacterGender.Male, 0, CharacterVoice.Voice5))

      zone.Actor ! Zone.Lattice.RequestSpawnPoint(1, player, 7)
      val reply = receiveOne(Duration.create(200, "ms"))
      assert(reply.isInstanceOf[Zone.Lattice.NoValidSpawnPoint])
      assert(reply.asInstanceOf[Zone.Lattice.NoValidSpawnPoint].zone_number == 1)
      assert(reply.asInstanceOf[Zone.Lattice.NoValidSpawnPoint].spawn_group.contains(7))
    }
  }
}

class ZonePopulationTest extends ActorTest {
  "ZonePopulationActor" should {
    "add new user to zones" in {
      val zone = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = { } }
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
      zone.Actor ! Zone.Init()
      expectNoMsg(200 milliseconds)

      assert(zone.Players.isEmpty)
      assert(zone.LivePlayers.isEmpty)
      zone.Population ! Zone.Population.Join(avatar)
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.isEmpty)
    }

    "remove user from zones" in {
      val zone = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = { } }
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
      zone.Actor ! Zone.Init()
      receiveOne(Duration.create(200, "ms")) //consume
      zone.Population ! Zone.Population.Join(avatar)
      expectNoMsg(Duration.create(100, "ms"))

      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      zone.Population ! Zone.Population.Leave(avatar)
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Players.isEmpty)
    }

    "associate user with a character" in {
      val zone = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = { } }
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player = Player(avatar)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
      zone.Actor ! Zone.Init()
      expectNoMsg(200 milliseconds)
      zone.Population ! Zone.Population.Join(avatar)
      expectNoMsg(Duration.create(100, "ms"))

      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.isEmpty)
      zone.Population ! Zone.Population.Spawn(avatar, player)
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.size == 1)
      assert(zone.LivePlayers.head == player)
    }

    "disassociate character from a user" in {
      val zone = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = { } }
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player = Player(avatar)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
      zone.Actor ! Zone.Init()
      expectNoMsg(200 milliseconds)
      zone.Population ! Zone.Population.Join(avatar)
      expectNoMsg(Duration.create(100, "ms"))
      zone.Population ! Zone.Population.Spawn(avatar, player)
      expectNoMsg(Duration.create(100, "ms"))

      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.size == 1)
      assert(zone.LivePlayers.head == player)
      zone.Population ! Zone.Population.Release(avatar)
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.isEmpty)
    }

    "user tries to Leave, but still has an associated character" in {
      val zone = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = { } }
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player = Player(avatar)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
      zone.Actor ! Zone.Init()
      expectNoMsg(200 milliseconds)
      zone.Population ! Zone.Population.Join(avatar)
      expectNoMsg(Duration.create(100, "ms"))
      zone.Population ! Zone.Population.Spawn(avatar, player)
      expectNoMsg(Duration.create(100, "ms"))

      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.size == 1)
      assert(zone.LivePlayers.head == player)
      zone.Population ! Zone.Population.Leave(avatar)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(zone.Players.isEmpty)
      assert(zone.LivePlayers.isEmpty)
      assert(reply.isInstanceOf[Zone.Population.PlayerHasLeft])
      assert(reply.asInstanceOf[Zone.Population.PlayerHasLeft].zone == zone)
      assert(reply.asInstanceOf[Zone.Population.PlayerHasLeft].player.contains(player))
    }

    "user tries to Spawn a character, but an associated character already exists" in {
      val zone = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = { } }
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player1 = Player(avatar)
      val player2 = Player(avatar)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
      zone.Actor ! Zone.Init()
      expectNoMsg(200 milliseconds)
      zone.Population ! Zone.Population.Join(avatar)
      expectNoMsg(Duration.create(100, "ms"))
      zone.Population ! Zone.Population.Spawn(avatar, player1)
      expectNoMsg(Duration.create(100, "ms"))

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
      val zone = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = { } }
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      val player = Player(avatar)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
      zone.Actor ! Zone.Init()
      expectNoMsg(200 milliseconds)

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

    "user tries to Release a character, but did not Spawn a character first" in {
      val zone = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = { } }
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
      zone.Actor ! Zone.Init()
      expectNoMsg(200 milliseconds)
      zone.Population ! Zone.Population.Join(avatar)
      expectNoMsg(Duration.create(100, "ms"))

      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.isEmpty)
      zone.Population ! Zone.Population.Release(avatar)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.isEmpty)
      assert(reply.isInstanceOf[Zone.Population.PlayerHasLeft])
      assert(reply.asInstanceOf[Zone.Population.PlayerHasLeft].zone == zone)
      assert(reply.asInstanceOf[Zone.Population.PlayerHasLeft].player.isEmpty)
    }

    "user adds character to list of retired characters" in {
      val zone = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = { } }
      val player = Player(Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5))
      player.Release
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
      zone.Actor ! Zone.Init()
      expectNoMsg(200 milliseconds)

      assert(zone.Corpses.isEmpty)
      zone.Population ! Zone.Corpse.Add(player)
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Corpses.size == 1)
      assert(zone.Corpses.head == player)
    }

    "user removes character from the list of retired characters" in {
      val zone = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = { } }
      val player = Player(Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5))
      player.Release
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
      zone.Actor ! Zone.Init()
      expectNoMsg(200 milliseconds)
      zone.Population ! Zone.Corpse.Add(player)
      expectNoMsg(Duration.create(100, "ms"))

      assert(zone.Corpses.size == 1)
      assert(zone.Corpses.head == player)
      zone.Population ! Zone.Corpse.Remove(player)
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Corpses.isEmpty)
    }

    "user removes THE CORRECT character from the list of retired characters" in {
      val zone = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = { } }
      val player1 = Player(Avatar("Chord1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5))
      player1.Release
      val player2 = Player(Avatar("Chord2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5))
      player2.Release
      val player3 = Player(Avatar("Chord3", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5))
      player3.Release
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
      zone.Actor ! Zone.Init()
      expectNoMsg(200 milliseconds)
      zone.Population ! Zone.Corpse.Add(player1)
      zone.Population ! Zone.Corpse.Add(player2)
      zone.Population ! Zone.Corpse.Add(player3)
      expectNoMsg(Duration.create(100, "ms"))

      assert(zone.Corpses.size == 3)
      assert(zone.Corpses.head == player1)
      assert(zone.Corpses(1) == player2)
      assert(zone.Corpses(2) == player3)
      zone.Population ! Zone.Corpse.Remove(player2)
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Corpses.size == 2)
      assert(zone.Corpses.head == player1)
      assert(zone.Corpses(1) == player3)
    }

    "user tries to add character to list of retired characters, but is not in correct state" in {
      val zone = new Zone("test", new ZoneMap(""), 0) { override def SetupNumberPools() = { } }
      val player = Player(Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Voice5))
      //player.Release !!important
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
      zone.Actor ! Zone.Init()
      expectNoMsg(200 milliseconds)

      assert(zone.Corpses.isEmpty)
      zone.Population ! Zone.Corpse.Add(player)
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Corpses.isEmpty)
    }
  }
}

class ZoneGroundDropItemTest extends ActorTest {
  val item = AmmoBox(GlobalDefinitions.bullet_9mm)
  val hub = new NumberPoolHub(new LimitedNumberSource(20))
  hub.register(item, 10)
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = { } }
  zone.GUID(hub)
  zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
  zone.Actor ! Zone.Init()
  expectNoMsg(200 milliseconds)

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
  val hub = new NumberPoolHub(new LimitedNumberSource(20))
  //hub.register(item, 10) //!important
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = { } }
  zone.GUID(hub)
  zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
  zone.Actor ! Zone.Init()
  expectNoMsg(200 milliseconds)

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
  val hub = new NumberPoolHub(new LimitedNumberSource(20))
  hub.register(item, 10) //!important
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = { } }
  //zone.GUID(hub) //!important
  zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
  zone.Actor ! Zone.Init()
  expectNoMsg(200 milliseconds)

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
  val hub = new NumberPoolHub(new LimitedNumberSource(20))
  hub.register(item, 10) //!important
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = { } }
  zone.GUID(hub) //!important
  zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
  zone.Actor ! Zone.Init()
  expectNoMsg(200 milliseconds)

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
  val hub = new NumberPoolHub(new LimitedNumberSource(20))
  hub.register(item, 10)
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = { } }
  zone.GUID(hub)
  zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
  zone.Actor ! Zone.Init()
  expectNoMsg(200 milliseconds)

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
  val hub = new NumberPoolHub(new LimitedNumberSource(20))
  hub.register(item, 10)
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = { } }
  zone.GUID(hub) //still registered to this zone
  zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
  zone.Actor ! Zone.Init()
  expectNoMsg(200 milliseconds)

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
  val hub = new NumberPoolHub(new LimitedNumberSource(20))
  hub.register(item, 10)
  val zone = new Zone("test", new ZoneMap("test-map"), 0) { override def SetupNumberPools() = { } }
  zone.GUID(hub) //still registered to this zone
  zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), ZoneTest.TestName)
  zone.Actor ! Zone.Init()
  expectNoMsg(200 milliseconds)

  "RemoveItem" should {
    "remove an item from the ground without callback (even if the item is not found)" in {
      receiveOne(1 second)
      assert(!zone.EquipmentOnGround.contains(item))
      zone.Ground ! Zone.Ground.DropItem(item, Vector3.Zero, Vector3.Zero)
      receiveOne(200 milliseconds)
      assert(zone.EquipmentOnGround.contains(item)) //dropped

      zone.Ground ! Zone.Ground.RemoveItem(item.GUID)
      expectNoMsg(500 milliseconds)
      assert(!zone.EquipmentOnGround.contains(item))

      zone.Ground ! Zone.Ground.RemoveItem(item.GUID) //repeat
      expectNoMsg(500 milliseconds)
      assert(!zone.EquipmentOnGround.contains(item))
    }
  }
}

object ZoneTest {
  val testNum = new AtomicInteger(1)
  def TestName : String = s"test${testNum.getAndIncrement()}"
}

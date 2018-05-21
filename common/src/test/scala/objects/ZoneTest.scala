// Copyright (c) 2017 PSForever
package objects

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects._
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterGender, PlanetSideEmpire, Vector3}
import net.psforever.objects.serverobject.structures.{Building, FoundationBuilder, StructureType}
import net.psforever.objects.zones.{Zone, ZoneActor, ZoneMap}
import net.psforever.objects.Vehicle
import org.specs2.mutable.Specification

import scala.concurrent.duration.Duration

class ZoneTest extends Specification {
  def test(a: Int, b : Zone, c : ActorContext) : Building = { Building.NoBuilding }

  "ZoneMap" should {
    "construct" in {
      new ZoneMap("map13")
      ok
    }

    "references bases by a positive building id (defaults to 0)" in {
      val map = new ZoneMap("map13")
      map.LocalBuildings mustEqual Map.empty
      map.LocalBuilding(10, FoundationBuilder(test))
      map.LocalBuildings.keySet.contains(10) mustEqual true
      map.LocalBuilding(-1, FoundationBuilder(test))
      map.LocalBuildings.keySet.contains(10) mustEqual true
      map.LocalBuildings.keySet.contains(-1) mustEqual false
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
  }

  val map13 = new ZoneMap("map13")
  map13.LocalBuilding(10, FoundationBuilder(test))
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
      guid1.AddPool("pool1", (0 to 50).toList)
      guid1.AddPool("pool2", (51 to 75).toList)
      zone.GUID(guid1) mustEqual true

      val obj = new TestObject()
      guid1.register(obj, "pool2").isSuccess mustEqual true
      guid1.WhichPool(obj) mustEqual Some("pool2")

      val guid2 : NumberPoolHub = new NumberPoolHub(new LimitedNumberSource(150))
      guid2.AddPool("pool3", (0 to 50).toList)
      guid2.AddPool("pool4", (51 to 75).toList)
      zone.GUID(guid2) mustEqual false
    }
  }
}

class ZoneActorTest extends ActorTest {
  "Zone" should {
    "have an Actor" in {
      val zone = new Zone("test", new ZoneMap("map6"), 1)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "test-actor")
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Actor != ActorRef.noSender)
    }

    "set up spawn groups based on buildings" in {
      val map6 = new ZoneMap("map6") {
        LocalBuilding(1, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1,1,1))))
        LocalObject(1, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        LocalObject(2, Terminal.Constructor(GlobalDefinitions.dropship_vehicle_terminal))
        LocalObject(3, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        ObjectToBuilding(1, 1)
        ObjectToBuilding(2, 1)
        ObjectToBuilding(3, 1)

        LocalBuilding(2, FoundationBuilder(Building.Structure(StructureType.Building)))
        LocalObject(7, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        ObjectToBuilding(7, 2)

        LocalBuilding(3, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1,1,1))))
        LocalObject(4, Terminal.Constructor(GlobalDefinitions.dropship_vehicle_terminal))
        LocalObject(5, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        LocalObject(6, Terminal.Constructor(GlobalDefinitions.dropship_vehicle_terminal))
        ObjectToBuilding(4, 3)
        ObjectToBuilding(5, 3)
        ObjectToBuilding(6, 3)
      }
      val zone = new Zone("test", map6, 1)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "test-init")
      zone.Actor ! Zone.Init()
      expectNoMsg(Duration.create(300, "ms"))

      val groups = zone.SpawnGroups()
      assert(groups.size == 2)
      zone.SpawnGroups().foreach({ case(building, tubes) =>
        if(building.Id == 1) {
          val building1 = zone.SpawnGroups(building)
          assert(tubes.length == 2)
          assert(tubes.head == building1.head)
          assert(tubes.head.GUID == PlanetSideGUID(1))
          assert(tubes(1) == building1(1))
          assert(tubes(1).GUID == PlanetSideGUID(3))
        }
        else if(building.Id == 3) {
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
        LocalBuilding(1, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1,1,1))))
        LocalObject(1, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        ObjectToBuilding(1, 1)

        LocalBuilding(3, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4,4,4))))
        LocalObject(5, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        ObjectToBuilding(5, 3)
      }
      val zone = new Zone("test", map6, 1)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "test-spawn")
      zone.Actor ! Zone.Init()
      expectNoMsg(Duration.create(300, "ms"))
      val player = Player(Avatar("Chord", PlanetSideEmpire.NEUTRAL, CharacterGender.Male, 0, 5))

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
        LocalBuilding(1, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1,1,1))))

        LocalBuilding(3, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4,4,4))))
        LocalObject(5, SpawnTube.Constructor(Vector3.Zero, Vector3.Zero))
        ObjectToBuilding(5, 3)
      }
      val zone = new Zone("test", map6, 1)
      zone.Actor = system.actorOf(Props(classOf[ZoneActor], zone), "test-no-spawn")
      zone.Actor ! Zone.Init()
      expectNoMsg(Duration.create(300, "ms"))
      val player = Player(Avatar("Chord", PlanetSideEmpire.NEUTRAL, CharacterGender.Male, 0, 5))

      zone.Actor ! Zone.Lattice.RequestSpawnPoint(1, player, 7)
      val reply = receiveOne(Duration.create(200, "ms"))
      assert(reply.isInstanceOf[Zone.Lattice.NoValidSpawnPoint])
      assert(reply.asInstanceOf[Zone.Lattice.NoValidSpawnPoint].zone_number == 1)
      assert(reply.asInstanceOf[Zone.Lattice.NoValidSpawnPoint].spawn_group.contains(7))
    }
  }
}

class ZonePopulationTest extends ActorTest {
  val testNum = new AtomicInteger(1)
  def TestName : String = s"test${testNum.getAndIncrement()}"

  "ZonePopulationActor" should {
    "add new user to zones" in {
      val zone = new Zone("test", new ZoneMap(""), 0)
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), TestName) ! "!"
      receiveOne(Duration.create(200, "ms")) //consume

      assert(zone.Players.isEmpty)
      assert(zone.LivePlayers.isEmpty)
      zone.Population ! Zone.Population.Join(avatar)
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Players.size == 1)
      assert(zone.Players.head == avatar)
      assert(zone.LivePlayers.isEmpty)
    }

    "remove user from zones" in {
      val zone = new Zone("test", new ZoneMap(""), 0)
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), TestName) ! "!"
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
      val zone = new Zone("test", new ZoneMap(""), 0)
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
      val player = Player(avatar)
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), TestName) ! "!"
      receiveOne(Duration.create(200, "ms")) //consume
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
      val zone = new Zone("test", new ZoneMap(""), 0)
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
      val player = Player(avatar)
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), TestName) ! "!"
      receiveOne(Duration.create(200, "ms")) //consume
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
      val zone = new Zone("test", new ZoneMap(""), 0)
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
      val player = Player(avatar)
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), TestName) ! "!"
      receiveOne(Duration.create(500, "ms")) //consume
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
      val zone = new Zone("test", new ZoneMap(""), 0)
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
      val player1 = Player(avatar)
      val player2 = Player(avatar)
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), TestName) ! "!"
      receiveOne(Duration.create(200, "ms")) //consume
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
      val zone = new Zone("test", new ZoneMap(""), 0)
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
      val player = Player(avatar)
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), TestName) ! "!"
      receiveOne(Duration.create(200, "ms")) //consume

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
      val zone = new Zone("test", new ZoneMap(""), 0)
      val avatar = Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5)
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), TestName) ! "!"
      receiveOne(Duration.create(200, "ms")) //consume
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
      val zone = new Zone("test", new ZoneMap(""), 0)
      val player = Player(Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5))
      player.Release
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), TestName) ! "!"
      receiveOne(Duration.create(200, "ms")) //consume

      assert(zone.Corpses.isEmpty)
      zone.Population ! Zone.Corpse.Add(player)
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Corpses.size == 1)
      assert(zone.Corpses.head == player)
    }

    "user removes character from the list of retired characters" in {
      val zone = new Zone("test", new ZoneMap(""), 0)
      val player = Player(Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5))
      player.Release
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), TestName) ! "!"
      receiveOne(Duration.create(200, "ms")) //consume
      zone.Population ! Zone.Corpse.Add(player)
      expectNoMsg(Duration.create(100, "ms"))

      assert(zone.Corpses.size == 1)
      assert(zone.Corpses.head == player)
      zone.Population ! Zone.Corpse.Remove(player)
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Corpses.isEmpty)
    }

    "user removes THE CORRECT character from the list of retired characters" in {
      val zone = new Zone("test", new ZoneMap(""), 0)
      val player1 = Player(Avatar("Chord1", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5))
      player1.Release
      val player2 = Player(Avatar("Chord2", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5))
      player2.Release
      val player3 = Player(Avatar("Chord3", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5))
      player3.Release
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), TestName) ! "!"
      receiveOne(Duration.create(200, "ms")) //consume
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
      val zone = new Zone("test", new ZoneMap(""), 0)
      val player = Player(Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5))
      //player.Release !!important
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), "testC") ! "!"
      receiveOne(Duration.create(500, "ms")) //consume

      assert(zone.Corpses.isEmpty)
      zone.Population ! Zone.Corpse.Add(player)
      expectNoMsg(Duration.create(100, "ms"))
      assert(zone.Corpses.isEmpty)
    }
  }
}

class ZoneGroundTest extends ActorTest {
  val item = AmmoBox(GlobalDefinitions.bullet_9mm)
  item.GUID = PlanetSideGUID(10)

  "ZoneGroundActor" should {
    "drop item on ground" in {
      val zone = new Zone("test", new ZoneMap(""), 0)
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), "drop-item-test") ! "!"
      receiveOne(Duration.create(200, "ms")) //consume

      assert(zone.EquipmentOnGround.isEmpty)
      assert(item.Position == Vector3.Zero)
      assert(item.Orientation == Vector3.Zero)
      zone.Ground ! Zone.DropItemOnGround(item, Vector3(1.1f, 2.2f, 3.3f), Vector3(4.4f, 5.5f, 6.6f))
      expectNoMsg(Duration.create(100, "ms"))

      assert(zone.EquipmentOnGround == List(item))
      assert(item.Position == Vector3(1.1f, 2.2f, 3.3f))
      assert(item.Orientation == Vector3(4.4f, 5.5f, 6.6f))
    }

    "get item from ground (success)" in {
      val zone = new Zone("test", new ZoneMap(""), 0)
      val player = Player(Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5))
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), "get-item-test-good") ! "!"
      receiveOne(Duration.create(200, "ms")) //consume
      zone.Ground ! Zone.DropItemOnGround(item, Vector3.Zero, Vector3.Zero)
      expectNoMsg(Duration.create(100, "ms"))

      assert(zone.EquipmentOnGround == List(item))
      zone.Ground ! Zone.GetItemOnGround(player, PlanetSideGUID(10))
      val reply = receiveOne(Duration.create(100, "ms"))

      assert(zone.EquipmentOnGround.isEmpty)
      assert(reply.isInstanceOf[Zone.ItemFromGround])
      assert(reply.asInstanceOf[Zone.ItemFromGround].player == player)
      assert(reply.asInstanceOf[Zone.ItemFromGround].item == item)
    }

    "get item from ground (failure)" in {
      val zone = new Zone("test", new ZoneMap(""), 0)
      val player = Player(Avatar("Chord", PlanetSideEmpire.TR, CharacterGender.Male, 0, 5))
      system.actorOf(Props(classOf[ZoneTest.ZoneInitActor], zone), "get-item-test-fail") ! "!"
      receiveOne(Duration.create(200, "ms")) //consume
      zone.Ground ! Zone.DropItemOnGround(item, Vector3.Zero, Vector3.Zero)
      expectNoMsg(Duration.create(100, "ms"))

      assert(zone.EquipmentOnGround == List(item))
      zone.Ground ! Zone.GetItemOnGround(player, PlanetSideGUID(11)) //wrong guid
      expectNoMsg(Duration.create(500, "ms"))
    }
  }
}

object ZoneTest {
  class ZoneInitActor(zone : Zone) extends Actor {
    def receive : Receive = {
      case "!" =>
        zone.Init(context)
        sender ! "!"
      case _ => ;
    }
  }
}

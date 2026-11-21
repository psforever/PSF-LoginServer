// Copyright (c) 2026 PSForever
package objects

import net.psforever.objects.avatar.{Avatar, AvatarBot}
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.environment.SeaLevel
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.zones.Zone
import net.psforever.objects.zones.blockmap.{BlockMapEntity, Sector}
import net.psforever.objects.{ExplosiveDeployable, GlobalDefinitions, Player, Tool, Vehicle}
import net.psforever.types.{CharacterSex, CharacterVoice, PlanetSideEmpire, PlanetSideGUID, Vector3}
import org.specs2.mutable.Specification

/**
  * The contents of one blockmap `Sector`: which kinds of entity it lists, how it tells them apart,
  * the order it keeps them in, and what it will and will not remove.
  */
class SectorTest extends Specification {
  private def sector: Sector = new Sector(longitude = 0, latitude = 0, span = 100)

  private def player(guid: Int): Player = {
    val p = Player(Avatar(0, s"player$guid", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
    p.GUID = PlanetSideGUID(guid)
    p
  }

  private def corpse(guid: Int): Player = {
    val p = player(guid)
    p.Die
    p.Release
    p
  }

  private def bot(guid: Int): AvatarBot = {
    val b = AvatarBot(s"bot$guid", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute, GlobalDefinitions.avatar_bot)
    b.GUID = PlanetSideGUID(guid)
    b
  }

  private def vehicle: Vehicle = new Vehicle(GlobalDefinitions.quadstealth)

  private def equipment: Tool = Tool(GlobalDefinitions.suppressor)

  private def deployable: ExplosiveDeployable = new ExplosiveDeployable(GlobalDefinitions.boomer)

  private def building(guid: Int): Building = {
    val b = Building(s"building$guid", guid = 0, map_id = 0, Zone.Nowhere, StructureType.Building)
    b.GUID = PlanetSideGUID(guid)
    b
  }

  private def amenity(guid: Int): Door = {
    val d = new Door(GlobalDefinitions.door)
    d.GUID = PlanetSideGUID(guid)
    d
  }

  private def environment: SeaLevel = SeaLevel(10f)

  private def projectile: Projectile =
    new Projectile(
      GlobalDefinitions.wasp_rocket_projectile,
      GlobalDefinitions.wasp_weapon_system,
      GlobalDefinitions.wasp_weapon_system.FireModes.head,
      Some((1, SourceEntry.None)),
      SourceEntry.None,
      GlobalDefinitions.wasp_weapon_system.ObjectId,
      Vector3(1, 2, 3),
      Vector3(4, 5, 6),
      Some(Vector3(7, 8, 9))
    )

  /** every list of the sector, by name */
  private def lists(s: Sector): Map[String, List[Any]] =
    Map(
      "livePlayers" -> s.livePlayerList,
      "bots"        -> s.botList,
      "corpses"     -> s.corpseList,
      "vehicles"    -> s.vehicleList,
      "equipment"   -> s.equipmentOnGroundList,
      "deployables" -> s.deployableList,
      "buildings"   -> s.buildingList,
      "amenities"   -> s.amenityList,
      "environment" -> s.environmentList,
      "projectiles" -> s.projectileList
    )

  "Sector" should {
    "start empty" in {
      val s = sector
      lists(s).values.forall(_.isEmpty) mustEqual true
      s.total mustEqual 0
    }

    "list each kind of entity in its own list" in {
      val s = sector
      val entities: Map[String, BlockMapEntity] = Map(
        "livePlayers" -> player(1),
        "bots"        -> bot(2),
        "corpses"     -> corpse(3),
        "vehicles"    -> vehicle,
        "equipment"   -> equipment,
        "deployables" -> deployable,
        "buildings"   -> building(4),
        "amenities"   -> amenity(5),
        "environment" -> environment,
        "projectiles" -> projectile
      )
      entities.values.forall(s.addTo) mustEqual true
      lists(s) mustEqual entities.map { case (name, e) => name -> List(e) }
      s.total mustEqual 10
    }

    "not list the same entity twice" in {
      val s = sector
      val entities: Seq[BlockMapEntity] =
        Seq(player(1), bot(2), corpse(3), vehicle, equipment, deployable, building(4), amenity(5), environment, projectile)
      entities.foreach(s.addTo)
      entities.map(s.addTo) mustEqual Seq.fill(10)(false)
      s.total mustEqual 10
    }

    "tell live players, bots, buildings, and amenities apart by GUID alone" in {
      val s = sector
      s.addTo(player(1)) mustEqual true
      s.addTo(player(1)) mustEqual false //a different object with the same GUID
      s.addTo(player(2)) mustEqual true
      s.addTo(bot(3)) mustEqual true
      s.addTo(bot(3)) mustEqual false
      s.addTo(building(4)) mustEqual true
      s.addTo(building(4)) mustEqual false
      s.addTo(amenity(5)) mustEqual true
      s.addTo(amenity(5)) mustEqual false
      (s.livePlayerList.size, s.botList.size, s.buildingList.size, s.amenityList.size) mustEqual ((2, 1, 1, 1))
    }

    "tell corpses, vehicles, equipment, deployables, environment, and projectiles apart by identity" in {
      val s = sector
      Seq[BlockMapEntity](corpse(1), corpse(1), vehicle, vehicle, equipment, equipment, deployable, deployable)
        .forall(s.addTo) mustEqual true
      s.addTo(environment) mustEqual true
      s.addTo(environment) mustEqual true //equal case classes, different objects
      s.addTo(projectile) mustEqual true
      s.addTo(projectile) mustEqual true
      (s.corpseList.size, s.vehicleList.size, s.equipmentOnGroundList.size, s.deployableList.size) mustEqual ((2, 2, 2, 2))
      (s.environmentList.size, s.projectileList.size) mustEqual ((2, 2))
    }

    "keep entities in the order they were added, appending after a removal" in {
      val s                = sector
      val (p1, p2, p3, p4) = (player(1), player(2), player(3), player(4))
      Seq(p1, p2, p3).foreach(s.addTo)
      s.livePlayerList mustEqual List(p1, p2, p3)
      s.removeFrom(p2)
      s.livePlayerList mustEqual List(p1, p3)
      s.addTo(p4)
      s.addTo(p2)
      s.livePlayerList mustEqual List(p1, p3, p4, p2)
    }

    "remove live players, bots, corpses, vehicles, equipment, deployables, and projectiles" in {
      val s        = sector
      val entities = Seq[BlockMapEntity](player(1), bot(2), corpse(3), vehicle, equipment, deployable, projectile)
      entities.foreach(s.addTo)
      entities.map(s.removeFrom) mustEqual Seq.fill(entities.size)(true)
      s.total mustEqual 0
      entities.map(s.removeFrom) mustEqual Seq.fill(entities.size)(false) //already gone
    }

    "remove a live player by GUID, not by identity" in {
      val s = sector
      s.addTo(player(1))
      s.removeFrom(player(1)) mustEqual true //a different object with the same GUID
      s.livePlayerList.isEmpty mustEqual true
    }

    "not remove buildings, amenities, or environment" in {
      val s            = sector
      val (bl, a, env) = (building(4), amenity(5), environment)
      Seq[BlockMapEntity](bl, a, env).foreach(s.addTo)
      Seq[BlockMapEntity](bl, a, env).map(s.removeFrom) mustEqual Seq(false, false, false)
      (s.buildingList, s.amenityList, s.environmentList) mustEqual ((List(bl), List(a), List(env)))
    }

    "not remove anything that is not a kind of entity it lists" in {
      val s = sector
      s.addTo(player(1))
      s.removeFrom("player1") mustEqual false
      s.total mustEqual 1
    }

    "move a player from the live list to the corpse list when it is added as a backpack" in {
      val s = sector
      val p = player(1)
      s.addTo(p) mustEqual true
      p.Die
      p.Release
      p.isBackpack mustEqual true
      s.addTo(p) mustEqual true
      s.livePlayerList.isEmpty mustEqual true
      s.corpseList mustEqual List(p)
      s.removeFrom(p) mustEqual true //removed from the corpse list, as a backpack
      s.total mustEqual 0
    }

    "list an amenity's environment fields along with it, once" in {
      val s    = sector
      val door = new Door(GlobalDefinitions.gr_door_ext) //has an interior door field
      door.GUID = PlanetSideGUID(5)
      s.addTo(door) mustEqual true
      s.amenityList mustEqual List(door)
      s.environmentList.size mustEqual 1
      s.addTo(door) mustEqual false
      s.environmentList.size mustEqual 1
    }
  }

  "Sector.Empty" should {
    "list nothing, whatever is added" in {
      Sector.Empty.addTo(player(1)) mustEqual false
      Sector.Empty.total mustEqual 0
    }
  }
}

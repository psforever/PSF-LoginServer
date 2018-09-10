// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.{ActorContext, ActorRef, Props}
import akka.routing.RandomPool
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects._
import net.psforever.objects.ce.Deployable
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.actor.UniqueNumberSystem
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.{Map => PairMap}

/**
  * A server object representing the one-landmass planets as well as the individual subterranean caverns.<br>
  * <br>
  * The concept of a "zone" is synonymous to the common vernacular "continent,"
  * commonly referred by names such as Hossin or Ishundar and internally identified as c2 and c7, respectively.
  * A `Zone` is composed of the abstracted concept of all the information pertinent for the simulation of the environment.
  * That is, "everything about the continent."
  * Physically, server objects and dynamic game objects are maintained through a local unique identifier system.
  * Static server objects originate from the `ZoneMap`.
  * Dynamic game objects originate from player characters.
  * (Write more later.)
  * @param zoneId the privileged name that can be used as the second parameter in the packet `LoadMapMessage`
  * @param zoneMap the map of server objects upon which this `Zone` is based
  * @param zoneNumber the numerical index of the `Zone` as it is recognized in a variety of packets;
  *                   also used by `LivePlayerList` to indicate a specific `Zone`
  * @see `ZoneMap`<br>
  *      `LoadMapMessage`<br>
  *      `LivePlayerList`
  */
class Zone(private val zoneId : String, zoneMap : ZoneMap, zoneNumber : Int) {
  /** Governs general synchronized external requests. */
  private var actor = ActorRef.noSender

  /** Used by the globally unique identifier system to coordinate requests. */
  private var accessor : ActorRef = ActorRef.noSender
  /** The basic support structure for the globally unique number system used by this `Zone`. */
  private var guid : NumberPoolHub = new NumberPoolHub(new LimitedNumberSource(65536))
  /** A synchronized `List` of items (`Equipment`) dropped by players on the ground and can be collected again. */
  private val equipmentOnGround : ListBuffer[Equipment] = ListBuffer[Equipment]()
  /** */
  private val vehicles : ListBuffer[Vehicle] = ListBuffer[Vehicle]()
  /** Used by the `Zone` to coordinate `Equipment` dropping and collection requests. */
  private var ground : ActorRef = ActorRef.noSender
  /** */
  private val constructions : ListBuffer[PlanetSideGameObject with Deployable] = ListBuffer[PlanetSideGameObject with Deployable]()
  /** */
  private var deployables : ActorRef = ActorRef.noSender
  /** */
  private var transport : ActorRef = ActorRef.noSender
  /** */
  private val players : TrieMap[Avatar, Option[Player]] = TrieMap[Avatar, Option[Player]]()
  /** */
  private val corpses : ListBuffer[Player] = ListBuffer[Player]()
  /** */
  private var population : ActorRef = ActorRef.noSender

  private var buildings : PairMap[Int, Building] = PairMap.empty[Int, Building]
  /** key - spawn zone id, value - buildings belonging to spawn zone */
  private var spawnGroups : Map[Building, List[SpawnTube]] = PairMap[Building, List[SpawnTube]]()
  /** */
  private var vehicleEvents : ActorRef = ActorRef.noSender

  /**
    * Establish the basic accessible conditions necessary for a functional `Zone`.<br>
    * <br>
    * Called from the `Actor` that governs this `Zone` when it is passed a constructor reference to the `Zone`.
    * Specifically, the order of calling follows: `InterstellarCluster.preStart -> ZoneActor.receive(Zone.Init()) -> Zone.Init`.
    * The basic method performs three main operations.
    * First, the `Actor`-driven aspect of the globally unique identifier system for this `Zone` is finalized.
    * Second, all supporting `Actor` agents are created, e.g., `ground`.
    * Third, the `ZoneMap` server objects are loaded and constructed within that aforementioned system.
    * To avoid being called more than once, there is a test whether the `accessor` for the globally unique identifier system has been changed.<br>
    * <br>
    * Execution of this operation should be fail-safe.
    * The chances of failure should be mitigated or skipped.
    * An testing routine should be run after the fact on the results of the process.
    * @see `ZoneActor.ZoneSetupCheck`
    * @param context a reference to an `ActorContext` necessary for `Props`
    */
  def Init(implicit context : ActorContext) : Unit = {
    if(accessor == ActorRef.noSender) {
      implicit val guid : NumberPoolHub = this.guid //passed into builderObject.Build implicitly
      SetupNumberPools()
      accessor = context.actorOf(RandomPool(25).props(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystem.AllocateNumberPoolActors(guid))), s"$Id-uns")
      ground = context.actorOf(Props(classOf[ZoneGroundActor], this, equipmentOnGround), s"$Id-ground")
      deployables = context.actorOf(Props(classOf[ZoneDeployableActor], this, constructions), s"$Id-deployables")
      transport = context.actorOf(Props(classOf[ZoneVehicleActor], this, vehicles), s"$Id-vehicles")
      population = context.actorOf(Props(classOf[ZonePopulationActor], this, players, corpses), s"$Id-players")

      BuildLocalObjects(context, guid)
      BuildSupportObjects()
      MakeBuildings(context)
      AssignAmenities()
      CreateSpawnGroups()
    }
  }

  def SetupNumberPools() : Unit = {
    guid.AddPool("environment", (0 to 3000).toList) //TODO tailor to suit requirements of zone
    //TODO unlump pools later; do not make any single pool too big
    guid.AddPool("dynamic", (3001 to 10000).toList).Selector = new RandomSelector //TODO all things will be registered here, for now
    guid.AddPool("b", (10001 to 15000).toList).Selector = new RandomSelector
    guid.AddPool("c", (15001 to 20000).toList).Selector = new RandomSelector
    guid.AddPool("d", (20001 to 25000).toList).Selector = new RandomSelector
    guid.AddPool("e", (25001 to 30000).toList).Selector = new RandomSelector
    guid.AddPool("f", (30001 to 35000).toList).Selector = new RandomSelector
    guid.AddPool("g", (35001 until 40100).toList).Selector = new RandomSelector
    guid.AddPool("projectiles", (Projectile.BaseUID until Projectile.RangeUID).toList)
    //TODO disabled temporarily to lighten load times
    //guid.AddPool("h", (40150 to 45000).toList).Selector = new RandomSelector
    //guid.AddPool("i", (45001 to 50000).toList).Selector = new RandomSelector
    //guid.AddPool("j", (50001 to 55000).toList).Selector = new RandomSelector
    //guid.AddPool("k", (55001 to 60000).toList).Selector = new RandomSelector
    //guid.AddPool("l", (60001 to 65535).toList).Selector = new RandomSelector
  }

  /**
    * A reference to the primary `Actor` that governs this `Zone`.
    * @return an `ActorRef`
    * @see `ZoneActor`<br>
    *      `Zone.Init`
    */
  def Actor : ActorRef = actor

  /**
    * Give this `Zone` an `Actor` that will govern its interactions sequentially.
    * @param zoneActor an `ActorRef` for this `Zone`;
    *                  will not overwrite any existing governance unless `noSender`
    * @return an `ActorRef`
    * @see `ZoneActor`
    */
  def Actor_=(zoneActor : ActorRef) : ActorRef = {
    if(actor == ActorRef.noSender) {
      actor = zoneActor
    }
    Actor
  }

  /**
    * The privileged name that can be used as the second parameter in the packet `LoadMapMessage`.
    * @return the name
    */
  def Id : String = zoneId

  /**
    * The map of server objects upon which this `Zone` is based
    * @return the map
    */
  def Map : ZoneMap = zoneMap

  /**
    * The numerical index of the `Zone` as it is recognized in a variety of packets.
    * @return the abstract index position of this `Zone`
    */
  def Number : Int = zoneNumber

  /**
    * The globally unique identifier system is synchronized via an `Actor` to ensure that concurrent requests do not clash.
    * A clash is merely when the same number is produced more than once by the same system due to concurrent requests.
    * @return synchronized reference to the globally unique identifier system
    */
  def GUID : ActorRef = accessor

  /**
    * Replace the current globally unique identifier system with a new one.
    * The replacement will not occur if the current system is populated or if its synchronized reference has been created.
    * The primary use of this function should be testing.
    * A warning will be issued.
    * @return synchronized reference to the globally unique identifier system
    */
  def GUID(hub : NumberPoolHub) : Boolean = {
    if(actor == ActorRef.noSender && guid.Pools.map({case ((_, pool)) => pool.Count}).sum == 0) {
      import org.fusesource.jansi.Ansi.Color.RED
      import org.fusesource.jansi.Ansi.ansi
      println(ansi().fgBright(RED).a(s"""Caution: replacement of the number pool system for zone $Id; function is for testing purposes only""").reset())
      guid = hub
      true
    }
    else {
      false
    }
  }

  /**
    * Wraps around the globally unique identifier system to insert a new number pool.
    * Throws exceptions for specific reasons if the pool can not be populated before the system has been started.
    * @see `NumberPoolHub.AddPool`
    * @param name the name of the pool
    * @param pool the numbers that will belong to the pool
    * @return `true`, if the new pool is created;
    *        `false`, if the new pool can not be created because the system has already been started
    */
  def AddPool(name : String, pool : Seq[Int]) : Boolean = {
    if(accessor == ActorRef.noSender) {
      guid.AddPool(name, pool.toList)
      true
    }
    else {
      false
    }
  }

  /**
    * Wraps around the globally unique identifier system to remove an existing number pool.
    * Throws exceptions for specific reasons if the pool can not be removed before the system has been started.
    * @see `NumberPoolHub.RemovePool`
    * @param name the name of the pool
    * @return `true`, if the new pool is un-made;
    *        `false`, if the new pool can not be removed because the system has already been started
    */
  def RemovePool(name : String) : Boolean = {
    if(accessor == ActorRef.noSender) {
      guid.RemovePool(name)
      true
    }
    else {
      false
    }
  }

  /**
    * Recover an object from the globally unique identifier system by the number that was assigned previously.
    * @param object_guid the globally unique identifier requested
    * @return the associated object, if it exists
    * @see `GUID(Int)`
    */
  def GUID(object_guid : Option[PlanetSideGUID]) : Option[PlanetSideGameObject] = {
    object_guid match {
      case Some(oguid) =>
        GUID(oguid.guid)
      case None =>
        None
    }
  }

  /**
    * Recover an object from the globally unique identifier system by the number that was assigned previously.
    * @param object_guid the globally unique identifier requested
    * @return the associated object, if it exists
    * @see `GUID(Int)`
    */
  def GUID(object_guid : PlanetSideGUID) : Option[PlanetSideGameObject] = GUID(object_guid.guid)

  /**
    * Recover an object from the globally unique identifier system by the number that was assigned previously.
    * The object must be upcast into due to the differtence between the storage type and the return type.
    * @param object_guid the globally unique identifier requested
    * @return the associated object, if it exists
    * @see `NumberPoolHub(Int)`
    */
  def GUID(object_guid : Int) : Option[PlanetSideGameObject] = guid(object_guid) match {
    case Some(obj) =>
      Some(obj.asInstanceOf[PlanetSideGameObject])
    case None =>
      None
  }

  /**
    * The `List` of items (`Equipment`) dropped by players on the ground and can be collected again.
    * @return the `List` of `Equipment`
    */
  def EquipmentOnGround : List[Equipment] = equipmentOnGround.toList

  def DeployableList : List[PlanetSideGameObject with Deployable] = constructions.toList

  def Vehicles : List[Vehicle] = vehicles.toList

  def Players : List[Avatar] = players.keys.toList

  def LivePlayers : List[Player] = players.values.collect( { case Some(tplayer) => tplayer }).toList

  def Corpses : List[Player] = corpses.toList

  /**
    * Coordinate `Equipment` that has been dropped on the ground or to-be-dropped on the ground.
    * @return synchronized reference to the ground
    * @see `ZoneGroundActor`<br>
    *      `Zone.DropItemOnGround`<br>
    *      `Zone.GetItemOnGround`<br>
    *      `Zone.ItemFromGround`
    */
  def Ground : ActorRef = ground

  def Deployables : ActorRef = deployables

  def Transport : ActorRef = transport

  def Population : ActorRef = population

  def Buildings : Map[Int, Building] = buildings

  def Building(id : Int) : Option[Building] = {
    buildings.get(id)
  }

  private def BuildLocalObjects(implicit context : ActorContext, guid : NumberPoolHub) : Unit = {
    Map.LocalObjects.foreach({ builderObject => builderObject.Build })
  }

  private def BuildSupportObjects() : Unit = {
    //guard against errors here, but don't worry about specifics; let ZoneActor.ZoneSetupCheck complain about problems
    //turret to weapon
    Map.TurretToWeapon.foreach({ case ((turret_guid, weapon_guid)) =>
      ((GUID(turret_guid) match {
        case Some(obj : FacilityTurret) =>
          Some(obj)
        case _ => ;
          None
      }) match {
        case Some(obj) =>
          obj.Weapons.get(1) match {
            case Some(slot) =>
              Some(obj, slot.Equipment)
            case None =>
              None
          }
        case None =>
          None
      }) match {
        case Some((obj, Some(weapon : Tool))) =>
          guid.register(weapon, weapon_guid)
          weapon.AmmoSlots.foreach(slot => guid.register(slot.Box, "dynamic"))
          obj.Inventory.Items.foreach(item => guid.register(item.obj, "dynamic")) //internal ammunition reserves, if any
        case _ => ;
      }
    })
  }

  private def MakeBuildings(implicit context : ActorContext) : PairMap[Int, Building] = {
    val buildingList = Map.LocalBuildings
    buildings = buildingList.map({case(building_id, constructor) => building_id -> constructor.Build(building_id, this) })
    buildings
  }

  private def AssignAmenities() : Unit = {
    Map.ObjectToBuilding.foreach({ case(object_guid, building_id) =>
      (buildings.get(building_id), guid(object_guid)) match {
        case (Some(building), Some(amenity)) =>
          building.Amenities = amenity.asInstanceOf[Amenity]
        case (None, _) | (_, None) => ; //let ZoneActor's sanity check catch this error
      }
    })
  }

  private def CreateSpawnGroups() : Unit = {
    buildings.values
      .filterNot { _.Position == Vector3.Zero }
      .map(building => { building -> building.Amenities.collect { case(obj : SpawnTube) => obj } })
      .filter( { case((_, spawns)) => spawns.nonEmpty })
      .foreach { SpawnGroups }
  }

  def SpawnGroups() : Map[Building, List[SpawnTube]] = spawnGroups

  def SpawnGroups(building : Building) : List[SpawnTube] = SpawnGroups(building.Id)

  def SpawnGroups(buildingId : Int) : List[SpawnTube] = {
    spawnGroups.find({ case((building, _)) => building.Id == buildingId }) match {
      case Some((_, list)) =>
        list
      case None =>
        List.empty[SpawnTube]
    }
  }

  def SpawnGroups(spawns : (Building, List[SpawnTube])) : Map[Building, List[SpawnTube]] = {
    val (building, tubes) = spawns
    val entry : Map[Building, List[SpawnTube]] = PairMap(building -> tubes)
    spawnGroups = spawnGroups ++ entry
    entry
  }

  /**
    * Provide bulk correspondence on all map entities that can be composed into packet messages and reported to a client.
    * These messages are sent in this fashion at the time of joining the server:<br>
    * - `BuildingInfoUpdateMessage`<br>
    * - `DensityLevelUpdateMessage`<br>
    * - `BroadcastWarpgateUpdateMessage`<br>
    * - `CaptureFlagUpdateMessage`<br>
    * - `ContinentalLockUpdateMessage`<br>
    * - `ModuleLimitsMessage`<br>
    * - `VanuModuleUpdateMessage`<br>
    * - `ZoneForcedCavernConnectionMessage`<br>
    * - `ZoneInfoMessage`<br>
    * - `ZoneLockInfoMessage`<br>
    * - `ZonePopulationUpdateMessage`
    * @return the `Zone` object
    */
  def ClientInitialization() : Zone = this

  def VehicleEvents : ActorRef = vehicleEvents

  def VehicleEvents_=(bus : ActorRef) : ActorRef = {
    if(vehicleEvents == ActorRef.noSender) {
      vehicleEvents = bus
    }
    VehicleEvents
  }
}

object Zone {
  /** Default value, non-zone area. */
  final val Nowhere : Zone = new Zone("nowhere", new ZoneMap("nowhere"), 99)

  /**
    * Overloaded constructor.
    * @param id the privileged name that can be used as the second parameter in the packet `LoadMapMessage`
    * @param map the map of server objects upon which this `Zone` is based
    * @param number the numerical index of the `Zone` as it is recognized in a variety of packets
    * @return a `Zone` object
    */
  def apply(id : String, map : ZoneMap, number : Int) : Zone = {
    new Zone(id, map, number)
  }

  /**
    * Message to initialize the `Zone`.
    * @see `Zone.Init(implicit ActorContext)`
    */
  final case class Init()

  object Population {
    /**
      * Message that introduces a user, by their `Avatar`, into a `Zone`.
      * That user will be counted as part of that zone's population.
      * The `avatar` may associate `Player` objects with itself in the future.
      * @param avatar the `Avatar` object
      */
    final case class Join(avatar : Avatar)
    /**
      * Message that excuses a user, by their `Avatar`, into a `Zone`.
      * That user will not longer be counted as part of that zone's population.
      * @see `PlayerHasLeft`
      * @param avatar the `Avatar` object
      */
    final case class Leave(avatar : Avatar)
    /**
      * Message that instructs the zone to disassociate a `Player` from this `Actor`.
      * @see `PlayerAlreadySpawned`<br>
      *       `PlayerCanNotSpawn`
      * @param avatar the `Avatar` object
      * @param player the `Player` object
      */
    final case class Spawn(avatar : Avatar, player : Player)
    /**
      * Message that instructs the zone to disassociate a `Player` from this `Actor`.
      * @see `PlayerHasLeft`
      * @param avatar the `Avatar` object
      */
    final case class Release(avatar : Avatar)
    /**
      * Message that acts in reply to `Leave(avatar)` or `Release(avatar)`.
      * In the former case, the avatar will have successfully left the zone, and `player` may be defined.
      * In the latter case, the avatar did not initially `Join` the zone, and `player` is `None`.
      * This message should not be considered a failure or a success case.
      * @see `Release`<br>
      *       `Leave`
      * @param zone the `Zone` object
      * @param player the `Player` object
      */
    final case class PlayerHasLeft(zone : Zone, player : Option[Player]) //Leave(avatar), but still has a player
    /**
      * Message that acts in reply to `Spawn(avatar, player)`, but the avatar already has a player.
      * @param player the `Player` object
      */
    final case class PlayerAlreadySpawned(zone : Zone, player : Player)
    /**
      * Message that acts in reply to `Spawn(avatar, player)`, but the avatar did not initially `Join` this zone.
      * @param zone the `Zone` object
      * @param player the `Player` object
      */
    final case class PlayerCanNotSpawn(zone : Zone, player : Player)
  }

  object Corpse {
    /**
      * Message that reports to the zone of a freshly dead player.
      * @param player the dead `Player`
      */
    final case class Add(player : Player)
    /**
      * Message that tells the zone to no longer mind the dead player.
      * @param player the dead `Player`
      */
    final case class Remove(player : Player)
  }

  object Lattice {
    /**
      * Message requesting that the current zone determine where a `player` can spawn.
      * @param zone_number this zone's numeric identifier
      * @param player the `Player` object
      * @param spawn_group the category of spawn points the request wants searched
      */
    final case class RequestSpawnPoint(zone_number : Int, player : Player, spawn_group : Int)
    /**
      * Message that returns a discovered spawn point to a request source.
      * @param zone_id the zone's text identifier
      * @param spawn_tube the spawn point holding object
      */
    final case class SpawnPoint(zone_id : String, spawn_tube : SpawnTube)
    /**
      * Message that informs a request source that a spawn point could not be discovered with the previous criteria.
      * @param zone_number this zone's numeric identifier
      * @param spawn_group the spawn point holding object;
      *                    if `None`, then the previous `zone_number` could not be found;
      *                    otherwise, no spawn points could be found in the zone
      */
    final case class NoValidSpawnPoint(zone_number : Int, spawn_group : Option[Int])
  }

  object Ground {
    final case class DropItem(item : Equipment, pos : Vector3, orient : Vector3)
    final case class ItemOnGround(item : Equipment, pos : Vector3, orient : Vector3)
    final case class CanNotDropItem(zone : Zone, item : Equipment, reason : String)

    final case class PickupItem(item_guid : PlanetSideGUID)
    final case class ItemInHand(item : Equipment)
    final case class CanNotPickupItem(zone : Zone, item_guid : PlanetSideGUID, reason : String)

    final case class RemoveItem(item_guid : PlanetSideGUID)
  }

  object Deployable {
    final case class Build(obj : PlanetSideGameObject with Deployable, withTool : ConstructionItem)
    final case class DeployableIsBuilt(obj : PlanetSideGameObject with Deployable, withTool : ConstructionItem)

    final case class Dismiss(obj : PlanetSideGameObject with Deployable)
    final case class DeployableIsDismissed(obj : PlanetSideGameObject with Deployable)
  }

  object Vehicle {
    final case class Spawn(vehicle : Vehicle)

    final case class Despawn(vehicle : Vehicle)

    final case class CanNotSpawn(zone : Zone, vehicle : Vehicle, reason : String)

    final case class CanNotDespawn(zone : Zone, vehicle : Vehicle, reason : String)
  }

  /**
    * Message to report the packet messages that initialize the client.
    * @param zone a `Zone` to have its buildings and continental parameters turned into packet data
    * @see `Zone.ClientInitialization()`<br>
    *      `InterstallarCluster`
    */
  final case class ClientInitialization(zone : Zone)

  object EquipmentIs {
    /**
      * Tha base `trait` connecting all `Equipment` object location tokens.
      */
    sealed trait ItemLocation
    /**
      * The target item is contained within another object.
      * @see `GridInventory`<br>
      *       `Container`
      * @param obj the containing object
      * @param index the slot where the target is located
      */
    final case class InContainer(obj : Container, index : Int) extends ItemLocation
    /**
      * The target item is found on the Ground.
      * @see `ZoneGroundActor`
      */
    final case class OnGround() extends ItemLocation
    /**
      * The target item exists but could not be found belonging to any expected region of the location.
      */
    final case class Orphaned() extends ItemLocation

    /**
      * An exhaustive search of the provided zone is conducted in search of the target `Equipment` object
      * and a token that qualifies the current location of the object in the zone is returned.
      * The following groups of objects are searched:
      * the inventories of all players and all corpses,
      * all vehicles trunks,
      * the lockers of all players and corpses;
      * and, if still not found, the ground is scoured too.
      * @see `ItemLocation`<br>
      *       `LockerContainer`
      * @param equipment the target object
      * @param guid that target object's globally unique identifier
      * @param continent the zone whose objects to search
      * @return a token that explains where the object is, if it is found in this zone;
      *         `None` is the token that is used to indicate not having been found
      */
    def Where(equipment : Equipment, guid : PlanetSideGUID, continent : Zone) : Option[Zone.EquipmentIs.ItemLocation] = {
      continent.GUID(guid) match {
        case Some(_) =>
          ((continent.LivePlayers ++ continent.Corpses).find(_.Find(guid).nonEmpty) match {
            case Some(tplayer) => Some((tplayer, tplayer.Find(guid)))
            case _ => None
          }).orElse(continent.Vehicles.find(_.Find(guid).nonEmpty) match {
            case Some(vehicle) => Some((vehicle, vehicle.Find(guid)))
            case _ => None
          }).orElse(continent.Players.find(_.Locker.Find(guid).nonEmpty) match {
            case Some(avatar) => Some((avatar.Locker, avatar.Locker.Find(guid)))
            case _ => None
          }) match {
            case Some((obj, Some(index))) =>
              Some(Zone.EquipmentIs.InContainer(obj, index))
            case _ =>
              continent.EquipmentOnGround.find(_.GUID == guid) match {
                case Some(_) =>
                  Some(Zone.EquipmentIs.OnGround())
                case None =>
                  Some(Zone.EquipmentIs.Orphaned())
              }
          }
        case None =>
          None
      }
    }
  }
}

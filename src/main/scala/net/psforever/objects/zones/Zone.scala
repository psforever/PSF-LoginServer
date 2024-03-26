// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects._
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.ce.{Deployable, DeployableCategory}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.{NumberPoolHub, UniqueNumberOps, UniqueNumberSetup}
import net.psforever.objects.guid.key.LoanedKey
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.painbox.{Painbox, PainboxDefinition}
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures._
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.serverobject.zipline.ZipLinePath
import net.psforever.types._
import org.log4s.Logger
import net.psforever.services.avatar.AvatarService
import net.psforever.services.local.LocalService
import net.psforever.services.vehicle.VehicleService

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.{Map => PairMap}
import scala.concurrent.duration._
import scalax.collection.Graph
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._

import scala.util.Try
import akka.actor.typed
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.zone.ZoneActor
import net.psforever.actors.zone.building.WarpGateLogic
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.geometry.d3.VolumetricGeometry
import net.psforever.objects.guid.pool.NumberPool
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.environment.EnvironmentAttribute
import net.psforever.objects.serverobject.interior.{InteriorAware, Sidedness}
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalMech
import net.psforever.objects.serverobject.shuttle.OrbitalShuttlePad
import net.psforever.objects.serverobject.terminals.{ProximityTerminal, Terminal}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vehicles.{MountedWeapons, UtilityType}
import net.psforever.objects.vital.etc.ExplodingEntityReason
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.prop.DamageWithPosition
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.blockmap.{BlockMap, SectorPopulation}
import net.psforever.services.Service
import net.psforever.zones.Zones

import scala.annotation.tailrec
import scala.collection.mutable
import scala.concurrent.{Future, Promise}

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
  *
  * @param id         the privileged name that can be used as the second parameter in the packet `LoadMapMessage`
  * @param map        the map of server objects upon which this `Zone` is based
  * @param zoneNumber the numerical index of the `Zone` as it is recognized in a variety of packets;
  *                   also used by `LivePlayerList` to indicate a specific `Zone`
  * @see `ZoneMap`<br>
  *      `LoadMapMessage`<br>
  *      `LivePlayerList`
  */
class Zone(val id: String, val map: ZoneMap, zoneNumber: Int) {

  /** Governs general synchronized external requests. */
  var actor: typed.ActorRef[ZoneActor.Command] = _

  /** Actor that handles SOI related functionality, for example if a player is in an SOI */
  private var soi = Default.Actor

  /** The basic support structure for the globally unique number system used by this `Zone`. */
  private var guid: NumberPoolHub = new NumberPoolHub(new MaxNumberSource(65536))
  /** The core of the unique number system, to which requests may be submitted.
    * @see `UniqueNumberSys`
    * @see `Zone.Init(ActorContext)`
    */
  private[zones] var unops: UniqueNumberOps = _

  /** The blockmap structure for partitioning entities and environmental aspects of the zone.
    * For a standard 8912`^`2 map, each of the four hundred formal map grids is 445.6m long and wide.
    * A `desiredSpanSize` of 100m divides the blockmap into 8100 sectors.
    * A `desiredSpanSize` of 50m divides the blockmap into 32041 sectors.
    */
  val blockMap: BlockMap = BlockMap(map.scale, desiredSpanSize = 100)

  /** A synchronized `List` of items (`Equipment`) dropped by players on the ground and can be collected again. */
  private val equipmentOnGround: ListBuffer[Equipment] = ListBuffer[Equipment]()

  /**
    */
  private val vehicles: ListBuffer[Vehicle] = ListBuffer[Vehicle]()

  /** Used by the `Zone` to coordinate `Equipment` dropping and collection requests. */
  private var ground: ActorRef = Default.Actor

  /**
    */
  private val constructions: ListBuffer[Deployable] = ListBuffer()

  /**
    */
  private var deployables: ActorRef = Default.Actor

  /**
    */
  private var transport: ActorRef = Default.Actor

  /**
    */
  private val players: TrieMap[Int, Option[Player]] = TrieMap[Int, Option[Player]]()

  /**
    */
  private val corpses: ListBuffer[Player] = ListBuffer[Player]()

  private var projectiles: ActorRef = Default.Actor
  private val projectileList: ListBuffer[Projectile] = ListBuffer[Projectile]()

  /**
    */
  private var population: ActorRef = Default.Actor

  private var buildings: PairMap[Int, Building] = PairMap.empty[Int, Building]

  private var lattice: Graph[Building, UnDiEdge] = Graph()

  /** key - spawn zone id, value - buildings belonging to spawn zone */
  private var spawnGroups: Map[Building, List[SpawnPoint]] = PairMap[Building, List[SpawnPoint]]()

  /**
    */
  private var projector: ActorRef = Default.Actor

  /**
    */
  private val hotspots: ListBuffer[HotSpotInfo] = ListBuffer[HotSpotInfo]()

  /**
    */
  private val hotspotHistory: ListBuffer[HotSpotInfo] = ListBuffer[HotSpotInfo]()

  /** calculate a approximated coordinate from a raw input coordinate */
  private var hotspotCoordinateFunc: Vector3 => Vector3 = Zone.HotSpot.Rules.OneToOne

  /** calculate a duration from a given interaction's participants */
  private var hotspotTimeFunc: (SourceEntry, SourceEntry) => FiniteDuration = Zone.HotSpot.Rules.NoTime

  private val linkDynamicTurretWeapon: mutable.HashMap[Int, Int] = mutable.HashMap[Int, Int]()

  /**
    */
  private var avatarEvents: ActorRef = Default.Actor

  /**
    */
  private var localEvents: ActorRef = Default.Actor

  /**
    */
  private var vehicleEvents: ActorRef = Default.Actor

  /**
    * When the zone has completed initializing, fulfill this promise.
    * @see `init(ActorContext)`
    */
  private var zoneInitialized: Promise[Boolean] = Promise[Boolean]()

  /**
    * When the zone has completed initializing, this will be the future.
    * @see `init(ActorContext)`
    */
  def ZoneInitialized(): Future[Boolean] = zoneInitialized.future

  /**
    * Establish the basic accessible conditions necessary for a functional `Zone`.<br>
    * <br>
    * Called from the `Actor` that governs this `Zone` when it is passed a constructor reference to the `Zone`.
    * Specifically, the order of calling follows: `InterstellarCluster.preStart -> ZoneActor.receive(Zone.Init()) -> Zone.Init`.
    * The basic method performs three main operations.
    * First, the `Actor`-driven aspect of the globally unique identifier system for this `Zone` is finalized.
    * Second, all supporting `Actor` agents are created, e.g., `ground`.
    * Third, the `ZoneMap` server objects are loaded and constructed within that aforementioned system.
    * To avoid being called more than once, there is a test whether the globally unique identifier system has been changed.<br>
    * <br>
    * Execution of this operation should be fail-safe.
    * The chances of failure should be mitigated or skipped.
    * A testing routine should be run after the fact on the results of the process.
    *
    * @see `ZoneActor.ZoneSetupCheck`
    * @param context a reference to an `ActorContext` necessary for `Props`
    */
  def init(implicit context: ActorContext): Unit = {
    if (unops == null) {
      SetupNumberPools()
      context.actorOf(Props(classOf[UniqueNumberSys], this, this.guid), s"zone-$id-uns")
      ground = context.actorOf(Props(classOf[ZoneGroundActor], this, equipmentOnGround), s"zone-$id-ground")
      deployables = context.actorOf(Props(classOf[ZoneDeployableActor], this, constructions, linkDynamicTurretWeapon), s"zone-$id-deployables")
      projectiles = context.actorOf(Props(classOf[ZoneProjectileActor], this, projectileList), s"zone-$id-projectiles")
      transport = context.actorOf(Props(classOf[ZoneVehicleActor], this, vehicles, linkDynamicTurretWeapon), s"zone-$id-vehicles")
      population = context.actorOf(Props(classOf[ZonePopulationActor], this, players, corpses), s"zone-$id-players")
      projector = context.actorOf(
        Props(classOf[ZoneHotSpotDisplay], this, hotspots, 15 seconds, hotspotHistory, 60 seconds),
        s"zone-$id-hotspots"
      )
      soi = context.actorOf(Props(classOf[SphereOfInfluenceActor], this), s"zone-$id-soi")

      avatarEvents = context.actorOf(Props(classOf[AvatarService], this), s"zone-$id-avatar-events")
      localEvents = context.actorOf(Props(classOf[LocalService], this), s"zone-$id-local-events")
      vehicleEvents = context.actorOf(Props(classOf[VehicleService], this), s"zone-$id-vehicle-events")

      implicit val guid: NumberPoolHub = this.guid //passed into builderObject.Build implicitly
      BuildLocalObjects(context, guid)
      BuildSupportObjects()
      MakeBuildings(context)
      MakeLattice()
      AssignAmenities()
      CreateSpawnGroups()
      PopulateBlockMap()
      validate()

      zoneInitialized.success(true)
    }
  }

  def validate(): Unit = {
    implicit val log: Logger = org.log4s.getLogger(s"zone/$id/sanity")

    //check bases
    map.objectToBuilding.values
      .toSet[Int]
      .foreach(building_id => {
        val target = Building(building_id)
        if (target.isEmpty) {
          log.error(s"expected a building for id #$building_id")
        } else if (!target.get.HasGUID) {
          log.error(s"building #$building_id was not registered")
        }
      })

    //check base to object associations
    map.objectToBuilding.keys.foreach(object_guid =>
      if (guid(object_guid).isEmpty) {
        log.error(s"expected object id $object_guid to exist, but it did not")
      }
    )

    //check door to lock association
    map.doorToLock.foreach({
      case (doorGuid, lockGuid) =>
        validateObject(doorGuid, (x: PlanetSideGameObject) => x.isInstanceOf[serverobject.doors.Door], "door")
        validateObject(lockGuid, (x: PlanetSideGameObject) => x.isInstanceOf[serverobject.locks.IFFLock], "IFF lock")
    })

    //check vehicle terminal to spawn pad association
    map.terminalToSpawnPad.foreach({
      case (termGuid, padGuid) =>
        validateObject(
          termGuid,
          (x: PlanetSideGameObject) => x.isInstanceOf[serverobject.terminals.Terminal],
          "vehicle terminal"
        )
        validateObject(
          padGuid,
          (x: PlanetSideGameObject) => x.isInstanceOf[serverobject.pad.VehicleSpawnPad],
          "vehicle spawn pad"
        )
    })

    //check implant terminal mech to implant terminal interface association
    map.terminalToInterface.foreach({
      case (mechGuid, interfaceGuid) =>
        validateObject(
          mechGuid,
          (x: PlanetSideGameObject) => x.isInstanceOf[ImplantTerminalMech],
          "implant terminal mech"
        )
        validateObject(
          interfaceGuid,
          (o: PlanetSideGameObject) => o.isInstanceOf[serverobject.terminals.Terminal],
          "implant terminal interface"
        )
    })

    //check manned turret to weapon association
    map.turretToWeapon.foreach({
      case (turretGuid, weaponGuid) =>
        validateObject(
          turretGuid,
          (o: PlanetSideGameObject) => o.isInstanceOf[serverobject.turret.FacilityTurret],
          "facility turret mount"
        )
        if (
          validateObject(
            weaponGuid,
            (o: PlanetSideGameObject) => o.isInstanceOf[net.psforever.objects.Tool],
            "facility turret weapon"
          )
        ) {
          if (GUID(weaponGuid).get.asInstanceOf[Tool].AmmoSlots.count(!_.Box.HasGUID) > 0) {
            log.error(s"expected weapon $weaponGuid has an unregistered ammunition unit")
          }
        }
    })
  }

  /**
    * Recover an object from a collection and perform any number of validating tests upon it.
    * If the object fails any tests, log an error.
    *
    * @param objectGuid  the unique indentifier being checked against the `guid` access point
    * @param test        a test for the discovered object;
    *                    expects at least `Type` checking
    * @param description an explanation of how the object, if not discovered, should be identified
    * @return `true` if the object was discovered and validates correctly;
    *         `false` if the object failed any tests
    */
  def validateObject(
      objectGuid: Int,
      test: PlanetSideGameObject => Boolean,
      description: String
  )(implicit log: Logger): Boolean = {
    try {
      if (!test(GUID(objectGuid).get)) {
        log.error(s"expected id $objectGuid to be a $description, but it was not")
        false
      } else {
        true
      }
    } catch {
      case e: Exception =>
        log.error(s"expected a $description at id $objectGuid but no object is initialized - $e")
        false
    }
  }

  def SetupNumberPools(): Unit = { /* override to tailor to suit requirements of zone */ }

  def findSpawns(
      faction: PlanetSideEmpire.Value,
      spawnGroups: Seq[SpawnGroup]
  ): List[(AmenityOwner, Iterable[SpawnPoint])] = {
    val ams = spawnGroups.contains(SpawnGroup.AMS)
    val structures = spawnGroups.collect {
      case SpawnGroup.Facility => StructureType.Facility
      case SpawnGroup.Tower => StructureType.Tower
      case SpawnGroup.WarpGate => StructureType.WarpGate
      case SpawnGroup.Sanctuary => StructureType.Building
    }
    SpawnGroups()
      .collect {
        case (building, spawns)
          if (building match {
            case warpGate: WarpGate => warpGate.Faction == faction || warpGate.Broadcast(faction)
            case _ => building.Faction == faction
          }) &&
            structures.contains(building.BuildingType) &&
            spawns.nonEmpty &&
            spawns.exists(_.isOffline == false) =>
          (building, spawns.filter(!_.isOffline))
      }
      .concat(
        (if (ams) Vehicles else List())
          .filter(vehicle =>
            vehicle.Definition == GlobalDefinitions.ams &&
              !vehicle.Destroyed &&
              vehicle.DeploymentState == DriveState.Deployed &&
              vehicle.Faction == faction
          )
          .map(vehicle =>
            (
              vehicle,
              vehicle.Utilities.values
                .filter(util => util.UtilType == UtilityType.ams_respawn_tube)
                .map(_().asInstanceOf[SpawnTube])
            )
          )
      )
      .toList
  }

  def findNearestSpawnPoints(
      faction: PlanetSideEmpire.Value,
      location: Vector3,
      spawnGroups: Seq[SpawnGroup]
  ): Option[List[SpawnPoint]] = {
    findSpawns(faction, spawnGroups)
      .sortBy {
        case (spawn, _) =>
          Vector3.DistanceSquared(location, spawn.Position.xy)
      }
      .collectFirst {
        case (_, spawnPoints) if spawnPoints.nonEmpty =>
          spawnPoints.toList
      }
  }

  /**
    * The numerical index of the `Zone` as it is recognized in a variety of packets.
    *
    * @return the abstract index position of this `Zone`
    */
  def Number: Int = zoneNumber

  /**
    * The globally unique identifier system ensures that concurrent requests do not clash.
    * A clash is merely when the same number is produced more than once by the same system due to concurrent requests.
    * @return reference to the globally unique identifier system
    */
  def GUID: UniqueNumberOps = unops

  /**
    * Replace the current globally unique identifier support structure with a new one.
    * The replacement will not occur if the current system is populated or if its synchronized reference has been created.
    * The primary use of this function should be testing.
    * A warning will be issued.
    * @return synchronized reference to the globally unique identifier system
    */
  def GUID(hub: NumberPoolHub): Boolean = {
    if (actor == null && guid.Pools.values.foldLeft(0)(_ + _.Count) == 0) {
      import org.fusesource.jansi.Ansi.Color.RED
      import org.fusesource.jansi.Ansi.ansi
      println(
        ansi()
          .fgBright(RED)
          .a(s"""Caution: replacement of the number pool system for zone $id; function is for testing purposes only""")
          .reset()
      )
      guid = hub
      true
    } else {
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
  def AddPool(name: String, pool: Seq[Int]): Option[NumberPool] = {
    if (unops == null) {
      guid.AddPool(name, pool.toList) match {
        case _: Exception => None
        case out => Some(out)
      }
    } else {
      None
    }
  }

  def GetEntities(definition: ObjectDefinition): List[PlanetSideGameObject] = {
    GetEntities(List(definition))
  }

  def GetEntities(definitions: List[ObjectDefinition]): List[PlanetSideGameObject] = {
    definitions
      .distinct
      .groupBy(_.registerAs)
      .flatMap { case (registerName, defs) =>
        GetEntities(registerName)
          .filter(obj => defs.contains(obj.Definition))
      }
      .toList
  }

  def GetEntities(name: String): List[PlanetSideGameObject] = {
    guid
      .GetPool(name)
      .map { pool =>
        pool
          .Numbers
          .flatMap(guid.apply(_))
          .collect { case obj: PlanetSideGameObject => obj }
      }
      .getOrElse(List[PlanetSideGameObject]())
  }

  /**
    * Wraps around the globally unique identifier system to remove an existing number pool.
    * Throws exceptions for specific reasons if the pool can not be removed before the system has been started.
    * @see `NumberPoolHub.RemovePool`
    * @param name the name of the pool
    * @return `true`, if the pool is un-made;
    *        `false`, if the pool can not be removed (because the system has already been started?)
    */
  def RemovePool(name: String): Boolean = {
    if (unops == null) {
      guid.RemovePool(name).nonEmpty
    } else {
      false
    }
  }

  /**
    * Recover an object from the globally unique identifier system by the number that was assigned previously.
    * @param object_guid the globally unique identifier requested
    * @return the associated object, if it exists
    * @see `GUID(Int)`
    */
  def GUID(object_guid: Option[PlanetSideGUID]): Option[PlanetSideGameObject] = {
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
  def GUID(object_guid: PlanetSideGUID): Option[PlanetSideGameObject] = GUID(object_guid.guid)

  /**
    * Recover an object from the globally unique identifier system by the number that was assigned previously.
    * The object must be upcast into due to the minor difference between the storage type and the return type.
    * @param object_guid the globally unique identifier requested
    * @return the associated object, if it exists
    * @see `NumberPoolHub(Int)`
    */
  def GUID(object_guid: Int): Option[PlanetSideGameObject] =
    guid(object_guid) match {
      case Some(obj) =>
        Some(obj.asInstanceOf[PlanetSideGameObject])
      case None =>
        None
    }

  /**
    * The `List` of items (`Equipment`) dropped by players on the ground and can be collected again.
    * @return the `List` of `Equipment`
    */
  def EquipmentOnGround: List[Equipment] = equipmentOnGround.toList

  def DeployableList: List[Deployable] = constructions.toList

  def Vehicles: List[Vehicle] = vehicles.toList

  def Players: List[Avatar] = players.values.flatten.map(_.avatar).toList

  def LivePlayers: List[Player] = players.values.flatten.toList

  def Corpses: List[Player] = corpses.toList

  /**
    * Coordinate `Equipment` that has been dropped on the ground or to-be-dropped on the ground.
    * @return synchronized reference to the ground
    * @see `ZoneGroundActor`<br>
    *      `Zone.DropItemOnGround`<br>
    *      `Zone.GetItemOnGround`<br>
    *      `Zone.ItemFromGround`
    */
  def Ground: ActorRef = ground

  def Deployables: ActorRef = deployables

  def Projectile: ActorRef = projectiles

  def Projectiles: List[Projectile] = projectileList.toList

  def Transport: ActorRef = transport

  def Population: ActorRef = population

  def Buildings: Map[Int, Building] = buildings

  def Building(id: Int): Option[Building] = {
    buildings.get(id)
  }

  def Building(name: String): Option[Building] = {
    buildings.values.find(_.Name == name)
  }

  def BuildingByMapId(map_id: Int): Option[Building] = {
    buildings.values.find(_.MapId == map_id)
  }

  def Lattice: Graph[Building, UnDiEdge] = {
    lattice
  }

  def AddIntercontinentalLatticeLink(bldgA: Building, bldgB: Building): Graph[Building, UnDiEdge] = {
    if ((this eq bldgA.Zone) && (bldgA.Zone ne bldgB.Zone)) {
      lattice ++= Set(bldgA ~ bldgB)
    }
    Lattice
  }

  def RemoveIntercontinentalLatticeLink(bldgA: Building, bldgB: Building): Graph[Building, UnDiEdge] = {
    if ((this eq bldgA.Zone) && (bldgA.Zone ne bldgB.Zone)) {
      lattice --= Set(bldgA ~ bldgB)
    }
    Lattice
  }

  def zipLinePaths: List[ZipLinePath] = {
    map.zipLinePaths
  }

  private def BuildLocalObjects(implicit context: ActorContext, guid: NumberPoolHub): Unit = {
    map.localObjects.foreach({ builderObject =>
      builderObject.Build

      val obj = guid(builderObject.Id)
      obj collect {
        case el: ZoneAware => el.Zone = this
      }
    })
  }

  private def BuildSupportObjects(): Unit = {
    //guard against errors here, but don't worry about specifics; let ZoneActor.ZoneSetupCheck complain about problems
    val other: ListBuffer[PlanetSideGameObject] = new ListBuffer[PlanetSideGameObject]()
    //turret to weapon
    map.turretToWeapon.foreach({
      case (turret_guid, weapon_guid) =>
        ((GUID(turret_guid) match {
          case Some(obj: FacilityTurret) =>
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
          case Some((obj, Some(weapon: Tool))) =>
            guid.register(weapon, weapon_guid)
            other ++= weapon.AmmoSlots.map(slot => slot.Box)
            other ++= obj.Inventory.Items.map(item => item.obj) //internal ammunition reserves, if any
          case _ => ;
        }
    })
    //after all fixed GUID's are defined  ...
    other.foreach(obj => guid.register(obj, obj.Definition.registerAs))
  }

  private def MakeBuildings(implicit context: ActorContext): PairMap[Int, Building] = {
    val buildingList = map.localBuildings
    val registrationKeys: Map[Int, Try[LoanedKey]] = buildingList.map {
      case ((_, building_guid: Int, _), _) =>
        (building_guid, guid.register(building_guid))
    }
    buildings = buildingList.map({
      case ((name, building_guid, map_id), constructor) if registrationKeys(building_guid).isSuccess =>
        val building = constructor.Build(name, building_guid, map_id, this)
        registrationKeys(building_guid).get.Object = building
        building_guid -> building
    })
    buildings
  }

  private def AssignAmenities(): Unit = {
    map.objectToBuilding.foreach({
      case (object_guid, building_id) =>
        (buildings.get(building_id), guid(object_guid)) match {
          case (Some(building), Some(amenity: Amenity)) =>
            building.Amenities = amenity
          case (Some(_), _) | (None, _) | (_, None) => () //let ZoneActor's sanity check catch this error
        }
    })
    Zone.AssignOutwardSideToDoors(zone = this)
    Zone.AssignSidednessToAmenities(zone = this)
    //ntu management (eventually move to a generic building startup function)
    buildings.values
      .flatMap(_.Amenities.filter(_.Definition == GlobalDefinitions.resource_silo))
      .collect {
        case silo: ResourceSilo =>
          silo.Actor ! Service.Startup()
      }
    //some painfields need to look for their closest door
    buildings.values
      .flatMap(_.Amenities.filter(_.Definition.isInstanceOf[PainboxDefinition]))
      .collect {
        case painbox: Painbox =>
          painbox.Actor ! Service.Startup()
      }
    //the orbital_buildings in sanctuary zones have to establish their shuttle routes
    map.shuttleBays
      .map { guid(_) }
      .collect { case Some(obj: OrbitalShuttlePad) =>
        obj.Actor ! Service.Startup()
      }
    //allocate soi information
    soi ! SOI.Build()
  }

  private def MakeLattice(): Unit = {
    lattice ++= map.latticeLink
      .filterNot {
        case (a, _) => a.contains("/") //ignore intercontinental lattice connections
      }
      .map {
        case (source, target) =>
          val (sourceBuilding, targetBuilding) = (Building(source), Building(target)) match {
            case (Some(sBuilding), Some(tBuilding)) => (sBuilding, tBuilding)
            case _ =>
              throw new NoSuchElementException(s"Zone $id - can't create lattice link between $source and $target.")
          }
          sourceBuilding ~ targetBuilding
      }
  }

  private def CreateSpawnGroups(): Unit = {
    buildings.values
      .filterNot { _.Position == Vector3.Zero }
      .map(building => { building -> building.Amenities.collect { case obj: SpawnPoint => obj } })
      .filter({ case (_, spawns) => spawns.nonEmpty })
      .foreach { SpawnGroups }

    buildings.values
      .filterNot { _.Position == Vector3.Zero }
      .collect { case building: WarpGate => building -> List(building.asInstanceOf[SpawnPoint]) }
      .foreach { SpawnGroups }
  }

  def SpawnGroups(): Map[Building, List[SpawnPoint]] = spawnGroups

  def SpawnGroups(building: Building): List[SpawnPoint] = SpawnGroups(building.MapId)

  def SpawnGroups(buildingId: Int): List[SpawnPoint] = {
    spawnGroups.find({ case (building, _) => building.MapId == buildingId }) match {
      case Some((_, list)) =>
        list
      case None =>
        List.empty[SpawnPoint]
    }
  }

  def SpawnGroups(spawns: (Building, List[SpawnPoint])): Map[Building, List[SpawnPoint]] = {
    val (building, points)                     = spawns
    val entry: Map[Building, List[SpawnPoint]] = PairMap(building -> points)
    spawnGroups = spawnGroups ++ entry
    entry
  }

  def PopulateBlockMap(): Unit = {
    vehicles.foreach { vehicle => blockMap.addTo(vehicle) }
    buildings.values.foreach { building =>
      blockMap.addTo(building)
      building.Amenities.foreach { amenity => blockMap.addTo(amenity) }
    }
    map.environment.foreach { env => blockMap.addTo(env) }
  }

  def StartPlayerManagementSystems(): Unit = {
    soi ! SOI.Start()
  }

  def StopPlayerManagementSystems(): Unit = {
    soi ! SOI.Stop()
  }

  def Activity: ActorRef = projector

  def HotSpots: List[HotSpotInfo] = hotSpotListDuplicate(hotspots).toList

  def HotSpotData: List[HotSpotInfo] = hotSpotListDuplicate(hotspotHistory).toList

  private def hotSpotListDuplicate(data: ListBuffer[HotSpotInfo]): ListBuffer[HotSpotInfo] = {
    val out = data map { info =>
      val outData = new HotSpotInfo(info.DisplayLocation)
      info.Activity.foreach {
        case (faction, report) =>
          val doctoredReport = outData.Activity(faction)
          doctoredReport.ReportOld(report.Heat)
          doctoredReport.SetLastReport(report.LastReport)
      }
      outData
    }
    out
  }

  def HotSpotCoordinateFunction: Vector3 => Vector3 = hotspotCoordinateFunc

  def HotSpotCoordinateFunction_=(func: Vector3 => Vector3): Vector3 => Vector3 = {
    hotspotCoordinateFunc = func
    Activity ! ZoneHotSpotProjector.UpdateMappingFunction()
    HotSpotCoordinateFunction
  }

  def HotSpotTimeFunction: (SourceEntry, SourceEntry) => FiniteDuration = hotspotTimeFunc

  def HotSpotTimeFunction_=(
      func: (SourceEntry, SourceEntry) => FiniteDuration
  ): (SourceEntry, SourceEntry) => FiniteDuration = {
    hotspotTimeFunc = func
    Activity ! ZoneHotSpotProjector.UpdateDurationFunction()
    HotSpotTimeFunction
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
  def ClientInitialization(): Zone = this

  def turretToWeapon: Map[Int, Int] = linkDynamicTurretWeapon.toMap[Int, Int] ++ map.turretToWeapon

  def AvatarEvents: ActorRef = avatarEvents

  def LocalEvents: ActorRef = localEvents

  def VehicleEvents: ActorRef = vehicleEvents

  //mainly for testing
  def Activity_=(bus: ActorRef): ActorRef = {
    projector = bus
    Activity
  }

  def AvatarEvents_=(bus: ActorRef): ActorRef = {
    avatarEvents = bus
    AvatarEvents
  }

  def LocalEvents_=(bus: ActorRef): ActorRef = {
    localEvents = bus
    LocalEvents
  }

  def VehicleEvents_=(bus: ActorRef): ActorRef = {
    vehicleEvents = bus
    VehicleEvents
  }
}

/**
  * A local class for spawning `Actor`s to manage the number pools for this zone,
  * create a number system operations class to access those pools within the context of registering and unregistering,
  * and assign that number pool operations class to the containing zone
  * through specific scope access.
  * @see `UniqueNumberOps`
  * @see `UniqueNumberSetup`
  * @see `UniqueNumberSetup.AllocateNumberPoolActors`
  * @see `Zone.unops`
  * @param zone the zone in which the operations class will be referenced
  * @param guid the number pool management class
  */
private class UniqueNumberSys(zone: Zone, guid: NumberPoolHub)
  extends UniqueNumberSetup(guid, UniqueNumberSetup.AllocateNumberPoolActors) {
  override def init(): UniqueNumberOps = {
    val unsys = super.init()
    zone.unops = unsys // zone.unops is accessible from here by virtue of being 'private[zones]`
    unsys
  }
}

object Zone {

  /** Default value, non-zone area. */
  final val Nowhere: Zone = new Zone("nowhere", new ZoneMap("nowhere"), 99)

  /**
    * Overloaded constructor.
    * @param id the privileged name that can be used as the second parameter in the packet `LoadMapMessage`
    * @param map the map of server objects upon which this `Zone` is based
    * @param number the numerical index of the `Zone` as it is recognized in a variety of packets
    * @return a `Zone` object
    */
  def apply(id: String, map: ZoneMap, number: Int): Zone = {
    new Zone(id, map, number)
  }

  private def AssignOutwardSideToDoors(zone: Zone): Unit = {
    //let ZoneActor's sanity check catch any missing entities
    //todo there are no doors in the training zones so we may skip that
    if (zone.map.cavern) {
      //todo what do?
      //almost all are type ancient_door and don't have many hints to determine outward-ness; there are no IFF locks
    } else if (
      PlanetSideEmpire.values
        .filterNot(_ == PlanetSideEmpire.NEUTRAL)
        .exists(fac => Zones.sanctuaryZoneNumber(fac) == zone.Number)
    ) {
      AssignOutwardSideToSanctuaryDoors(zone)
    } else {
      AssignOutwardSidetoContinentDoors(zone)
    }
  }

  private def AssignOutwardSideToSanctuaryDoors(zone: Zone): Unit = {
    val map = zone.map
    val guid = zone.guid
    AssignOutwardsToIFFLockedDoors(zone)
    //doors with IFF locks belong to towers and are always between; the locks are always outside
    map.doorToLock
      .map { case (door, lock) => (guid(door), guid(lock)) }
      .collect { case (Some(door: Door), Some(lock: IFFLock)) =>
        door.WhichSide = Sidedness.StrictlyBetweenSides
        lock.WhichSide = Sidedness.OutsideOf
      }
    //spawn building doors
    val buildings = zone.Buildings.values
    val amenityList = buildings
      .collect {
        case b
          if b.Definition.Name.startsWith("VT_building_") =>
          val amenities = b.Amenities
          (
            amenities.collect { case door: Door if door.Definition == GlobalDefinitions.gr_door_mb_ext => door },
            amenities.collect { case door: Door if door.Definition == GlobalDefinitions.gr_door_mb_lrg => door },
            amenities.filter(_.Definition == GlobalDefinitions.order_terminal),
            amenities.filter(_.Definition == GlobalDefinitions.respawn_tube_sanctuary)
          )
      }
    amenityList.foreach { case (entranceDoors, trainingRangeDoors, terminals, tubes) =>
      entranceDoors.foreach { door =>
        val doorPosition = door.Position
        val closestTerminal = terminals.minBy(t => Vector3.DistanceSquared(doorPosition, t.Position))
        val closestTube = tubes.minBy(t => Vector3.DistanceSquared(doorPosition, t.Position))
        door.WhichSide = Sidedness.StrictlyBetweenSides
        door.Outwards = Vector3.Unit(closestTerminal.Position.xy - closestTube.Position.xy)
      }
      //training zone warp doors
      val sampleDoor = entranceDoors.head.Position.xy;
      {
        val doorToDoorVector = Vector3.Unit(sampleDoor - entranceDoors(1).Position.xy)
        val (listADoors, listBDoors) = trainingRangeDoors
          .sortBy(door => Vector3.DistanceSquared(door.Position.xy, sampleDoor))
          .partition { door =>
            Vector3.ScalarProjection(doorToDoorVector, Vector3.Unit(door.Position.xy - sampleDoor)) > 0f
          }
       Seq(listADoors, listBDoors)
      }.foreach { doors =>
        val door0PosXY = doors.head.Position.xy
        val door1PosXY = doors(1).Position.xy
        val door2PosXY = doors(2).Position.xy
        val outwardsMiddle = Vector3.Unit((door0PosXY + door2PosXY) * 0.5f - door1PosXY)
        val center = door1PosXY + (outwardsMiddle * 19.5926f)
        doors.head.Outwards = Vector3.Unit(center - door0PosXY)
        doors(1).Outwards = outwardsMiddle
        doors(2).Outwards = Vector3.Unit(center - door2PosXY)
      }
    }
    //hart building doors
    buildings
      .collect {
        case b
          if b.Definition.Name.startsWith("orbital_building_") =>
          val amenities = b.Amenities
          (
            amenities.filter(_.Definition == GlobalDefinitions.gr_door_mb_ext),
            amenities.filter(_.Definition == GlobalDefinitions.gr_door_mb_orb)
          )
      }
      .foreach { case (entranceDoors, hartDoors) =>
        entranceDoors.foreach { door =>
          val isReallyADoor = door.asInstanceOf[Door]
          val doorPosition = door.Position
          val closestHartDoor = hartDoors.minBy(t => Vector3.DistanceSquared(doorPosition, t.Position))
          isReallyADoor.WhichSide = Sidedness.StrictlyBetweenSides
          isReallyADoor.Outwards = Vector3.Unit(doorPosition.xy - closestHartDoor.Position.xy)
        }
      }
  }

  private def AssignOutwardSidetoContinentDoors(zone: Zone): Unit = {
    val map = zone.map
    val guid = zone.guid
    AssignOutwardsToIFFLockedDoors(zone)
    val buildingsToDoors = zone.Buildings.values.map(b => (b, b.Amenities.collect { case d: Door => d })).toMap
    //external doors with IFF locks are always between and outside, respectively
    map.doorToLock
      .map { case (door, lock) => (guid(door), guid(lock)) }
      .collect { case (Some(door: Door), Some(lock: IFFLock))
        if door.Definition.environmentField.exists(f => f.attribute == EnvironmentAttribute.InteriorField) =>
        door.WhichSide = Sidedness.StrictlyBetweenSides
        lock.WhichSide = Sidedness.OutsideOf
      }
    //for major facilities, external doors in the courtyard are paired, connected by a passage between ground and walls
    //they are the only external doors that do not have iff locks
    buildingsToDoors
      .filter { case (b, _) => b.BuildingType == StructureType.Facility }
      .foreach { case (_, doors) =>
        var unpairedDoors = doors.collect {
          case d: Door
            if d.Definition == GlobalDefinitions.gr_door_ext && !map.doorToLock.contains(d.GUID.guid) =>
            d
        }
        var pairedDoors = Seq[(Door, Door)]()
        while (unpairedDoors.size > 1) {
          val sampleDoor = unpairedDoors.head
          val sampleDoorPosition = sampleDoor.Position.xy
          val distances = Float.MaxValue +: unpairedDoors
            .map(d => Vector3.DistanceSquared(d.Position.xy, sampleDoorPosition))
            .drop(1)
          val min = distances.min
          val indexOfClosestDoor = distances.indexWhere(_ == min)
          val otherDoor = unpairedDoors(indexOfClosestDoor)
          unpairedDoors = unpairedDoors.slice(1, indexOfClosestDoor) ++ unpairedDoors.drop(indexOfClosestDoor + 1)
          pairedDoors = pairedDoors :+ (sampleDoor, otherDoor)
        }
        pairedDoors.foreach { case (door1, door2) =>
          //give each paired courtyard door an outward-ness
          val outwards = Vector3.Unit(door1.Position.xy - door2.Position.xy)
          door1.Outwards = outwards
          door1.WhichSide = Sidedness.StrictlyBetweenSides
          door2.Outwards = Vector3.neg(outwards)
          door2.WhichSide = Sidedness.StrictlyBetweenSides
        }
      }
    //bunkers do not define a formal interior, so their doors are solely exterior
    buildingsToDoors
      .filter { case (b, _) => b.BuildingType == StructureType.Bunker }
      .foreach { case (_, doors) =>
        doors.foreach(_.WhichSide = Sidedness.OutsideOf)
      }
  }

  private def AssignOutwardsToIFFLockedDoors(zone: Zone): Unit = {
    val guid = zone.guid
    //doors with nearby locks use those locks as their unlocking mechanism and their outwards indication
    zone.map.doorToLock
      .map { case (doorGUID: Int, lockGUID: Int) => (guid(doorGUID), guid(lockGUID)) }
      .collect {
        case (Some(door: Door), Some(lock: IFFLock)) =>
          door.Outwards = lock.Outwards
          door.Actor ! Door.UpdateMechanism(IFFLock.testLock(lock))
        case _ => ()
      }
  }

  private def AssignSidednessToAmenities(zone: Zone): Unit = {
    //let ZoneActor's sanity check catch any missing entities
    //todo training zones, where everything is outside
    if (zone.map.cavern) {
      //todo what do?
      /*
      quite a few amenities are disconnected from buildings
      there are two orientations of terminal/spawn pad
      as aforementioned, door outwards and sidedness is not assignable at the moment
      */
    } else if (
      PlanetSideEmpire.values
        .filterNot(_ == PlanetSideEmpire.NEUTRAL)
        .exists(fac => Zones.sanctuaryZoneNumber(fac) == zone.Number)
    ) {
      AssignSidednessToSanctuaryAmenities(zone)
    } else {
      AssignSidednessToContinentAmenities(zone)
    }
  }

  private def AssignSidednessToSanctuaryAmenities(zone: Zone): Unit = {
    val map = zone.map
    val guid = zone.guid
    //only tower doors possess locks and those are always external
    map.doorToLock
      .map { case (_, lock) => guid(lock) }
      .collect {
        case Some(lock: IFFLock) =>
          lock.WhichSide = Sidedness.OutsideOf
      }
    //medical terminals are always inside
    zone.buildings
      .values
      .flatMap(_.Amenities)
      .collect {
        case pt: ProximityTerminal if pt.Definition == GlobalDefinitions.medical_terminal =>
          pt.WhichSide = Sidedness.InsideOf
      }
    //repair silos and landing pads have multiple components and all of these are outside
    //we have to search all terminal entities because the repair silos are not installed anywhere
    guid
      .GetPool(name = "terminals")
      .map(_.Numbers.flatMap(number => guid(number)))
      .getOrElse(List())
      .collect {
        case pt: ProximityTerminal
          if pt.Definition == GlobalDefinitions.repair_silo =>
          val guid = pt.GUID.guid
          Seq(guid, guid + 1, guid + 2, guid + 3)
        case pt: ProximityTerminal
          if pt.Definition.Name.startsWith("pad_landing_") =>
          val guid = pt.GUID.guid
          Seq(guid, guid + 1, guid + 2)
      }
      .flatten[Int]
      .map(guid(_))
      .collect {
        case Some(pt: ProximityTerminal) =>
          pt.WhichSide = Sidedness.OutsideOf
      }
    //the following terminals are installed outside
    map.terminalToSpawnPad
      .keys
      .flatMap(guid(_))
      .collect {
        case terminal: Terminal =>
          terminal.WhichSide = Sidedness.OutsideOf
      }
  }

  private def AssignSidednessToContinentAmenities(zone: Zone): Unit = {
    val map = zone.map
    val guid = zone.guid
    val buildingsMap = zone.buildings.values
    //door locks on external doors are also external while the door is merely "between"; all other locks are internal
    map.doorToLock
      .map { case (door, lock) => (guid(door), guid(lock))}
      .collect {
        case (Some(door: Door), Some(lock: IFFLock))
          if door.Definition.environmentField.exists(f => f.attribute == EnvironmentAttribute.InteriorField) =>
          lock.WhichSide = Sidedness.OutsideOf
      }
    //medical terminals are always inside
    buildingsMap
      .flatMap(_.Amenities)
      .collect {
        case pt: ProximityTerminal
          if pt.Definition == GlobalDefinitions.medical_terminal || pt.Definition == GlobalDefinitions.adv_med_terminal =>
          pt.WhichSide = Sidedness.InsideOf
      }
    //repair silos and landing pads have multiple components and all of these are outside
    buildingsMap
      .flatMap(_.Amenities)
      .collect {
        case pt: ProximityTerminal
          if pt.Definition == GlobalDefinitions.repair_silo =>
          val guid = pt.GUID.guid
          Seq(guid, guid + 1, guid + 2, guid + 3)
        case pt: ProximityTerminal
          if pt.Definition.Name.startsWith("pad_landing_") =>
          val guid = pt.GUID.guid
          Seq(guid, guid + 1, guid + 2)
      }
      .toSeq
      .flatten[Int]
      .map(guid(_))
      .collect {
        case Some(pt: ProximityTerminal) =>
          pt.WhichSide = Sidedness.OutsideOf
      }
    //all vehicle spawn pads are outside, save for the ground vehicle pad in the tech plants
    buildingsMap.collect {
      case b
        if b.Definition == GlobalDefinitions.tech_plant =>
        b.Amenities
          .collect { case pad: VehicleSpawnPad => pad }
          .minBy(_.Position.z)
          .WhichSide = Sidedness.InsideOf
    }
    //all vehicle terminals are outside of their owning facilities in the courtyard
    //the only exceptions are vehicle terminals in tech plants and the dropship center air terminal
    map.terminalToSpawnPad
      .keys
      .flatMap(guid(_))
      .collect {
        case terminal: Terminal
          if terminal.Definition != GlobalDefinitions.dropship_vehicle_terminal &&
            terminal.Owner.asInstanceOf[Building].Definition != GlobalDefinitions.tech_plant =>
          terminal.WhichSide = Sidedness.OutsideOf
      }
  }

  object Population {

    /**
      * Message that introduces a user, by their `Avatar`, into a `Zone`.
      * That user will be counted as part of that zone's population.
      * The `avatar` may associate `Player` objects with itself in the future.
      * @param avatar the `Avatar` object
      */
    final case class Join(avatar: Avatar)

    /**
      * Message that excuses a user, by their `Avatar`, into a `Zone`.
      * That user will not longer be counted as part of that zone's population.
      * @see `PlayerHasLeft`
      * @param avatar the `Avatar` object
      */
    final case class Leave(avatar: Avatar)

    /**
      * Message that instructs the zone to disassociate a `Player` from this `Actor`.
      *
      * @see `PlayerAlreadySpawned`<br>
      *      `PlayerCanNotSpawn`
      * @param avatar the `Avatar` object
      * @param player the `Player` object
      */
    final case class Spawn(avatar: Avatar, player: Player, avatarActor: typed.ActorRef[AvatarActor.Command])

    /**
      * Message that instructs the zone to disassociate a `Player` from this `Actor`.
      * @see `PlayerHasLeft`
      * @param avatar the `Avatar` object
      */
    final case class Release(avatar: Avatar)

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
    final case class PlayerHasLeft(zone: Zone, player: Option[Player]) //Leave(avatar), but still has a player
    /**
      * Message that acts in reply to `Spawn(avatar, player)`, but the avatar already has a player.
      * @param player the `Player` object
      */
    final case class PlayerAlreadySpawned(zone: Zone, player: Player)

    /**
      * Message that acts in reply to `Spawn(avatar, player)`, but the avatar did not initially `Join` this zone.
      * @param zone the `Zone` object
      * @param player the `Player` object
      */
    final case class PlayerCanNotSpawn(zone: Zone, player: Player)
  }

  object Corpse {

    /**
      * Message that reports to the zone of a freshly dead player.
      * @param player the dead `Player`
      */
    final case class Add(player: Player)

    /**
      * Message that tells the zone to no longer mind the dead player.
      * @param player the dead `Player`
      */
    final case class Remove(player: Player)
  }

  object Ground {
    final case class DropItem(item: Equipment, pos: Vector3, orient: Vector3)
    final case class ItemOnGround(item: Equipment, pos: Vector3, orient: Vector3)
    final case class CanNotDropItem(zone: Zone, item: Equipment, reason: String)

    final case class PickupItem(item_guid: PlanetSideGUID)
    final case class ItemInHand(item: Equipment)
    final case class CanNotPickupItem(zone: Zone, item_guid: PlanetSideGUID, reason: String)

    final case class RemoveItem(item_guid: PlanetSideGUID)
  }

  object Deployable {
    final case class Build(obj: Deployable)
    final case class BuildByOwner(obj: Deployable, owner: Player, withTool: ConstructionItem)
    final case class Setup()
    final case class IsBuilt(obj: Deployable)
    final case class CanNotBeBuilt(obj: Deployable, withTool: ConstructionItem)

    final case class Dismiss(obj: Deployable)
    final case class IsDismissed(obj: Deployable)
  }

  object Vehicle {
    final case class Spawn(vehicle: Vehicle)

    final case class Despawn(vehicle: Vehicle)

    final case class HasSpawned(zone: Zone, vehicle: Vehicle)

    final case class HasDespawned(zone: Zone, vehicle: Vehicle)

    final case class CanNotSpawn(zone: Zone, vehicle: Vehicle, reason: String)

    final case class CanNotDespawn(zone: Zone, vehicle: Vehicle, reason: String)
  }

  object HotSpot {
    trait Activity {
      def defender: SourceEntry

      def attacker: SourceEntry

      def location: Vector3
    }

    final case class Conflict(defender: SourceEntry, attacker: SourceEntry, location: Vector3) extends Activity

    final case class NonEvent() extends Activity {
      def defender: SourceEntry = SourceEntry.None

      def attacker: SourceEntry = SourceEntry.None

      def location: Vector3 = Vector3.Zero
    }

    object Activity {
      def apply(data: DamageResult): Activity = {
        data.adversarial match {
          case Some(adversity) => Conflict(adversity.defender, adversity.attacker, data.interaction.hitPos)
          case None => NonEvent()
        }
      }

      def apply(defender: SourceEntry, attacker: SourceEntry, location: Vector3): Activity =
        Conflict(defender, attacker, location)
    }

    final case class Cleanup()

    final case class ClearAll()

    final case class Update(faction: PlanetSideEmpire.Value, zone_num: Int, priority: Int, info: List[HotSpotInfo])

    final case class UpdateNow()

    object Rules {

      /**
        * Produce hotspot coordinates based on map coordinates.
        * Return the same coordinate as output that was input.
        * The default function.
        * @param pos the absolute position of the activity reported
        * @return the position for a hotspot
        */
      def OneToOne(pos: Vector3): Vector3 = pos

      /**
        * Determine a duration for which the hotspot will be displayed on the zone map.
        * The default function.
        * @param defender the defending party
        * @param attacker the attacking party
        * @return the duration
        */
      def NoTime(defender: SourceEntry, attacker: SourceEntry): FiniteDuration = 0 seconds
    }
  }

  /**
    * Message to report the packet messages that initialize the client.
    * @param zone a `Zone` to have its buildings and continental parameters turned into packet data
    * @see `Zone.ClientInitialization()`<br>
    *      `InterstallarCluster`
    */
  final case class ClientInitialization(zone: Zone)

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
    final case class InContainer(obj: Container, index: Int) extends ItemLocation

    final case class Mounted(obj: MountedWeapons, index: Int) extends ItemLocation

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
     * the lockers of all players and corpses,
     * all vehicles's weapon mounts and trunks,
     * all weapon-mounted deployables's mounted turrets,
     * all facilities's natural mounted turrets;
     * and, if still not found, the ground is scoured too;
     * and, if still not found after that, it __shouldn't__ exist (in this zone).
      * @see `ItemLocation`
      * @see `LockerContainer`
      * @param equipment the target object
      * @param guid that target object's globally unique identifier
      * @param continent the zone whose objects to search
      * @return a token that explains where the object is, if it is found in this zone;
      *         `None` is the token that is used to indicate not having been found
      */
    def Where(equipment: Equipment, guid: PlanetSideGUID, continent: Zone): Option[Zone.EquipmentIs.ItemLocation] = {
      continent.GUID(guid) match {
        case Some(_) =>
          ((continent.LivePlayers ++ continent.Corpses).find(_.Find(guid).nonEmpty) match {
            case Some(tplayer) => Some((tplayer, tplayer.Find(guid)))
            case _             => None
          }).orElse(continent.Vehicles.find(_.Find(guid).nonEmpty) match {
            case Some(vehicle) => Some((vehicle, vehicle.Find(guid)))
            case _             => None
          }).orElse(continent.Players.find(_.locker.Find(guid).nonEmpty) match {
            case Some(avatar) => Some((avatar.locker, avatar.locker.Find(guid)))
            case _            => None
          }).orElse({
            (continent.DeployableList.filter( d => d.Definition.DeployCategory == DeployableCategory.FieldTurrets ) ++
              continent.DeployableList.filter( d => d.Definition.DeployCategory == DeployableCategory.SmallTurrets )).find {
              case w: MountedWeapons => w.Weapons.values.flatMap(_.Equipment).exists(_ eq equipment)
              case _                 => false
            } match {
              case Some(deployable) => Some((deployable, Some(0)))
              case _                => None
            }
          }).orElse({
            continent.Buildings.values
              .flatMap(_.Amenities).find {
              case w: MountedWeapons => w.Weapons.values.flatMap(_.Equipment).exists(_ eq equipment)
              case _                 => false
            } match {
              case Some(turret) => Some((turret.asInstanceOf[MountedWeapons], Some(0)))
              case _            => None
            }
          })
          match {
            case Some((obj: MountedWeapons, Some(index))) =>
              Some(Zone.EquipmentIs.Mounted(obj, index))
            case Some((obj: Container, Some(index))) =>
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

  /**
    * Starting from an overworld zone facility,
    * find a lattice connected cavern facility that is the same faction as this starting building.
    * Except for the necessary examination of the major facility on the other side of a warp gate pair,
    * do not let the search escape the current zone into another.
    * If we start in a cavern zone, do not continue a fruitless search;
    * just fail.
    * @return the discovered faction-aligned cavern facility
    */
  def findConnectedCavernFacility(building: Building): Option[Building] = {
    if (building.Zone.map.cavern) {
      None
    } else {
      val neighbors = building.AllNeighbours.getOrElse(Set.empty[Building]).toList
      recursiveFindConnectedCavernFacility(building.Faction, neighbors.headOption, neighbors.drop(1), Set(building.MapId))
    }
  }

  /**
    * Starting from an overworld zone facility,
    * find a lattice connected cavern facility that is the same faction as this starting building.
    * Except for the necessary examination of the major facility on the other side of a warp gate pair,
    * do not let the search escape the current zone into another.
    * @param currBuilding the proposed current facility to check
    * @param nextNeighbors the facilities that are yet to be searched
    * @param visitedNeighbors the facilities that have been searched already
    * @return the discovered faction-aligned cavern facility
    */
  @tailrec
  private def recursiveFindConnectedCavernFacility(
                                                    sampleFaction: PlanetSideEmpire.Value,
                                                    currBuilding: Option[Building],
                                                    nextNeighbors: List[Building],
                                                    visitedNeighbors: Set[Int]
                                                  ): Option[Building] = {
    if(currBuilding.isEmpty) {
      None
    } else {
      val building = currBuilding.head
      if (!visitedNeighbors.contains(building.MapId)
          && (building match {
        case wg: WarpGate => wg.Faction == sampleFaction || wg.Broadcast(sampleFaction)
        case _            => building.Faction == sampleFaction
      })
          && !building.CaptureTerminalIsHacked
          && building.NtuLevel > 0
          && (building.Generator match {
        case Some(o) => o.Condition != PlanetSideGeneratorState.Destroyed
        case _       => true
      })
      ) {
        (building match {
          case wg: WarpGate => traverseWarpGateInSearchOfOwnedCavernFaciity(sampleFaction, wg)
          case _ => None
        }) match {
          case out @ Some(_) =>
            out
          case _ =>
            val newVisitedNeighbors = visitedNeighbors ++ Set(building.MapId)
            val newNeighbors = nextNeighbors ++ building.AllNeighbours
              .getOrElse(Set.empty[Building])
              .toList
              .filterNot { b => newVisitedNeighbors.contains(b.MapId) }
            recursiveFindConnectedCavernFacility(
              sampleFaction,
              newNeighbors.headOption,
              newNeighbors.drop(1),
              newVisitedNeighbors
            )
        }
      } else {
        recursiveFindConnectedCavernFacility(
          sampleFaction,
          nextNeighbors.headOption,
          nextNeighbors.drop(1),
          visitedNeighbors ++ Set(building.MapId)
        )
      }
    }
  }

  /**
    * Trace the extended neighborhood of the warp gate to a cavern facility that has the same faction affinity.
    * @param faction the faction that all connected factions must have affinity with
    * @param target the warp gate from which to conduct a local search
    * @return if discovered, the first faction affiliated facility in a connected cavern
    */
  private def traverseWarpGateInSearchOfOwnedCavernFaciity(
                                                            faction: PlanetSideEmpire.Value,
                                                            target: WarpGate
                                                          ): Option[Building] = {
    WarpGateLogic.findNeighborhoodWarpGate(target.Neighbours.getOrElse(Nil)) match {
      case Some(gate) if gate.Zone.map.cavern =>
        WarpGateLogic.findNeighborhoodNormalBuilding(gate.Neighbours(faction).getOrElse(Nil))
      case _ =>
        None
    }
  }

  /**
    * Allocates `Damageable` targets within the vicinity of server-prepared damage dealing
    * and informs those entities that they have affected by the aforementioned damage.
    * Usually, this is considered an "explosion;" but, the application can be utilized for a variety of unbound damage.
    * @param zone the zone in which the damage should occur
    * @param source the entity that embodies the damage (information)
    * @param createInteraction how the interaction for this damage is to prepared
    * @param testTargetsFromZone a custom test for determining whether the allocated targets are affected by the damage
    * @param acquireTargetsFromZone the main target-collecting algorithm
    * @return a list of affected entities;
    *         only mostly complete due to the exclusion of objects whose damage resolution is different than usual
    */
  def serverSideDamage(
                        zone: Zone,
                        source: PlanetSideGameObject with FactionAffinity with Vitality,
                        createInteraction: (PlanetSideGameObject with FactionAffinity with Vitality, PlanetSideGameObject with FactionAffinity with Vitality) => DamageInteraction,
                        testTargetsFromZone: (PlanetSideGameObject, PlanetSideGameObject, Float) => Boolean = distanceCheck,
                        acquireTargetsFromZone: (Zone, PlanetSideGameObject with FactionAffinity with Vitality, DamageWithPosition) => List[PlanetSideServerObject with Vitality] = findAllTargets
                    ): List[PlanetSideServerObject] = {
    source.Definition.innateDamage match {
      case Some(damage) =>
        serverSideDamage(zone, source, damage, createInteraction, testTargetsFromZone, acquireTargetsFromZone)
      case None =>
        Nil
    }
  }

  /**
    * Allocates `Damageable` targets within the vicinity of server-prepared damage dealing
    * and informs those entities that they have affected by the aforementioned damage.
    * Usually, this is considered an "explosion;" but, the application can be utilized for a variety of unbound damage.
    * @see `DamageInteraction`
    * @see `DamageResult`
    * @see `DamageWithPosition`
    * @see `Vitality.Damage`
    * @see `Vitality.DamageOn`
    * @see `VitalityDefinition`
    * @see `VitalityDefinition.innateDamage`
    * @see `Zone.LocalEvents`
    * @param zone the zone in which the damage should occur
    * @param source the entity that embodies the damage (information)
    * @param createInteraction how the interaction for this damage is to prepared
    * @param testTargetsFromZone a custom test for determining whether the allocated targets are affected by the damage
    * @param acquireTargetsFromZone the main target-collecting algorithm
    * @return a list of affected entities;
    *         only mostly complete due to the exclusion of objects whose damage resolution is different than usual
    */
  def serverSideDamage(
                        zone: Zone,
                        source: PlanetSideGameObject with FactionAffinity with Vitality,
                        properties: DamageWithPosition,
                        createInteraction: (PlanetSideGameObject with FactionAffinity with Vitality, PlanetSideGameObject with FactionAffinity with Vitality) => DamageInteraction,
                        testTargetsFromZone: (PlanetSideGameObject, PlanetSideGameObject, Float) => Boolean,
                        acquireTargetsFromZone: (Zone, PlanetSideGameObject with FactionAffinity with Vitality, DamageWithPosition) => List[PlanetSideServerObject with Vitality]
                      ): List[PlanetSideServerObject] = {
    //collect targets that can be damaged
    val pssos = acquireTargetsFromZone(zone, source, properties)
    val radius = properties.DamageRadius * properties.DamageRadius
    //restrict to targets according to the detection plan
    val allAffectedTargets = pssos.filter { target => testTargetsFromZone(source, target, radius) }
    //inform remaining targets that they have suffered damage
    allAffectedTargets
      .foreach { target => target.Actor ! Vitality.Damage(createInteraction(source, target).calculate()) }
    allAffectedTargets
  }

  /**
   * na
   * @param source na
   * @param damageProperties na
   * @param targets na
   * @return na
   */
  def allOnSameSide(
                     source: PlanetSideGameObject,
                     damageProperties: DamageWithPosition,
                     targets: List[PlanetSideServerObject with Vitality]
                   ): List[PlanetSideServerObject with Vitality] = {
    source match {
      case awareSource: InteriorAware if !damageProperties.DamageThroughWalls =>
        allOnSameSide(awareSource.WhichSide, targets)
      case _ if !damageProperties.DamageThroughWalls =>
        val sourcePosition = source.Position
        targets
          .sortBy(t => Vector3.DistanceSquared(sourcePosition, t.Position))
          .collectFirst { case awareSource: InteriorAware => allOnSameSide(awareSource.WhichSide, targets) }
          .getOrElse(targets)
      case _ =>
        targets
    }
  }

  /**
   * na
   * @param side na
   * @param targets na
   * @return na
   */
  def allOnSameSide(
                     side: Sidedness,
                     targets: List[PlanetSideServerObject with Vitality]
                   ): List[PlanetSideServerObject with Vitality] = {
    targets.flatMap {
      case awareTarget: InteriorAware if !Sidedness.equals(side, awareTarget.WhichSide) => None
      case anyTarget => Some(anyTarget)
    }
  }

  /**
    * na
    * @see `DamageWithPosition`
    * @see `Zone.blockMap.sector`
    * @param zone   the zone in which the explosion should occur
    * @param source a game entity that is treated as the origin and is excluded from results
    * @param damagePropertiesBySource information about the effect/damage
    * @return a list of affected entities
    */
  def findAllTargets(
                      zone: Zone,
                      source: PlanetSideGameObject with Vitality,
                      damagePropertiesBySource: DamageWithPosition
                    ): List[PlanetSideServerObject with Vitality] = {
    findAllTargets(
      zone,
      source,
      source.Position,
      damagePropertiesBySource,
      damagePropertiesBySource.DamageRadius,
      getAllTargets
    )
  }

  /**
    * na
    * @see `DamageWithPosition`
    * @see `Zone.blockMap.sector`
    * @param zone   the zone in which the explosion should occur
    * @param sourcePosition a custom position that is used as the origin of the explosion;
    *                       not necessarily related to source
    * @param source a game entity that is treated as the origin and is excluded from results
    * @param damagePropertiesBySource information about the effect/damage
    * @return a list of affected entities
    */
  def findAllTargets(
                      zone: Zone,
                      source: PlanetSideGameObject with Vitality,
                      sourcePosition: Vector3,
                      damagePropertiesBySource: DamageWithPosition
                    ): List[PlanetSideServerObject with Vitality] = {
    findAllTargets(
      zone,
      source,
      sourcePosition,
      damagePropertiesBySource,
      damagePropertiesBySource.DamageRadius,
      getAllTargets
    )
  }

  /**
    * na
    * @see `DamageWithPosition`
    * @see `Zone.blockMap.sector`
    * @param zone   the zone in which the explosion should occur
    * @param sourcePosition a position that is used as the origin of the explosion
    * @param damagePropertiesBySource information about the effect/damage
    * @param getTargetsFromSector get this list of entities from a sector
    * @return a list of affected entities
    */
  def findAllTargets(
                      zone: Zone,
                      source: PlanetSideGameObject with Vitality,
                      sourcePosition: Vector3,
                      damagePropertiesBySource: DamageWithPosition,
                      radius: Float,
                      getTargetsFromSector: SectorPopulation => List[PlanetSideServerObject with Vitality]
                    ): List[PlanetSideServerObject with Vitality] = {
    allOnSameSide(
      source,
      damagePropertiesBySource,
      findAllTargets(zone, sourcePosition, radius, getTargetsFromSector).filter { target => target ne source }
    )
  }

  def findAllTargets(
                      sector: SectorPopulation,
                      source: PlanetSideGameObject with Vitality,
                      damagePropertiesBySource: DamageWithPosition,
                      getTargetsFromSector: SectorPopulation => List[PlanetSideServerObject with Vitality]
                    ): List[PlanetSideServerObject with Vitality] = {
    allOnSameSide(
      source,
      damagePropertiesBySource,
      getTargetsFromSector(sector)
    )
  }

  /**
   * na
   * @see `DamageWithPosition`
   * @see `Zone.blockMap.sector`
   * @param zone   the zone in which the explosion should occur
   * @param sourcePosition a position that is used as the origin of the explosion
   * @param radius idistance
   * @param getTargetsFromSector get this list of entities from a sector
   * @return a list of affected entities
   */
  def findAllTargets(
                      zone: Zone,
                      sourcePosition: Vector3,
                      radius: Float,
                      getTargetsFromSector: SectorPopulation => List[PlanetSideServerObject with Vitality]
                    ): List[PlanetSideServerObject with Vitality] = {
    getTargetsFromSector(zone.blockMap.sector(sourcePosition.xy, radius))
  }

  def getAllTargets(sector: SectorPopulation): List[PlanetSideServerObject with Vitality] = {
    //collect all targets that can be damaged
    //players
    val playerTargets = sector.livePlayerList.filterNot { _.VehicleSeated.nonEmpty }
    //vehicles
    val vehicleTargets = sector.vehicleList.filterNot { v => v.Destroyed || v.MountedIn.nonEmpty }
    //deployables
    val deployableTargets = sector.deployableList.filterNot { _.Destroyed }
    //amenities
    val soiTargets = sector.amenityList.collect { case amenity: Vitality if !amenity.Destroyed => amenity }
    //altogether ...
    playerTargets ++ vehicleTargets ++ deployableTargets ++ soiTargets
  }

  /**
    * na
    * @param instigation what previous event happened, if any, that caused this explosion
    * @param source a game object that represents the source of the explosion
    * @param target a game object that is affected by the explosion
    * @return a `DamageInteraction` object
    */
  def explosionDamage(
                       instigation: Option[DamageResult]
                     )
                     (
                       source: PlanetSideGameObject with FactionAffinity with Vitality,
                       target: PlanetSideGameObject with FactionAffinity with Vitality
                     ): DamageInteraction = {
    explosionDamage(instigation, target.Position)(source, target)
  }

  /**
    * na
    * @param instigation what previous event happened, if any, that caused this explosion
    * @param explosionPosition the coordinates of the detected explosion
    * @param source a game object that represents the source of the explosion
    * @param target a game object that is affected by the explosion
    * @return a `DamageInteraction` object
    */
  def explosionDamage(
                       instigation: Option[DamageResult],
                       explosionPosition: Vector3
                     )
                     (
                       source: PlanetSideGameObject with FactionAffinity with Vitality,
                       target: PlanetSideGameObject with FactionAffinity with Vitality
                     ): DamageInteraction = {
    DamageInteraction(
      SourceEntry(target),
      ExplodingEntityReason(source, target.DamageModel, instigation),
      explosionPosition
    )
  }

  /**
    * Two game entities are considered "near" each other if they are within a certain distance of one another.
    * A default function literal mainly used for `serverSideDamage`.
    * @see `ObjectDefinition.Geometry`
    * @see `serverSideDamage`
    * @param obj1 a game entity, should be the source of the damage
    * @param obj2 a game entity, should be the target of the damage
    * @param maxDistance the square of the maximum distance permissible between game entities
    *                    before they are no longer considered "near"
    * @return `true`, if the two entities are near enough to each other;
    *        `false`, otherwise
    */
  def distanceCheck(obj1: PlanetSideGameObject, obj2: PlanetSideGameObject, maxDistance: Float): Boolean = {
    distanceCheck(obj1.Definition.Geometry(obj1), obj2.Definition.Geometry(obj2), maxDistance)
  }

  /**
    * Two game entities are considered "near" each other if they are within a certain distance of one another.
    * @param g1 the geometric representation of a game entity
    * @param g2 the geometric representation of a game entity
    * @param maxDistance    the square of the maximum distance permissible between game entities
    *                       before they are no longer considered "near"
    * @return `true`, if the target entities are near enough to each other;
    *        `false`, otherwise
    */
  private def distanceCheck(g1: VolumetricGeometry, g2: VolumetricGeometry, maxDistance: Float): Boolean = {
    Vector3.DistanceSquared(g1.center.asVector3, g2.center.asVector3) <= maxDistance ||
    distanceCheck(g1, g2) <= maxDistance
  }
  /**
    * Two game entities are considered "near" each other if they are within a certain distance of one another.
    * @see `PrimitiveGeometry.pointOnOutside`
    * @see `Vector3.DistanceSquared`
    * @see `Vector3.neg`
    * @see `Vector3.Unit`
    * @param g1 the geometric representation of a game entity
    * @param g2 the geometric representation of a game entity
    * @return the crude distance between the two geometric representations
    */
  def distanceCheck(g1: VolumetricGeometry, g2: VolumetricGeometry): Float = {
    val dir = Vector3.Unit(g2.center.asVector3 - g1.center.asVector3)
    val point1 = g1.pointOnOutside(dir).asVector3
    val point2 = g2.pointOnOutside(Vector3.neg(dir)).asVector3
    Vector3.DistanceSquared(point1, point2)
  }
}

// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.{ActorContext, ActorRef, Props}
import akka.routing.RandomPool
import net.psforever.objects.ballistics.{Projectile, SourceEntry}
import net.psforever.objects._
import net.psforever.objects.ce.Deployable
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.{NumberPoolHub, TaskResolver}
import net.psforever.objects.guid.actor.UniqueNumberSystem
import net.psforever.objects.guid.key.LoanedKey
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.painbox.{Painbox, PainboxDefinition}
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{Amenity, AmenityOwner, Building, StructureType, WarpGate}
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.serverobject.zipline.ZipLinePath
import net.psforever.types.{DriveState, PlanetSideEmpire, PlanetSideGUID, SpawnGroup, Vector3}
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
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.vehicles.UtilityType

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

  /** Actor that handles SOI related functionality, for example if a player is in a SOI */
  private var soi = Default.Actor

  /** Used by the globally unique identifier system to coordinate requests. */
  private var accessor: ActorRef = ActorRef.noSender

  /** The basic support structure for the globally unique number system used by this `Zone`. */
  private var guid: NumberPoolHub = new NumberPoolHub(new MaxNumberSource(65536))

  /** A synchronized `List` of items (`Equipment`) dropped by players on the ground and can be collected again. */
  private val equipmentOnGround: ListBuffer[Equipment] = ListBuffer[Equipment]()

  /**
    */
  private val vehicles: ListBuffer[Vehicle] = ListBuffer[Vehicle]()

  /** Used by the `Zone` to coordinate `Equipment` dropping and collection requests. */
  private var ground: ActorRef = Default.Actor

  private var taskResolver: ActorRef = Default.Actor

  /**
    */
  private val constructions: ListBuffer[PlanetSideGameObject with Deployable] = ListBuffer()

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
    * A testing routine should be run after the fact on the results of the process.
    *
    * @see `ZoneActor.ZoneSetupCheck`
    * @param context a reference to an `ActorContext` necessary for `Props`
    */
  def init(implicit context: ActorContext): Unit = {
    if (accessor == ActorRef.noSender) {
      SetupNumberPools()
      taskResolver = CreateTaskResolvers(context)
      accessor = context.actorOf(
        RandomPool(25).props(
          Props(classOf[UniqueNumberSystem], this.guid, UniqueNumberSystem.AllocateNumberPoolActors(this.guid))
        ),
        s"zone-$id-uns"
      )
      ground = context.actorOf(Props(classOf[ZoneGroundActor], this, equipmentOnGround), s"zone-$id-ground")
      deployables = context.actorOf(Props(classOf[ZoneDeployableActor], this, constructions), s"zone-$id-deployables")
      transport = context.actorOf(Props(classOf[ZoneVehicleActor], this, vehicles), s"zone-$id-vehicles")
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

      validate()
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
          (x: PlanetSideGameObject) => x.isInstanceOf[serverobject.implantmech.ImplantTerminalMech],
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

  def SetupNumberPools(): Unit = {
    guid.AddPool("environment", (0 to 3000).toList) //TODO tailor to suit requirements of zone
    //TODO unlump pools later; do not make any single pool too big
    guid.AddPool("dynamic", (3001 to 10000).toList).Selector =
      new RandomSelector //TODO all things will be registered here, for now
    guid.AddPool("b", (10001 to 15000).toList).Selector = new RandomSelector
    guid.AddPool("c", (15001 to 20000).toList).Selector = new RandomSelector
    guid.AddPool("d", (20001 to 25000).toList).Selector = new RandomSelector
    guid.AddPool("e", (25001 to 30000).toList).Selector = new RandomSelector
    guid.AddPool("f", (30001 to 35000).toList).Selector = new RandomSelector
    guid.AddPool("g", (35001 until 40100).toList).Selector = new RandomSelector
    guid.AddPool("projectiles", (Projectile.baseUID until Projectile.rangeUID).toList)
    guid.AddPool("locker-contents", (40150 until 40450).toList).Selector = new RandomSelector
    //TODO disabled temporarily to lighten load times
    //guid.AddPool("h", (40150 to 45000).toList).Selector = new RandomSelector
    //guid.AddPool("i", (45001 to 50000).toList).Selector = new RandomSelector
    //guid.AddPool("j", (50001 to 55000).toList).Selector = new RandomSelector
    //guid.AddPool("k", (55001 to 60000).toList).Selector = new RandomSelector
    //guid.AddPool("l", (60001 to 65535).toList).Selector = new RandomSelector
  }

  def findSpawns(
      faction: PlanetSideEmpire.Value,
      spawnGroups: Seq[SpawnGroup]
  ): List[(AmenityOwner, Iterable[SpawnPoint])] = {
    val ams = spawnGroups.contains(SpawnGroup.AMS)
    val structures = spawnGroups.collect {
      case SpawnGroup.Facility =>
        StructureType.Facility
      case SpawnGroup.Tower =>
        StructureType.Tower
      case SpawnGroup.WarpGate =>
        StructureType.WarpGate
      case SpawnGroup.Sanctuary =>
        StructureType.Building
    }

    SpawnGroups()
      .filter {
        case (building, spawns) =>
          spawns.nonEmpty &&
          spawns.exists(_.isOffline == false) &&
          structures.contains(building.BuildingType)
      }
      .filter {
        case (building, _) =>
          building match {
            case warpGate: WarpGate =>
              warpGate.Faction == faction || warpGate.Faction == PlanetSideEmpire.NEUTRAL || warpGate.Broadcast
            case _ =>
              building.Faction == faction
          }
      }
      .map {
        case (building, spawns: List[SpawnPoint]) =>
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
    * The globally unique identifier system is synchronized via an `Actor` to ensure that concurrent requests do not clash.
    * A clash is merely when the same number is produced more than once by the same system due to concurrent requests.
    * @return synchronized reference to the globally unique identifier system
    */
  def GUID: ActorRef = accessor

  /**
    * Replace the current globally unique identifier system with a new one.
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
  def AddPool(name: String, pool: Seq[Int]): Boolean = {
    if (accessor == Default.Actor || accessor == null) {
      guid.AddPool(name, pool.toList)
      true
    } else {
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
  def RemovePool(name: String): Boolean = {
    if (accessor == Default.Actor) {
      guid.RemovePool(name)
      true
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
    * The object must be upcast into due to the differtence between the storage type and the return type.
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

  def DeployableList: List[PlanetSideGameObject with Deployable] = constructions.toList

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
    val other: ListBuffer[IdentifiableEntity] = new ListBuffer[IdentifiableEntity]()
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
    other.foreach(obj => guid.register(obj, "dynamic"))
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
          case (Some(building), Some(amenity)) =>
            building.Amenities = amenity.asInstanceOf[Amenity]
          case (None, _) | (_, None) => ; //let ZoneActor's sanity check catch this error
        }
    })
    //ntu management (eventually move to a generic building startup function)
    buildings.values
      .flatMap(_.Amenities.filter(_.Definition == GlobalDefinitions.resource_silo))
      .collect {
        case silo: ResourceSilo =>
          silo.Actor ! "startup"
      }
    //some painfields need to look for their closest door
    buildings.values
      .flatMap(_.Amenities.filter(_.Definition.isInstanceOf[PainboxDefinition]))
      .collect {
        case painbox: Painbox =>
          painbox.Actor ! "startup"
      }
    //allocate soi information
    soi ! SOI.Build()
  }

  private def MakeLattice(): Unit = {
    lattice ++= map.latticeLink.map {
      case (source, target) =>
        val (sourceBuilding, targetBuilding) = (Building(source), Building(target)) match {
          case (Some(sBuilding), Some(tBuilding)) => (sBuilding, tBuilding)
          case _ =>
            throw new NoSuchElementException(s"Can't create lattice link between $source $target. Source is missing")
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

  def tasks: ActorRef = taskResolver

  protected def CreateTaskResolvers(context: ActorContext, numberCreated: Int = 20): ActorRef = {
    context.actorOf(RandomPool(numberCreated).props(Props[TaskResolver]()), s"zone-$id-taskResolver")
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
    final case class Build(obj: PlanetSideGameObject with Deployable, withTool: ConstructionItem)
    final case class DeployableIsBuilt(obj: PlanetSideGameObject with Deployable, withTool: ConstructionItem)

    final case class Dismiss(obj: PlanetSideGameObject with Deployable)
    final case class DeployableIsDismissed(obj: PlanetSideGameObject with Deployable)
  }

  object Vehicle {
    final case class Spawn(vehicle: Vehicle)

    final case class Despawn(vehicle: Vehicle)

    final case class CanNotSpawn(zone: Zone, vehicle: Vehicle, reason: String)

    final case class CanNotDespawn(zone: Zone, vehicle: Vehicle, reason: String)
  }

  object HotSpot {
    final case class Activity(defender: SourceEntry, attacker: SourceEntry, location: Vector3)

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

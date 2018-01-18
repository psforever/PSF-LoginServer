// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.{ActorContext, ActorRef, Props}
import akka.routing.RandomPool
import net.psforever.objects.serverobject.doors.Base
import net.psforever.objects.{PlanetSideGameObject, Player, Vehicle}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.actor.UniqueNumberSystem
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.packet.GamePacket
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

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
  guid.AddPool("environment", (0 to 4000).toList)
  guid.AddPool("dynamic", (4001 to 12000).toList).Selector = new RandomSelector //TODO unlump pools later; do not make too big
  /** A synchronized `List` of items (`Equipment`) dropped by players on the ground and can be collected again. */
  private val equipmentOnGround : ListBuffer[Equipment] = ListBuffer[Equipment]()
  /** Used by the `Zone` to coordinate `Equipment` dropping and collection requests. */
  private var ground : ActorRef = ActorRef.noSender
  /** */
  private var vehicles : List[Vehicle] = List[Vehicle]()
  /** */
  private var transport : ActorRef = ActorRef.noSender

  private var bases : List[Base] = List()

  /**
    * Establish the basic accessible conditions necessary for a functional `Zone`.<br>
    * <br>
    * Called from the `Actor` that governs this `Zone` when it is passed a constructor reference to the `Zone`.
    * Specifically, the order of calling follows: `InterstellarCluster.preStart -> ZoneActor.receive(Zone.Init()) -> Zone.Init`.
    * The basic method performs three main operations.
    * First, the `Actor`-driven aspect of the globally unique identifier system for this `Zone` is finalized.
    * Second, all supporting `Actor` agents are created, e.g., `ground`.
    * Third, the `ZoneMap` server objects are loaded and constructed within that aforementioned system.
    * To avoid being called more than once, there is a test whether the `accessor` for the globally unique identifier system has been changed.
    * @param context a reference to an `ActorContext` necessary for `Props`
    */
  def Init(implicit context : ActorContext) : Unit = {
    if(accessor == ActorRef.noSender) {
      implicit val guid : NumberPoolHub = this.guid //passed into builderObject.Build implicitly
      accessor = context.actorOf(RandomPool(25).props(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystem.AllocateNumberPoolActors(guid))), s"$Id-uns")
      ground = context.actorOf(Props(classOf[ZoneGroundActor], equipmentOnGround), s"$Id-ground")
      transport = context.actorOf(Props(classOf[ZoneVehicleActor], this), s"$Id-vehicles")

      Map.LocalObjects.foreach({ builderObject =>
        builderObject.Build
      })

      MakeBases(Map.LocalBases)
    }
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
    * @return synchronized reference to the globally unique identifier system
    */
  def GUID(hub : NumberPoolHub) : Boolean = {
    if(actor == ActorRef.noSender && guid.Pools.map({case ((_, pool)) => pool.Count}).sum == 0) {
      guid = hub
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

  def Vehicles : List[Vehicle] = vehicles

  def AddVehicle(vehicle : Vehicle) : List[Vehicle] = {
    vehicles = vehicles :+ vehicle
    Vehicles
  }

  def RemoveVehicle(vehicle : Vehicle) : List[Vehicle] = {
    vehicles = recursiveFindVehicle(vehicles.iterator, vehicle) match {
      case Some(index) =>
        vehicles.take(index) ++ vehicles.drop(index + 1)
      case None => ;
        vehicles
    }
    Vehicles
  }

  @tailrec private def recursiveFindVehicle(iter : Iterator[Vehicle], target : Vehicle, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      if(iter.next.equals(target)) {
        Some(index)
      }
      else {
        recursiveFindVehicle(iter, target, index + 1)
      }
    }
  }

  /**
    * Coordinate `Equipment` that has been dropped on the ground or to-be-dropped on the ground.
    * @return synchronized reference to the ground
    * @see `ZoneGroundActor`<br>
    *      `Zone.DropItemOnGround`<br>
    *      `Zone.GetItemOnGround`<br>
    *      `Zone.ItemFromGround`
    */
  def Ground : ActorRef = ground

  def Transport : ActorRef = transport

  def MakeBases(num : Int) : List[Base] = {
    bases = (1 to num).map(id => new Base(id)).toList
    bases
  }

  def Base(id : Int) : Option[Base] = {
    bases.lift(id)
  }

  /**
    * Provide bulk correspondence on all map entities that can be composed into packet messages and reported to a client.
    * These messages are sent in this fashion at the time of joining the server:<br>
    * - `BroadcastWarpgateUpdateMessage`<br>
    * - `BuildingInfoUpdateMessage`<br>
    * - `CaptureFlagUpdateMessage`<br>
    * - `ContinentalLockUpdateMessage`<br>
    * - `DensityLevelUpdateMessage`<br>
    * - `ModuleLimitsMessage`<br>
    * - `VanuModuleUpdateMessage`<br>
    * - `ZoneForcedCavernConnectionMessage`<br>
    * - `ZoneInfoMessage`<br>
    * - `ZoneLockInfoMessage`<br>
    * - `ZonePopulationUpdateMessage`
    * @return a `List` of `GamePacket` messages
    */
  def ClientInitialization() : List[GamePacket] = {
    //TODO unimplemented
    List.empty[GamePacket]
  }

  /**
    * Provide bulk correspondence on all server objects that can be composed into packet messages and reported to a client.
    * These messages are sent in this fashion at the time of joining a specific `Zone`:<br>
    * - `HackMessage`<br>
    * - `PlanetsideAttributeMessage`<br>
    * - `SetEmpireMessage`<br>
    * - `TimeOfDayMessage`<br>
    * - `WeatherMessage`
    * @return a `List` of `GamePacket` messages
    */
  def ClientConfiguration() : List[GamePacket] = {
    //TODO unimplemented
    List.empty[GamePacket]
  }
}

object Zone {
  /**
    * Message to initialize the `Zone`.
    * @see `Zone.Init(implicit ActorContext)`
    */
  final case class Init()

  /**
    * Message to relinguish an item and place in on the ground.
    * @param item the piece of `Equipment`
    * @param pos where it is dropped
    * @param orient in which direction it is facing when dropped
    */
  final case class DropItemOnGround(item : Equipment, pos : Vector3, orient : Vector3)

  /**
    * Message to attempt to acquire an item from the ground (before somoene else?).
    * @param player who wants the piece of `Equipment`
    * @param item_guid the unique identifier of the piece of `Equipment`
    */
  final case class GetItemOnGround(player : Player, item_guid : PlanetSideGUID)

  /**
    * Message to give an item from the ground to a specific user.
    * @param player who wants the piece of `Equipment`
    * @param item the piece of `Equipment`
    */
  final case class ItemFromGround(player : Player, item : Equipment)

  final case class SpawnVehicle(vehicle : Vehicle)

  final case class DespawnVehicle(vehicle : Vehicle)

  /**
    * Message to report the packet messages that initialize the client.
    * @param list a `List` of `GamePacket` messages
    * @see `Zone.ClientInitialization()`<br>
    *      `InterstallarCluster`
    */
  final case class ClientInitialization(list : List[GamePacket])

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
}

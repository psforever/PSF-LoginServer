// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import net.psforever.objects.serverobject.structures.FoundationBuilder
import net.psforever.objects.serverobject.{PlanetSideServerObject, ServerObjectBuilder}
import scalax.collection.Graph
import scalax.collection.GraphPredef._, scalax.collection.GraphEdge._

/**
  * The fixed instantiation and relation of a series of server objects.<br>
  * <br>
  * Asides from a `List` of server objects to be built, the operation between any server objects
  * and the connected functionality emerging from more complex data structures is codified by this object.
  * In the former case, all `Terminal` server objects for a `Zone` are to be defined herein.
  * In the latter case, the arrangement of server objects into groups called facilities is also to be defined herein.
  * Much like a `BasicDefinition` to an object, `ZoneMap` should not maintain mutable information for the companion `Zone`.
  * Use it as a blueprint.<br>
  * <br>
  * The "training zones" are the best example of the difference between a `ZoneMap` and a `Zone.`
  * ("Course" will be used as an unofficial location and layout descriptor.)
  * `tzdrtr` is the Terran Republic driving course.
  * `tzdrvs` is the Vanu Sovereignty driving course.
  * While each course can have different objects and object states, i.e., a `Zone`,
  * both of these courses utilize the same basic server object layout because they are built from the same blueprint, i.e., a `ZoneMap`.
  * @param name the privileged name that can be used as the first parameter in the packet `LoadMapMessage`
  * @see `ServerObjectBuilder`<br>
  *      `LoadMapMessage`
  */
class ZoneMap(private val name : String) {
  private var scale : MapScale = MapScale.Dim8192
  private var localObjects : List[ServerObjectBuilder[_]] = List()
  private var linkTurretWeapon : Map[Int, Int] = Map()
  private var linkTerminalPad : Map[Int, Int] = Map()
  private var linkTerminalInterface : Map[Int, Int] = Map()
  private var linkDoorLock : Map[Int, Int] = Map()
  private var linkObjectBase : Map[Int, Int] = Map()
  private var buildings : Map[(String, Int, Int), FoundationBuilder] = Map()
  private var lattice: Set[(String, String)] = Set()
  private var checksum : Long = 0

  def Name : String = name

  def Scale : MapScale = scale

  def Scale_=(dim : MapScale) : MapScale = {
    scale = dim
    Scale
  }

  def Checksum : Long = checksum

  def Checksum_=(value : Long) : Long = {
    checksum = value
    Checksum
  }

  /**
    * The list of all server object builder wrappers that have been assigned to this `ZoneMap`.
    * @return the `List` of all `ServerObjectBuilders` known to this `ZoneMap`
    */
  def LocalObjects : List[ServerObjectBuilder[_]] =  {
    localObjects
  }

  /**
    * Append the builder for a server object to the list of builders known to this `ZoneMap`.
    * @param id the unique id that will be assigned to this entity
    * @param constructor the logic that initializes the emitted entity
    * @param owning_building_guid The guid of the building this object should belong to, if specified
    * @param door_guid The guid of the door this object (typically a lock) should be linked to, if specified
    * @param terminal_guid The guid of the terminal this object (typically a spawn pad) should be linked to, if specified
    * @return the current number of builders
    */
  def LocalObject[A <: PlanetSideServerObject](id : Int, constructor : ServerObjectBuilder.ConstructorType[A], owning_building_guid : Int = 0, door_guid : Int = 0, terminal_guid: Int = 0) : Int = {
    if(id > 0) {
      localObjects = localObjects :+ ServerObjectBuilder[A](id, constructor)

      if(owning_building_guid > 0) {
        ObjectToBuilding(id, owning_building_guid)
      }

      if(door_guid > 0) {
        DoorToLock(door_guid, id)
      }

      if(terminal_guid > 0) {
        TerminalToSpawnPad(terminal_guid, id)
      }
    }
    localObjects.size
  }

  def LocalBuildings : Map[(String, Int, Int), FoundationBuilder] = buildings

  def LocalBuilding(name : String, building_guid : Int, map_id : Int, constructor : FoundationBuilder) : Int = {
    if(building_guid > 0) {
      buildings = buildings ++ Map((name, building_guid, map_id) -> constructor)
    }
    buildings.size
  }

  def ObjectToBuilding : Map[Int, Int] = linkObjectBase

  def ObjectToBuilding(object_guid : Int, building_id : Int) : Unit = {
    linkObjectBase = linkObjectBase ++ Map(object_guid -> building_id)
  }

  def DoorToLock : Map[Int, Int] = linkDoorLock

  def DoorToLock(door_guid : Int, lock_guid : Int) : Unit = {
    linkDoorLock = linkDoorLock ++ Map(door_guid -> lock_guid)
  }

  def TerminalToSpawnPad : Map[Int, Int] = linkTerminalPad

  def TerminalToSpawnPad(terminal_guid : Int, pad_guid : Int) : Unit = {
    linkTerminalPad = linkTerminalPad ++ Map(terminal_guid -> pad_guid)
  }

  def TerminalToInterface : Map[Int, Int] = linkTerminalInterface

  def TerminalToInterface(interface_guid : Int, terminal_guid : Int) : Unit = {
    linkTerminalInterface = linkTerminalInterface ++ Map(interface_guid -> terminal_guid)
  }

  def TurretToWeapon : Map[Int, Int] = linkTurretWeapon

  def TurretToWeapon(turret_guid : Int, weapon_guid : Int) : Unit = {
    linkTurretWeapon = linkTurretWeapon ++ Map(turret_guid -> weapon_guid)
  }

  def LatticeLink : Set[(String, String)] = lattice

  def LatticeLink(source : String, target: String) : Unit = {
    lattice = lattice ++ Set((source, target))
  }
}

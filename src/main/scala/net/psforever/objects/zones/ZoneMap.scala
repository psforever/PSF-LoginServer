// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import net.psforever.objects.serverobject.structures.FoundationBuilder
import net.psforever.objects.serverobject.zipline.ZipLinePath
import net.psforever.objects.serverobject.{PlanetSideServerObject, ServerObjectBuilder}

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
  *
  * @param name the privileged name that can be used as the first parameter in the packet `LoadMapMessage`
  * @see `ServerObjectBuilder`<br>
  *      `LoadMapMessage`
  */
class ZoneMap(val name: String) {
  var scale: MapScale                                               = MapScale.Dim8192
  var localObjects: List[ServerObjectBuilder[_]]                    = List()
  var checksum: Long                                                = 0
  var zipLinePaths: List[ZipLinePath]                               = List()
  var cavern: Boolean                                               = false
  private var linkTurretWeapon: Map[Int, Int]                       = Map()
  private var linkTerminalPad: Map[Int, Int]                        = Map()
  private var linkTerminalInterface: Map[Int, Int]                  = Map()
  private var linkDoorLock: Map[Int, Int]                           = Map()
  private var linkObjectBase: Map[Int, Int]                         = Map()
  private var buildings: Map[(String, Int, Int), FoundationBuilder] = Map()
  private var lattice: Set[(String, String)]                        = Set()

  /**
    * Append the builder for a server object to the list of builders known to this `ZoneMap`.
    *
    * @param id                 the unique id that will be assigned to this entity
    * @param constructor        the logic that initializes the emitted entity
    * @param owningBuildingGuid The guid of the building this object should belong to, if specified
    * @param doorGuid           The guid of the door this object (typically a lock) should be linked to, if specified
    * @param terminalGuid       The guid of the terminal this object (typically a spawn pad) should be linked to, if specified
    * @return the current number of builders
    */
  def addLocalObject[A <: PlanetSideServerObject](
      id: Int,
      constructor: ServerObjectBuilder.ConstructorType[A],
      owningBuildingGuid: Int = 0,
      doorGuid: Int = 0,
      terminalGuid: Int = 0
  ): Int = {
    if (id > 0) {
      localObjects = localObjects :+ ServerObjectBuilder[A](id, constructor)

      if (owningBuildingGuid > 0) {
        linkObjectToBuilding(id, owningBuildingGuid)
      }

      if (doorGuid > 0) {
        linkDoorToLock(doorGuid, id)
      }

      if (terminalGuid > 0) {
        linkTerminalToSpawnPad(terminalGuid, id)
      }
    }
    localObjects.size
  }

  def localBuildings: Map[(String, Int, Int), FoundationBuilder] = buildings

  def addLocalBuilding(name: String, buildingGuid: Int, mapId: Int, constructor: FoundationBuilder): Int = {
    if (buildingGuid > 0) {
      buildings = buildings ++ Map((name, buildingGuid, mapId) -> constructor)
    }
    buildings.size
  }

  def objectToBuilding: Map[Int, Int] = linkObjectBase

  def linkObjectToBuilding(objectGuid: Int, buildingId: Int): Unit = {
    linkObjectBase = linkObjectBase ++ Map(objectGuid -> buildingId)
  }

  def doorToLock: Map[Int, Int] = linkDoorLock

  def linkDoorToLock(doorGuid: Int, lockGuid: Int): Unit = {
    linkDoorLock = linkDoorLock ++ Map(doorGuid -> lockGuid)
  }

  def terminalToSpawnPad: Map[Int, Int] = linkTerminalPad

  def linkTerminalToSpawnPad(terminalGuid: Int, padGuid: Int): Unit = {
    linkTerminalPad = linkTerminalPad ++ Map(terminalGuid -> padGuid)
  }

  def terminalToInterface: Map[Int, Int] = linkTerminalInterface

  def linkTerminalToInterface(terminalGuid: Int, interfaceGuid: Int): Unit = {
    linkTerminalInterface = linkTerminalInterface ++ Map(terminalGuid -> interfaceGuid)
  }

  def turretToWeapon: Map[Int, Int] = linkTurretWeapon

  def linkTurretToWeapon(turretGuid: Int, weaponGuid: Int): Unit = {
    linkTurretWeapon = linkTurretWeapon ++ Map(turretGuid -> weaponGuid)
  }

  def latticeLink: Set[(String, String)] = lattice

  def addLatticeLink(source: String, target: String): Unit = {
    lattice = lattice ++ Set((source, target))
  }

}

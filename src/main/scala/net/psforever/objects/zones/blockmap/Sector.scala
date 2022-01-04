// Copyright (c) 2021 PSForever
package net.psforever.objects.zones.blockmap

import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.ce.Deployable
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.environment.PieceOfEnvironment
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.{Player, Vehicle}

import scala.collection.mutable.ListBuffer

/**
  * The collections of entities in a sector conglomerate.
  */
trait SectorPopulation {
  def range: Float

  def livePlayerList: List[Player]

  def corpseList: List[Player]

  def vehicleList: List[Vehicle]

  def equipmentOnGroundList: List[Equipment]

  def deployableList: List[Deployable]

  def buildingList: List[Building]

  def amenityList: List[Amenity]

  def environmentList: List[PieceOfEnvironment]

  def projectileList: List[Projectile]

  /**
    * A count of all the entities in all the lists.
    */
  def total: Int = {
    livePlayerList.size +
    corpseList.size +
    vehicleList.size +
    equipmentOnGroundList.size +
    deployableList.size +
    buildingList.size +
    amenityList.size +
    environmentList.size +
    projectileList.size
  }
}

/**
  * Information about the sector.
  */
trait SectorTraits {
  /** the starting coordinate of the region (in terms of width) */
  def longitude: Float
  /** the starting coordinate of the region (in terms of length) */
  def latitude: Float
  /** how width and long is the region */
  def span: Int
}

/**
  * Custom lists of entities for sector buckets.
  * @param eqFunc a custom equivalence function to distinguish between the entities in the list
  * @tparam A the type of object that will be the entities stored in the list
  */
class SectorListOf[A](eqFunc: (A, A) => Boolean = (a: A, b: A) => a equals b) {
  private val internalList: ListBuffer[A] = ListBuffer[A]()

  /**
    * Insert the entity into the list as long as it cannot be found in the list
    * according to the custom equivalence function.
    * @param elem the entity
    * @return a conventional list of entities
    */
  def addTo(elem: A): List[A] = {
    internalList.indexWhere { item => eqFunc(elem, item) } match {
      case -1 => internalList.addOne(elem)
      case _  => ;
    }
    list
  }

  /**
    * Remove the entity from the list as long as it can be found in the list
    * according to the custom equivalence function.
    * @param elem the entity
    * @return a conventional list of entities
    */
  def removeFrom(elem: A): List[A] = {
    internalList.indexWhere { item => eqFunc(elem, item) } match {
      case -1    => ;
      case index => internalList.remove(index)
    }
    list
  }

  /**
    * Cast this specialized list of entities into a conventional list of entities.
    * @return a conventional list of entities
    */
  def list: List[A] = internalList.toList
}

/**
  * The bucket of a blockmap structure
  * that contains lists of entities that, within a given span of coordinate distance,
  * are considered neighbors.
  * While the coordinate space that supports a blockmap (?) may be any combination of two dimensions,
  * the sectors are always square.
  * @param longitude a starting coordinate of the region (in terms of width)
  * @param latitude a starting coordinate of the region (in terms of length)
  * @param span the distance across the sector in both directions
  */
class Sector(val longitude: Int, val latitude: Int, val span: Int)
  extends SectorPopulation {
  private val livePlayers: SectorListOf[Player] = new SectorListOf[Player](
    (a: Player, b: Player) => a.CharId == b.CharId
  )

  private val corpses: SectorListOf[Player] = new SectorListOf[Player](
    (a: Player, b: Player) => a.GUID == b.GUID || (a eq b)
  )

  private val vehicles: SectorListOf[Vehicle] = new SectorListOf[Vehicle](
    (a: Vehicle, b: Vehicle) => a eq b
  )

  private val equipmentOnGround: SectorListOf[Equipment] = new SectorListOf[Equipment](
    (a: Equipment, b: Equipment) => a eq b
  )

  private val deployables: SectorListOf[Deployable] = new SectorListOf[Deployable](
    (a: Deployable, b: Deployable) => a eq b
  )

  private val buildings: SectorListOf[Building] = new SectorListOf[Building](
    (a: Building, b: Building) => a eq b
  )

  private val amenities: SectorListOf[Amenity] = new SectorListOf[Amenity](
    (a: Amenity, b: Amenity) => a eq b
  )

  private val environment: SectorListOf[PieceOfEnvironment] = new SectorListOf[PieceOfEnvironment](
    (a: PieceOfEnvironment, b: PieceOfEnvironment) => a eq b
  )

  private val projectiles: SectorListOf[Projectile] = new SectorListOf[Projectile](
    (a: Projectile, b: Projectile) => a.id == b.id
  )

  def range: Float = span.toFloat

  def livePlayerList : List[Player] = livePlayers.list

  def corpseList: List[Player] = corpses.list

  def vehicleList: List[Vehicle] = vehicles.list

  def equipmentOnGroundList: List[Equipment] = equipmentOnGround.list

  def deployableList: List[Deployable] = deployables.list

  def buildingList: List[Building] = buildings.list

  def amenityList : List[Amenity] = amenities.list

  def environmentList: List[PieceOfEnvironment] = environment.list

  def projectileList: List[Projectile] = projectiles.list

  /**
    * Appropriate an entity added to this blockmap bucket
    * inot a list of objects that are like itself.
    * @param o the entity
    * @return whether or not the entity was added
    */
  def addTo(o: BlockMapEntity): Boolean = {
    o match {
      case p: Player =>
        //players and corpses are the same kind of object, but are distinguished by a single flag
        //when adding to the "corpse" list, first attempt to remove from the "player" list
        if (!p.isBackpack) {
          livePlayers.list.size < livePlayers.addTo(p).size
        }
        else {
          livePlayers.removeFrom(p)
          corpses.list.size < corpses.addTo(p).size
        }
      case v: Vehicle =>
        vehicles.list.size < vehicles.addTo(v).size
      case e: Equipment =>
        equipmentOnGround.list.size < equipmentOnGround.addTo(e).size
      case d: Deployable =>
        deployables.list.size < deployables.addTo(d).size
      case b: Building =>
        buildings.list.size < buildings.addTo(b).size
      case a: Amenity =>
        amenities.list.size < amenities.addTo(a).size
      case e: PieceOfEnvironment =>
        environment.list.size < environment.addTo(e).size
      case p: Projectile =>
        projectiles.list.size < projectiles.addTo(p).size
      case _ =>
        false
    }
  }

  /**
    * Remove an entity added to this blockmap bucket
    * from a list of already-added objects that are like itself.
    * @param o the entity
    * @return whether or not the entity was removed
    */
  def removeFrom(o: Any): Boolean = {
    o match {
      case p: Player =>
        livePlayers.list.size > livePlayers.removeFrom(p).size ||
        corpses.list.size > corpses.removeFrom(p).size
      case v: Vehicle =>
        vehicles.list.size > vehicles.removeFrom(v).size
      case e: Equipment =>
        equipmentOnGround.list.size > equipmentOnGround.removeFrom(e).size
      case d: Deployable =>
        deployables.list.size > deployables.removeFrom(d).size
      case p: Projectile =>
        projectiles.list.size > projectiles.removeFrom(p).size
      case _ =>
        false
    }
  }
}

/**
  * The specific datastructure that is mentioned when using the term "sector conglomerate".
  * Typically used to compose the lists of entities from various individual sectors.
  * @param livePlayerList the living players
  * @param corpseList the dead players
  * @param vehicleList vehicles
  * @param equipmentOnGroundList dropped equipment
  * @param deployableList deployed combat engineering gear
  * @param buildingList the structures
  * @param amenityList the structures within the structures
  * @param environmentList fields that represent the game world environment
  */
class SectorGroup(
                   val range: Float,
                   val livePlayerList: List[Player],
                   val corpseList: List[Player],
                   val vehicleList: List[Vehicle],
                   val equipmentOnGroundList: List[Equipment],
                   val deployableList: List[Deployable],
                   val buildingList: List[Building],
                   val amenityList: List[Amenity],
                   val environmentList: List[PieceOfEnvironment],
                   val projectileList: List[Projectile]
                 )
  extends SectorPopulation

object SectorGroup {
  /**
    * Overloaded constructor that takes a single sector
    * and transfers the lists of entities into a single conglomeration of the sector populations.
    * @param sector the sector to be counted
    * @return a `SectorGroup` object
    */
  def apply(sector: Sector): SectorGroup = {
    new SectorGroup(
      sector.range,
      sector.livePlayerList,
      sector.corpseList,
      sector.vehicleList,
      sector.equipmentOnGroundList,
      sector.deployableList,
      sector.buildingList,
      sector.amenityList,
      sector.environmentList,
      sector.projectileList
    )
  }

  /**
    * Overloaded constructor that takes a single sector
    * and transfers the lists of entities into a single conglomeration of the sector populations.
    * @param range a custom range value
    * @param sector the sector to be counted
    * @return a `SectorGroup` object
    */
  def apply(range: Float, sector: Sector): SectorGroup = {
    new SectorGroup(
      range,
      sector.livePlayerList,
      sector.corpseList,
      sector.vehicleList,
      sector.equipmentOnGroundList,
      sector.deployableList,
      sector.buildingList,
      sector.amenityList,
      sector.environmentList,
      sector.projectileList
    )
  }

  /**
    * Overloaded constructor that takes a group of sectors
    * and condenses all of the lists of entities into a single conglomeration of the sector populations.
    * @param sectors the series of sectors to be counted
    * @return a `SectorGroup` object
    */
  def apply(sectors: Iterable[Sector]): SectorGroup = {
    if (sectors.isEmpty) {
      SectorGroup(range = 0, sectors = Nil)
    } else if (sectors.size == 1) {
      SectorGroup(sectors.head.range, sectors)
    } else {
      SectorGroup(sectors.maxBy { _.range }.range, sectors)
    }
  }

  /**
    * Overloaded constructor that takes a group of sectors
    * and condenses all of the lists of entities into a single conglomeration of the sector populations.
    * @param range a custom range value
    * @param sectors the series of sectors to be counted
    * @return a `SectorGroup` object
    */
  def apply(range: Float, sectors: Iterable[Sector]): SectorGroup = {
    if (sectors.isEmpty) {
      new SectorGroup(range, Nil, Nil, Nil, Nil, Nil, Nil, Nil, Nil, Nil)
    } else if (sectors.size == 1) {
      val sector = sectors.head
      new SectorGroup(
        range,
        sector.livePlayerList,
        sector.corpseList,
        sector.vehicleList,
        sector.equipmentOnGroundList,
        sector.deployableList,
        sector.buildingList,
        sector.amenityList,
        sector.environmentList,
        sector.projectileList
      )
    } else {
      new SectorGroup(
        range,
        sectors.flatMap { _.livePlayerList }.toList.distinct,
        sectors.flatMap { _.corpseList }.toList.distinct,
        sectors.flatMap { _.vehicleList }.toList.distinct,
        sectors.flatMap { _.equipmentOnGroundList }.toList.distinct,
        sectors.flatMap { _.deployableList }.toList.distinct,
        sectors.flatMap { _.buildingList }.toList.distinct,
        sectors.flatMap { _.amenityList }.toList.distinct,
        sectors.flatMap { _.environmentList }.toList.distinct,
        sectors.flatMap { _.projectileList }.toList.distinct
      )
    }
  }
}

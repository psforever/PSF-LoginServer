// Copyright (c) 2021 PSForever
package net.psforever.objects.zones.blockmap

import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.ce.Deployable
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.environment.PieceOfEnvironment
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.avatar.AvatarBot
import net.psforever.objects.entity.IdentifiableEntity

/**
  * The collections of entities in a sector conglomerate.
  */
trait SectorPopulation {
  def rangeX: Float

  def rangeY: Float

  def livePlayerList: List[Player]

  def botList: List[AvatarBot]

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
    botList.size +
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
  /* The lists are held directly, each starting as the shared empty list, Nil: a zone has thousands of sectors,
     and most of their lists stay empty. Each list is only ever replaced, never changed, so one handed out stays as it was. */
  private var livePlayers: List[Player]                  = Nil
  private var bots: List[AvatarBot]                      = Nil
  private var corpses: List[Player]                      = Nil
  private var vehicles: List[Vehicle]                    = Nil
  private var equipmentOnGround: List[Equipment]         = Nil
  private var deployables: List[Deployable]              = Nil
  private var buildings: List[Building]                  = Nil
  private var amenities: List[Amenity]                   = Nil
  private var environment: List[PieceOfEnvironment]      = Nil
  private var projectiles: List[Projectile]              = Nil

  def rangeX: Float = span.toFloat

  def rangeY: Float = span.toFloat

  def livePlayerList : List[Player] = livePlayers

  def botList : List[AvatarBot] = bots

  def corpseList: List[Player] = corpses

  def vehicleList: List[Vehicle] = vehicles

  def equipmentOnGroundList: List[Equipment] = equipmentOnGround

  def deployableList: List[Deployable] = deployables

  def buildingList: List[Building] = buildings

  def amenityList : List[Amenity] = amenities

  def environmentList: List[PieceOfEnvironment] = environment

  def projectileList: List[Projectile] = projectiles

  /**
    * Appropriate an entity added to this blockmap bucket
    * inot a list of objects that are like itself.
    * @param o the entity
    * @return whether or not the entity was added
    */
  def addTo(o: BlockMapEntity): Boolean = {
    o match {
      case p: Player if p.isBackpack =>
        //when adding to the "corpse" list, first attempt to remove from the "player" list
        livePlayers = Sector.remove(livePlayers, p, Sector.sameGuid)
        val before = corpses
        corpses = Sector.add(before, p, Sector.sameObject)
        corpses ne before
      case p: Player =>
        val before = livePlayers
        livePlayers = Sector.add(before, p, Sector.sameGuid)
        livePlayers ne before
      case ab: AvatarBot =>
        val before = bots
        bots = Sector.add(before, ab, Sector.sameGuid)
        bots ne before
      case v: Vehicle =>
        val before = vehicles
        vehicles = Sector.add(before, v, Sector.sameObject)
        vehicles ne before
      case e: Equipment =>
        val before = equipmentOnGround
        equipmentOnGround = Sector.add(before, e, Sector.sameObject)
        equipmentOnGround ne before
      case d: Deployable =>
        val before = deployables
        deployables = Sector.add(before, d, Sector.sameObject)
        deployables ne before
      case b: Building =>
        val before = buildings
        buildings = Sector.add(before, b, Sector.sameGuid)
        buildings ne before
      case a: Amenity =>
        val before = amenities
        amenities = Sector.add(before, a, Sector.sameGuid)
        val added = amenities ne before
        if (added) {
          a.Definition.environmentField.foreach(field => environment = Sector.add(environment, field.create(a), Sector.sameObject))
        }
        added
      case e: PieceOfEnvironment =>
        val before = environment
        environment = Sector.add(before, e, Sector.sameObject)
        environment ne before
      case p: Projectile =>
        val before = projectiles
        projectiles = Sector.add(before, p, Sector.sameProjectile)
        projectiles ne before
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
      case p: Player if p.isBackpack =>
        val before = corpses
        corpses = Sector.remove(before, p, Sector.sameObject)
        corpses ne before
      case p: Player =>
        val before = livePlayers
        livePlayers = Sector.remove(before, p, Sector.sameGuid)
        livePlayers ne before
      case ab: AvatarBot =>
        val before = bots
        bots = Sector.remove(before, ab, Sector.sameGuid)
        bots ne before
      case v: Vehicle =>
        val before = vehicles
        vehicles = Sector.remove(before, v, Sector.sameObject)
        vehicles ne before
      case e: Equipment =>
        val before = equipmentOnGround
        equipmentOnGround = Sector.remove(before, e, Sector.sameObject)
        equipmentOnGround ne before
      case d: Deployable =>
        val before = deployables
        deployables = Sector.remove(before, d, Sector.sameObject)
        deployables ne before
      case p: Projectile =>
        val before = projectiles
        projectiles = Sector.remove(before, p, Sector.sameProjectile)
        projectiles ne before
      case _ =>
        false
    }
  }
}

object Sector {
  /* how the entities of each list are told apart */
  private val sameGuid: (IdentifiableEntity, IdentifiableEntity) => Boolean = (a, b) => a.GUID == b.GUID
  private val sameObject: (AnyRef, AnyRef) => Boolean                       = (a, b) => a eq b
  private val sameProjectile: (Projectile, Projectile) => Boolean           = (a, b) => a.id == b.id

  /**
    * The list with the entity appended, unless the list already has one the same as it.
    * @return the list itself, if the entity was not added
    */
  private def add[A](list: List[A], elem: A, same: (A, A) => Boolean): List[A] = {
    if (list.exists(item => same(elem, item))) list else list :+ elem
  }

  /**
    * The list without the first entity the same as the given one.
    * @return the list itself, if no such entity was found
    */
  private def remove[A](list: List[A], elem: A, same: (A, A) => Boolean): List[A] = {
    list.indexWhere(item => same(elem, item)) match {
      case -1    => list
      case index => list.patch(index, Nil, 1)
    }
  }

  /**
    * An sector that is empty forever.
    */
  final val Empty = new Sector(longitude = 0, latitude = 0, span = 0) {
    override def addTo(o : BlockMapEntity): Boolean = false
  }
}

/**
  * The specific datastructure that is mentioned when using the term "sector conglomerate".
  * Typically used to compose the lists of entities from various individual sectors.
  * @param livePlayerList the living players
  * @param botList the bot avatars
  * @param corpseList the dead players
  * @param vehicleList vehicles
  * @param equipmentOnGroundList dropped equipment
  * @param deployableList deployed combat engineering gear
  * @param buildingList the structures
  * @param amenityList the structures within the structures
  * @param environmentList fields that represent the game world environment
  */
/**
  * The entity lists are declared by-name and retained as `lazy val`s, so each category is
  * condensed on first access and memoized thereafter.
  * A sector group frequently spans a great many sectors while callers usually consult only one or
  * two of the ten categories, and the group is rebuilt on hot paths such as the per-movement-packet
  * local sector refresh, so condensing a category only when it is read keeps that cost proportional
  * to what is actually used.
  */
class SectorGroup(
                   val rangeX: Float,
                   val rangeY: Float,
                   livePlayers: => List[Player],
                   bots: => List[AvatarBot],
                   corpses: => List[Player],
                   vehicles: => List[Vehicle],
                   equipmentOnGround: => List[Equipment],
                   deployables: => List[Deployable],
                   buildings: => List[Building],
                   amenities: => List[Amenity],
                   environment: => List[PieceOfEnvironment],
                   projectiles: => List[Projectile]
                 )
  extends SectorPopulation {
  lazy val livePlayerList: List[Player] = livePlayers
  lazy val botList: List[AvatarBot] = bots
  lazy val corpseList: List[Player] = corpses
  lazy val vehicleList: List[Vehicle] = vehicles
  lazy val equipmentOnGroundList: List[Equipment] = equipmentOnGround
  lazy val deployableList: List[Deployable] = deployables
  lazy val buildingList: List[Building] = buildings
  lazy val amenityList: List[Amenity] = amenities
  lazy val environmentList: List[PieceOfEnvironment] = environment
  lazy val projectileList: List[Projectile] = projectiles
}

object SectorGroup {
  /** cached sector of no range and no entity coverage */
  final val emptySector: SectorGroup = SectorGroup(range = 0, sectors = Nil)

  /**
    * Overloaded constructor that takes a single sector
    * and transfers the lists of entities into a single conglomeration of the sector populations.
    * @param sector the sector to be counted
    * @return a `SectorGroup` object
    */
  def apply(sector: Sector): SectorGroup = {
    SectorGroup(sector.rangeX, sector.rangeY, sector)
  }

  /**
    * Overloaded constructor that takes a single sector
    * and transfers the lists of entities into a single conglomeration of the sector populations.
    * @param range a custom range value
    * @param sector the sector to be counted
    * @return a `SectorGroup` object
    */
  def apply(range: Float, sector: Sector): SectorGroup = {
    SectorGroup(range, range, sector)
  }

  /**
    * Overloaded constructor that takes a single sector
    * and transfers the lists of entities into a single conglomeration of the sector populations.
    * @param rangeX a custom range value for the x-axis
    * @param rangeY a custom range value for the y-axis
    * @param sector the sector to be counted
    * @return a `SectorGroup` object
    */
  def apply(rangeX: Float, rangeY: Float, sector: Sector): SectorGroup = {
    new SectorGroup(
      rangeX,
      rangeY,
      sector.livePlayerList.filterNot(p => p.spectator || !p.allowInteraction),
      sector.botList.filterNot(b => !b.allowInteraction),
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
      SectorGroup.emptySector
    } else if (sectors.size == 1) {
      SectorGroup(sectors.head.rangeX, sectors.head.rangeY, sectors)
    } else {
      SectorGroup(sectors.maxBy { _.rangeX }.rangeX, sectors.maxBy { _.rangeY }.rangeY, sectors)
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
    SectorGroup(range, range, sectors)
  }

  /**
    * Overloaded constructor that takes a group of sectors
    * and condenses all of the lists of entities into a single conglomeration of the sector populations.
    * @param rangeX a custom range value for the x-axis
    * @param rangeY a custom range value for the y-axis
    * @param sectors the series of sectors to be counted
    * @return a `SectorGroup` object
    */
  def apply(rangeX: Float, rangeY: Float, sectors: Iterable[Sector]): SectorGroup = {
    if (sectors.isEmpty) {
      new SectorGroup(rangeX, rangeY, Nil, Nil, Nil, Nil, Nil, Nil, Nil, Nil, Nil, Nil)
    } else if (sectors.size == 1) {
      val sector = sectors.head
      new SectorGroup(
        rangeX,
        rangeY,
        sector.livePlayerList.filterNot(p => p.spectator || !p.allowInteraction),
        sector.botList.filterNot(b => !b.allowInteraction),
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
        rangeX,
        rangeY,
        sectors.flatMap { _.livePlayerList }.toList.distinct.filterNot(p => p.spectator || !p.allowInteraction),
        sectors.flatMap { _.botList }.toList.distinct.filterNot(b => !b.allowInteraction),
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

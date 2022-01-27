// Copyright (c) 2021 PSForever
package net.psforever.objects.zones.blockmap

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.serverobject.environment.PieceOfEnvironment
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.zones.MapScale
import net.psforever.types.Vector3

import scala.collection.mutable.ListBuffer

/**
  * A data structure which divides coordinate space into buckets or coordinate spans.
  * The function of the blockmap is to organize the instantiated game objects (entities)
  * that can be represented in coordinate space into a bucket each or into multiple buckets each
  * that reflect their locality with other game objects in the same coordinate space.
  * Polling based on either positions or on entities should be able to recover a lists of entities
  * that are considered neighbors in the context of that position and a rectangular distance around the position.
  * The purpose of the blockmap is to improve targeting when making such locality determinations.<br>
  * <br>
  * The coordinate space of a PlanetSide zone may contain 65535 entities, one of which is the same target entity.
  * A bucket on the blockmap should contain only a small fraction of the full zone's entities.
  * @param fullMapWidth maximum width of the coordinate space (m)
  * @param fullMapHeight maximum height of the coordinate space (m)
  * @param desiredSpanSize the amount of coordinate space attributed to each bucket in the blockmap (m)
  */
class BlockMap(fullMapWidth: Int, fullMapHeight: Int, desiredSpanSize: Int) {
  /** a clamping of the desired span size to a realistic value to use for the span size;
    * blocks can not be too small, but also should not be much larger than the width of the representable region
    * a block spanning as wide as the map is an acceptable cap
    */
  val spanSize: Int = math.min(math.max(10, desiredSpanSize), fullMapWidth)
  /** how many sectors are in a row;
    * the far side sector may run off into un-navigable regions but will always contain a sliver of represented map space,
    * for example, on a 0-10 grid where the span size is 3, the spans will begin at (0, 3, 6, 9)
    * and the last span will only have two-thirds of its region valid;
    * the invalid, not represented regions should be silently ignored
    */
  val blocksInRow: Int = fullMapWidth / spanSize + (if (fullMapWidth % spanSize > 0) 1 else 0)
  /** the sectors / blocks / buckets into which entities that submit themselves are divided;
    * while the represented region need not be square, the sectors are defined as squares
    */
  val blocks: ListBuffer[Sector] = {
    val horizontal: List[Int] = List.range(0, fullMapWidth, spanSize)
    val vertical: List[Int] = List.range(0, fullMapHeight, spanSize)
    ListBuffer.newBuilder[Sector].addAll(
      vertical.flatMap { latitude =>
        horizontal.map { longitude =>
          new Sector(longitude, latitude, spanSize)
        }
      }
    ).result()
  }

  /**
    * Given a blockmap entity,
    * one that is allegedly represented on this blockmap,
    * find the sector conglomerate in which this entity is allocated.
    * @see `BlockMap.quickToSectorGroup`
    * @param entity the target entity
    * @return a conglomerate sector which lists all of the entities in the discovered sector(s)
    */
  def sector(entity: BlockMapEntity): SectorPopulation = {
    entity.blockMapEntry match {
      case Some(entry) => BlockMap.quickToSectorGroup(entry.sectors.map { blocks })
      case None        => SectorGroup(Nil)
    }
  }

  /**
    * Given a coordinate position within representable space and a range from that representable space,
    * find the sector conglomerate to which this range allocates.
    * @see `BlockMap.findSectorIndices`
    * @see `BlockMap.quickToSectorGroup`
    * @param p the game world coordinates
    * @param range the axis distance from the provided coordinates
    * @return a conglomerate sector which lists all of the entities in the discovered sector(s)
    */
  def sector(p: Vector3, range: Float): SectorPopulation = {
    BlockMap.quickToSectorGroup(range, BlockMap.findSectorIndices(blockMap = this, p, range).map { blocks } )
  }

  /**
    * Allocate this entity into appropriate sectors on the blockmap.
    * @see `addTo(BlockMapEntity, Vector3)`
    * @param target the entity
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def addTo(target: BlockMapEntity): SectorPopulation = {
    addTo(target, target.Position)
  }

  /**
    * Allocate this entity into appropriate sectors on the blockmap
    * at the provided game world coordinates.
    * @see `addTo(BlockMapEntity, Vector3, Float)`
    * @see `BlockMap.rangeFromEntity`
    * @param target the entity
    * @param toPosition the custom game world coordinates that indicate the central sector
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def addTo(target: BlockMapEntity, toPosition: Vector3): SectorPopulation = {
    addTo(target, toPosition, BlockMap.rangeFromEntity(target))
  }

  /**
    * Allocate this entity into appropriate sectors on the blockmap
    * using the provided custom axis range.
    * @see `addTo(BlockMapEntity, Vector3, Float)`
    * @param target the entity
    * @param range the custom distance from the central sector along the major axes
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def addTo(target: BlockMapEntity, range: Float): SectorPopulation = {
    addTo(target, target.Position, range)
  }

  /**
    * Allocate this entity into appropriate sectors on the blockmap
    * using the provided game world coordinates and the provided axis range.
    * @see `BlockMap.findSectorIndices`
    * @param target the entity
    * @param toPosition the game world coordinates that indicate the central sector
    * @param range the distance from the central sector along the major axes
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def addTo(target: BlockMapEntity, toPosition: Vector3, range: Float): SectorPopulation = {
    val to = BlockMap.findSectorIndices(blockMap = this, toPosition, range)
    val toSectors = to.toSet.map { blocks }
    toSectors.foreach { block => block.addTo(target) }
    target.blockMapEntry = Some(BlockMapEntry(toPosition, range, to.toSet))
    BlockMap.quickToSectorGroup(range, toSectors)
  }

  /**
    * Deallocate this entity from appropriate sectors on the blockmap.
    * @see `actuallyRemoveFrom(BlockMapEntity, Vector3, Float)`
    * @param target the entity
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def removeFrom(target: BlockMapEntity): SectorPopulation = {
    target.blockMapEntry match {
      case Some(entry) => actuallyRemoveFrom(target, entry.coords, entry.range)
      case None        => SectorGroup(Nil)
    }
  }

  /**
    * Deallocate this entity from appropriate sectors on the blockmap.
    * Other parameters are included for symmetry with a respective `addto` method,
    * but are ignored since removing an entity from a sector from which it is not represented is ill-advised
    * as is not removing an entity from any sector that it occupies.
    * @see `removeFrom(BlockMapEntity)`
    * @param target the entity
    * @param fromPosition ignored
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def removeFrom(target: BlockMapEntity, fromPosition: Vector3): SectorPopulation = {
    removeFrom(target)
  }

  /**
    * Deallocate this entity from appropriate sectors on the blockmap.
    * Other parameters are included for symmetry with a respective `addto` method,
    * but are ignored since removing an entity from a sector from which it is not represented is ill-advised
    * as is not removing an entity from any sector that it occupies.
    * @see `removeFrom(BlockMapEntity)`
    * @param target the entity
    * @param range ignored
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def removeFrom(target: BlockMapEntity, range: Float): SectorPopulation =
    removeFrom(target)

  /**
    * Deallocate this entity from appropriate sectors on the blockmap.
    * Other parameters are included for symmetry with a respective `addto` method,
    * but are ignored since removing an entity from a sector from which it is not represented is ill-advised
    * as is not removing an entity from any sector that it occupies.
    * @see `removeFrom(BlockMapEntity)`
    * @param target the entity
    * @param fromPosition ignored
    * @param range ignored
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def removeFrom(target: BlockMapEntity, fromPosition: Vector3, range: Float): SectorPopulation = {
    removeFrom(target)
  }

  /**
    * Deallocate this entity from appropriate sectors on the blockmap.
    * Really.
    * @param target the entity
    * @param fromPosition the game world coordinates that indicate the central sector
    * @param range the distance from the central sector along the major axes
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  private def actuallyRemoveFrom(target: BlockMapEntity, fromPosition: Vector3, range: Float): SectorPopulation = {
    target.blockMapEntry match {
      case Some(entry) =>
        target.blockMapEntry = None
        val from = entry.sectors.map { blocks }
        from.foreach { block => block.removeFrom(target) }
        BlockMap.quickToSectorGroup(range, from)
      case None =>
        SectorGroup(Nil)
    }
  }

  /**
    * Move an entity on the blockmap structure and update the prerequisite internal information.
    * @see `move(BlockMapEntity, Vector3, Vector3, Float)`
    * @param target the entity
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def move(target: BlockMapEntity): SectorPopulation = {
    target.blockMapEntry match {
      case Some(entry) => move(target, target.Position, entry.coords, entry.range)
      case None        => SectorGroup(Nil)
    }
  }

  /**
    * Move an entity on the blockmap structure and update the prerequisite internal information.
    * @see `move(BlockMapEntity, Vector3, Vector3, Float)`
    * @param target the entity
    * @param toPosition the next location of the entity in world coordinates
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def move(target: BlockMapEntity, toPosition: Vector3): SectorPopulation = {
    target.blockMapEntry match {
      case Some(entry) => move(target, toPosition, entry.coords, entry.range)
      case _           => SectorGroup(Nil)
    }
  }

  /**
    * Move an entity on the blockmap structure and update the prerequisite internal information.
    * @see `move(BlockMapEntity, Vector3)`
    * @param target the entity
    * @param toPosition the next location of the entity in world coordinates
    * @param fromPosition ignored
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def move(target: BlockMapEntity, toPosition: Vector3, fromPosition: Vector3): SectorPopulation = {
    move(target, toPosition)
  }

  /**
    * Move an entity on the blockmap structure and update the prerequisite internal information.
    * @param target the entity
    * @param toPosition the next location of the entity in world coordinates
    * @param fromPosition the current location of the entity in world coordinates
    * @param range the distance from the location along the major axes
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def move(target: BlockMapEntity, toPosition: Vector3, fromPosition: Vector3, range: Float): SectorPopulation = {
    target.blockMapEntry match {
      case Some(entry) =>
        val from = entry.sectors
        val to = BlockMap.findSectorIndices(blockMap = this, toPosition, range).toSet
        to.diff(from).foreach { index => blocks(index).addTo(target) }
        from.diff(to).foreach { index => blocks(index).removeFrom(target) }
        target.blockMapEntry = Some(BlockMapEntry(toPosition, range, to))
        BlockMap.quickToSectorGroup(range, to.map { blocks })
      case None    =>
        SectorGroup(Nil)
    }
  }
}

object BlockMap {
  /**
    * Overloaded constructor that uses a `MapScale` field, common with `Zone` entities.
    * @param scale the two-dimensional scale of the map
    * @param desiredSpanSize the length and width of a sector
    * @return a ` BlockMap` entity
    */
  def apply(scale: MapScale, desiredSpanSize: Int): BlockMap = {
    new BlockMap(scale.width.toInt, scale.height.toInt, desiredSpanSize)
  }

  /**
    * The blockmap is mapped to a coordinate range in two directions,
    * so find the indices of the sectors that correspond to the region
    * defined by the range around a coordinate position.
    * @param blockMap the blockmap structure
    * @param p the coordinate position
    * @param range a rectangular range aigned with lateral axes extending from a coordinate position
    * @return the indices of the sectors in the blockmap structure
    */
  def findSectorIndices(blockMap: BlockMap, p: Vector3, range: Float): Iterable[Int] = {
    findSectorIndices(blockMap.spanSize, blockMap.blocksInRow, blockMap.blocks.size, p, range)
  }

  /**
    * The blockmap is mapped to a coordinate range in two directions,
    * so find the indices of the sectors that correspond to the region
    * defined by the range around a coordinate position.
    * @param spanSize the length and width of a sector
    * @param blocksInRow the number of sectors across the width (in a row) of the blockmap
    * @param blocksTotal the number of sectors in the blockmap
    * @param p the coordinate position
    * @param range a rectangular range aigned with lateral axes extending from a coordinate position
    * @return the indices of the sectors in the blockmap structure
    */
  private def findSectorIndices(spanSize: Int, blocksInRow: Int, blocksTotal: Int, p: Vector3, range: Float): Iterable[Int] = {
    val corners = {
      /*
      find the corners of a rectangular region extending in all cardinal directions from the position;
      transform these corners into four sector indices;
      if the first index matches the last index, the position and range are only in one sector;
        [----][----][----]
        [----][1234][----]
        [----][----][----]
      if the first and the second or the first and the third are further apart than an adjacent column or row,
      then the missing indices need to be filled in and all of those sectors include the position and range;
        [----][----][----][----][----]
        [----][1   ][    ][2   ][----]
        [----][    ][    ][    ][----]
        [----][3   ][    ][4   ][----]
        [----][----][----][----][----]
      if neither of the previous, just return all distinct corner indices
        [----][----][----][----]      [----][----][----]      [----][----][----][----]
        [----][1   ][2   ][----]      [----][1  2][----]      [----][1  3][2  4][----]
        [----][3   ][4   ][----]      [----][3  4][----]      [----][----][----][----]
        [----][----][----][----]      [----][----][----]
       */
      val blocksInColumn = blocksTotal / blocksInRow
      val lowx = math.max(0, p.x - range)
      val highx = math.min(p.x + range, (blocksInRow * spanSize - 1).toFloat)
      val lowy = math.max(0, p.y - range)
      val highy = math.min(p.y + range, (blocksInColumn * spanSize - 1).toFloat)
      Seq( (lowx,  lowy), (highx, lowy), (lowx,  highy), (highx, highy) )
    }.map { case (x, y) =>
      (y / spanSize).toInt * blocksInRow + (x / spanSize).toInt
    }
    if (corners.head == corners(3)) {
      List(corners.head)
    } else if (corners(1) - corners.head > 1 || corners(2) - corners.head > blocksInRow) {
      (0 to (corners(2) - corners.head) / blocksInRow).flatMap { d =>
        val perRow = d * blocksInRow
        (corners.head + perRow) to (corners(1) + perRow)
      }
    } else {
      corners.distinct
    }
  }

  /**
    * Calculate the range expressed by a certain entity that can be allocated into a sector on the blockmap.
    * Entities have different ways of expressing these ranges.
    * @param target the entity
    * @param defaultRadius a default radius, if no specific case is discovered;
    *                      if no default case, the default-default case is a single unit (`1.0f`)
    * @return the distance from a central position along the major axes
    */
  def rangeFromEntity(target: BlockMapEntity, defaultRadius: Option[Float] = None): Float = {
    target match {
      case b: Building =>
        //use the building's sphere of influence
        b.Definition.SOIRadius.toFloat// * 0.5f

      case o: PlanetSideGameObject =>
        //use the server geometry
        val pos = target.Position
        val v = o.Definition.Geometry(o)
        math.sqrt(math.max(
          Vector3.DistanceSquared(pos, v.pointOnOutside(Vector3(1,0,0)).asVector3),
          Vector3.DistanceSquared(pos, v.pointOnOutside(Vector3(0,1,0)).asVector3)
        )).toFloat

      case e: PieceOfEnvironment =>
        //use the bounds (like server geometry, but is alawys a rectangle on the XY-plane)
        val bounds = e.collision.bounding
        math.max(bounds.top - bounds.base, bounds.right - bounds.left) * 0.5f

      case _ =>
        //default and default-default
        defaultRadius.getOrElse(1.0f)
    }
  }

  /**
    * If only one sector, just return that sector.
    * If a group of sectors, organize them into a single referential sector.
    * @param to all allocated sectors
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def quickToSectorGroup(to: Iterable[Sector]): SectorPopulation = {
    if (to.size == 1) {
      SectorGroup(to.head)
    } else {
      SectorGroup(to)
    }
  }

  /**
    * If only one sector, just return that sector.
    * If a group of sectors, organize them into a single referential sector.
    * @param range a custom range value
    * @param to all allocated sectors
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def quickToSectorGroup(range: Float, to: Iterable[Sector]): SectorPopulation = {
    if (to.size == 1) {
      SectorGroup(range, to.head)
    } else {
      SectorGroup(range, to)
    }
  }
}

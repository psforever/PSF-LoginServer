// Copyright (c) 2021 PSForever
package net.psforever.objects.zones

import net.psforever.objects.ce.Deployable
import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.environment.PieceOfEnvironment
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.{PlanetSideGameObject, Player, Vehicle}
import net.psforever.types.Vector3

import scala.collection.mutable.ListBuffer

sealed case class BlockMapEntry(coords: Vector3, range: Float, sectors: Set[Int])

trait BlockMapEntity
  extends WorldEntity {
  private var _blockMapEntry: Option[BlockMapEntry] = None
  private var _updateBlockMapEntryFunc: (BlockMapEntity, Vector3) => Boolean = BlockMapEntity.doNotUpdateBlockMap

  def blockMapEntry: Option[BlockMapEntry] = _blockMapEntry

  def blockMapEntry_=(entry: Option[BlockMapEntry]): Option[BlockMapEntry] = {
    entry match {
      case None =>
        _updateBlockMapEntryFunc = BlockMapEntity.doNotUpdateBlockMap
        _blockMapEntry = None
      case Some(_) =>
        _updateBlockMapEntryFunc = BlockMapEntity.updateBlockMap
        _blockMapEntry = entry
    }
    entry
  }

  def sector(zone: Zone, range: Float): SectorPopulation = {
    zone.blockMap.sector(
      //TODO same zone check?
      _blockMapEntry match {
        case Some(entry) => entry.coords
        case None => Position
      },
      range
    )
  }

  def updateBlockMapEntry(newCoords: Vector3): Boolean = _updateBlockMapEntryFunc(this, newCoords)
}

object BlockMapEntity {
  private def doNotUpdateBlockMap(target: BlockMapEntity, newCoords: Vector3): Boolean = false

  private def updateBlockMap(target: BlockMapEntity, newCoords: Vector3): Boolean = {
    val oldEntry = target.blockMapEntry.get
    target.blockMapEntry = Some(BlockMapEntry(newCoords, oldEntry.range, oldEntry.sectors))
    true
  }
}

trait SectorPopulation {
  def livePlayerList: List[Player]

  def corpseList: List[Player]

  def vehicleList: List[Vehicle]

  def equipmentOnGroundList: List[Equipment]

  def deployableList: List[Deployable]

  def buildingList: List[Building]

  def environmentList: List[PieceOfEnvironment]

  def total: Int = {
    livePlayerList.size +
    corpseList.size +
    vehicleList.size +
    equipmentOnGroundList.size +
    deployableList.size +
    buildingList.size +
    environmentList.size
  }
}

trait SectorTraits {
  def longitude: Float

  def latitude: Float

  def span: Int
}

class SectorListOf[A](eqFunc: (A, A) => Boolean = (a: A, b: A) => a equals b) {
  private val internalList: ListBuffer[A] = ListBuffer[A]()

  def addTo(elem: A): List[A] = {
    internalList.indexWhere { item => eqFunc(elem, item) } match {
      case -1 => internalList.addOne(elem)
      case _  => ;
    }
    list
  }

  def removeFrom(elem: A): List[A] = {
    internalList.indexWhere { item => eqFunc(elem, item) } match {
      case -1    => ;
      case index => internalList.remove(index)
    }
    list
  }

  def list: List[A] = internalList.toList
}

class Sector(val longitude: Int, val latitude: Int, val span: Int)
  extends SectorPopulation {
  val livePlayers: SectorListOf[Player] = new SectorListOf[Player](
    (a: Player, b: Player) => a.CharId == b.CharId
  )

  val corpses: SectorListOf[Player] = new SectorListOf[Player](
    (a: Player, b: Player) => a.GUID == b.GUID || (a eq b)
  )

  val vehicles: SectorListOf[Vehicle] = new SectorListOf[Vehicle](
    (a: Vehicle, b: Vehicle) => a eq b
  )

  val equipmentOnGround: SectorListOf[Equipment] = new SectorListOf[Equipment](
    (a: Equipment, b: Equipment) => a eq b
  )

  val deployables: SectorListOf[Deployable] = new SectorListOf[Deployable](
    (a: Deployable, b: Deployable) => a eq b
  )

  val buildings: SectorListOf[Building] = new SectorListOf[Building](
    (a: Building, b: Building) => a.Name.equals(b.Name)
  )

  val environment: SectorListOf[PieceOfEnvironment] = new SectorListOf[PieceOfEnvironment](
    (a: PieceOfEnvironment, b: PieceOfEnvironment) => a eq b
  )

  def livePlayerList : List[Player] = livePlayers.list

  def corpseList: List[Player] = corpses.list

  def vehicleList: List[Vehicle] = vehicles.list

  def equipmentOnGroundList: List[Equipment] = equipmentOnGround.list

  def deployableList: List[Deployable] = deployables.list

  def buildingList: List[Building] = buildings.list

  def environmentList: List[PieceOfEnvironment] = environment.list

  def addTo(o: BlockMapEntity): Boolean = {
    o match {
      case p: Player =>
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
      case e: PieceOfEnvironment =>
        environment.list.size < environment.addTo(e).size
      case _ =>
        false
    }
  }

  def removeFrom(o: Any): Boolean = {
    o match {
      case p: Player =>
        if (!p.isBackpack) livePlayers.list.size > livePlayers.removeFrom(p).size
        else corpses.list.size > corpses.removeFrom(p).size
      case v: Vehicle =>
        vehicles.list.size > vehicles.removeFrom(v).size
      case e: Equipment =>
        equipmentOnGround.list.size > equipmentOnGround.removeFrom(e).size
      case d: Deployable =>
        deployables.list.size > deployables.removeFrom(d).size
      case _ =>
        false
    }
  }
}

class SectorGroup(
                   val livePlayerList: List[Player],
                   val corpseList: List[Player],
                   val vehicleList: List[Vehicle],
                   val equipmentOnGroundList: List[Equipment],
                   val deployableList: List[Deployable],
                   val buildingList: List[Building],
                   val environmentList: List[PieceOfEnvironment]
                 )
  extends SectorPopulation

object SectorGroup {
  def apply(sectors: Iterable[Sector]): SectorGroup = {
    new SectorGroup(
      sectors.flatMap { _.livePlayerList }.toList.distinct,
      sectors.flatMap { _.corpseList }.toList.distinct,
      sectors.flatMap { _.vehicleList }.toList.distinct,
      sectors.flatMap { _.equipmentOnGroundList }.toList.distinct,
      sectors.flatMap { _.deployableList }.toList.distinct,
      sectors.flatMap { _.buildingList }.toList.distinct,
      sectors.flatMap { _.environmentList }.toList.distinct
    )
  }
}

class BlockMap(fullMapWidth: Int, fullMapHeight: Int, desiredSpanSize: Int) {
  val spanSize: Int = math.min(math.max(1, desiredSpanSize), fullMapWidth)
  val blocksInRow: Int = fullMapWidth / spanSize + (if (fullMapWidth % spanSize > 0) 1 else 0)
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

  def sector(entity: BlockMapEntity): SectorPopulation = {
    entity.blockMapEntry match {
      case Some(entry) =>
        val output = entry.sectors.map { blocks }
        if (output.size == 1) {
          output.head
        } else {
          SectorGroup(output)
        }
      case None =>
        SectorGroup(Nil)
    }
  }

  def sector(p: Vector3, range: Float): SectorPopulation = {
    val output = BlockMap.sectorIndices(blockMap = this, p, range).map { blocks }
    if (output.size == 1) {
      output.head
    } else {
      SectorGroup(output)
    }
  }

  def addTo(target: BlockMapEntity): SectorPopulation = {
    addTo(target, target.Position)
  }

  def addTo(target: BlockMapEntity, toPosition: Vector3): SectorPopulation = {
    addTo(target, toPosition, BlockMap.rangeFromEntity(target))
  }

  def addTo(target: BlockMapEntity, range: Float): SectorPopulation = {
    addTo(target, target.Position, range)
  }

  def addTo(target: BlockMapEntity, toPosition: Vector3, range: Float): SectorPopulation = {
    val to = BlockMap.sectorIndices(blockMap = this, toPosition, range)
    val toSectors = to.toSet.map { blocks }
    toSectors.foreach { block => block.addTo(target) }
    target.blockMapEntry = Some(BlockMapEntry(toPosition, range, to.toSet))
    if (to.size == 1) {
      toSectors.head
    } else {
      SectorGroup(toSectors)
    }
  }

  def removeFrom(target: BlockMapEntity): SectorPopulation = {
    target.blockMapEntry match {
      case Some(entry) => removeFrom(target, entry.coords, entry.range)
      case None        => SectorGroup(Nil)
    }
  }

  def removeFrom(target: BlockMapEntity, fromPosition: Vector3): SectorPopulation = {
    removeFrom(target)
  }

  def removeFrom(target: BlockMapEntity, range: Float): SectorPopulation =
    removeFrom(target)

  def removeFrom(target: BlockMapEntity, fromPosition: Vector3, range: Float): SectorPopulation = {
    val from = target.blockMapEntry.get.sectors.map { blocks }
    target.blockMapEntry = None
    from.foreach { block => block.removeFrom(target) }
    if (from.size == 1) {
      from.head
    } else {
      SectorGroup(from)
    }
  }

  def ensureRemoveFrom(target: BlockMapEntity): SectorPopulation = {
    target.blockMapEntry = None
    val foundSectors = blocks.filter { sector =>
      target match {
        case p: Player     => !p.isBackpack && sector.livePlayers.list.contains(p) || sector.corpses.list.contains(p)
        case v: Vehicle    => sector.vehicles.list.contains(v)
        case e: Equipment  => sector.equipmentOnGround.list.contains(e)
        case d: Deployable => sector.deployables.list.contains(d)
        case _             => false
      }
    }
    foundSectors.foreach { _.removeFrom(target) }
    SectorGroup(foundSectors)
  }

  def move(target: BlockMapEntity): SectorPopulation = {
    target.blockMapEntry match {
      case Some(entry) => move(target, target.Position, entry.coords, entry.range)
      case None        => SectorGroup(Nil)
    }
  }

  def move(target: BlockMapEntity, toPosition: Vector3): SectorPopulation = {
    target.blockMapEntry match {
      case Some(entry) => move(target, toPosition, entry.coords, entry.range)
      case None        => SectorGroup(Nil)
    }
  }

  def move(target: BlockMapEntity, toPosition: Vector3, fromPosition: Vector3): SectorPopulation = {
    move(target, toPosition)
  }

  def move(target: BlockMapEntity, toPosition: Vector3, fromPosition: Vector3, range: Float): SectorPopulation = {
    target.blockMapEntry match {
      case Some(entry) =>
        val from = entry.sectors
        val to = BlockMap.sectorIndices(blockMap = this, toPosition, range).toSet
        from.diff(to).foreach { index => blocks(index).removeFrom(target) }
        to.diff(from).foreach { index => blocks(index).addTo(target) }
        val out = to.map { blocks }
        target.blockMapEntry = Some(BlockMapEntry(toPosition, range, to))
        if (out.size == 1) {
          out.head
        } else {
          SectorGroup(out)
        }
      case None    =>
        SectorGroup(Nil)
    }
  }
}

object BlockMap {
  def apply(scale: MapScale, spanSize: Int): BlockMap = {
    new BlockMap(scale.width.toInt, scale.height.toInt, spanSize)
  }

  def sectorIndices(blockMap: BlockMap, p: Vector3, range: Float): Iterable[Int] = {
    sectorIndices(blockMap.spanSize, blockMap.blocksInRow, blockMap.blocks.size, p, range)
  }

  def sectorIndices(spanSize: Int, blocksInRow: Int, blocksTotal: Int, p: Vector3, range: Float): Iterable[Int] = {
    val corners = {
      val blocksInColumn = blocksTotal / blocksInRow
      val lowx = math.max(0, p.x - range)
      val highx = math.min(p.x + range, (blocksInRow * spanSize - 1).toFloat)
      val lowy = math.max(0, p.y - range)
      val highy = math.min(p.y + range, (blocksInColumn * spanSize - 1).toFloat)
      Seq( (lowx,  lowy), (highx, lowy), (lowx,  highy), (highx, highy) )
    }.map { case (x, y) =>
      (y / spanSize).toInt * blocksInRow + (x / spanSize).toInt
    }
    if (corners(1) - corners.head > 1 || corners(2) - corners.head > blocksInRow) {
      (0 to (corners(2) - corners.head) / blocksInRow).flatMap { d =>
        val perRow = d * blocksInRow
        (corners.head + perRow) to (corners(1) + perRow)
      }
    } else {
      corners.distinct
    }
  }

  def rangeFromEntity(target: BlockMapEntity, defaultRadius: Option[Float] = None): Float = {
    target match {
      case b: Building =>
        b.Definition.SOIRadius.toFloat * 0.5f

      case o: PlanetSideGameObject =>
        val pos = target.Position
        val v = o.Definition.Geometry(o)
        math.sqrt(math.max(
          Vector3.DistanceSquared(pos, v.pointOnOutside(Vector3(1,0,0)).asVector3),
          Vector3.DistanceSquared(pos, v.pointOnOutside(Vector3(0,1,0)).asVector3)
        )).toFloat

      case e: PieceOfEnvironment =>
        val bounds = e.collision.bounding
        math.max(bounds.top - bounds.base, bounds.right - bounds.left) * 0.5f

      case _ =>
        defaultRadius.getOrElse(1.0f)
    }
  }
}

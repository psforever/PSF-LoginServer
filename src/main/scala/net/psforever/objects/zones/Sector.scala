// Copyright (c) 2021 PSForever
package net.psforever.objects.zones

import net.psforever.objects.ce.Deployable
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.environment.PieceOfEnvironment
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.{PlanetSideGameObject, Player, Vehicle}
import net.psforever.types.Vector3

import scala.collection.mutable.ListBuffer

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
  def longitude: Int

  def latitude: Int

  def span: Int
}

class SectorListOf[A](eqFunc: (A, A) => Boolean = (a: A, b: A) => a equals b){
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
    (a: Player, b: Player) => a eq b
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

  def addTo(o: Any): Boolean = {
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

class BlockMap(fullMapSize: Int, spanSize: Int) {
  val (blocks, blocksInRow): (ListBuffer[Sector], Int) = {
    val corners: List[Int] = List.range(0, fullMapSize, spanSize)
    (ListBuffer.newBuilder[Sector].addAll(
      corners.flatMap { latitude =>
        corners.map { longitude =>
          new Sector(longitude, latitude, spanSize)
        }
      }
    ).result(), corners.size)
  }

  private def sectorIndices(p: Vector3, range: Float): Iterable[Int] = {
    val corners = Seq(
      p + Vector3(-range,  range, 0),
      p + Vector3( range,  range, 0),
      p + Vector3(-range, -range, 0),
      p + Vector3( range, -range, 0)
    ).map { d => (d.y / spanSize).toInt * blocksInRow + (d.x / spanSize).toInt }
    if (corners(1) - corners.head > 2 || corners(3) - corners.head > 2) {
      var d = 0
      (corners.head to corners(2)).flatMap { _ =>
        val _out = corners.head + d to corners(1) + d
        d += blocksInRow
        _out
      }
    } else {
      corners.distinct.sorted
    }
  }

  def sector(p: Vector3, range: Float): SectorPopulation = {
    val output = sectorIndices(p, range).map { blocks }
    if (output.size == 1) {
      output.head
    } else {
      SectorGroup(output)
    }
  }

  def addTo(target: PlanetSideGameObject): SectorPopulation = {
    val range = {
      val v = target.Definition.Geometry(target)
      val pos = target.Position
      math.sqrt(math.max(
        Vector3.DistanceSquared(pos, v.pointOnOutside(Vector3(1,0,0)).asVector3),
        Vector3.DistanceSquared(pos, v.pointOnOutside(Vector3(0,1,0)).asVector3)
      ))
    }.toFloat
    addTo(target, target.Position, range)
  }

  def addTo(target: PlanetSideGameObject, range: Float): SectorPopulation = {
    addTo(target, target.Position, range)
  }

  def addTo(target: Any, toPosition: Vector3, range: Float): SectorPopulation = {
    val to = sectorIndices(toPosition, range).toSet.map { blocks }
    to.foreach { block => block.addTo(target) }
    if (to.size == 1) {
      to.head
    } else {
      SectorGroup(to)
    }
  }

  def removeFrom(target: PlanetSideGameObject): SectorPopulation = {
    val range = {
      val v = target.Definition.Geometry(target)
      val pos = target.Position
      math.sqrt(math.max(
        Vector3.DistanceSquared(pos, v.pointOnOutside(Vector3(1,0,0)).asVector3),
        Vector3.DistanceSquared(pos, v.pointOnOutside(Vector3(0,1,0)).asVector3)
      ))
    }.toFloat
    removeFrom(target, target.Position, range)
  }

  def removeFrom(target: PlanetSideGameObject, range: Float): SectorPopulation =
    removeFrom(target, target.Position, range)

  def removeFrom(target: Any, fromPosition: Vector3, range: Float): SectorPopulation = {
    val from = sectorIndices(fromPosition, range).toSet.map { blocks }
    from.foreach { block => block.removeFrom(target) }
    if (from.size == 1) {
      from.head
    } else {
      SectorGroup(from)
    }
  }

  def ensureRemoveFrom(target: PlanetSideGameObject): SectorPopulation = {
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

  def move(target: PlanetSideGameObject, toPosition: Vector3, range: Float): SectorPopulation =
    move(target, toPosition, target.Position, range)

  def move(target: Any, toPosition: Vector3, fromPosition: Vector3, range: Float): SectorPopulation = {
    val from = sectorIndices(fromPosition, range).toSet
    val to = sectorIndices(toPosition, range).toSet
    from.diff(to).foreach { index => blocks(index).removeFrom(target) }
    to.diff(from).foreach { index => blocks(index).addTo(target) }
    val out = to.map { blocks }
    if (out.size == 1) {
      out.head
    } else {
      SectorGroup(out)
    }
  }
}

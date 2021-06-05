// Copyright (c) 2021 PSForever
package net.psforever.objects.zones.blockmap

import net.psforever.objects.ce.Deployable
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.environment.PieceOfEnvironment
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.{Player, Vehicle}

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

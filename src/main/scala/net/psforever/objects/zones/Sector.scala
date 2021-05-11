// Copyright (c) 2021 PSForever
package net.psforever.objects.zones

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
}

class SectorGroup(sectors: Iterable[Sector])
  extends SectorPopulation {
  def livePlayerList : List[Player] = sectors.flatMap { _.livePlayerList } toList

  def corpseList: List[Player] = sectors.flatMap { _.corpseList } toList

  def vehicleList: List[Vehicle] = sectors.flatMap { _.vehicleList } toList

  def equipmentOnGroundList: List[Equipment] = sectors.flatMap { _.equipmentOnGroundList } toList

  def deployableList: List[Deployable] = sectors.flatMap { _.deployableList } toList

  def buildingList: List[Building] = sectors.flatMap { _.buildingList } toList

  def environmentList: List[PieceOfEnvironment] = sectors.flatMap { _.environmentList } toList
}

class BlockMap(spanSize: Int) {
  val blocks: ListBuffer[Sector] = {
    val corners: List[Int] = List.range(0, 8192, spanSize)
    val allCorners = corners ++ {
      val edge = spanSize * corners.length
      if (edge < 8192) {
        List(edge)
      } else {
        Nil
      }
    }
    ListBuffer.newBuilder[Sector](
      allCorners.flatMap { latitude =>
        allCorners.map { longitude =>
          new Sector(longitude, latitude, spanSize)
        }
      }
    )
  }
}

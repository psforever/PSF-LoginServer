// Copyright (c) 2019 PSForever
package net.psforever.objects.teamwork

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire

class Squad(squadId : PlanetSideGUID, alignment : PlanetSideEmpire.Value) extends IdentifiableEntity {
  super.GUID_=(squadId)
  private val faction : PlanetSideEmpire.Value = alignment //does not change
  private var zoneId : Option[Int] = None
  private var task : String = ""
  private var description : String = ""
  private val membership : Array[Member] = Array.fill[Member](10)(new Member)
  private val availability : Array[Boolean] = Array.fill[Boolean](10)(true)
  private var listed : Boolean = false
  private var leaderPositionIndex : Int = 0

  override def GUID_=(d : PlanetSideGUID) : PlanetSideGUID = GUID

  def Faction : PlanetSideEmpire.Value = faction

  def ZoneId : Int = zoneId.getOrElse({
    membership.headOption match {
      case Some(leader) =>
        leader.ZoneId
      case _ =>
        0
    }
  })

  def ZoneId_=(id : Int) : Int = {
    ZoneId_=(Some(id))
  }

  def ZoneId_=(id : Option[Int]) : Int = {
    zoneId = id
    ZoneId
  }

  def Task : String = task

  def Task_=(assignment : String) : String = {
    task = assignment
    Task
  }

  def Description : String = description

  def Description_=(desc : String) : String = {
    description = desc
    Description
  }

  def Listed : Boolean = listed

  def Listed_=(announce : Boolean) : Boolean = {
    listed = announce
    Listed
  }

  def Membership : Array[Member] = membership

  def Availability : Array[Boolean] = availability

  def LeaderPositionIndex : Int = leaderPositionIndex

  def LeaderPositionIndex_=(position : Int) : Int = {
    if(availability.lift(position).contains(true)) {
      leaderPositionIndex = position
    }
    LeaderPositionIndex
  }

  def Leader : String = {
    membership.lift(leaderPositionIndex) match {
      case Some(member) =>
        member.Name
      case None =>
        ""
    }
  }

  def Size : Int = membership.count(member => !member.Name.equals(""))

  def Capacity : Int = availability.count(open => open)
}

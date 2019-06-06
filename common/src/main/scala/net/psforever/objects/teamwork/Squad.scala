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
  private val membership : Array[Member] = Array.fill[Member](10)(new Member)
  private val availability : Array[Boolean] = Array.fill[Boolean](10)(true)
  private var listed : Boolean = false
  private var leaderPositionIndex : Int = 0
  private var autoApproveInvitationRequests : Boolean = false
  private var locationFollowsSquadLead : Boolean = false

  override def GUID_=(d : PlanetSideGUID) : PlanetSideGUID = GUID

  def Faction : PlanetSideEmpire.Value = faction

  def CustomZoneId : Boolean = zoneId.isDefined

  def ZoneId : Int = zoneId.getOrElse({
    membership.lift(leaderPositionIndex) match {
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

  def Listed : Boolean = listed

  def Listed_=(announce : Boolean) : Boolean = {
    listed = announce
    Listed
  }

  def LocationFollowsSquadLead : Boolean = locationFollowsSquadLead

  def LocationFollowsSquadLead_=(follow : Boolean) : Boolean = {
    locationFollowsSquadLead = follow
    LocationFollowsSquadLead
  }

  def AutoApproveInvitationRequests : Boolean = autoApproveInvitationRequests

  def AutoApproveInvitationRequests_=(autoApprove : Boolean) : Boolean = {
    autoApproveInvitationRequests = autoApprove
    AutoApproveInvitationRequests
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

object Squad {
  final val Blank = new Squad(PlanetSideGUID(0), PlanetSideEmpire.NEUTRAL) {
    override def ZoneId : Int = 0
    override def ZoneId_=(id : Int) : Int = 0
    override def ZoneId_=(id : Option[Int]) : Int = 0
    override def Task_=(assignment : String) : String =  ""
    override def Listed_=(announce : Boolean) : Boolean = false
    override def Membership : Array[Member] = Array.empty[Member]
    override def Availability : Array[Boolean] = Array.fill[Boolean](10)(false)
    override def LeaderPositionIndex_=(position : Int) : Int = 0
  }
}

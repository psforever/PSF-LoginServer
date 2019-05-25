// Copyright (c) 2019 PSForever
package net.psforever.objects.teamwork

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire

class Squad(squadId : PlanetSideGUID, alignment : PlanetSideEmpire.Value) extends IdentifiableEntity {
  super.GUID_=(squadId)
  private val faction : PlanetSideEmpire.Value = alignment //does not change
  private val zoneId : Option[String] = None
  private var task : String = ""
  private var description : String = ""
  private val membership : Array[Member] = Array.fill[Member](10)(new Member)
  private val availability : Array[Boolean] = Array.fill[Boolean](10)(true)

  override def GUID_=(d : PlanetSideGUID) : PlanetSideGUID = GUID

  def Faction : PlanetSideEmpire.Value = faction

  def ZoneId : String = zoneId.getOrElse({
    membership.headOption match {
      case Some(leader) =>
        leader.ZoneId
      case _ =>
        "Nowhere"
    }
  })

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

  def Membership : Array[Member] = membership

  def Availability : Array[Boolean] = availability

  def Size : Int = membership.count(member => !member.Name.equals(""))

  def Capacity : Int = availability.count(open => open)
}

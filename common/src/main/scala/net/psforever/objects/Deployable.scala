// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire

trait Deployable extends FactionAffinity {
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private var owner : Option[PlanetSideGUID] = None

  def Faction : PlanetSideEmpire.Value = faction

  override def Faction_=(toFaction : PlanetSideEmpire.Value) : PlanetSideEmpire.Value = {
    faction = toFaction
    Faction
  }

  def Owner : Option[PlanetSideGUID] = owner

  def Owner_=(owner : PlanetSideGUID) : Option[PlanetSideGUID] = Owner_=(Some(owner))

  def Owner_=(owner : Player) : Option[PlanetSideGUID] = Owner_=(Some(owner.GUID))

  def Owner_=(owner : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    owner match {
      case Some(_) =>
        this.owner = owner
      case None =>
        this.owner = None
    }
    Owner
  }
}

object Deployable {
  final case class DeployableBuilt(obj : PlanetSideGameObject with Deployable)
}

class ExplosiveDeployable(cdef : ObjectDefinition) extends PlanetSideGameObject
  with Deployable {

  def Definition = cdef
}

class SensorDeployable(cdef : ObjectDefinition) extends PlanetSideGameObject
  with Deployable
  with Hackable {
  def Definition = cdef
}

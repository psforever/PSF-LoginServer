// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.locks

import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.types.{PlanetSideEmpire, Vector3}

class IFFLock extends PlanetSideServerObject {
  private var hackedBy : Option[(Player, Vector3)] = None

  def HackedBy : Option[(Player, Vector3)] = hackedBy

  def HackedBy_=(agent : Player) : Option[(Player, Vector3)] = HackedBy_=(Some(agent))

  def HackedBy_=(agent : Option[Player]) : Option[(Player, Vector3)] = {
    hackedBy match {
      case None =>
        if(agent.isDefined) {
          hackedBy = Some(agent.get, agent.get.Position)
        }
      case Some(_) =>
        if(agent.isEmpty) {
          hackedBy = None
        }
        else if(agent.get.Faction != hackedBy.get._1.Faction) {
          hackedBy = Some(agent.get, agent.get.Position) //overwrite
        }
    }
    HackedBy
  }

  def Definition : IFFLockDefinition = GlobalDefinitions.external_lock
}

object IFFLock {
  def apply() : IFFLock = {
    new IFFLock
  }
}

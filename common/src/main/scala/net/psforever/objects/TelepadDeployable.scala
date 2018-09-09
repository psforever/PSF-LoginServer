// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ce.SimpleDeployable
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.packet.game.PlanetSideGUID

class TelepadDeployable(ddef : DeployableDefinition) extends SimpleDeployable(ddef) {
  private var router : Option[PlanetSideGUID] = None
  private var activated : Boolean = false

  def Router : Option[PlanetSideGUID] = router

  def Router_=(rguid : PlanetSideGUID) : Option[PlanetSideGUID] = Router_=(Some(rguid))

  def Router_=(rguid : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    router match {
      case None =>
        router = rguid
      case Some(_) =>
        if(rguid.isEmpty || rguid.contains(PlanetSideGUID(0))) {
          router = None
        }
    }
    Router
  }

  def Active : Boolean = activated

  def Active_=(state : Boolean) : Boolean = {
    activated = state
    Active
  }
}

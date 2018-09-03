// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.ConstructionItemDefinition
import net.psforever.packet.game.PlanetSideGUID

class Telepad(private val cdef : ConstructionItemDefinition) extends ConstructionItem(cdef) {
  private var router : Option[PlanetSideGUID] = None

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
}

object Telepad {
  def apply(cdef : ConstructionItemDefinition) : Telepad = {
    new Telepad(cdef)
  }
}
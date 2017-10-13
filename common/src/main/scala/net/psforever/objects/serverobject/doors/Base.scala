// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.doors

import net.psforever.types.PlanetSideEmpire

/**
  * A temporary class to represent "facilities" and "structures."
  * @param id the map id of the base
  */
class Base(private val id : Int) {
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL

  def Id : Int = id

  def Faction : PlanetSideEmpire.Value = faction

  def Faction_=(emp : PlanetSideEmpire.Value) : PlanetSideEmpire.Value = {
    faction = emp
    Faction
  }
}

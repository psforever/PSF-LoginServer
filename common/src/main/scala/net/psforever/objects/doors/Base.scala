// Copyright (c) 2017 PSForever
package net.psforever.objects.doors

import net.psforever.types.PlanetSideEmpire

class Base(private val id : Int) {
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL

  def Id : Int = id

  def Faction : PlanetSideEmpire.Value = faction

  def Faction_=(emp : PlanetSideEmpire.Value) : PlanetSideEmpire.Value = {
    faction = emp
    Faction
  }
}

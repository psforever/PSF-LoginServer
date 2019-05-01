// Copyright (c) 2019 PSForever
package net.psforever.objects.zones

import net.psforever.types.{PlanetSideEmpire, Vector3}

class HotSpotInfo(val Factions : Seq[PlanetSideEmpire.Value],
                  val DisplayLocation : Vector3
                 ) {
  def Attacker : Set[PlanetSideEmpire.Value] = Factions take 1 toSet

  def Defenders : Set[PlanetSideEmpire.Value] = Factions drop 1 toSet
}

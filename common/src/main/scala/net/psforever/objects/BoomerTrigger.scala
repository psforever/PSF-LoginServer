// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.equipment.RemoteUnit
import net.psforever.types.PlanetSideEmpire

class BoomerTrigger extends SimpleItem(GlobalDefinitions.boomer_trigger) with RemoteUnit {
  override def Faction_=(fact: PlanetSideEmpire.Value): PlanetSideEmpire.Value = Faction
}

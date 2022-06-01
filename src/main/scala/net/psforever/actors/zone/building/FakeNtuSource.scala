// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import net.psforever.objects.{GlobalDefinitions, NtuContainer}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.structures.Building

/**
  * A nanite transfer unit provision device for this building.
  * It does not actually belong to the building as an `Amenity`-level feature.
  * In essence, "it does not exist".
  * @param building the building
  */
protected class FakeNtuSource(private val building: Building)
  extends PlanetSideServerObject
    with NtuContainer {
  override def NtuCapacitor = Int.MaxValue.toFloat
  override def NtuCapacitor_=(a: Float) = Int.MaxValue.toFloat
  override def MaxNtuCapacitor = Int.MaxValue.toFloat
  override def Faction = building.Faction
  override def Zone = building.Zone
  override def Definition = GlobalDefinitions.resource_silo
}

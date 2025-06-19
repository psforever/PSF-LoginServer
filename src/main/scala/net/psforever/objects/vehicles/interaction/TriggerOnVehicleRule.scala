// Copyright (c) 2025 PSForever
package net.psforever.objects.vehicles.interaction

import net.psforever.objects.ce.TriggerTest
import net.psforever.objects.ExplosiveDeployable
import net.psforever.objects.geometry.d3.VolumetricGeometry
import net.psforever.objects.zones.Zone

case object TriggerOnVehicleRule
  extends TriggerTest {
  def test(g: VolumetricGeometry, obj: ExplosiveDeployable, radius: Float): Boolean = {
    Zone.distanceCheck(g, obj, radius * radius)
  }
}

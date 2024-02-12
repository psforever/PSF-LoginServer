// Copyright (c) 2023 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.types.{PlanetSideGUID, Vector3}

final case class UniqueAmenity(
    zoneNumber: Int,
    guid: PlanetSideGUID,
    position: Vector3
) extends SourceUniqueness

object UniqueAmenity {
  def apply(obj: Amenity): UniqueAmenity = {
    UniqueAmenity(obj.Zone.Number, obj.GUID, obj.Position)
  }
}

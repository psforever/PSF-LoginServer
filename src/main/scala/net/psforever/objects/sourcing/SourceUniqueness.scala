// Copyright (c) 2024 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.ce.Deployable
import net.psforever.objects.{PlanetSideGameObject, Player, TurretDeployable, Vehicle}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.serverobject.turret.FacilityTurret

trait SourceUniqueness

object SourceUniqueness {
  def apply(target: PlanetSideGameObject with FactionAffinity): SourceUniqueness = {
    target match {
      case obj: Player           => UniquePlayer(obj)
      case obj: Vehicle          => UniqueVehicle(obj)
      case obj: FacilityTurret   => UniqueAmenity(obj)
      case obj: Amenity          => UniqueAmenity(obj)
      case obj: TurretDeployable => UniqueDeployable(obj)
      case obj: Deployable       => UniqueDeployable(obj)
      case obj: Building         => UniqueBuilding(obj)
      case _                     => UniqueObject(target)
    }
  }
}

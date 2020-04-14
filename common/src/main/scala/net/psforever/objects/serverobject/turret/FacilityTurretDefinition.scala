// Copyright (c) 2019 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.serverobject.structures.AmenityDefinition
import net.psforever.objects.vital.{StandardResolutions, StandardVehicleDamage, StandardVehicleResistance}

/**
  * The definition for any `FacilityTurret`.
  * @param objectId the object's identifier number
  */
class FacilityTurretDefinition(private val objectId : Int) extends AmenityDefinition(objectId)
  with TurretDefinition {
  DamageUsing = StandardVehicleDamage
  ResistUsing = StandardVehicleResistance
  Model = StandardResolutions.FacilityTurrets
}


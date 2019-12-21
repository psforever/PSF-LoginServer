// Copyright (c) 2019 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.vital.{StandardResolutions, StandardVehicleDamage, StandardVehicleResistance}

/**
  * The definition for any `FacilityTurret`.
  * @param objectId the object's identifier number
  */
class FacilityTurretDefinition(private val objectId : Int) extends ObjectDefinition(objectId)
  with TurretDefinition {
  Damage = StandardVehicleDamage
  Resistance = StandardVehicleResistance
  Model = StandardResolutions.FacilityTurrets
}


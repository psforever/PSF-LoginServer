// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.structures

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.{DamageResistanceModel, StandardAmenityResistance, StandardResolutions, VitalityDefinition}
import net.psforever.objects.vital.resistance.ResistanceProfileMutators

final case class AutoRepair(amount: Int, start: Long, repeat: Long, drain: Float)

abstract class AmenityDefinition(objectId: Int)
    extends ObjectDefinition(objectId)
    with ResistanceProfileMutators
    with DamageResistanceModel
    with VitalityDefinition {
  Name = "amenity"
  DamageUsing = DamageCalculations.AgainstVehicle
  ResistUsing = StandardAmenityResistance
  Model = StandardResolutions.Amenities

  var autoRepair: Option[AutoRepair] = None

  def autoRepair_=(auto: AutoRepair): Option[AutoRepair] = {
    autoRepair = Some(auto)
    autoRepair
  }
}

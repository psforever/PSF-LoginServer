// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.vital.resolution._

object NoResolutions
    extends DamageResistanceCalculations(
      ResolutionCalculations.NoDamage,
      ResolutionCalculations.NoApplication
    )

object InfantryResolutions
    extends DamageResistanceCalculations(
      ResolutionCalculations.InfantryDamage,
      ResolutionCalculations.InfantryApplication
    )

object MaxResolutions
    extends DamageResistanceCalculations(
      ResolutionCalculations.MaxDamage,
      ResolutionCalculations.InfantryApplication
    )

object VehicleResolutions
    extends DamageResistanceCalculations(
      ResolutionCalculations.VehicleDamageAfterResist,
      ResolutionCalculations.VehicleApplication
    )

object SimpleResolutions
    extends DamageResistanceCalculations(
      ResolutionCalculations.VehicleDamageAfterResist,
      ResolutionCalculations.SimpleApplication
    )

object ComplexDeployableResolutions
    extends DamageResistanceCalculations(
      ResolutionCalculations.VehicleDamageAfterResist,
      ResolutionCalculations.ComplexDeployableApplication
    )

object StandardResolutions extends ResolutionSelection {
  def Infantry: ResolutionCalculations.Form           = InfantryResolutions.Calculate
  def Max: ResolutionCalculations.Form                = MaxResolutions.Calculate
  def Vehicle: ResolutionCalculations.Form            = VehicleResolutions.Calculate
  def Aircraft: ResolutionCalculations.Form           = VehicleResolutions.Calculate
  def SimpleDeployables: ResolutionCalculations.Form  = SimpleResolutions.Calculate
  def ComplexDeployables: ResolutionCalculations.Form = ComplexDeployableResolutions.Calculate
  def FacilityTurrets: ResolutionCalculations.Form    = SimpleResolutions.Calculate
  def Amenities: ResolutionCalculations.Form          = SimpleResolutions.Calculate
}

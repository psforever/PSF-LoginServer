// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.vital.resolution._

object NoResolutions extends DamageResistCalculations(
  ResolutionCalculations.NoDamage,
  ResolutionCalculations.NoApplication
)

object InfantryResolutions extends DamageResistCalculations(
  ResolutionCalculations.InfantryDamageAfterResist,
  ResolutionCalculations.InfantryApplication
)

object MaxResolutions extends DamageResistCalculations(
  ResolutionCalculations.MaxDamageAfterResist,
  ResolutionCalculations.InfantryApplication
)

object VehicleResolutions extends DamageResistCalculations(
  ResolutionCalculations.VehicleDamageAfterResist,
  ResolutionCalculations.VehicleApplication
)

object StandardResolutions extends ResolutionSelection {
  def Infantry : ResolutionCalculations.Form = InfantryResolutions.Calculate
  def Max : ResolutionCalculations.Form = MaxResolutions.Calculate
  def Vehicle : ResolutionCalculations.Form = VehicleResolutions.Calculate
  def Aircraft : ResolutionCalculations.Form = VehicleResolutions.Calculate
}

// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.vital.resolution._

object NoResolutions
    extends DamageResistanceCalculations(
      ResolutionCalculations.NoDamage,
      ResolutionCalculations.NoApplication
    )

object AnyResolutions
    extends DamageResistanceCalculations(
      ResolutionCalculations.WildcardCalculations,
      ResolutionCalculations.WildcardApplication
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

object BfrResolutions
  extends DamageResistanceCalculations(
    ResolutionCalculations.VehicleDamageAfterResist,
    ResolutionCalculations.BfrApplication
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

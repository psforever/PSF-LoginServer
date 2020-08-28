// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.ballistics.{ObjectSource, PlayerSource, SourceEntry, VehicleSource}
import net.psforever.objects.vital.projectile.ProjectileCalculations
import net.psforever.objects.vital.resistance.{ResistanceCalculations, ResistanceSelection}

object NoResistance
  extends ResistanceCalculations[SourceEntry](
    ResistanceCalculations.ValidInfantryTarget,
    ResistanceCalculations.NoResistExtractor
  )

object InfantryHitResistance
  extends ResistanceCalculations[PlayerSource](
    ResistanceCalculations.ValidInfantryTarget,
    ResistanceCalculations.ExoSuitDirectExtractor
  )

object InfantrySplashResistance
  extends ResistanceCalculations[PlayerSource](
    ResistanceCalculations.ValidInfantryTarget,
    ResistanceCalculations.ExoSuitSplashExtractor
  )

object InfantryLashResistance
  extends ResistanceCalculations[PlayerSource](
    ResistanceCalculations.ValidInfantryTarget,
    ResistanceCalculations.MaximumResistance
  )

object InfantryAggravatedResistance
  extends ResistanceCalculations[PlayerSource](
    ResistanceCalculations.ValidInfantryTarget,
    ResistanceCalculations.ExoSuitAggravatedExtractor
  )

object VehicleHitResistance
  extends ResistanceCalculations[VehicleSource](
    ResistanceCalculations.ValidVehicleTarget,
    ResistanceCalculations.VehicleDirectExtractor
  )

object VehicleSplashResistance
  extends ResistanceCalculations[VehicleSource](
    ResistanceCalculations.ValidVehicleTarget,
    ResistanceCalculations.VehicleSplashExtractor
  )

object VehicleLashResistance
  extends ResistanceCalculations[VehicleSource](
    ResistanceCalculations.ValidVehicleTarget,
    ResistanceCalculations.NoResistExtractor
  )

object VehicleAggravatedResistance
  extends ResistanceCalculations[VehicleSource](
    ResistanceCalculations.ValidVehicleTarget,
    ResistanceCalculations.VehicleAggravatedExtractor
  )

object AmenityHitResistance
  extends ResistanceCalculations[ObjectSource](
    ResistanceCalculations.ValidAmenityTarget,
    ResistanceCalculations.OtherDirectExtractor
  )

object AmenitySplashResistance
  extends ResistanceCalculations[ObjectSource](
    ResistanceCalculations.ValidAmenityTarget,
    ResistanceCalculations.OtherSplashExtractor
  )

object NoResistanceSelection extends ResistanceSelection {
  def Direct: ProjectileCalculations.Form     = None
  def Splash: ProjectileCalculations.Form     = None
  def Lash: ProjectileCalculations.Form       = None
  def Aggravated: ProjectileCalculations.Form = None
}

object StandardInfantryResistance extends ResistanceSelection {
  def Direct: ProjectileCalculations.Form     = InfantryHitResistance.Calculate
  def Splash: ProjectileCalculations.Form     = InfantrySplashResistance.Calculate
  def Lash: ProjectileCalculations.Form       = InfantryLashResistance.Calculate
  def Aggravated: ProjectileCalculations.Form = InfantryAggravatedResistance.Calculate
}

object StandardVehicleResistance extends ResistanceSelection {
  def Direct: ProjectileCalculations.Form     = VehicleHitResistance.Calculate
  def Splash: ProjectileCalculations.Form     = VehicleSplashResistance.Calculate
  def Lash: ProjectileCalculations.Form       = VehicleLashResistance.Calculate
  def Aggravated: ProjectileCalculations.Form = VehicleAggravatedResistance.Calculate
}

object StandardAmenityResistance extends ResistanceSelection {
  def Direct: ProjectileCalculations.Form     = AmenityHitResistance.Calculate
  def Splash: ProjectileCalculations.Form     = AmenityHitResistance.Calculate
  def Lash: ProjectileCalculations.Form       = None
  def Aggravated: ProjectileCalculations.Form = None
}

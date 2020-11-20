// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.vital.resistance.{ResistanceCalculations, ResistanceSelection}

object NoResistance
  extends ResistanceCalculations(
    ResistanceCalculations.ValidInfantryTarget,
    ResistanceCalculations.NoResistExtractor
  )

object InfantryHitResistance
  extends ResistanceCalculations(
    ResistanceCalculations.ValidInfantryTarget,
    ResistanceCalculations.ExoSuitDirectExtractor
  )

object InfantrySplashResistance
  extends ResistanceCalculations(
    ResistanceCalculations.ValidInfantryTarget,
    ResistanceCalculations.ExoSuitSplashExtractor
  )

object InfantryLashResistance
  extends ResistanceCalculations(
    ResistanceCalculations.ValidInfantryTarget,
    ResistanceCalculations.MaximumResistance
  )

object InfantryAggravatedResistance
  extends ResistanceCalculations(
    ResistanceCalculations.ValidInfantryTarget,
    ResistanceCalculations.ExoSuitAggravatedExtractor
  )

object VehicleHitResistance
  extends ResistanceCalculations(
    ResistanceCalculations.ValidVehicleTarget,
    ResistanceCalculations.VehicleDirectExtractor
  )

object VehicleSplashResistance
  extends ResistanceCalculations(
    ResistanceCalculations.ValidVehicleTarget,
    ResistanceCalculations.VehicleSplashExtractor
  )

object VehicleLashResistance
  extends ResistanceCalculations(
    ResistanceCalculations.ValidVehicleTarget,
    ResistanceCalculations.NoResistExtractor
  )

object VehicleAggravatedResistance
  extends ResistanceCalculations(
    ResistanceCalculations.ValidVehicleTarget,
    ResistanceCalculations.VehicleAggravatedExtractor
  )

object AmenityHitResistance
  extends ResistanceCalculations(
    ResistanceCalculations.ValidAmenityTarget,
    ResistanceCalculations.OtherDirectExtractor
  )

object AmenitySplashResistance
  extends ResistanceCalculations(
    ResistanceCalculations.ValidAmenityTarget,
    ResistanceCalculations.OtherSplashExtractor
  )

object NoResistanceSelection extends ResistanceSelection {
  def Direct: ResistanceSelection.Format     = ResistanceSelection.None
  def Splash: ResistanceSelection.Format     = ResistanceSelection.None
  def Lash: ResistanceSelection.Format       = ResistanceSelection.None
  def Aggravated: ResistanceSelection.Format = ResistanceSelection.None
}

object StandardInfantryResistance extends ResistanceSelection {
  def Direct: ResistanceSelection.Format     = InfantryHitResistance.Calculate
  def Splash: ResistanceSelection.Format     = InfantrySplashResistance.Calculate
  def Lash: ResistanceSelection.Format       = InfantryLashResistance.Calculate
  def Aggravated: ResistanceSelection.Format = InfantryAggravatedResistance.Calculate
}

object StandardVehicleResistance extends ResistanceSelection {
  def Direct: ResistanceSelection.Format     = VehicleHitResistance.Calculate
  def Splash: ResistanceSelection.Format     = VehicleSplashResistance.Calculate
  def Lash: ResistanceSelection.Format       = VehicleLashResistance.Calculate
  def Aggravated: ResistanceSelection.Format = VehicleAggravatedResistance.Calculate
}

object StandardAmenityResistance extends ResistanceSelection {
  def Direct: ResistanceSelection.Format     = AmenityHitResistance.Calculate
  def Splash: ResistanceSelection.Format     = AmenityHitResistance.Calculate
  def Lash: ResistanceSelection.Format       = ResistanceSelection.None
  def Aggravated: ResistanceSelection.Format = ResistanceSelection.None
}

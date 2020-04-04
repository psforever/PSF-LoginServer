// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.vital.damage._
import net.psforever.objects.vital.damage.DamageCalculations._

/**
  * A protected super class for calculating "no damage."
  * Used for `NoDamage` but also for the base of `*LashDamage` calculation objects
  * to maintain the polymorphic identity of `DamageCalculations`.
  */
protected class NoDamageBase extends DamageCalculations(
  DamageCalculations.NoDamage,
  DamageWithModifiers(NoDamageAgainst),
  TooFar
)

object NoDamage extends NoDamageBase

object InfantryHitDamage extends DamageCalculations(
  DirectHitDamageWithDegrade,
  DamageWithModifiers(DamageAgainstExoSuit),
  DistanceBetweenTargetandSource
)

object MaxHitDamage extends DamageCalculations(
  DirectHitDamageWithDegrade,
  DamageWithModifiers(DamageAgainstMaxSuit),
  DistanceBetweenTargetandSource
)

object VehicleHitDamage extends DamageCalculations(
  DirectHitDamageWithDegrade,
  DamageWithModifiers(DamageAgainstVehicle),
  DistanceBetweenTargetandSource
)

object AircraftHitDamage extends DamageCalculations(
  DirectHitDamageWithDegrade,
  DamageWithModifiers(DamageAgainstAircraft),
  DistanceBetweenTargetandSource
)

object InfantrySplashDamage extends DamageCalculations(
  SplashDamageWithRadialDegrade,
  DamageWithModifiers(DamageAgainstExoSuit),
  DistanceFromExplosionToTarget
)

object MaxSplashDamage extends DamageCalculations(
  SplashDamageWithRadialDegrade,
  DamageWithModifiers(DamageAgainstMaxSuit),
  DistanceFromExplosionToTarget
)

object VehicleSplashDamage extends DamageCalculations(
  SplashDamageWithRadialDegrade,
  DamageWithModifiers(DamageAgainstVehicle),
  DistanceFromExplosionToTarget
)

object AircraftSplashDamage extends DamageCalculations(
  SplashDamageWithRadialDegrade,
  DamageWithModifiers(DamageAgainstAircraft),
  DistanceFromExplosionToTarget
)

object InfantrySplashDamageDirect extends DamageCalculations(
  SplashDamageWithRadialDegrade,
  DamageWithModifiers(DamageAgainstAircraft),
  NoDistance
)

object InfantryLashDamage extends DamageCalculations(
  LashDamage,
  DamageWithModifiers(DamageAgainstExoSuit),
  DistanceBetweenTargetandSource
)

object MaxLashDamage extends DamageCalculations(
  LashDamage,
  DamageWithModifiers(DamageAgainstMaxSuit),
  DistanceBetweenTargetandSource
)

object VehicleLashDamage extends DamageCalculations(
  LashDamage,
  DamageWithModifiers(DamageAgainstVehicle),
  DistanceBetweenTargetandSource
)

object AircraftLashDamage extends DamageCalculations(
  LashDamage,
  DamageWithModifiers(DamageAgainstAircraft),
  DistanceBetweenTargetandSource
)

object AmenityHitDamage extends DamageCalculations(
  DirectHitDamageWithDegrade,
  DamageWithModifiers(DamageAgainstVehicle),
  DistanceBetweenTargetandSource
)

object AmenitySplashDamage extends DamageCalculations(
  SplashDamageWithRadialDegrade,
  DamageWithModifiers(DamageAgainstVehicle),
  DistanceFromExplosionToTarget
)

object NoDamageSelection extends DamageSelection {
  def Direct = None
  def Splash = None
  def Lash = None
}

object StandardInfantryDamage extends DamageSelection {
  def Direct = InfantryHitDamage.Calculate
  def Splash = InfantrySplashDamage.Calculate
  def Lash = InfantryLashDamage.Calculate
}

object StandardMaxDamage extends DamageSelection {
  def Direct = MaxHitDamage.Calculate
  def Splash = MaxSplashDamage.Calculate
  def Lash = MaxLashDamage.Calculate
}

object StandardVehicleDamage extends DamageSelection {
  def Direct = VehicleHitDamage.Calculate
  def Splash = VehicleSplashDamage.Calculate
  def Lash = VehicleLashDamage.Calculate
}

object StandardAircraftDamage extends DamageSelection {
  def Direct = AircraftHitDamage.Calculate
  def Splash = AircraftSplashDamage.Calculate
  def Lash = AircraftLashDamage.Calculate
}

object StandardDeployableDamage extends DamageSelection {
  def Direct = VehicleHitDamage.Calculate
  def Splash = VehicleSplashDamage.Calculate
  def Lash = NoDamage.Calculate
}

object StandardAmenityDamage extends DamageSelection {
  def Direct = AmenityHitDamage.Calculate
  def Splash = AmenitySplashDamage.Calculate
  def Lash = NoDamage.Calculate
}

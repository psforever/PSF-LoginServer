// Copyright (c) 2017 PSForever
//package net.psforever.objects.vital
//
//import net.psforever.objects.vital.StandardAmenityDamage.None
//import net.psforever.objects.vital.StandardDeployableDamage.None
//import net.psforever.objects.vital.damage._
//import net.psforever.objects.vital.damage.DamageCalculations._
//import net.psforever.objects.vital.projectile.ProjectileCalculations
//
///**
//  * A protected super class for calculating "no damage."
//  * Used for `NoDamage` but also for the base of `*LashDamage` calculation objects
//  * to maintain the polymorphic identity of `DamageCalculations`.
//  */
//protected class NoDamageBase
//    extends DamageCalculations(
//      DamageCalculations.NoDamage,
//      DamageWithModifiers(NoDamageAgainst),
//      TooFar
//    )
//
//object NoDamage extends NoDamageBase
//
//object InfantryHitDamage
//    extends DamageCalculations(
//      DirectHitDamageWithDegrade,
//      DamageWithModifiers(DamageAgainstExoSuit),
//      DistanceBetweenTargetandSource
//    )
//
//object MaxHitDamage
//    extends DamageCalculations(
//      DirectHitDamageWithDegrade,
//      DamageWithModifiers(DamageAgainstMaxSuit),
//      DistanceBetweenTargetandSource
//    )
//
//object VehicleHitDamage
//    extends DamageCalculations(
//      DirectHitDamageWithDegrade,
//      DamageWithModifiers(DamageAgainstVehicle),
//      DistanceBetweenTargetandSource
//    )
//
//object AircraftHitDamage
//    extends DamageCalculations(
//      DirectHitDamageWithDegrade,
//      DamageWithModifiers(DamageAgainstAircraft),
//      DistanceBetweenTargetandSource
//    )
//
//object InfantrySplashDamage
//    extends DamageCalculations(
//      SplashDamageWithRadialDegrade,
//      DamageWithModifiers(DamageAgainstExoSuit),
//      DistanceFromExplosionToTarget
//    )
//
//object MaxSplashDamage
//    extends DamageCalculations(
//      SplashDamageWithRadialDegrade,
//      DamageWithModifiers(DamageAgainstMaxSuit),
//      DistanceFromExplosionToTarget
//    )
//
//object VehicleSplashDamage
//    extends DamageCalculations(
//      SplashDamageWithRadialDegrade,
//      DamageWithModifiers(DamageAgainstVehicle),
//      DistanceFromExplosionToTarget
//    )
//
//object AircraftSplashDamage
//    extends DamageCalculations(
//      SplashDamageWithRadialDegrade,
//      DamageWithModifiers(DamageAgainstAircraft),
//      DistanceFromExplosionToTarget
//    )
//
//object InfantrySplashDamageDirect
//    extends DamageCalculations(
//      SplashDamageWithRadialDegrade,
//      DamageWithModifiers(DamageAgainstAircraft),
//      NoDistance
//    )
//
//object InfantryLashDamage
//    extends DamageCalculations(
//      LashDamage,
//      DamageWithModifiers(DamageAgainstExoSuit),
//      DistanceBetweenTargetandSource
//    )
//
//object MaxLashDamage
//    extends DamageCalculations(
//      LashDamage,
//      DamageWithModifiers(DamageAgainstMaxSuit),
//      DistanceBetweenTargetandSource
//    )
//
//object VehicleLashDamage
//    extends DamageCalculations(
//      LashDamage,
//      DamageWithModifiers(DamageAgainstVehicle),
//      DistanceBetweenTargetandSource
//    )
//
//object AircraftLashDamage
//    extends DamageCalculations(
//      LashDamage,
//      DamageWithModifiers(DamageAgainstAircraft),
//      DistanceBetweenTargetandSource
//    )
//
//object AmenityHitDamage
//    extends DamageCalculations(
//      DirectHitDamageWithDegrade,
//      DamageWithModifiers(DamageAgainstVehicle),
//      DistanceBetweenTargetandSource
//    )
//
//object AmenitySplashDamage
//    extends DamageCalculations(
//      SplashDamageWithRadialDegrade,
//      DamageWithModifiers(DamageAgainstVehicle),
//      DistanceFromExplosionToTarget
//    )
//
//object NoDamageSelection extends DamageSelection {
//  def Direct = None
//  def Splash = None
//  def Lash   = None
//  def Aggravated : ProjectileCalculations.Form = None
//}
//
//object StandardInfantryDamage extends DamageSelection {
//  def Direct : ProjectileCalculations.Form = InfantryHitDamage.Calculate
//  def Splash : ProjectileCalculations.Form = InfantrySplashDamage.Calculate
//  def Lash : ProjectileCalculations.Form = InfantryLashDamage.Calculate
//  def Aggravated : ProjectileCalculations.Form = InfantrySplashDamage.Calculate
//}
//
//object StandardMaxDamage extends DamageSelection {
//  def Direct : ProjectileCalculations.Form = MaxHitDamage.Calculate
//  def Splash : ProjectileCalculations.Form = MaxSplashDamage.Calculate
//  def Lash : ProjectileCalculations.Form = MaxLashDamage.Calculate
//  def Aggravated : ProjectileCalculations.Form = None
//}
//
//object StandardVehicleDamage extends DamageSelection {
//  def Direct : ProjectileCalculations.Form = VehicleHitDamage.Calculate
//  def Splash : ProjectileCalculations.Form = VehicleSplashDamage.Calculate
//  def Lash : ProjectileCalculations.Form = VehicleLashDamage.Calculate
//  def Aggravated : ProjectileCalculations.Form = None
//}
//
//object StandardAircraftDamage extends DamageSelection {
//  def Direct : ProjectileCalculations.Form = AircraftHitDamage.Calculate
//  def Splash : ProjectileCalculations.Form = AircraftSplashDamage.Calculate
//  def Lash : ProjectileCalculations.Form = AircraftLashDamage.Calculate
//  def Aggravated : ProjectileCalculations.Form = None
//}
//
//object StandardDeployableDamage extends DamageSelection {
//  def Direct : ProjectileCalculations.Form = VehicleHitDamage.Calculate
//  def Splash : ProjectileCalculations.Form = VehicleSplashDamage.Calculate
//  def Lash : ProjectileCalculations.Form = NoDamage.Calculate
//  def Aggravated : ProjectileCalculations.Form = None
//}
//
//object StandardAmenityDamage extends DamageSelection {
//  def Direct : ProjectileCalculations.Form = AmenityHitDamage.Calculate
//  def Splash : ProjectileCalculations.Form = AmenitySplashDamage.Calculate
//  def Lash : ProjectileCalculations.Form = NoDamage.Calculate
//  def Aggravated : ProjectileCalculations.Form = None
//}

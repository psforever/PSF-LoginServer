// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.types.Vector3
import net.psforever.objects.ballistics.{Projectile, ProjectileResolution, ResolvedProjectile}
import DamageCalculations._

trait ProjectileCalculations {
  def Calculate(data : ResolvedProjectile) : Int
}

object ProjectileCalculations {
  type Form = (ResolvedProjectile)=>Int
}

abstract class DamageCalculations(damages : DamagesType,
                                  extractor : DamageWithModifiersType,
                                  distanceFunc : DistanceType) extends ProjectileCalculations {
  def Calculate(data : ResolvedProjectile) : Int = {
    val projectile = data.projectile
    damages(
      projectile,
      extractor(projectile.profile, List(projectile.fire_mode.Modifiers)),
      distanceFunc(data)
    )
  }
}

object NoDamage extends DamageCalculations(
  DamageCalculations.NoDamage,
  DamageWithModifiers(NoDamageAgainst),
  TooFar
)

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

object InfantryLashDamage {
  def Calculate(data : ResolvedProjectile) : Int = (InfantryHitDamage.Calculate(data) * 0.2f).toInt
}

object MaxLashDamage {
  def Calculate(data : ResolvedProjectile) : Int = (MaxHitDamage.Calculate(data) * 0.2f).toInt
}

object VehicleLashDamage {
  def Calculate(data : ResolvedProjectile) : Int = (VehicleHitDamage.Calculate(data) * 0.2f).toInt
}

object AircraftLashDamage {
  def Calculate(data : ResolvedProjectile) : Int = (AircraftHitDamage.Calculate(data) * 0.2f).toInt
}

object DamageCalculations {
  //types
  type DamagesType = (Projectile, Int, Float)=>Int
  type DamageWithModifiersType = (DamageProfile, List[DamageProfile])=>Int
  type DistanceType = (ResolvedProjectile)=>Float

  //raw damage selectors
  def NoDamageAgainst(profile : DamageProfile) : Int = 0

  def DamageAgainstExoSuit(profile : DamageProfile) : Int = profile.Damage0

  def DamageAgainstVehicle(profile : DamageProfile) : Int = profile.Damage1

  def DamageAgainstAircraft(profile : DamageProfile) : Int = profile.Damage2

  def DamageAgainstMaxSuit(profile : DamageProfile) : Int = profile.Damage3

  //raw damage selection functions
  def DamageWithModifiers(extractor : (DamageProfile)=>Int)(base : DamageProfile, modifiers : List[DamageProfile]) : Int = {
    extractor(base) + modifiers.foldLeft(0)(_ + extractor(_))
  }

  //damage calculation functions
  def NoDamage(projectile : Projectile, rawDamage : Int, distance : Float) : Int = 0

  def DirectHitDamageWithDegrade(projectile : Projectile, rawDamage: Int, distance: Float): Int = {
    val profile = projectile.profile
    if(distance <= profile.DistanceMax) {
      if(profile.DistanceNoDegrade == profile.DistanceMax || distance <= profile.DistanceNoDegrade) {
        rawDamage
      }
      else {
        rawDamage - ((rawDamage - profile.DegradeMultiplier * rawDamage) * ((distance - profile.DistanceNoDegrade) / (profile.DistanceMax - profile.DistanceNoDegrade))).toInt
      }
    }
    else {
      0
    }
  }

  def SplashDamageWithRadialDegrade(projectile : Projectile, rawDamage : Int, distance : Float) : Int = {
    if(distance <= projectile.profile.DamageRadius) {
      rawDamage + ((rawDamage - (projectile.profile.DamageAtEdge * rawDamage)) * distance / projectile.profile.DamageRadius).toInt
    }
    else {
      0
    }
  }

  //distance functions
  def NoDistance(data : ResolvedProjectile) : Float = 0

  def TooFar(data : ResolvedProjectile) : Float = Float.MaxValue

  def DistanceBetweenTargetandSource(data : ResolvedProjectile) : Float = {
    Vector3.Distance(data.target.Position, data.projectile.owner.Position)
  }

  def DistanceFromExplosionToTarget(data : ResolvedProjectile) : Float = {
    Vector3.Distance(data.target.Position, data.hit_pos)
  }
}

trait DamageSelection {
  final def None : ProjectileCalculations.Form = NoDamage.Calculate

  def Direct : ProjectileCalculations.Form
  def Splash : ProjectileCalculations.Form
  def Lash : ProjectileCalculations.Form

  def apply(data : ResolvedProjectile) : ProjectileCalculations.Form = {
    data.resolution match {
      case ProjectileResolution.Hit => Direct
      case ProjectileResolution.Splash => Splash
      case ProjectileResolution.Lash => Lash
      case _ => None
    }
  }
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

//TODO temporary workaround?
object DamageSelectionByTarget {
  import net.psforever.types.ExoSuitType
  import net.psforever.objects.GlobalDefinitions
  import net.psforever.objects.ballistics.{PlayerSource, VehicleSource}

  def apply(data : ResolvedProjectile) : ProjectileCalculations.Form = {
    (data.target match {
      case target : PlayerSource =>
        if(target.ExoSuit == ExoSuitType.MAX) {
          StandardMaxDamage
        }
        else {
          StandardInfantryDamage
        }
      case target : VehicleSource =>
        if(GlobalDefinitions.isFlightVehicle(target.Definition)) {
          StandardAircraftDamage
        }
        else {
          StandardVehicleDamage
        }
      case _ =>
        StandardVehicleDamage
    })(data)
  }
}

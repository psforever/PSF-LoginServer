// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects._
import net.psforever.objects.ballistics.{Projectile, ResolvedProjectile}
import net.psforever.types.{ExoSuitType, Vector3}

trait DamageCalculator {
  def Calculate(data : ResolvedProjectile) : Int
}

class VanillaInfantryHitDamage extends DamageCalculator {
  def Calculate(data : ResolvedProjectile) : Int = {
    val projectile = data.projectile
    damages(
      projectile,
      DamageCalculations.RawDamage(DamageCalculations.DamageAgainstExoSuit, projectile.profile, List(projectile.fire_mode.Modifiers)),
      Vector3.Distance(data.target.Position, projectile.owner.Position)
    )
  }

  //calculators
  def damages(projectile : Projectile, rawDamage: Int, distance: Float): Int = {
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
}

object DamageCalculations {
  //damage profile extractors
  def NoDamageAgainst(profile : DamageProfile) : Int = 0

  def DamageAgainstExoSuit(profile : DamageProfile) : Int = profile.Damage0

  def DamageAgainstVehicle(profile : DamageProfile) : Int = profile.Damage1

  def DamageAgainstFlyingVehicle(profile : DamageProfile) : Int = profile.Damage2

  def DamageAgainstMaxSuit(profile : DamageProfile) : Int = profile.Damage3

  def RawDamageAgainst(target : Player) : (DamageProfile)=>Int = target.ExoSuit match {
    case ExoSuitType.MAX =>
      DamageAgainstMaxSuit
    case _ =>
      DamageAgainstExoSuit
  }

  def RawDamageAgainst(target : Vehicle) : (DamageProfile)=>Int = {
    if(GlobalDefinitions.isFlightVehicle(target.Definition)) {
      DamageAgainstFlyingVehicle
    }
    else {
      DamageAgainstVehicle
    }
  }

  def RawDamageAgainst(target : PlanetSideGameObject) : (DamageProfile)=>Int = {
    target match {
      case obj : Player =>
        RawDamageAgainst(obj)
      case obj : Vehicle =>
        RawDamageAgainst(obj)
      case _ =>
        NoDamageAgainst
    }
  }

  def RawDamage(extractor : (DamageProfile)=>Int, base : DamageProfile, modifiers : List[DamageProfile]) : Int = {
    extractor(base) + modifiers.foldLeft(0)(_ + extractor(_))
  }

  def RawDamage(target : Player, projectile : Projectile) : Int = {
    RawDamage(RawDamageAgainst(target), projectile.profile, List(projectile.fire_mode.Modifiers))
  }

  def RawDamage(target : Vehicle, projectile : Projectile) : Int = {
    RawDamage(RawDamageAgainst(target), projectile.profile, List(projectile.fire_mode.Modifiers))
  }

  def RawDamage(target : PlanetSideGameObject, projectile : Projectile) : Int = {
    RawDamage(RawDamageAgainst(target), projectile.profile, List(projectile.fire_mode.Modifiers))
  }

  //resistance extractors
  def HitResistance(target : ResistanceProfile) : Int = {
    target.ResistanceDirectHit
  }

  def DamagesAfterResist(damages : Int, resistance : Int, currentHP : Int, currentArmor : Int) : (Int, Int) = {
    if(damages > 0) {
      if(currentArmor <= 0) {
        (damages, 0)
      }
      else if(damages > resistance) {
        val resistedDam = damages - resistance
        if(resistedDam >= currentArmor) {
          (resistedDam - currentArmor, currentArmor)
        }
        else {
          (0, resistedDam)
        }
      }
      else {
        (0, 0)
      }
    }
    else {
      (0, 0)
    }
  }

  def DamagesAfterResistMAX(damages : Int, resistance : Int, currentHP : Int, currentArmor : Int) : (Int, Int) = {
    val resistedDam = damages - resistance
    if(resistedDam > 0) {
      if(currentArmor <= 0) {
        (resistedDam, 0)
      }
      else if(resistedDam >= currentArmor) {
        (resistedDam - currentArmor, currentArmor)
      }
      else {
        (0, resistedDam)
      }
    }
    else {
      (0, 0)
    }
  }

  def ResistanceFunc(target : Player) : (Int,Int,Int,Int)=>(Int,Int) = {
    target.ExoSuit match {
      case ExoSuitType.MAX => DamagesAfterResistMAX
      case _ => DamagesAfterResist
    }
  }

  def SplashResistance(target : Player) : Int = {
    ExoSuitDefinition.Select(target.ExoSuit).ResistanceSplash
  }

  //calculators
  def damages(projectile : Projectile, rawDamage: Int, distance: Float): Int = {
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

  //entries
  def CalculateHitDamage(target : Player, res : ResolvedProjectile, distance : Float) : (Int, Int, Int) = {
    val currentResistance = HitResistance(target)
    val rawDamage = RawDamage(target, res.projectile)
    val currentDamage = damages(res.projectile, rawDamage, distance)
    val (a, b) = ResistanceFunc(target)(currentDamage, currentResistance, target.Health, target.Armor)
    (rawDamage, a, b)
  }

  def CalculateHitDamage(target : Vehicle, res : ResolvedProjectile, distance : Float) : (Int, Int, Int) = {
    val rawDamage = RawDamage(target, res.projectile)
    (rawDamage, damages(res.projectile, rawDamage, distance), 0)
  }

  def CalculateHitDamage(target : PlanetSideGameObject, res : ResolvedProjectile, distance : Float) : (Int, Int, Int) = {
    target match {
      case obj : Player =>
        CalculateHitDamage(obj, res, distance)
      case obj : Vehicle =>
        CalculateHitDamage(obj, res, distance)
      case _ =>
        (0, 0, 0)
    }
  }

  def CalculateSplashDamage(target : Player, projectile : Projectile, distance : Float) : (Int, Int, Int) = {
    val currentResistance = SplashResistance(target)
    val rawDamage = RawDamage(target, projectile)
    if(distance <= projectile.profile.DamageRadius) {
      val currentDamage = rawDamage + ((rawDamage - (projectile.profile.DamageAtEdge * rawDamage)) * distance / projectile.profile.DamageRadius).toInt
      val (a, b) = ResistanceFunc(target)(currentDamage, currentResistance, target.Health, target.Armor)
      (rawDamage, a, b)
    }
    else {
      (0,0,0)
    }
  }

  def CalculateSplashDamage(target : Vehicle, projectile : Projectile, distance : Float) : (Int, Int, Int) = {
    (0,0,0)
  }

  def CalculateSplashDamage(target : PlanetSideGameObject, projectile : ResolvedProjectile, distance : Float) : (Int, Int, Int) = {
    target match {
      case obj : Player =>
        CalculateSplashDamage(obj, projectile.projectile, distance)
      case obj : Vehicle =>
        CalculateSplashDamage(obj, projectile.projectile, distance)
      case _ =>
        (0,0,0)
    }
  }
}

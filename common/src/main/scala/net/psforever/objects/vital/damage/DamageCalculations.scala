// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.damage

import net.psforever.types.Vector3
import net.psforever.objects.ballistics.{Projectile, ResolvedProjectile}
import net.psforever.objects.vital.projectile.ProjectileCalculations
import DamageCalculations._

/**
  * The base class for function literal description related to calculating damage information.<br>
  * <br>
  * Implementing functionality of the children is the product of three user-defined processes
  * and information for the calculation is extracted from the to-be-provided weapon discharge information.
  * The specific functions passed into this object typically operate simultaneously normally
  * and are related to the target and the kind of interaction the weapon discharge had with the target.
  * @param damages function by which damage is modified by distance
  * @param extractor function that recovers damage information
  * @param distanceFunc a function to calculate the distance for scaling the damage, if valid
  */
abstract class DamageCalculations(damages : DamagesType,
                                  extractor : DamageWithModifiersType,
                                  distanceFunc : DistanceType) extends ProjectileCalculations {
  /**
    * Combine the damage and distance data extracted from the `ResolvedProjectile` entry.
    * @param data the historical `ResolvedProjectile` information
    * @return the damage value
    */
  def Calculate(data : ResolvedProjectile) : Int = {
    val projectile = data.projectile
    val profile = projectile.profile
    val modifiers = if(profile.UseDamage1Subtract) {
      List(projectile.fire_mode.Modifiers, data.target.Modifiers.Subtract)
    }
    else {
      List(projectile.fire_mode.Modifiers)
    }
    damages(
      projectile,
      extractor(profile, modifiers),
      distanceFunc(data)
    )
  }
}

object DamageCalculations {
  //types
  type DamagesType = (Projectile, Int, Float)=>Int
  type DamageWithModifiersType = (DamageProfile, List[DamageProfile])=>Int
  type DistanceType = ResolvedProjectile=>Float

  //raw damage selectors
  def NoDamageAgainst(profile : DamageProfile) : Int = 0

  def DamageAgainstExoSuit(profile : DamageProfile) : Int = profile.Damage0

  def DamageAgainstVehicle(profile : DamageProfile) : Int = profile.Damage1

  def DamageAgainstAircraft(profile : DamageProfile) : Int = profile.Damage2

  def DamageAgainstMaxSuit(profile : DamageProfile) : Int = profile.Damage3

  def DamageAgainstUnknown(profile : DamageProfile) : Int = profile.Damage4

  //raw damage selection functions
  /**
    * Get damage information from a series of profiles related to the weapon discharge.
    * @param extractor the function that recovers the damage value
    * @param base the profile from which primary damage is to be selected
    * @param modifiers alternate profiles that will modify the base damage value
    * @return the accumulated damage value
    */
  //TODO modifiers come from various sources; expand this part of the calculation model in the future
  def DamageWithModifiers(extractor : DamageProfile=>Int)(base : DamageProfile, modifiers : List[DamageProfile]) : Int = {
    extractor(base) + modifiers.foldLeft(0)(_ + extractor(_))
  }

  //damage calculation functions
  def NoDamage(projectile : Projectile, rawDamage : Int, distance : Float) : Int = 0

  /**
    * Use an unmodified damage value.
    * @param projectile information about the weapon discharge (itself);
    *                   unused
    * @param rawDamage the accumulated amount of damage
    * @param distance how far the source was from the target;
    *                 unused
    * @return the rawDamage value
    */
  def SameHit(projectile : Projectile, rawDamage : Int, distance : Float) : Int = rawDamage

  /**
    * Modify the base damage based on the degrade distance of the projectile type
    * and its maximum effective distance.
    * Calls out "direct hit" damage but is recycled for other damage types as well.
    * @param projectile information about the weapon discharge (itself)
    * @param rawDamage the accumulated amount of damage
    * @param distance how far the source was from the target
    * @return the modified damage value
    */
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

  /**
    * Modify the base damage based on the radial distance of the target from the center of an explosion.
    * Calls out "splash" damage exclusively.
    * @param projectile information about the weapon discharge (itself)
    * @param rawDamage the accumulated amount of damage
    * @param distance how far the origin of the explosion was from the target
    * @return the modified damage value
    */
  def SplashDamageWithRadialDegrade(projectile : Projectile, rawDamage : Int, distance : Float) : Int = {
    val radius = projectile.profile.DamageRadius
    if(distance <= radius) {
      val base : Float = projectile.profile.DamageAtEdge
      val degrade : Float = (1 - base) * ((radius - distance)/radius) + base
      (rawDamage * degrade).toInt
    }
    else {
      0
    }
  }

  /**
    * Calculate a lash damage value.
    * The target needs to be more than five meters away.
    * Since lash damage occurs after the direct hit projectile's damage begins to degrade,
    * the minimum of five less than distance or zero is calculated.
    * @param projectile information about the weapon discharge (itself)
    * @param rawDamage the accumulated amount of damage
    * @param distance how far the source was from the target
    * @return the modified damage value
    */
  def LashDamage(projectile : Projectile, rawDamage : Int, distance : Float) : Int = {
    if(distance > 5) {
      (DirectHitDamageWithDegrade(projectile, rawDamage, math.max(distance - 5, 0f)) * 0.2f) toInt
    }
    else {
      0
    }
  }

  //distance functions
  def NoDistance(data : ResolvedProjectile) : Float = 0

  def TooFar(data : ResolvedProjectile) : Float = Float.MaxValue

  def DistanceBetweenTargetandSource(data : ResolvedProjectile) : Float = {
    //Vector3.Distance(data.target.Position, data.projectile.owner.Position)
    DistanceBetweenOriginAndImpact(data)
  }

  def DistanceFromExplosionToTarget(data : ResolvedProjectile) : Float = {
    math.max(Vector3.Distance(data.target.Position, data.hit_pos) - 1, 0)
    //DistanceBetweenOriginAndImpact(data)
  }

  def DistanceBetweenOriginAndImpact(data : ResolvedProjectile) : Float = {
    math.max(Vector3.Distance(data.projectile.shot_origin, data.hit_pos) - 0.5f, 0)
  }
}

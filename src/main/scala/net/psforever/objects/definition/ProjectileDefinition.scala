// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.ballistics.Projectiles
import net.psforever.objects.vital.base.DamageType
import net.psforever.objects.vital.projectile.DistanceDegrade
import net.psforever.objects.vital.prop.DamageWithPosition

/**
  * The definition that outlines the damage-dealing characteristics of any projectile.
  * `Tool` objects emit `ProjectileDefinition` objects and that is later wrapped into a `Projectile` object.
  * @param objectId the object's identifier number
  */
class ProjectileDefinition(objectId: Int)
  extends ObjectDefinition(objectId)
    with DamageWithPosition {
  /** ascertain that this object is a valid projectile type */
  private val projectileType: Projectiles.Types.Value     = Projectiles.Types(objectId) //let throw NoSuchElementException
  /** how much faster (or slower) the projectile moves (m/s^2^) */
  private var acceleration: Int                     = 0
  /** when the acceleration stops being applied (s) */
  private var accelerationUntil: Float              = 0f
  /** number of seconds before an airborne projectile's damage begins to degrade (s) */
  private var degradeDelay: Float                   = 1f
  /** the rate of degrade of projectile damage after the degrade delay */
  private var degradeMultiplier: Float              = 1f
  /** the out-of-the-muzzle speed of a projectile (m/s) */
  private var initialVelocity: Int                  = 1
  /** for how long the projectile exists (s) */
  private var lifespan: Float                       = 1f
  /** for lashing damage, how far away a target will be affected by the projectile (m) */
  private var lashRadius : Float                    = 0f
  /** the projectile is represented by a server-side entity
    * that is updated by the projectile owner
    * and transmitted to all projectile observers;
    * `true` spawns a server-managed object */
  private var existsOnRemoteClients: Boolean        = false
  /** the values used by the `ObjectCreateMessage` packet for construction of the server-managed projectile
    * `0, 0` are artificial values;
    * the oicw_little_buddy is undefined for these values */
  private var remoteClientData: (Int, Int) = (0, 0)
  /** this projectile follows its target, after a fashion */
  private var autoLock: Boolean         = false
  /** the projectile tries to confer the jammered status effect to its target(s) */
  private var jammerProjectile: Boolean = false
  /** projectile takes the form of a type of "grenade";
    * grenades arc with gravity rather than travel in a relatively straight path */
  private var grenade_projectile: Boolean = false
  /** radiation clouds create independent damage-dealing areas in a zone that last for the projectile's lifespan */
  var radiation_cloud: Boolean = false
  //derived calculations
  /** the calculated distance at which the projectile have traveled far enough to despawn (m);
    * typically handled as the projectile no longer performing damage;
    * occasionally, this value is purely mathematical as opposed to realistic, e.g., the melee weapons */
  private var distanceMax: Float              = 0f
  /** how far the projectile will travel while accelerating (m) */
  private var distanceFromAcceleration: Float = 0f
  /** how far the projectile will travel while not degrading (m);
    * this field is not to be used in the place of minimum radial damage */
  private var distanceNoDegrade: Float        = 0f
  /** after acceleration, if any, what is the final speed of the projectile (m/s) */
  private var finalVelocity: Float            = 0f
  Name = "projectile"
  Modifiers = DistanceDegrade
  registerAs = "projectiles"

  def ProjectileType: Projectiles.Types.Value = projectileType

  def Acceleration: Int = acceleration

  def Acceleration_=(accel: Int): Int = {
    acceleration = accel
    Acceleration
  }

  def AccelerationUntil: Float = accelerationUntil

  def AccelerationUntil_=(accelUntil: Float): Float = {
    accelerationUntil = accelUntil
    AccelerationUntil
  }

  def ProjectileDamageType: DamageType.Value = CausesDamageType

  def ProjectileDamageType_=(damageType1: DamageType.Value): DamageType.Value = {
    CausesDamageType = damageType1
    ProjectileDamageType
  }

  def ProjectileDamageTypeSecondary: DamageType.Value = CausesDamageTypeSecondary

  def ProjectileDamageTypeSecondary_=(damageTypeSecondary1: DamageType.Value): DamageType.Value = {
    CausesDamageTypeSecondary = damageTypeSecondary1
    ProjectileDamageTypeSecondary
  }

  def ProjectileDamageTypes : Set[DamageType.Value] = AllDamageTypes

  def DegradeDelay: Float = degradeDelay

  def DegradeDelay_=(degradeDelay: Float): Float = {
    this.degradeDelay = degradeDelay
    DegradeDelay
  }

  def DegradeMultiplier: Float = degradeMultiplier

  def DegradeMultiplier_=(degradeMultiplier: Float): Float = {
    this.degradeMultiplier = degradeMultiplier
    DegradeMultiplier
  }

  def InitialVelocity: Int = initialVelocity

  def InitialVelocity_=(initialVelocity: Int): Int = {
    this.initialVelocity = initialVelocity
    InitialVelocity
  }

  def Lifespan: Float = lifespan

  def Lifespan_=(lifespan: Float): Float = {
    this.lifespan = lifespan
    Lifespan
  }

  def LashRadius: Float = lashRadius

  def LashRadius_=(radius: Float): Float = {
    lashRadius = radius
    LashRadius
  }

  def ExistsOnRemoteClients: Boolean = existsOnRemoteClients

  def ExistsOnRemoteClients_=(existsOnRemoteClients: Boolean): Boolean = {
    this.existsOnRemoteClients = existsOnRemoteClients
    ExistsOnRemoteClients
  }

  def RemoteClientData: (Int, Int) = remoteClientData

  def RemoteClientData_=(remoteClientData: (Int, Int)): (Int, Int) = {
    this.remoteClientData = remoteClientData
    RemoteClientData
  }

  def AutoLock: Boolean = autoLock

  def AutoLock_=(lockState: Boolean): Boolean = {
    autoLock = lockState
    AutoLock
  }

  def JammerProjectile: Boolean = jammerProjectile

  def JammerProjectile_=(effect: Boolean): Boolean = {
    jammerProjectile = effect
    JammerProjectile
  }

  def GrenadeProjectile : Boolean = grenade_projectile

  def GrenadeProjectile_=(isGrenade : Boolean) : Boolean = {
    grenade_projectile = isGrenade
    GrenadeProjectile
  }

  def DistanceMax : Float = distanceMax //accessor only

  def DistanceFromAcceleration: Float = distanceFromAcceleration //accessor only

  def DistanceNoDegrade: Float = distanceNoDegrade //accessor only

  def FinalVelocity: Float = finalVelocity //accessor only
}

object ProjectileDefinition {
  def apply(projectileType: Projectiles.Types.Value): ProjectileDefinition = {
    new ProjectileDefinition(projectileType.id)
  }

  /**
    * Calculate the secondary fields of the projectile's damage.
    * Depending on whether the appropriate fields are defined,
    * it may calculate for "damage over distance", typically associated with straight-fire direct hit projectiles,
    * or for "radial damage", typically associated with explosive splash projectiles.
    * @param pdef the projectile's definition, often called its profile
    */
  def CalculateDerivedFields(pdef: ProjectileDefinition): Unit = {
    val (distanceMax, distanceFromAcceleration, finalVelocity): (Float, Float, Float) = if (pdef.Acceleration == 0) {
      (pdef.InitialVelocity * pdef.Lifespan, 0, pdef.InitialVelocity.toFloat)
    } else {
      val distanceFromAcceleration =
        (pdef.AccelerationUntil * pdef.InitialVelocity) + (0.5f * pdef.Acceleration * pdef.AccelerationUntil * pdef.AccelerationUntil)
      val finalVelocity             = pdef.InitialVelocity + pdef.Acceleration * pdef.AccelerationUntil
      val distanceAfterAcceleration = finalVelocity * (pdef.Lifespan - pdef.AccelerationUntil)
      (distanceFromAcceleration + distanceAfterAcceleration, distanceFromAcceleration, finalVelocity)
    }
    pdef.distanceMax = distanceMax
    pdef.distanceFromAcceleration = distanceFromAcceleration
    pdef.finalVelocity = finalVelocity

    pdef.distanceNoDegrade = if (pdef.DegradeDelay == 0f) {
      pdef.distanceMax
    } else if (pdef.DegradeDelay < pdef.AccelerationUntil) {
      (pdef.DegradeDelay * pdef.InitialVelocity) + (0.5f * pdef.Acceleration * pdef.DegradeDelay * pdef.DegradeDelay)
    } else {
      pdef.distanceFromAcceleration + pdef.finalVelocity * (pdef.DegradeDelay - pdef.AccelerationUntil)
    }
  }
}

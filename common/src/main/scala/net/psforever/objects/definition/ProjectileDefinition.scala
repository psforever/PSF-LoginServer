// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.ballistics.Projectiles
import net.psforever.objects.equipment.JammingUnit
import net.psforever.objects.vital.{DamageType, StandardDamageProfile}

/**
  * The definition that outlines the damage-dealing characteristics of any projectile.
  * `Tool` objects emit `ProjectileDefinition` objects and that is later wrapped into a `Projectile` object.
  * @param objectId the object's identifier number
  */
class ProjectileDefinition(objectId : Int) extends ObjectDefinition(objectId)
  with JammingUnit
  with StandardDamageProfile {
  private val projectileType : Projectiles.Value = Projectiles(objectId) //let throw NoSuchElementException
  private var acceleration : Int = 0
  private var accelerationUntil : Float = 0f
  private var damageType : DamageType.Value = DamageType.None
  private var damageTypeSecondary : DamageType.Value = DamageType.None
  private var degradeDelay : Float = 1f
  private var degradeMultiplier : Float = 1f
  private var initialVelocity : Int = 1
  private var lifespan : Float = 1f
  private var damageAtEdge : Float = 1f
  private var damageRadius : Float = 1f
  private var useDamage1Subtract : Boolean = false
  private var existsOnRemoteClients : Boolean = false //`true` spawns a server-managed object
  private var remoteClientData : (Int, Int) = (0, 0) //artificial values; for ObjectCreateMessage packet (oicw_little_buddy is undefined)
  private var autoLock : Boolean = false
  private var additionalEffect : Boolean = false
  private var jammerProjectile : Boolean = false
  //derived calculations
  private var distanceMax : Float = 0f
  private var distanceFromAcceleration : Float = 0f
  private var distanceNoDegrade : Float = 0f
  private var finalVelocity : Float = 0f
  Name = "projectile"

  def ProjectileType : Projectiles.Value = projectileType

  def UseDamage1Subtract : Boolean = useDamage1Subtract

  def UseDamage1Subtract_=(useDamage1Subtract : Boolean) : Boolean = {
    this.useDamage1Subtract = useDamage1Subtract
    UseDamage1Subtract
  }

  def Acceleration : Int = acceleration

  def Acceleration_=(accel : Int) : Int = {
    acceleration = accel
    Acceleration
  }

  def AccelerationUntil : Float = accelerationUntil

  def AccelerationUntil_=(accelUntil : Float) : Float = {
    accelerationUntil = accelUntil
    AccelerationUntil
  }

  def ProjectileDamageType : DamageType.Value = damageType

  def ProjectileDamageType_=(damageType1 : DamageType.Value) : DamageType.Value = {
    damageType = damageType1
    ProjectileDamageType
  }

  def ProjectileDamageTypeSecondary : DamageType.Value = damageTypeSecondary

  def ProjectileDamageTypeSecondary_=(damageTypeSecondary1 : DamageType.Value) : DamageType.Value = {
    damageTypeSecondary = damageTypeSecondary1
    ProjectileDamageTypeSecondary
  }

  def DegradeDelay : Float = degradeDelay

  def DegradeDelay_=(degradeDelay : Float) : Float = {
    this.degradeDelay = degradeDelay
    DegradeDelay
  }

  def DegradeMultiplier : Float = degradeMultiplier

  def DegradeMultiplier_=(degradeMultiplier : Float) : Float = {
    this.degradeMultiplier = degradeMultiplier
    DegradeMultiplier
  }

  def InitialVelocity : Int = initialVelocity

  def InitialVelocity_=(initialVelocity : Int) : Int = {
    this.initialVelocity = initialVelocity
    InitialVelocity
  }

  def Lifespan : Float = lifespan

  def Lifespan_=(lifespan : Float) : Float = {
    this.lifespan = lifespan
    Lifespan
  }

  def DamageAtEdge : Float = damageAtEdge

  def DamageAtEdge_=(damageAtEdge : Float) : Float = {
    this.damageAtEdge = damageAtEdge
    DamageAtEdge
  }

  def DamageRadius : Float = damageRadius

  def DamageRadius_=(damageRadius : Float) : Float = {
    this.damageRadius = damageRadius
    DamageRadius
  }

  def ExistsOnRemoteClients : Boolean = existsOnRemoteClients

  def ExistsOnRemoteClients_=(existsOnRemoteClients : Boolean) : Boolean = {
    this.existsOnRemoteClients = existsOnRemoteClients
    ExistsOnRemoteClients
  }

  def RemoteClientData : (Int, Int) = remoteClientData

  def RemoteClientData_=(remoteClientData : (Int, Int)) : (Int, Int) = {
    this.remoteClientData = remoteClientData
    RemoteClientData
  }

  def AutoLock : Boolean = autoLock

  def AutoLock_=(lockState : Boolean) : Boolean = {
    autoLock = lockState
    AutoLock
  }

  def AdditionalEffect : Boolean = additionalEffect

  def AdditionalEffect_=(effect : Boolean) : Boolean = {
    additionalEffect = effect
    AdditionalEffect
  }

  def JammerProjectile : Boolean = jammerProjectile

  def JammerProjectile_=(effect : Boolean) : Boolean = {
    jammerProjectile = effect
    JammerProjectile
  }

  def DistanceMax : Float = distanceMax //accessor only

  def DistanceFromAcceleration : Float = distanceFromAcceleration //accessor only

  def DistanceNoDegrade : Float = distanceNoDegrade //accessor only

  def FinalVelocity : Float = finalVelocity //accessor only
}

object ProjectileDefinition {
  def apply(projectileType : Projectiles.Value) : ProjectileDefinition = {
    new ProjectileDefinition(projectileType.id)
  }

  def CalculateDerivedFields(pdef : ProjectileDefinition) : Unit = {
    val (distanceMax, distanceFromAcceleration, finalVelocity) : (Float, Float, Float) = if(pdef.Acceleration == 0f) {
      (pdef.InitialVelocity * pdef.Lifespan, 0, pdef.InitialVelocity)
    }
    else {
      val distanceFromAcceleration = (pdef.AccelerationUntil * pdef.InitialVelocity) + (0.5f * pdef.Acceleration * pdef.AccelerationUntil * pdef.AccelerationUntil)
      val finalVelocity = pdef.InitialVelocity + pdef.Acceleration * pdef.AccelerationUntil
      val distanceAfterAcceleration = finalVelocity * (pdef.Lifespan - pdef.AccelerationUntil)
      (distanceFromAcceleration + distanceAfterAcceleration, distanceFromAcceleration, finalVelocity)
    }
    pdef.distanceMax = distanceMax
    pdef.distanceFromAcceleration = distanceFromAcceleration
    pdef.finalVelocity = finalVelocity

    pdef.distanceNoDegrade = if(pdef.DegradeDelay == 0f) {
      pdef.distanceMax
    }
    else if(pdef.DegradeDelay < pdef.AccelerationUntil) {
      (pdef.DegradeDelay * pdef.InitialVelocity) + (0.5f * pdef.Acceleration * pdef.DegradeDelay * pdef.DegradeDelay)
    }
    else {
      pdef.distanceFromAcceleration + pdef.finalVelocity * (pdef.DegradeDelay - pdef.AccelerationUntil)
    }
  }
}

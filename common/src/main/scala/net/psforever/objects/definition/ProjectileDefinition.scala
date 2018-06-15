// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.ballistics.{DamageProfile, DamageType, Projectiles}

/**
  * The definition that outlines the damage-dealing characteristics of any projectile.
  * `Tool` objects emit `ProjectileDefinition` objects and that is later wrapped into a `Projectile` object.
  * @param objectId the object's identifier number
  */
class ProjectileDefinition(objectId : Int) extends ObjectDefinition(objectId) with DamageProfile {
  private val projectileType : Projectiles.Value = Projectiles(objectId) //let throw NoSuchElementException
  private var damage0 : Int = 0
  private var damage1 : Option[Int] = None
  private var damage2 : Option[Int] = None
  private var damage3 : Option[Int] = None
  private var damage4 : Option[Int] = None
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
  Name = "projectile"

  def ProjectileType : Projectiles.Value = projectileType

  def UseDamage1Subtract : Boolean = useDamage1Subtract

  def UseDamage1Subtract_=(useDamage1Subtract : Boolean) : Boolean = {
    this.useDamage1Subtract = useDamage1Subtract
    UseDamage1Subtract
  }

  def Damage0 : Int = damage0

  def Damage0_=(damage : Int) : Int = {
    damage0 = damage
    damage0
  }

  def Damage0_=(damage : Option[Int]) : Int = {
    damage0 = damage match {
      case Some(value) => value
      case None => 0 //can not be set to None
    }
    Damage0
  }

  def Damage1 : Int = damage1.getOrElse(Damage0)

  def Damage1_=(damage : Int) : Int = Damage1_=(Some(damage))

  def Damage1_=(damage : Option[Int]) : Int = {
    this.damage1 = damage
    Damage1
  }

  def Damage2 : Int = damage2.getOrElse(Damage1)

  def Damage2_=(damage : Int) : Int = Damage2_=(Some(damage))

  def Damage2_=(damage : Option[Int]) : Int = {
    this.damage2 = damage
    Damage2
  }

  def Damage3 : Int = damage3.getOrElse(Damage2)

  def Damage3_=(damage : Int) : Int = Damage3_=(Some(damage))

  def Damage3_=(damage : Option[Int]) : Int = {
    this.damage3 = damage
    Damage3
  }

  def Damage4 : Int = damage4.getOrElse(Damage3)

  def Damage4_=(damage : Int) : Int = Damage4_=(Some(damage))

  def Damage4_=(damage : Option[Int]) : Int = {
    this.damage4 = damage
    Damage4
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
}

object ProjectileDefinition {
  def apply(projectileType : Projectiles.Value) : ProjectileDefinition = {
    new ProjectileDefinition(projectileType.id)
  }
}

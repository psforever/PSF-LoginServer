// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.equipment.{DamageType, Projectiles}

class ProjectileDefinition(objectId : Int) extends EquipmentDefinition(objectId) {
  private val projectileType : Projectiles.Value = Projectiles(objectId) //let throw NoSuchElementException
  private var damage0 : Int = 0
  private var damage1 : Int = -1
  private var damage2 : Int = -1
  private var damage3 : Int = -1
  private var damage4 : Int = -1
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
    this.damage0 = damage
    Damage0
  }

  def Damage1 : Int = damage1
  def Damage1_=(damage : Int) : Int = {
    this.damage1 = damage
    Damage1
  }

  def Damage2 : Int = damage2
  def Damage2_=(damage : Int) : Int = {
    this.damage2 = damage
    Damage2
  }

  def Damage3 : Int = damage3
  def Damage3_=(damage : Int) : Int = {
    this.damage3 = damage
    Damage3
  }

  def Damage4 : Int = damage4
  def Damage4_=(damage : Int) : Int = {
    this.damage4 = damage
    Damage4
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

//  def apply(objectId: Int) : ProjectileDefinition = {
//    new ProjectileDefinition(objectId)
//  }

  def apply(projectileType : Projectiles.Value) : ProjectileDefinition = {
    new ProjectileDefinition(projectileType.id)
  }
}

// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.equipment.DamageType

/**
  * From a `Player` ...
  */
final case class Projectile(private val from : Int, damage0 : Int, damage1 : Int, damage2 : Int, damage3 : Int, damage4 : Int,
                            addDamage0 : Int, addDamage1 : Int, addDamage2 : Int, addDamage3 : Int, addDamage4 : Int,
                            damageAtEdge : Float, damageRadius : Float, damageType : Int , degradeDelay : Float,
                            degradeMultiplier : Float, initialVelocity : Int, lifespan : Float ) {

  def FromWeaponId : Int = from
  def Damage0 : Int = damage0
  def Damage1 : Int = damage1
  def Damage2 : Int = damage2
  def Damage3 : Int = damage3
  def Damage4 : Int = damage4
  def AddDamage0 : Int = addDamage0
  def AddDamage1 : Int = addDamage1
  def AddDamage2 : Int = addDamage2
  def AddDamage3 : Int = addDamage3
  def AddDamage4 : Int = addDamage4
  def DamageAtEdge : Float = damageAtEdge
  def DamageRadius : Float = damageRadius
  def DamageType = damageType
  def DegradeDelay : Float = degradeDelay
  def DegradeMultiplier : Float = degradeMultiplier
  def InitialVelocity : Int = initialVelocity
  def Lifespan : Float = lifespan

}


object Projectile {
  def apply(player: Player, from : Int, damage0 : Int, damage1 : Int, damage2 : Int, damage3 : Int, damage4 : Int,
            addDamage0 : Int, addDamage1 : Int, addDamage2 : Int, addDamage3 : Int, addDamage4 : Int,
            damageAtEdge : Float, damageRadius : Float, damageType : DamageType.Value, degradeDelay : Float,
            degradeMultiplier : Float, initialVelocity : Int, lifespan : Float ): Projectile = {
    new Projectile(from, damage0, damage1, damage2, damage3, damage4, addDamage0, addDamage1, addDamage2, addDamage3, addDamage4,
      damageAtEdge, damageRadius, damageType.id, degradeDelay, degradeMultiplier, initialVelocity, lifespan)
  }
}
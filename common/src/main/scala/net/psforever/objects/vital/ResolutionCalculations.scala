// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.ballistics.{PlayerSource, ResolvedProjectile}
import net.psforever.objects.{PlanetSideGameObject, Player, Vehicle}

trait ResolutionCalculations[ApplyType <: PlanetSideGameObject] {
  def Calculate(damages : ProjectileCalculations.Form, resistances : ProjectileCalculations.Form, data : ResolvedProjectile) : (ApplyType)=>Unit
}

abstract class DamageResistCalculations[A, B <: PlanetSideGameObject](calcFunc : (ResolvedProjectile)=>((Int, Int)=>A),
                                                                      applyFunc : (A, ResolvedProjectile)=>(B)=>Unit)
  extends ResolutionCalculations[B] {
  def Calculate(damages : ProjectileCalculations.Form, resistances : ProjectileCalculations.Form, data : ResolvedProjectile) : (B)=>Unit = {
    val dam : Int = damages(data)
    val res : Int = resistances(data)
    val mod = calcFunc(data)
    val modDam = mod(dam, res)
    applyFunc(modDam, data)
  }
}

abstract class PlayerResolutions[A](calcFunc : (ResolvedProjectile)=>((Int, Int)=>A),
                                    applyFunc : (A, ResolvedProjectile)=>(Player)=>Unit)
  extends DamageResistCalculations[A, Player](calcFunc, applyFunc)

abstract class VehicleResolutions[A](calcFunc : (ResolvedProjectile)=>((Int, Int)=>A),
                                    applyFunc : (A, ResolvedProjectile)=>(Vehicle)=>Unit)
  extends DamageResistCalculations[A, Vehicle](calcFunc, applyFunc)

object InfantryResolutions extends PlayerResolutions(
  ResolutionCalculations.InfantryDamageAfterResist,
  ResolutionCalculations.InfantryApplication
)

object MaxResolutions extends PlayerResolutions(
  ResolutionCalculations.MaxDamageAfterResist,
  ResolutionCalculations.InfantryApplication
)

object VehicleResolutions extends VehicleResolutions(
  ResolutionCalculations.VehicleDamageAfterResist,
  ResolutionCalculations.VehicleApplication
)

object ResolutionCalculations {
  type Form[ApplyType <: PlanetSideGameObject] = (ProjectileCalculations.Form, ProjectileCalculations.Form, ResolvedProjectile)=>(ApplyType=>Unit)

  def InfantryDamageAfterResist(data : ResolvedProjectile) : (Int, Int)=>(Int, Int) = {
    val target = data.target.asInstanceOf[PlayerSource]
    InfantryDamageAfterResist(target.health, target.armor)
  }

  def InfantryDamageAfterResist(currentHP : Int, currentArmor : Int)(damages : Int, resistance : Int) : (Int, Int) = {
    if(damages > 0) {
      if(currentArmor <= 0) {
        (damages, 0) //no armor; health damage
      }
      else if(damages > resistance) {
        val resistedDam = damages - resistance
        //(resistedDam, resistance)
        if(resistance <= currentArmor) {
          (resistedDam, resistance) //armor and health damage
        }
        else {
          (resistedDam + (resistance - currentArmor), currentArmor) //deplete armor; health damage + bonus
        }
      }
      else {
        (0, damages) //too weak; armor damage (less than resistance)
      }
    }
    else {
      (0, 0) //no damage
    }
  }

  def MaxDamageAfterResist(data : ResolvedProjectile) : (Int, Int)=>(Int, Int) = {
    val target = data.target.asInstanceOf[PlayerSource]
    MaxDamageAfterResist(target.health, target.armor)
  }

  def MaxDamageAfterResist(currentHP : Int, currentArmor : Int)(damages : Int, resistance : Int) : (Int, Int) = {
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

  def VehicleDamageAfterResist(data : ResolvedProjectile) : (Int, Int)=>Int = {
    VehicleDamageAfterResist
  }

  def VehicleDamageAfterResist(damages : Int, resistance : Int) : Int = {
    if(damages > resistance) {
      damages - resistance
    }
    else {
      damages
    }
  }

  def InfantryApplication(damageValues : (Int, Int), data : ResolvedProjectile)(target : Player) : Unit = {
    val (a, b) = damageValues
    if(target.isAlive && (a != 0 || b != 0)) {
      target.History(data)
      if(target.Armor - b < 0) {
        val originalArmor = target.Armor
        target.Armor = 0
        target.Health = target.Health - a - (originalArmor - b) //spill over
      }
      else {
        target.Armor = target.Armor - b
        target.Health = target.Health - a
      }
    }
  }

  def VehicleApplication(damage : Int, data : ResolvedProjectile)(target : Vehicle) : Unit = {
    if(target.Health > 0) {
      if(target.Shields > damage) {
        target.Shields = target.Shields - damage
      }
      else if(target.Shields > 0) {
        val damageToHealth = damage - target.Shields
        target.Shields = 0
        target.Health = target.Health - damageToHealth
      }
      else {
        target.Health = target.Health - damage
      }
    }
  }
}

trait ResolutionSelection {
  def Infantry : ResolutionCalculations.Form[_]
  def Max : ResolutionCalculations.Form[_]
  def Vehicle : ResolutionCalculations.Form[_]
  def Aircraft : ResolutionCalculations.Form[_]
}

object StandardResolutions extends ResolutionSelection {
  def Infantry : ResolutionCalculations.Form[Player] = InfantryResolutions.Calculate
  def Max : ResolutionCalculations.Form[Player] = MaxResolutions.Calculate
  def Vehicle : ResolutionCalculations.Form[Vehicle] = VehicleResolutions.Calculate
  def Aircraft : ResolutionCalculations.Form[Vehicle] = VehicleResolutions.Calculate
}

// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resolution

import net.psforever.objects.{Player, TurretDeployable, Vehicle}
import net.psforever.objects.ballistics.PlayerSource
import net.psforever.objects.ce.{ComplexDeployable, Deployable}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.resistance.ResistanceSelection
import net.psforever.types.ImplantType

/**
  * The base for the combining step of all projectile-induced damage calculation function literals.
  */
trait ResolutionCalculations {

  /**
    * The exposed entry for the calculation function literal defined by this base.
    * @param damages the function literal that accumulates and calculates damages
    * @param resistances the function literal that collects resistance values
    * @param data the historical damage information
    * @return a function literal that encapsulates delayed modification instructions for certain objects
    */
  def calculate(
      damages: DamageCalculations.Selector,
      resistances: ResistanceSelection.Format,
      data: DamageInteraction
  ): ResolutionCalculations.Output
}

object ResolutionCalculations {
  type Output = Any => DamageResult
  type Form   = (DamageCalculations.Selector, ResistanceSelection.Format, DamageInteraction) => Output

  def NoDamage(data: DamageInteraction)(a: Int, b: Int): Int = 0

  def InfantryDamage(data: DamageInteraction): (Int, Int) => (Int, Int) = {
    data.target match {
      case target: PlayerSource =>
        if(data.cause.source.DamageToHealthOnly) {
          DamageToHealthOnly(target.health)
        } else {
          InfantryDamageAfterResist(target.health, target.armor)
        }
      case _ =>
        InfantryDamageAfterResist(0, 0)
    }
  }

  def DamageToHealthOnly(currentHP: Int)(damages: Int, resistance: Int): (Int, Int) = {
    if (damages > 0 && currentHP > 0) {
      if(damages > resistance) {
        (damages - resistance, 0)
      } else {
        (damages, 0)
      }
    } else {
      (0, 0)
    }
  }

  def InfantryDamageAfterResist(currentHP: Int, currentArmor: Int)(damages: Int, resistance: Int): (Int, Int) = {
    if (damages > 0 && currentHP > 0) {
      if (currentArmor <= 0) {
        (damages, 0) //no armor; health damage
      } else if (damages > resistance) {
        val resistedDam = damages - resistance
        //(resistedDam, resistance)
        if (resistance <= currentArmor) {
          (resistedDam, resistance) //armor and health damage
        } else {
          (resistedDam + (resistance - currentArmor), currentArmor) //deplete armor; health damage + bonus
        }
      } else {
        (0, damages) //too weak; armor damage (less than resistance)
      }
    } else {
      (0, 0) //no damage
    }
  }

  def MaxDamage(data: DamageInteraction): (Int, Int) => (Int, Int) = {
    data.target match {
      case target: PlayerSource =>
        if(data.cause.source.DamageToHealthOnly) {
          DamageToHealthOnly(target.health)
        } else {
          MaxDamageAfterResist(target.health, target.armor)
        }
      case _ =>
        MaxDamageAfterResist(0, 0)
    }
  }

  def MaxDamageAfterResist(currentHP: Int, currentArmor: Int)(damages: Int, resistance: Int): (Int, Int) = {
    val resistedDam = damages - resistance
    if (resistedDam > 0 && currentHP > 0) {
      if (currentArmor <= 0) {
        (resistedDam, 0) //no armor; health damage
      } else if (resistedDam >= currentArmor) {
        (resistedDam - currentArmor, currentArmor) //deplete armor; health damage
      } else {
        (0, resistedDam) //too weak; armor damage (less than resistance)
      }
    } else {
      (0, 0) //no damage
    }
  }

  /**
    * Unlike with `Infantry*` and with `Max*`'s,
    * `VehicleDamageAfterResist` does not necessarily need to validate its target object.
    * The required input is sufficient.
    * @param data the historical damage information
    * @return a function literal for dealing with damage values and resistance values together
    */
  def VehicleDamageAfterResist(data: DamageInteraction): (Int, Int) => Int = {
    VehicleDamageAfterResist
  }

  def VehicleDamageAfterResist(damages: Int, resistance: Int): Int = {
    if (damages > resistance) {
      damages - resistance
    } else {
      damages
    }
  }

  def NoApplication(damageValue: Int, data: DamageInteraction)(target: Any): DamageResult = data

  def SubtractWithRemainder(current: Int, damage: Int): (Int, Int) = {
    val a               = Math.max(0, current - damage)
    val remainingDamage = Math.abs(current - damage - a)
    (a, remainingDamage)
  }

  private def CanDamage(obj: Vitality with FactionAffinity, damage: Int, data: DamageInteraction): Boolean = {
    obj.Health > 0 && Damageable.CanDamage(obj, damage, data)
  }

  /**
    * The expanded `(Any)=>Unit` function for infantry.
    * Apply the damage values to the capacitor (if shielded NC max), health field and personal armor field for an infantry target.
    * @param damageValues a tuple containing damage values for: health, personal armor
    * @param data the historical damage information
    * @param target the `Player` object to be affected by these damage values (at some point)
    */
  def InfantryApplication(damageValues: (Int, Int), data: DamageInteraction)(target: Any): DamageResult = {
    target match {
      case player: Player =>
        var (a, b) = damageValues
        var result = (0, 0)
        if (player.isAlive && !(a == 0 && b == 0)) {
          if (player.Capacitor.toInt > 0 && player.isShielded) {
            // Subtract armour damage from capacitor
            result = SubtractWithRemainder(player.Capacitor.toInt, b)
            player.Capacitor = result._1.toFloat
            b = result._2

            // Then follow up with health damage if any capacitor is left
            result = SubtractWithRemainder(player.Capacitor.toInt, a)
            player.Capacitor = result._1.toFloat
            a = result._2
          }

          player.avatar.implants.flatten.find(x => x.definition.implantType == ImplantType.PersonalShield) match {
            case Some(implant) if implant.active =>
              // Subtract armour damage from stamina
              result = SubtractWithRemainder(player.avatar.stamina, b)
              player.avatar = player.avatar.copy(stamina = result._1)
              b = result._2

              // Then follow up with health damage if any stamina is left
              result = SubtractWithRemainder(player.avatar.stamina, a)
              player.avatar = player.avatar.copy(stamina = result._1)
              a = result._2

            case _ => ;
          }

          // Subtract any remaining armour damage from armour
          result = SubtractWithRemainder(player.Armor, b)
          player.Armor = result._1
          b = result._2

          val originalHealth = player.Health
          // Then bleed through to health if armour ran out
          result = SubtractWithRemainder(player.Health, b)
          player.Health = result._1
          b = result._2

          // Finally, apply health damage to health
          result = SubtractWithRemainder(player.Health, a)
          player.Health = result._1
          a = result._2

          // If any health damage was applied also drain an amount of stamina equal to half the health damage
          if (player.Health < originalHealth) {
            val delta = originalHealth - player.Health
            player.avatar =
              player.avatar.copy(stamina = math.max(0, player.avatar.stamina - math.floor(delta / 2).toInt))
          }
        }
      case _ =>
    }
    data
  }

  /**
    * The expanded `(Any)=>Unit` function for vehicles.
    * Apply the damage value to the shield field and then the health field (that order) for a vehicle target.
    * @param damage the raw damage
    * @param data the historical damage information
    * @param target the `Vehicle` object to be affected by these damage values (at some point)
    */
  def VehicleApplication(damage: Int, data: DamageInteraction)(target: Any): DamageResult = {
    target match {
      case vehicle: Vehicle if CanDamage(vehicle, damage, data) =>
        val shields = vehicle.Shields
        if (shields > damage) {
          vehicle.Shields = shields - damage
        } else if (shields > 0) {
          vehicle.Health = vehicle.Health - (damage - shields)
          vehicle.Shields = 0
        } else {
          vehicle.Health = vehicle.Health - damage
        }
      case _ => ;
    }
    data
  }

  def SimpleApplication(damage: Int, data: DamageInteraction)(target: Any): DamageResult = {
    target match {
      case obj: Deployable if CanDamage(obj, damage, data) =>
        obj.Health -= damage
      case turret: FacilityTurret if CanDamage(turret, damage, data) =>
        turret.Health -= damage
      case amenity: Amenity if CanDamage(amenity, damage, data) =>
        amenity.Health -= damage
      case _ => ;
    }
    data
  }

  def ComplexDeployableApplication(damage: Int, data: DamageInteraction)(target: Any): DamageResult = {
    target match {
      case ce: ComplexDeployable if CanDamage(ce, damage, data) =>
        if (ce.Shields > 0) {
          if (damage > ce.Shields) {
            ce.Health -= (damage - ce.Shields)
            ce.Shields = 0
          } else {
            ce.Shields -= damage
          }
        } else {
          ce.Health -= damage
        }

      case ce: TurretDeployable if CanDamage(ce, damage, data) =>
        if (ce.Shields > 0) {
          if (damage > ce.Shields) {
            ce.Health -= (damage - ce.Shields)
            ce.Shields = 0
          } else {
            ce.Shields -= damage
          }
        } else {
          ce.Health -= damage
        }

      case _ => ;
    }
    data
  }
}

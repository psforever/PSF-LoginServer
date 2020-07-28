// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

import net.psforever.objects.Tool
import net.psforever.objects.vital.damage.{DamageModifiers, DamageProfile}

import scala.collection.mutable

class FireModeDefinition extends DamageModifiers {

  /** indices pointing to all ammo types used, in (an) order
    * the ammo types list will be available from the `ToolDefinition`
    */
  private val ammoTypeIndices: mutable.ListBuffer[Int] = mutable.ListBuffer[Int]()

  /** custom indices pointing to the projectile type used for this mode's ammo types
    * for most weapon fire modes, this list will be empty and the projectile will align with the `ammoTypeIndex`
    * if at least one ammo type has a redirected projectile type, all projectiles must be defined for this mode
    */
  private val projectileTypeIndices: mutable.ListBuffer[Int] = mutable.ListBuffer[Int]()

  /** ammunition slot number this fire mode utilizes */
  private var ammoSlotIndex: Int = 0

  /** how many rounds are replenished each reload cycle */
  private var magazine: Int = 1

  /** how many rounds are replenished each reload cycle, per type of ammunition loaded
    * key - ammo type index, value - magazine capacity
    */
  private val customAmmoMagazine: mutable.HashMap[Ammo.Value, Int] = mutable.HashMap[Ammo.Value, Int]()

  /** how much is subtracted from the magazine each fire cycle;
    * most weapons will only fire 1 round per fire cycle; the flamethrower in fire mode 1 fires 50
    */
  private var roundsPerShot: Int = 1

  /** how many sub-rounds are queued per round fired;
    * the flechette fires 8 pellets per shell and generates 8 fire reports before the ammo count goes down
    */
  private var chamber: Int = 1

  /** modifiers for each damage type */
  private val modifiers: FireModeDamageModifiers = new FireModeDamageModifiers

  def AmmoSlotIndex: Int = ammoSlotIndex

  def AmmoSlotIndex_=(index: Int): Int = {
    ammoSlotIndex = index
    AmmoSlotIndex
  }

  def AmmoTypeIndices: mutable.ListBuffer[Int] = ammoTypeIndices

  def AmmoTypeIndices_=(index: Int): mutable.ListBuffer[Int] = {
    ammoTypeIndices += index
  }

  def ProjectileTypeIndices: mutable.ListBuffer[Int] = projectileTypeIndices

  def ProjectileTypeIndices_=(index: Int): mutable.ListBuffer[Int] = {
    projectileTypeIndices += index
  }

  def Magazine: Int = magazine

  def Magazine_=(inMagazine: Int): Int = {
    magazine = inMagazine
    Magazine
  }

  def CustomMagazine: mutable.HashMap[Ammo.Value, Int] = customAmmoMagazine

  def CustomMagazine_=(kv: (Ammo.Value, Int)): mutable.HashMap[Ammo.Value, Int] = {
    val (ammoTypeIndex, cap) = kv
    customAmmoMagazine += ammoTypeIndex -> cap
    CustomMagazine
  }

  def RoundsPerShot: Int = roundsPerShot

  def RoundsPerShot_=(round: Int): Int = {
    roundsPerShot = round
    RoundsPerShot
  }

  def Chamber: Int = chamber

  def Chamber_=(inChamber: Int): Int = {
    chamber = inChamber
    Chamber
  }

  def Add: FireModeDamageModifiers = modifiers

  /**
    * Shoot a weapon, remove an anticipated amount of ammunition.
    * @param weapon the weapon
    * @param rounds The number of rounds to remove, if specified
    * @return the size of the weapon's magazine after discharge
    */
  def Discharge(weapon: Tool, rounds: Option[Int] = None): Int = {
    val dischargedAmount = rounds match {
      case Some(rounds: Int) => rounds
      case _                 => RoundsPerShot
    }
    weapon.Magazine - dischargedAmount
  }
}

class PelletFireModeDefinition extends FireModeDefinition {

  /**
    * Shoot a weapon, remove an anticipated amount of ammunition.<br>
    * <br>
    * For a weapon that has a number of sub-rounds chambered, each will generate unique weapon fire per fire cycle.
    * Once all of the sub-rounds have been accounted for, the number of rounds for a single fire cycle will subtract.
    * Since all fire cycles will abide by this chambered number of sub-rounds, the count is reset.
    * @param weapon the weapon
    * @return the size of the weapon's magazine after discharge
    */
  override def Discharge(weapon: Tool, rounds: Option[Int] = None): Int = {
    val ammoSlot     = weapon.AmmoSlot
    val magazine     = weapon.Magazine
    val chamber: Int = ammoSlot.Chamber = ammoSlot.Chamber - 1
    if (chamber <= 0) {
      ammoSlot.Chamber = Chamber
      magazine - RoundsPerShot
    } else {
      magazine
    }
  }
}

class InfiniteFireModeDefinition extends FireModeDefinition {

  /**
    * Shoot a weapon, remove an anticipated amount of ammunition.<br>
    * <br>
    * No rounds will be subtracted ever.
    * The weapon can keep firing as much as the user wants.
    * Since the PlanetSide client also has an internal understanding of ammo values in weapons,
    * it may interfere with the functionality of this fire mode
    * if the size of the magazine is not implicitly set per fire cycle.
    * Works well with melee weapons.
    * @param weapon the weapon
    * @return the size of the weapon's magazine after discharge;
    *         will always return 1
    */
  override def Discharge(weapon: Tool, rounds: Option[Int] = None): Int = 1
}

class FireModeDamageModifiers extends DamageProfile {
  private var damage0: Int = 0
  private var damage1: Int = 0
  private var damage2: Int = 0
  private var damage3: Int = 0
  private var damage4: Int = 0

  def Damage0: Int = damage0

  def Damage0_=(damage: Int): Int = {
    damage0 = damage
    Damage0
  }

  def Damage1: Int = damage1

  def Damage1_=(damage: Int): Int = {
    damage1 = damage
    Damage1
  }

  def Damage2: Int = damage2

  def Damage2_=(damage: Int): Int = {
    damage2 = damage
    Damage2
  }

  def Damage3: Int = damage3

  def Damage3_=(damage: Int): Int = {
    damage3 = damage
    Damage3
  }

  def Damage4: Int = damage4

  def Damage4_=(damage: Int): Int = {
    damage4 = damage
    Damage4
  }
}

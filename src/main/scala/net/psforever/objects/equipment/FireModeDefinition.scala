// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

import net.psforever.objects.Tool
import net.psforever.objects.vital.SpecificDamageProfile
import net.psforever.objects.vital.damage.DamageModifiers

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
  private val modifiers: SpecificDamageProfile = new SpecificDamageProfile

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

  def Add: SpecificDamageProfile = modifiers

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

class PelletFireModeDefinition
  extends FireModeDefinition {
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

class InfiniteFireModeDefinition
  extends FireModeDefinition {

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

/**
  * Shoot a weapon, remove an anticipated amount of ammunition.<br>
  * <br>
  * Hold down the fire trigger to create a damage multiplier.
  * After the multiplier has reach complete/full, expend additional ammunition to sustain it.
  * @param time the duration until the charge is full (milliseconds)
  * @param drainInterval the curation between ticks of ammunition depletion after "full charge"
  */
class ChargeFireModeDefinition(private val time: Long, private val drainInterval: Long, private val roundsPerInterval: Int = 1)
  extends FireModeDefinition {
  def Time: Long = time

  def DrainInterval: Long = drainInterval

  def RoundsPerInterval: Int = roundsPerInterval
}

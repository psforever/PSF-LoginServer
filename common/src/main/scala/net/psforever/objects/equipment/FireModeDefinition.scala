// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

import net.psforever.objects.Tool

import scala.collection.mutable

class FireModeDefinition {
  /** indices pointing to all ammo types used */
  private val ammoTypeIndices : mutable.ListBuffer[Int] = mutable.ListBuffer[Int]()
  /** ammunition slot number this fire mode utilizes */
  private var ammoSlotIndex : Int = 0
  /** how many rounds are replenished each reload cycle */
  private var magazine : Int = 1
  /** how much is subtracted from the magazine each fire cycle;
    * most weapons will only fire 1 round per fire cycle; the flamethrower, fire mode 1, fires 50 */
  private var rounds : Int = 1
  /** how many sub-rounds are queued per round fired;
    * the flechette fires 8 pellets per shell and generates 8 fire reports before the ammo count goes down */
  private var chamber : Int = 1

  //damage modifiers will follow here ... ?

  def AmmoSlotIndex : Int = ammoSlotIndex

  def AmmoSlotIndex_=(index : Int) : Int = {
    ammoSlotIndex = index
    AmmoSlotIndex
  }

  def AmmoTypeIndices : mutable.ListBuffer[Int] = ammoTypeIndices

  def AmmoTypeIndices_=(index : Int) : mutable.ListBuffer[Int] = {
    ammoTypeIndices += index
  }

  def Magazine : Int = magazine

  def Magazine_=(inMagazine : Int) : Int = {
    magazine = inMagazine
    Magazine
  }

  def Rounds : Int = rounds

  def Rounds_=(round : Int) : Int = {
    rounds = round
    Rounds
  }

  def Chamber : Int = chamber

  def Chamber_=(inChamber : Int) : Int = {
    chamber = inChamber
    Chamber
  }

  /**
    * Shoot a weapon, remove an anticipated amount of ammunition.
    * @param weapon the weapon
    * @return the size of the weapon's magazine after discharge
    */
  def Discharge(weapon : Tool) : Int = {
    weapon.Magazine - Rounds
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
  override def Discharge(weapon : Tool) : Int = {
    val ammoSlot = weapon.AmmoSlot
    val magazine = weapon.Magazine
    val chamber : Int = ammoSlot.Chamber = ammoSlot.Chamber - 1
    if(chamber <= 0) {
      ammoSlot.Chamber = Chamber
      magazine - Rounds
    }
    else {
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
  override def Discharge(weapon : Tool) : Int = 1
}

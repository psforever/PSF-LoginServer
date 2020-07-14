// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.damage

/**
  * The different values for five common types of damage that can be dealt, based on target and application.
  * In the same way, the five damage modifiers that are applied to the same kind of damage.
  */
trait DamageProfile {

  /** `damage0` is for basic infantry */
  def Damage0: Int

  /** `damage0` is for basic infantry */
  def Damage0_=(damage: Int): Int

  /** `damage1` is for armor, amenities, deployables, etc. */
  def Damage1: Int

  /** `damage1` is for armor, amenities, deployables, etc. */
  def Damage1_=(damage: Int): Int

  /** `damage2` if for aircraft */
  def Damage2: Int

  /** `damage2` if for aircraft */
  def Damage2_=(damage: Int): Int

  /** `damage3` is for mechanized infantry */
  def Damage3: Int

  /** `damage3` is for mechanized infantry */
  def Damage3_=(damage: Int): Int

  /** `damage4` is for battleframe robotics */
  def Damage4: Int

  /** `damage4` is for battleframe robotics */
  def Damage4_=(damage: Int): Int
}

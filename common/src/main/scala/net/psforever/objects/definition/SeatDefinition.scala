// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.vehicles.SeatArmorRestriction

/**
  * The definition for a seat.
  */
class SeatDefinition extends BasicDefinition {

  /** a restriction on the type of exo-suit a person can wear */
  private var armorRestriction: SeatArmorRestriction.Value = SeatArmorRestriction.NoMax

  /** the user can escape while the vehicle is moving */
  private var bailable: Boolean = false

  /** any controlled weapon */
  private var weaponMount: Option[Int] = None
  Name = "seat"

  def ArmorRestriction: SeatArmorRestriction.Value = {
    this.armorRestriction
  }

  def ArmorRestriction_=(restriction: SeatArmorRestriction.Value): SeatArmorRestriction.Value = {
    this.armorRestriction = restriction
    restriction
  }

  def Bailable: Boolean = {
    this.bailable
  }

  def Bailable_=(canBail: Boolean): Boolean = {
    this.bailable = canBail
    canBail
  }

  def ControlledWeapon: Option[Int] = {
    this.weaponMount
  }

  def ControlledWeapon_=(wep: Int): Option[Int] = {
    ControlledWeapon_=(Some(wep))
  }

  def ControlledWeapon_=(wep: Option[Int]): Option[Int] = {
    this.weaponMount = wep
    ControlledWeapon
  }
}

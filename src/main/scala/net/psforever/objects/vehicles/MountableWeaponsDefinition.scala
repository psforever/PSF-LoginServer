// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.serverobject.mount.MountableDefinition

import scala.collection.mutable

trait MountableWeaponsDefinition
  extends MountedWeaponsDefinition
  with MountableDefinition {
  private val _controlledWeapons: mutable.HashMap[Int, Set[Int]] = mutable.HashMap[Int, Set[Int]]()

  def controlledWeapons(): Map[Int, Set[Int]] = _controlledWeapons.toMap

  def controlledWeapons(seat: Int, weapon: Int): Map[Int, Set[Int]] = {
    _controlledWeapons.put(seat, Set(weapon))
    _controlledWeapons.toMap
  }

  def controlledWeapons(seat: Int, weapons: Set[Int]): Map[Int, Set[Int]] = {
    _controlledWeapons.put(seat, weapons)
    _controlledWeapons.toMap
  }
}

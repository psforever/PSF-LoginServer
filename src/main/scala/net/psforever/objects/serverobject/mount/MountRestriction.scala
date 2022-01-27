// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.types.ExoSuitType

trait MountRestriction[A] {
  def test(target: A): Boolean
}

case object MaxOnly extends MountRestriction[Player] {
  def test(target: Player): Boolean = target.ExoSuit == ExoSuitType.MAX
}

case object NoMax extends MountRestriction[Player] {
  def test(target: Player): Boolean = target.ExoSuit != ExoSuitType.MAX
}

case object NoReinforcedOrMax extends MountRestriction[Player] {
  def test(target: Player): Boolean = target.ExoSuit != ExoSuitType.Reinforced && target.ExoSuit != ExoSuitType.MAX
}

case object Unrestricted extends MountRestriction[Player] {
  def test(target: Player): Boolean = true
}

case object SmallCargo extends MountRestriction[Vehicle] {
  def test(target: Vehicle): Boolean = {
    target.Definition == GlobalDefinitions.ant ||
    target.Definition == GlobalDefinitions.quadassault ||
    target.Definition == GlobalDefinitions.quadstealth ||
    target.Definition == GlobalDefinitions.fury ||
    target.Definition == GlobalDefinitions.switchblade ||
    target.Definition == GlobalDefinitions.two_man_assault_buggy ||
    target.Definition == GlobalDefinitions.skyguard ||
    target.Definition == GlobalDefinitions.twomanheavybuggy ||
    target.Definition == GlobalDefinitions.twomanhoverbuggy ||
    target.Definition == GlobalDefinitions.threemanheavybuggy ||
    target.Definition == GlobalDefinitions.lightning
  }
}

case object LargeCargo extends MountRestriction[Vehicle] {
  def test(target: Vehicle): Boolean = {
    GlobalDefinitions.isBattleFrameVehicle(target.Definition) || !target.Definition.CanFly
  }
}

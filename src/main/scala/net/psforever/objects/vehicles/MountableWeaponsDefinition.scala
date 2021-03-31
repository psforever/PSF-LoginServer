// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.serverobject.mount.MountableDefinition

import scala.collection.mutable

trait MountableWeaponsDefinition
  extends MountedWeaponsDefinition
  with MountableDefinition {
  val controlledWeapons: mutable.HashMap[Int, Int] = mutable.HashMap[Int, Int]()
}

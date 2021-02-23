// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

import net.psforever.objects.definition.BasicDefinition

trait MountableDefinition[A]
  extends BasicDefinition {
  def occupancy: Int = 1

  def restriction: MountRestriction[A]

  def bailable: Boolean
}

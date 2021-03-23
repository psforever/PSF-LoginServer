// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

import net.psforever.objects.definition.BasicDefinition

trait MountableSpaceDefinition[A]
  extends BasicDefinition {
  def occupancy: Int

  def restriction: MountRestriction[A]

  def bailable: Boolean
}

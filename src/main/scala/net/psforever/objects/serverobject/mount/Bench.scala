// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

import net.psforever.objects.Player

class Bench(private val sdef: MountableDefinition[Player]) extends ManyMountableSpace[Player] {
  override protected def testToMount(target: Player): Boolean = target.VehicleSeated.isEmpty && super.testToMount(target)

  def definition: MountableDefinition[Player] = sdef
}

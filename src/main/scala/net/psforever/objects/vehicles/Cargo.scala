// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.mount.{MountableDefinition, SingleMountableSpace}

class Cargo(private val cdef: MountableDefinition[Vehicle]) extends SingleMountableSpace[Vehicle] {
  override protected def testToMount(target: Vehicle): Boolean = target.MountedIn.isEmpty && super.testToMount(target)

  def definition: MountableDefinition[Vehicle] = cdef
}

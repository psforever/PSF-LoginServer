// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.mount.{MountableSpace, MountableSpaceDefinition}

class Cargo(private val cdef: MountableSpaceDefinition[Vehicle]) extends MountableSpace[Vehicle] {
  override protected def testToMount(target: Vehicle): Boolean = target.MountedIn.isEmpty && super.testToMount(target)

  def definition: MountableSpaceDefinition[Vehicle] = cdef
}

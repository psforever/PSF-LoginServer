// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.mount.{MountableSpace, MountableSpaceDefinition}

class Cargo(private val cdef: MountableSpaceDefinition[Vehicle]) extends MountableSpace[Vehicle] {
  def definition: MountableSpaceDefinition[Vehicle] = cdef
}

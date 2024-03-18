// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.mount.{MountableSpace, MountableSpaceDefinition}
import net.psforever.types.BailType

class Cargo(private val cdef: MountableSpaceDefinition[Vehicle]) extends MountableSpace[Vehicle] {
  override def unmount(target: Option[Vehicle], bailType: BailType.Value): Option[Vehicle] = {
    val outcome = super.unmount(target, bailType)
    target.collect {
      case v if outcome.isEmpty && !isOccupiedBy(v) =>
        v.MountedIn = None
    }
    outcome
  }

  def definition: MountableSpaceDefinition[Vehicle] = cdef
}

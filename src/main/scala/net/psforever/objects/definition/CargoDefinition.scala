// Copyright (c) 2021 PSForever
package net.psforever.objects.definition

import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.mount.{LargeCargo, MountRestriction, MountableSpaceDefinition}

class CargoDefinition extends MountableSpaceDefinition[Vehicle] {
  Name = "cargo"
  def occupancy: Int = 1

  var restriction: MountRestriction[Vehicle] = LargeCargo

  var bailable: Boolean = true
}

// Copyright (c) 2021 PSForever
package net.psforever.objects.definition

import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.mount.{LargeCargo, MountRestriction, MountableDefinition}

class CargoDefinition extends MountableDefinition[Vehicle] {
  Name = "cargo"

  var restriction: MountRestriction[Vehicle] = LargeCargo

  var bailable: Boolean = false
}

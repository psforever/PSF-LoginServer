// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

import net.psforever.objects.Player

class SeatDefinition extends MountableSpaceDefinition[Player] {
  Name = "mount"
  var occupancy: Int = 1

  var restriction: MountRestriction[Player] = NoMax

  var bailable: Boolean = false
}

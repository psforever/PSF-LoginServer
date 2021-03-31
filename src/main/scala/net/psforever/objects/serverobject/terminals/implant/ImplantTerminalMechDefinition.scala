// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals.implant

import net.psforever.objects.serverobject.mount.{MountInfo, MountableDefinition, SeatDefinition, Unrestricted}
import net.psforever.objects.serverobject.structures.AmenityDefinition

/**
  * The `Definition` for any `Terminal` that is of a type "implant_terminal_interface."
  * Implant terminals are composed of two components.
  * This `Definition` constructs the visible mechanical tube component that can be mounted.
  */
class ImplantTerminalMechDefinition
  extends AmenityDefinition(410)
  with MountableDefinition {
  Name = "implant_terminal_mech"

  /* key - mount index, value - mount object */
  Seats += 0 -> new SeatDefinition() {
    restriction = Unrestricted
  }
  /* key - entry point index, value - mount index */
  MountPoints += 1 -> MountInfo(0)
}

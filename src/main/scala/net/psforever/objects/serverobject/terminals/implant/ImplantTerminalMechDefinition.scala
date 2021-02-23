// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals.implant

import net.psforever.objects.serverobject.mount.{SeatDefinition, Unrestricted}
import net.psforever.objects.serverobject.structures.AmenityDefinition

/**
  * The `Definition` for any `Terminal` that is of a type "implant_terminal_interface."
  * Implant terminals are composed of two components.
  * This `Definition` constructs the visible mechanical tube component that can be mounted.
  */
class ImplantTerminalMechDefinition extends AmenityDefinition(410) {
  /* key - mount index, value - mount object */
  private val seats: Map[Int, SeatDefinition] = Map(0 -> new SeatDefinition() {
    restriction = Unrestricted
  })
  /* key - entry point index, value - mount index */
  private val mountPoints: Map[Int, Int] = Map(1 -> 0)
  Name = "implant_terminal_mech"

  def Seats: Map[Int, SeatDefinition] = seats

  def MountPoints: Map[Int, Int] = mountPoints
}

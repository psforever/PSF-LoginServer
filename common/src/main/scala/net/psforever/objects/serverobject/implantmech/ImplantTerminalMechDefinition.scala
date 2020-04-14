// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.implantmech

import net.psforever.objects.definition.SeatDefinition
import net.psforever.objects.serverobject.structures.AmenityDefinition

/**
  * The `Definition` for any `Terminal` that is of a type "implant_terminal_interface."
  * Implant terminals are composed of two components.
  * This `Definition` constructs the visible mechanical tube component that can be mounted.
  */
class ImplantTerminalMechDefinition extends AmenityDefinition(410) {
  /* key - seat index, value - seat object */
  private val seats : Map[Int, SeatDefinition] = Map(0 -> new SeatDefinition)
  /* key - entry point index, value - seat index */
  private val mountPoints : Map[Int, Int] = Map(1 -> 0)
  Name = "implant_terminal_mech"

  def Seats : Map[Int, SeatDefinition] = seats

  def MountPoints : Map[Int, Int] = mountPoints
}

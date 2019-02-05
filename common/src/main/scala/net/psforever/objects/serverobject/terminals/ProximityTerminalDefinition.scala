// Copyright (c) 2019 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player

/**
  *The definition for any `Terminal` that can be accessed for amenities and services,
  * triggered when a certain distance from the unit itself (proximity-based).
  * @param objectId the object's identifier number
  */
class ProximityTerminalDefinition(objectId : Int) extends TerminalDefinition(objectId) with ProximityDefinition {
  def Request(player : Player, msg : Any) : Terminal.Exchange = Terminal.NoDeal()
}

// Copyright (c) 2025 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.ActorContext
import net.psforever.objects.{Default, Player}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.structures.Amenity

/**
  * The definition for any `Terminal` that is of a type "main_terminal".
  * Main terminal objects are used to upload or remove a virus from a major facility
  * @param objectId the object's identifier number
  */
class MainTerminalDefinition(objectId: Int) extends TerminalDefinition(objectId) {
  def Request(player: Player, msg: Any): Terminal.Exchange = Terminal.NoDeal()
}

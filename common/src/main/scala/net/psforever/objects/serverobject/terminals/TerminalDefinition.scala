// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.objects.definition.converter.TerminalConverter

/**
  * The basic definition for any `Terminal` object.
  * @param objectId the object's identifier number
  */
abstract class TerminalDefinition(objectId : Int) extends net.psforever.objects.definition.ObjectDefinition(objectId) {
  Name = "terminal"
  Packet = new TerminalConverter

  /**
    * The unimplemented functionality for the entry function of form of activity
    * processed by this terminal and codified by the input message (a "request").
    * @see `Terminal.Exchange`
    * @param player the player who made the request
    * @param msg the request message
    * @return a message that resolves the transaction
    */
  def Request(player : Player, msg : Any) : Terminal.Exchange
}

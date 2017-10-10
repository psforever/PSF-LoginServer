// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor

/**
  * An `Actor` that handles messages being dispatched to a specific `Terminal`.<br>
  * <br>
  * For now, the only important message being managed is `Terminal.Request`.
  * @param term the `Terminal` object being governed
  */
class TerminalControl(term : Terminal) extends Actor {
  def receive : Receive = {
    case Terminal.Request(player, msg) =>
      sender ! Terminal.TerminalMessage(player, msg, term.Request(player, msg))

    case TemporaryTerminalMessages.Hack(player) =>
      term.HackedBy = player

    case TemporaryTerminalMessages.ClearHack() =>
      term.HackedBy = None

    case _ =>
      sender ! Terminal.NoDeal()
  }

  override def toString : String = term.Definition.Name
}

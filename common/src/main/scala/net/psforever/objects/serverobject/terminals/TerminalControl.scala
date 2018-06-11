// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

/**
  * An `Actor` that handles messages being dispatched to a specific `Terminal`.
  * @param term the `Terminal` object being governed
  */
class TerminalControl(term : Terminal) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = term

  def receive : Receive = checkBehavior.orElse {
    case Terminal.Request(player, msg) =>
      sender ! Terminal.TerminalMessage(player, msg, term.Request(player, msg))

    case CommonMessages.Hack(player) =>
      term.HackedBy = player
      sender ! true
    case CommonMessages.ClearHack() =>
      term.HackedBy = None

    case _ => ;
  }

  override def toString : String = term.Definition.Name
}

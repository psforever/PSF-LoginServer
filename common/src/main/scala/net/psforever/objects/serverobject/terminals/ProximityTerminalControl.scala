// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.terminals.Terminal.TerminalMessage

/**
  *
  * An `Actor` that handles messages being dispatched to a specific `ProximityTerminal`.
  * Although this "terminal" itself does not accept the same messages as a normal `Terminal` object,
  * it returns the same type of messages - wrapped in a `TerminalMessage` - to the `sender`.
  * @param term the proximity unit (terminal)
  */
class ProximityTerminalControl(term : ProximityTerminal) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = term

  def receive : Receive = checkBehavior.orElse {
    case CommonMessages.Use(player) =>
      val hadNoUsers = term.NumberUsers == 0
      if(term.AddUser(player.GUID) == 1 && hadNoUsers) {
        sender ! TerminalMessage(player, null, Terminal.StartProximityEffect(term))
      }

    case CommonMessages.Unuse(player) =>
      val hadUsers = term.NumberUsers > 0
      if(term.RemoveUser(player.GUID) == 0 && hadUsers) {
        sender ! TerminalMessage(player, null, Terminal.StopProximityEffect(term))
      }

    case _ =>
      sender ! Terminal.NoDeal()
  }

  override def toString : String = term.Definition.Name
}

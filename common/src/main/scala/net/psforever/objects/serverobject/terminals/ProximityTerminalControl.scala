// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.terminals.Terminal.TerminalMessage

class ProximityTerminalControl(term : ProximityTerminal) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = term

  def receive : Receive = checkBehavior.orElse {
    case ProximityTerminal.Use(player) =>
      val hadNoUsers = term.NumberUsers == 0
      if(term.AddUser(player.GUID) == 1 && hadNoUsers) {
        sender ! TerminalMessage(player, null, Terminal.StartProximityEffect(term))
      }

    case ProximityTerminal.Unuse(player) =>
      val hadUsers = term.NumberUsers > 0
      if(term.RemoveUser(player.GUID) == 0 && hadUsers) {
        sender ! TerminalMessage(player, null, Terminal.StopProximityEffect(term))
      }

    case _ =>
      sender ! Terminal.NoDeal()
  }

  override def toString : String = term.Definition.Name
}

// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

class RepairRearmControl(term : RepairRearmSilo) extends Actor with FactionAffinityBehavior.Check with ProximityUnit.Use {
  def FactionObject : FactionAffinity = term

  def TerminalObject : Terminal with ProximityUnit = term

  def receive : Receive = checkBehavior
    .orElse(proximityBehavior)
    .orElse {
      case Terminal.Request(player, msg) =>
        sender ! Terminal.TerminalMessage(player, msg, term.Request(player, msg))

      case _ => ;
    }

  override def toString : String = term.Definition.Name
}

// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, SimpleItem}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.HackableBehavior


class CaptureTerminalControl(terminal : CaptureTerminal) extends Actor with FactionAffinityBehavior.Check with HackableBehavior.GenericHackable {
  def FactionObject : FactionAffinity = terminal
  def HackableObject = terminal

  def receive : Receive = checkBehavior
    .orElse(hackableBehavior)
    .orElse {
      case CommonMessages.Use(player, Some(item : SimpleItem)) if item.Definition == GlobalDefinitions.remote_electronics_kit =>
        val canHack = terminal.HackedBy match {
          case Some(info) => info.hackerFaction != player.Faction
          case _ => terminal.Faction != player.Faction
        }
        if(canHack) {
          sender ! CommonMessages.Hack(player, terminal, Some(item))
        }

      case _ => ; //no default message
  }
}

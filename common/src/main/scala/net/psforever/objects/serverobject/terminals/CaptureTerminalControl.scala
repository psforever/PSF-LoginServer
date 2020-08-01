// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, SimpleItem}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.{GenericHackables, HackableBehavior}

class CaptureTerminalControl(terminal: CaptureTerminal)
    extends Actor
    with FactionAffinityBehavior.Check
    with HackableBehavior.GenericHackable {
  def FactionObject: FactionAffinity = terminal
  def HackableObject                 = terminal

  def receive: Receive =
    checkBehavior
      .orElse(hackableBehavior)
      .orElse {
        case CommonMessages.Use(player, Some(item: SimpleItem))
            if item.Definition == GlobalDefinitions.remote_electronics_kit =>
          val canHack = terminal.HackedBy match {
            case Some(info) => info.hackerFaction != player.Faction
            case _          => terminal.Faction != player.Faction
          }
          if (canHack) {
            sender() ! CommonMessages.Progress(
              GenericHackables.GetHackSpeed(player, terminal),
              CaptureTerminals.FinishHackingCaptureConsole(terminal, player, 3212836864L),
              GenericHackables.HackingTickAction(progressType = 1, player, terminal, item.GUID)
            )
          }

        case _ => ; //no default message
      }
}

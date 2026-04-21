// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals.capture

import akka.actor.Actor
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.hackable.{GenericHackables, HackableBehavior}
import net.psforever.objects.{GlobalDefinitions, SimpleItem}
import net.psforever.packet.game.HackState1

class CaptureTerminalControl(terminal: CaptureTerminal)
    extends Actor
    with FactionAffinityBehavior.Check
    with HackableBehavior.GenericHackable {
  def FactionObject: CaptureTerminal  = terminal
  def HackableObject: CaptureTerminal = terminal

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
              CaptureTerminals.FinishHackingCaptureConsole(terminal, player, unk = -1),
              GenericHackables.HackingTickAction(HackState1.Unk1, player, terminal, item.GUID, CaptureTerminals.EndHackProgress)
            )
          }

        case _ => () //no default message
      }
}

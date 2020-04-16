// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, SimpleItem}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.DamageableAmenity
import net.psforever.objects.serverobject.hackable.{GenericHackables, HackableBehavior}
import net.psforever.objects.serverobject.repair.RepairableAmenity
import net.psforever.objects.serverobject.structures.Building

/**
  * An `Actor` that handles messages being dispatched to a specific `Terminal`.
  * @param term the `Terminal` object being governed
  */
class TerminalControl(term : Terminal) extends Actor
  with FactionAffinityBehavior.Check
  with HackableBehavior.GenericHackable
  with DamageableAmenity
  with RepairableAmenity {
  def FactionObject = term
  def HackableObject = term
  def DamageableObject = term
  def RepairableObject = term

  def receive : Receive = checkBehavior
    .orElse(hackableBehavior)
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse {
      case Terminal.Request(player, msg) =>
        sender ! Terminal.TerminalMessage(player, msg, term.Request(player, msg))

      case CommonMessages.Use(player, Some(item : SimpleItem)) if item.Definition == GlobalDefinitions.remote_electronics_kit =>
        //TODO setup certifications check
        term.Owner match {
          case b : Building if (b.Faction != player.Faction || b.CaptureConsoleIsHacked) && term.HackedBy.isEmpty =>
            sender ! CommonMessages.Progress(
              GenericHackables.GetHackSpeed(player, term),
              GenericHackables.FinishHacking(term, player, 3212836864L),
              GenericHackables.HackingTickAction(progressType = 1, player, term, item.GUID)
            )
          case _ => ;
        }
      case _ => ;
    }

  override def toString : String = term.Definition.Name
}

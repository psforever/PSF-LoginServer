// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.{GlobalDefinitions, SimpleItem}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.{Damageable, DamageableAmenity}
import net.psforever.objects.serverobject.hackable.{GenericHackables, HackableBehavior}
import net.psforever.objects.serverobject.repair.{AmenityAutoRepair, RepairableAmenity}
import net.psforever.objects.serverobject.structures.Building

/**
  * An `Actor` that handles messages being dispatched to a specific `Terminal`.
  * @param term the `Terminal` object being governed
  */
class TerminalControl(term: Terminal)
    extends Actor
    with FactionAffinityBehavior.Check
    with HackableBehavior.GenericHackable
    with DamageableAmenity
    with RepairableAmenity
    with AmenityAutoRepair {
  def FactionObject    = term
  def HackableObject   = term
  def DamageableObject = term
  def RepairableObject = term
  def AutoRepairObject = term

  def receive: Receive =
    checkBehavior
      .orElse(hackableBehavior)
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse(autoRepairBehavior)
      .orElse {
        case Terminal.Request(player, msg) =>
          TerminalControl.Dispatch(sender(), term, Terminal.TerminalMessage(player, msg, term.Request(player, msg)))

        case CommonMessages.Use(player, Some(item: SimpleItem))
            if item.Definition == GlobalDefinitions.remote_electronics_kit =>
          //TODO setup certifications check
          term.Owner match {
            case b: Building if (b.Faction != player.Faction || b.CaptureTerminalIsHacked) && term.HackedBy.isEmpty =>
              sender() ! CommonMessages.Progress(
                GenericHackables.GetHackSpeed(player, term),
                GenericHackables.FinishHacking(term, player, 3212836864L),
                GenericHackables.HackingTickAction(progressType = 1, player, term, item.GUID)
              )
            case _ => ;
          }

        case _ => ;
      }

  override protected def DamageAwareness(target : Target, cause : ResolvedProjectile, amount : Any) : Unit = {
    tryAutoRepair()
    super.DamageAwareness(target, cause, amount)
  }

  override protected def DestructionAwareness(target: Damageable.Target, cause: ResolvedProjectile) : Unit = {
    tryAutoRepair()
    super.DestructionAwareness(target, cause)
  }

  override def PerformRepairs(target : Target, amount : Int) : Int = {
    val newHealth = super.PerformRepairs(target, amount)
    if(newHealth == target.Definition.MaxHealth) {
      stopAutoRepair()
    }
    newHealth
  }

  override def toString: String = term.Definition.Name
}

object TerminalControl {
  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    msg.response match {
      case Terminal.NoDeal() => sender ! msg
      case _ =>
        terminal.Definition.Dispatch(sender, terminal, msg)
    }
  }
}

// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.ActorRef
import net.psforever.objects.{GlobalDefinitions, SimpleItem, Tool}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.{Damageable, DamageableAmenity}
import net.psforever.objects.serverobject.hackable.{GenericHackables, HackableBehavior}
import net.psforever.objects.serverobject.repair.{AmenityAutoRepair, RepairableAmenity}
import net.psforever.objects.serverobject.structures.{Building, PoweredAmenityControl}
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.packet.game.HackState1
import net.psforever.services.local.support.HackClearActor
import net.psforever.services.local.ClearMessage

/**
  * An `Actor` that handles messages being dispatched to a specific `Terminal`.
  * @param term the `Terminal` object being governed
  */
class TerminalControl(term: Terminal)
  extends PoweredAmenityControl
    with FactionAffinityBehavior.Check
    with HackableBehavior.GenericHackable
    with DamageableAmenity
    with RepairableAmenity
    with AmenityAutoRepair {
  def FactionObject: Terminal    = term
  def HackableObject: Terminal   = term
  def DamageableObject: Terminal = term
  def RepairableObject: Terminal = term
  def AutoRepairObject: Terminal = term

  val commonBehavior: Receive = checkBehavior
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse(autoRepairBehavior)

  def poweredStateLogic: Receive =
    commonBehavior
      .orElse(hackableBehavior)
      .orElse {
        case Terminal.Request(player, msg) =>
          TerminalControl.Dispatch(sender(), term, Terminal.TerminalMessage(player, msg, term.Request(player, msg)))

        case CommonMessages.Use(player, Some(item: SimpleItem))
          if item.Definition == GlobalDefinitions.remote_electronics_kit =>
          //TODO setup certifications check
          term.Owner match {
            case b: Building if (b.Faction != player.Faction || b.CaptureTerminalIsHacked) && term.HackedBy.isEmpty =>
              //order terminals are 90 / 1, or 60 / ?
              sender() ! CommonMessages.Progress(
                GenericHackables.GetHackSpeed(player, term),
                GenericHackables.FinishHacking(term, player, hackValue = -1, hackClearValue = -1),
                GenericHackables.HackingTickAction(HackState1.Unk1, player, term, item.GUID)
              )
            case _ => ()
          }
        case CommonMessages.UploadVirus(player, Some(item: Tool), virus)
          if item.Definition == GlobalDefinitions.trek =>
          term.Owner match {
            case _: Building =>
              sender() ! CommonMessages.Progress(
                1.66f,
                GenericHackables.FinishVirusAction(term, player, hackValue = -1, hackClearValue = -1, virus),
                GenericHackables.HackingTickAction(HackState1.Unk1, player, term, item.GUID)
              )
            case _ => ()
          }
        case CommonMessages.RemoveVirus(player, Some(item: SimpleItem))
          if item.Definition == GlobalDefinitions.remote_electronics_kit =>
          term.Owner match {
            case _: Building =>
              sender() ! CommonMessages.Progress(
                1.66f,
                GenericHackables.FinishVirusAction(term, player, hackValue = -1, hackClearValue = -1, virus=8L),
                GenericHackables.HackingTickAction(HackState1.Unk1, player, term, item.GUID)
              )
            case _ => ()
          }
        case _ => ()
      }

  def unpoweredStateLogic : Receive = commonBehavior
    .orElse(clearHackBehavior)
    .orElse {
      case Terminal.Request(player, msg) =>
        sender() ! Terminal.TerminalMessage(player, msg, Terminal.NoDeal())

      case _ => ()
    }

  override protected def DamageAwareness(target: Target, cause: DamageResult, amount: Any) : Unit = {
    tryAutoRepair()
    super.DamageAwareness(target, cause, amount)
  }

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult) : Unit = {
    tryAutoRepair()
    if (term.HackedBy.nonEmpty) {
      val zone = term.Zone
      zone.LocalEvents ! ClearMessage(HackClearActor.ObjectIsResecured(term))
    }
    super.DestructionAwareness(target, cause)
  }

  override def PerformRepairs(target : Target, amount : Int) : Int = {
    val newHealth = super.PerformRepairs(target, amount)
    if(newHealth == target.Definition.MaxHealth) {
      stopAutoRepair()
    }
    newHealth
  }

  override def tryAutoRepair() : Boolean = {
    isPowered && super.tryAutoRepair()
  }

  def powerTurnOffCallback() : Unit = {
    stopAutoRepair()
    //clear hack state
    if (term.HackedBy.nonEmpty) {
      val zone = term.Zone
      zone.LocalEvents ! ClearMessage(HackClearActor.ObjectIsResecured(term))
    }
  }

  def powerTurnOnCallback() : Unit = {
    tryAutoRepair()
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

// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.{Default, GlobalDefinitions, SimpleItem}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.DamageableAmenity
import net.psforever.objects.serverobject.hackable.{GenericHackables, HackableBehavior}
import net.psforever.objects.serverobject.repair.RepairableAmenity
import net.psforever.objects.serverobject.structures.{AutoRepairStats, Building}

import scala.concurrent.duration._

/**
  * An `Actor` that handles messages being dispatched to a specific `Terminal`.
  * @param term the `Terminal` object being governed
  */
class TerminalControl(term: Terminal)
    extends Actor
    with FactionAffinityBehavior.Check
    with HackableBehavior.GenericHackable
    with DamageableAmenity
    with RepairableAmenity {
  def FactionObject    = term
  def HackableObject   = term
  def DamageableObject = term
  def RepairableObject = term

  private var periodicRepair: Cancellable = Default.Cancellable

  def receive: Receive =
    checkBehavior
      .orElse(hackableBehavior)
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
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

        case NtuCommand.Grant(_, 0) =>
          periodicRepair.cancel()

        case NtuCommand.Grant(_, _) =>
          term.Definition.autoRepair match {
            case Some(repair : AutoRepairStats) =>
              PerformRepairs(term, repair.amount)
            case _ => ;
          }

        case _ => ;
      }

  override protected def DamageAwareness(target : Target, cause : ResolvedProjectile, amount : Any) : Unit = {
    import akka.actor.typed.scaladsl.adapter.ClassicActorRefOps
    term.Definition.autoRepair match {
      case Some(AutoRepairStats(_, start, interval, drain)) if periodicRepair.isCancelled =>
        import scala.concurrent.ExecutionContext.Implicits.global
        periodicRepair = context.system.scheduler.scheduleWithFixedDelay(
          start milliseconds,
          interval milliseconds,
          term.Owner.Actor,
          BuildingActor.Ntu(NtuCommand.Request(drain, new ClassicActorRefOps(self).toTyped[NtuCommand.Grant]))
        )
      case _ => ;
    }
    super.DamageAwareness(target, cause, amount)
  }

  override def PerformRepairs(target : Target, amount : Int) : Int = {
    val newHealth = super.PerformRepairs(target, amount)
    if(newHealth == target.Definition.MaxHealth) {
      periodicRepair.cancel()
    }
    newHealth
  }

  override def toString: String = term.Definition.Name
}

object TerminalControl {
  private case class PeriodicRepair()

  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    msg.response match {
      case Terminal.NoDeal() => sender ! msg
      case _ =>
        terminal.Definition.Dispatch(sender, terminal, msg)
    }
  }
}

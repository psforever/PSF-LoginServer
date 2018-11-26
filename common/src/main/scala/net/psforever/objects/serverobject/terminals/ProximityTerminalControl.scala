// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.{DefaultCancellable, Player}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3
import services.local.{LocalAction, LocalServiceMessage}
import services.{Service, ServiceManager}

import scala.concurrent.duration._

/**
  * An `Actor` that handles messages being dispatched to a specific `ProximityTerminal`.
  * Although this "terminal" itself does not accept the same messages as a normal `Terminal` object,
  * it returns the same type of messages - wrapped in a `TerminalMessage` - to the `sender`.
  * @param term the proximity unit (terminal)
  */
class ProximityTerminalControl(term : Terminal with ProximityUnit) extends Actor with FactionAffinityBehavior.Check {
  var service : ActorRef = ActorRef.noSender
  var terminalAction : Cancellable = DefaultCancellable.obj

  def FactionObject : FactionAffinity = term

  def TerminalObject : Terminal with ProximityUnit = term

  def receive : Receive = Start

  def Start : Receive = checkBehavior
    .orElse {
    case Service.Startup() =>
      ServiceManager.serviceManager ! ServiceManager.Lookup("local")

    case ServiceManager.LookupResult("local", ref) =>
      service = ref
      context.become(Run)

    case _ => ;
  }

  def Run : Receive = checkBehavior
    .orElse {
      case CommonMessages.Use(player) =>
        if(TerminalObject.Definition.asInstanceOf[ProximityDefinition].TargetValidation.exists(p => p(player))) {
          Use(player)
        }

      case CommonMessages.Unuse(player) =>
        Unuse(player)

      case ProximityTerminalControl.TerminalAction() =>
        val proxDef = TerminalObject.Definition.asInstanceOf[ProximityDefinition]
        val radius = proxDef.UseRadius * proxDef.UseRadius
        val validation = proxDef.TargetValidation
        TerminalObject.Targets.foreach(target => {
          if(Vector3.DistanceSquared(TerminalObject.Position, target.Position) <= radius && validation.exists(p => p(target))) {
            //TODO stuff
          }
          else {
            Unuse(target)
          }
        })

      case CommonMessages.Hack(player) =>
        term.HackedBy = player
        sender ! true

      case CommonMessages.ClearHack() =>
        term.HackedBy = None

      case _ => ;
    }

  def Use(player : Player) : Unit = {
    val hadNoUsers = TerminalObject.NumberUsers == 0
    if(TerminalObject.AddUser(player) == 1 && hadNoUsers) {
      import scala.concurrent.ExecutionContext.Implicits.global
      println("start terminal action")
      terminalAction.cancel
      terminalAction = context.system.scheduler.schedule(500 milliseconds, 500 milliseconds, self, ProximityTerminalControl.TerminalAction())
      service ! LocalServiceMessage(player.Continent, LocalAction.ProximityTerminalEffect(PlanetSideGUID(0), TerminalObject.GUID, true))
    }
  }

  def Unuse(player : Player) : Unit = {
    val hadUsers = TerminalObject.NumberUsers > 0
    if(TerminalObject.RemoveUser(player) == 0 && hadUsers) {
      terminalAction.cancel
      service ! LocalServiceMessage(player.Continent, LocalAction.ProximityTerminalEffect(PlanetSideGUID(0), TerminalObject.GUID, false))
    }
  }

  override def toString : String = term.Definition.Name
}

object ProximityTerminalControl {
  def ValidatePlayerTarget(target : Any) : Boolean = {
    target match {
      case p : Player =>
        p.Health > 0 && p.Health < p.MaxHealth && p.Armor < p.MaxArmor
      case _ =>
        false
    }
  }

  private case class TerminalAction()

  private case class CancelTerminalAction()
}

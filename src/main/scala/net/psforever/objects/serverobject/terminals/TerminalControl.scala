// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.ActorRef
import net.psforever.objects.Player
import net.psforever.objects.avatar.Certification
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.{GlobalDefinitions, SimpleItem, Tool}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.{Damageable, DamageableAmenity}
import net.psforever.objects.serverobject.hackable.{GenericHackables, HackableBehavior}
import net.psforever.objects.serverobject.repair.{AmenityAutoRepair, RepairableAmenity}
import net.psforever.objects.serverobject.structures.{BaseVirus, Building, PoweredAmenityControl}
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.packet.game.packets.HackState1
import net.psforever.services.local.support.{HackClearActor, HackClearEnvelope}

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
          if item.Definition == GlobalDefinitions.remote_electronics_kit &&
            TerminalControl.CanHack(player, term) =>
              //order terminals are 90 / 1, or 60 / ?
              sender() ! CommonMessages.Progress(
                GenericHackables.GetHackSpeed(player, term),
                GenericHackables.FinishHacking(term, player, hackValue = -1, hackClearValue = -1),
                GenericHackables.HackingTickAction(HackState1.Unk1, player, term, item.GUID)
              )

        case CommonMessages.UploadVirus(player, Some(item: Tool), virus)
          if item.Definition == GlobalDefinitions.trek &&
            BaseVirus(virus).exists(_ != BaseVirus.NoVirus) &&
              TerminalControl.CanUploadVirus(player, term) =>
              term.activeVirusUploaders.put(player.GUID, player)
              sender() ! CommonMessages.Progress(
                1.66f,
                () => GenericHackables.FinishVirusAction(term, player, hackValue = -1, hackClearValue = -1, virus)(),
                GenericHackables.HackingTickAction(HackState1.Unk1, player, term, item.GUID, TerminalControl.EndVirusUploadProgress)(_)
              )

        case CommonMessages.RemoveVirus(player, Some(item: Equipment))
          if item.Definition == GlobalDefinitions.remote_electronics_kit &&
            TerminalControl.CanRemoveVirus(player, term) =>
              sender() ! CommonMessages.Progress(
                1.66f,
                () => GenericHackables.FinishVirusAction(term, player, hackValue = -1, hackClearValue = -1, virus = BaseVirus.NoVirus.id)(),
                GenericHackables.HackingTickAction(HackState1.Unk1, player, term, item.GUID, TerminalControl.EndVirusRemoveProgress)(_)
              )
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
      zone.LocalEvents ! HackClearEnvelope(HackClearActor.ObjectIsResecured(term))
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
      zone.LocalEvents ! HackClearEnvelope(HackClearActor.ObjectIsResecured(term))
    }
  }

  def powerTurnOnCallback() : Unit = {
    tryAutoRepair()
  }

  override def toString: String = term.Definition.Name
}

object TerminalControl {
  private val BasicHackCerts: Set[Certification] = Set(
    Certification.Hacking,
    Certification.AdvancedHacking,
    Certification.ExpertHacking,
    Certification.ElectronicsExpert
  )

  private val AdvancedHackCerts: Set[Certification] = Set(
    Certification.AdvancedHacking,
    Certification.ExpertHacking,
    Certification.ElectronicsExpert
  )

  private val VirusInstallCerts: Set[Certification] = Set(
    Certification.DataCorruption,
    Certification.ElectronicsExpert
  )

  private val VirusRemoveCerts: Set[Certification] = Set(
    Certification.ExpertHacking,
    Certification.ElectronicsExpert
  )

  private def CanHack(player: Player, term: Terminal): Boolean = {
    val certs = player.avatar.certifications
    val hasCert = term.Definition match {
      case GlobalDefinitions.cert_terminal |
           GlobalDefinitions.medical_terminal |
           GlobalDefinitions.implant_terminal_interface =>
        certs.intersect(BasicHackCerts).nonEmpty
      case _ =>
        certs.intersect(AdvancedHackCerts).nonEmpty
    }
    hasCert && (term.Owner match {
      case b: Building =>
        (b.Faction != player.Faction || b.CaptureTerminalIsHacked) && term.HackedBy.isEmpty
      case _ =>
        false
    })
  }

  def CanUploadVirus(player: Player, term: Terminal): Boolean = {
    val hasCert = player.avatar.certifications.intersect(VirusInstallCerts).nonEmpty
    val isValidTerminal = term.Definition == GlobalDefinitions.main_terminal
    hasCert && isValidTerminal && (term.Owner match {
      case b: Building =>
        b.Faction != player.Faction && (
          b.virusId == BaseVirus.NoVirus.id ||
          b.virusInstallTime.forall(t => System.currentTimeMillis() - t >= 120000L)
        )
      case _ =>
        false
    })
  }

  def CanRemoveVirus(player: Player, term: Terminal): Boolean = {
    val hasCert = player.avatar.certifications.intersect(VirusRemoveCerts).nonEmpty
    val isValidTerminal = term.Definition == GlobalDefinitions.main_terminal
    hasCert && isValidTerminal && (term.Owner match {
      case b: Building =>
        b.Faction == player.Faction && b.virusId != BaseVirus.NoVirus.id
      case _ =>
        false
    })
  }

  def EndVirusUploadProgress(target: PlanetSideServerObject, hacker: Player): Boolean = {
    target match {
      case term: Terminal =>
        val shouldCancel = !term.activeVirusUploaders.contains(hacker.GUID) || !CanUploadVirus(hacker, term)
        if (shouldCancel) {
          term.activeVirusUploaders.remove(hacker.GUID)
        }
        shouldCancel
      case _ => false
    }
  }

  def EndVirusRemoveProgress(target: PlanetSideServerObject, hacker: Player): Boolean = {
    target match {
      case term: Terminal =>
        !CanRemoveVirus(hacker, term)
      case _ => false
    }
  }


  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    msg.response match {
      case Terminal.NoDeal() => sender ! msg
      case _ =>
        terminal.Definition.Dispatch(sender, terminal, msg)
    }
  }
}

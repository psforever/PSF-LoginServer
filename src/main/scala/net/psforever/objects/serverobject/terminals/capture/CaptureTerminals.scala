// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.terminals.capture

import net.psforever.objects.Player
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.structures.{Building, StructureType, WarpGate}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.PlanetSideEmpire

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object CaptureTerminals {
  private val log = org.log4s.getLogger("CaptureTerminals")

  /**
   * The process of hacking an object is completed.
   * Pass the message onto the hackable object and onto the local events system.
   * @param target the `Hackable` object that has been hacked
   * @param hackingPlayer The player that hacked the control console
   * @param unk na;
   *            used by `HackMessage` as `unk5`
   * @see `HackMessage`
   */
  //TODO add params here depending on which params in HackMessage are important
  def FinishHackingCaptureConsole(target: CaptureTerminal, hackingPlayer: Player, unk: Int)(): Unit = {
    import akka.pattern.ask

    // Wait for the target actor to set the HackedBy property
    import scala.concurrent.ExecutionContext.Implicits.global
    ask(target.Actor, CommonMessages.Hack(hackingPlayer, target))(timeout = 2 second)
      .mapTo[CommonMessages.EntityHackState]
      .onComplete {
        case Success(_) =>
          log.info(s"${hackingPlayer.toString} hacked a ${target.Definition.Name}")
          val zone = target.Zone
          val zoneid = zone.id
          val events = zone.LocalEvents
          val isResecured = hackingPlayer.Faction == target.Faction
          events ! LocalServiceMessage(
            zoneid,
            LocalAction.TriggerSound(hackingPlayer.GUID, target.HackSound, hackingPlayer.Position, 30, 0.49803925f)
          )
          if (isResecured) {
            // Resecure the CC
            events ! LocalServiceMessage(
              zoneid,
              LocalAction.ResecureCaptureTerminal(target, PlayerSource(hackingPlayer))
            )
          } else {
            // Start the CC hack timer
            events ! LocalServiceMessage(
              zoneid,
              LocalAction.StartCaptureTerminalHack(target)
            )
          }
        case Failure(_) =>
          log.warn(s"Hack message failed on target guid: ${target.GUID}")
      }
  }

  /**
   * Check if the state of connected facilities has changed since the hack progress began. It accounts for a friendly facility
   * on the other side of a warpgate as well in case there are no friendly facilities in the same zone
   * @param target the `Hackable` object that has been hacked
   * @param hacker the player performing the action
   * @return `true`, if the hack should be ended; `false`, otherwise
   */
  def EndHackProgress(target: PlanetSideServerObject, hacker: Player): Boolean = {
    val building = target.asInstanceOf[CaptureTerminal].Owner.asInstanceOf[Building]
    val hackerFaction = hacker.Faction
    if (GenericHackables.ForceDomeProtectsFromHacking(target, hacker)) {
      true
    } else if (building.Faction == PlanetSideEmpire.NEUTRAL ||
      building.BuildingType == StructureType.Tower ||
      building.Faction == hackerFaction) {
      false
    } else {
      val stopHackingCount = building.Neighbours match {
        case Some(neighbors) =>
          neighbors.count {
            case wg: WarpGate if wg.Faction == hackerFaction =>
              true
            case wg: WarpGate =>
              val friendlyBaseOpt = for {
                otherWg <- wg.Neighbours.flatMap(_.find(_.isInstanceOf[WarpGate]))
                friendly <- otherWg.Neighbours.flatMap(_.collectFirst { case b: Building if !b.isInstanceOf[WarpGate] => b })
              } yield friendly
              friendlyBaseOpt.exists { fb =>
                fb.Faction == hackerFaction &&
                  !fb.CaptureTerminalIsHacked &&
                  fb.NtuLevel > 0
              }
            case b =>
              b.Faction == hackerFaction &&
                !b.CaptureTerminalIsHacked &&
                b.NtuLevel > 0
          }
        case None => 0
      }
      stopHackingCount == 0
    }
  }
}

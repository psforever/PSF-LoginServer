package net.psforever.objects.serverobject.terminals.capture

import net.psforever.objects.Player
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

import scala.util.{Failure, Success}

object CaptureTerminals {import scala.concurrent.duration._
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
  def FinishHackingCaptureConsole(target: CaptureTerminal, hackingPlayer: Player, unk: Long)(): Unit = {
    import akka.pattern.ask

    log.info(s"${hackingPlayer.toString} hacked a ${target.Definition.Name}")
    // Wait for the target actor to set the HackedBy property
    import scala.concurrent.ExecutionContext.Implicits.global
    ask(target.Actor, CommonMessages.Hack(hackingPlayer, target))(timeout = 2 second)
      .mapTo[CommonMessages.EntityHackState]
      .onComplete {
        case Success(_) =>
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
}

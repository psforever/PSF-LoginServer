package net.psforever.objects.serverobject.terminals.capture

import net.psforever.objects.Player
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

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
  def FinishHackingCaptureConsole(target: CaptureTerminal, hackingPlayer: Player, unk: Long)(): Unit = {
    import akka.pattern.ask

    import scala.concurrent.duration._
    log.info(s"${hackingPlayer.toString} hacked a ${target.Definition.Name}")
    // Wait for the target actor to set the HackedBy property
    import scala.concurrent.ExecutionContext.Implicits.global
    ask(target.Actor, CommonMessages.Hack(hackingPlayer, target))(1 second).mapTo[Boolean].onComplete {
      case Success(_) =>
        target.Zone.LocalEvents ! LocalServiceMessage(
          target.Zone.id,
          LocalAction.TriggerSound(hackingPlayer.GUID, target.HackSound, hackingPlayer.Position, 30, 0.49803925f)
        )

        val isResecured = hackingPlayer.Faction == target.Faction

        if (isResecured) {
          // Resecure the CC
          target.Zone.LocalEvents ! LocalServiceMessage(
            target.Zone.id,
            LocalAction.ResecureCaptureTerminal(
              target
            )
          )
        } else {
          // Start the CC hack timer
          target.Zone.LocalEvents ! LocalServiceMessage(
            target.Zone.id,
            LocalAction.StartCaptureTerminalHack(
              target
            )
          )
        }
      case Failure(_) => log.warn(s"Hack message failed on target guid: ${target.GUID}")
    }
  }


}
